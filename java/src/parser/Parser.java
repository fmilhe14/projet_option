package parser;

import components.Component;
import components.PairOfComponents;
import components.Service;
import org.chocosolver.solver.Solver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;

import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;
import static java.util.stream.IntStream.range;
import static org.chocosolver.solver.variables.VariableFactory.bounded;

public class Parser {
    private Solver solver;
    private String fileName;

    private Properties prop;
    private int nbNodes;
    private List<Integer> nodeModels;
    private List<Integer> modelValues;
    private List<Integer> networkTopology;

    private int nbServices;
    private int[][][] components;
    private int[][][][] servicesTopologies;

    private final String COMPONENT_NAMES = " |([A-Za-z]+[0-9]+(-[a-z])? ?,)";


    public Parser(Solver solver, String fileName) {
        this.solver = solver;
        this.fileName = fileName;

        String propertiesURL = System.getProperty("user.dir").concat("/java/ressources/").concat(fileName);
        InputStream input = null;
        prop = new Properties();

        try {
            input = new FileInputStream(propertiesURL);
            prop.load(input);

            nbNodes = parseInt(getProperty("sites.number"));
            nodeModels = collectIntValues("sites.hostsmodelpersite");
            modelValues = collectIntValues("host.models");
            networkTopology = collectIntValues("network.topology");

            nbServices = parseInt(getProperty("services.number"));
            components = collectIntInArraysThirdDegree("services.components");
            servicesTopologies = collectIntInArrayFourthDegree("services.topologies");

            checkProperties();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkProperties() throws InvalidPropertiesFormatException {
        if (nodeModels.size() != nbNodes) {
            throw new InvalidPropertiesFormatException("Number of models in host.hostmodelpersite differs from sites.number");
        } else if (networkTopology.size() != nbNodes * nbNodes * 2) {
            throw new InvalidPropertiesFormatException("Matrix given in network.topology has the wrong size");
        } else if (components.length != nbServices) {
            throw new InvalidPropertiesFormatException("Matrix given in services.components has the wrong size");
        } else if (servicesTopologies.length != nbServices) {
            throw new InvalidPropertiesFormatException("Matrix given in services.topologies has the wrong size");
        } else {
            for (int i = 0; i < nbServices; i++) {
                if (servicesTopologies[i].length != components[i].length) {
                    throw new InvalidPropertiesFormatException("At least one of the sub matrices given in services.topologies has the wrong size");
                }
            }
        }
    }

    private String getProperty(String key) {
        return prop.getProperty(key);
    }

    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(new Parser(new Solver(), "edge.properties").networkLatencies()));
    }

    public int[][] networkLatencies() {
        return modelValuePerNodePair(1);
    }

    public int[][] networkBandwidths() {
        return modelValuePerNodePair(0);
    }

    public int[] networkCpus() {
        return modelValuePerNode(1);
    }

    public int[] networkMem() {
        return modelValuePerNode(2);
    }

    /**
     * @param idBase  offset for component id.
     * @param service an array describing the given service,
     * @return a List of Component objects representing the components of the given service.
     */
    private List<Component> makeComponentsList(int idBase, int[][] service) {
        List<Component> rep = new LinkedList<>();
        int[] component;
        int id;
        String variableName;

        for (int i = 0; i < service.length; i++) {
            component = service[i];
            id = idBase + i;
            variableName = "position_component_" + id;

            if (component.length == 1) {
                rep.add(new Component(id, 0, 0, bounded(variableName, component[0], component[0], solver)));
            } else {
                rep.add(new Component(id, component[0], component[1], bounded(variableName, 1, nbNodes, solver)));
            }
        }

        return rep;
    }

    /**
     * @param key the property in which to collect.
     * @return all the integer values stored in the given property
     * @throws InvalidKeyException if the given property key is not in the properties file
     */
    private List<Integer> collectIntValues(String key) throws InvalidKeyException {
        String property = getProperty(key);

        if (property == null) {
            throw new InvalidKeyException("property ".concat(key).concat(" is not in ").concat(fileName));
        } else {
            ArrayList<Integer> rep = new ArrayList<>();
            Matcher m = compile("-?[0-9]+").matcher(property);
            while (m.find()) rep.add(parseInt(m.group()));
            return rep;
        }
    }

    /**
     * Ex: Call this with modelValueIndex = 2 to get the array of RAM per node in the model.
     *
     * @param modelValueIndex the index in which, for each model in host.models, the required value is stored.
     * @return an array containing the value of the required property for each node.
     */
    private int[] modelValuePerNode(int modelValueIndex) {
        return nodeModels
                .stream()
                .mapToInt(m -> modelValues.get((m - 1) * 5 + modelValueIndex))
                .toArray();
    }

    /**
     * @param modelValueIndex 0 for bandwidth, 1 for latency
     * @return the matrix of the values of the required property for each pair of nodes.
     */
    private int[][] modelValuePerNodePair(int modelValueIndex) {
        return range(0, nbNodes)
                .mapToObj(i -> range(0, nbNodes)
                        .map(j -> networkTopology.get((i * 5 + j) * 2 + modelValueIndex))
                        .toArray()
                ).toArray(int[][]::new);
    }

    public List<Service> services() {

        List<Service> rep = new ArrayList<>();

        int[][] service;

        int id = 0;

        for (int i = 0; i < nbServices; i++) {
            service = components[i];


            id += components.length;
            List<Component> components = makeComponentsList(id, service);

            Map<PairOfComponents, Integer> latencies = new HashMap<>();
            Map<PairOfComponents, Integer> bandwidths = new HashMap<>();

            PairOfComponents pairOfComponents;
            int[] pairRequirements;

            for (int j = 0; j < service.length; j++) {
                for (int k = j + 1; k < service.length; k++) {

                    pairOfComponents = new PairOfComponents(components.get(j), components.get(k));
                    pairRequirements = servicesTopologies[i][j][k];

                    bandwidths.put(pairOfComponents, pairRequirements[1]);
                    latencies.put(pairOfComponents, pairRequirements[2]);
                }
            }

            rep.add(new Service(components, latencies, bandwidths));
        }
        return rep;
    }

    /**
     * Call this method on a property whose value is a 3rd degree nested array-like String of ints
     * (such as {{{1, 2}, {3}}, {{4}}} ).
     *
     * @param key The name of the property whose values to collect.
     * @return The values in the required property in the same structure as given in the properties file.
     * @throws InvalidKeyException if {@code key} is not a property in the given properties file.
     */
    private int[][][] collectIntInArraysThirdDegree(String key) throws InvalidKeyException {
        return collectIntInArray(key, this::buildMatrixThirdDegree);
    }

    /**
     * Same as the above method but with fourth degree properties.
     *
     * @param key The name of the property whose values to collect.
     * @return The values in the required property in the same structure as given in the properties file.
     * @throws InvalidKeyException if {@code key} is not a property in the given properties file.
     */
    private int[][][][] collectIntInArrayFourthDegree(String key) throws InvalidKeyException {
        return collectIntInArray(key, this::buildMatrixFourthDegree);
    }

    /**
     * Builds the matrix (actually a 3rd degree array) to be returned by the above function.
     *
     * @param tree the parsing tree generated from the property given in the above method
     * @return The values in the required property in the same structure as given in the properties file.
     */
    private int[][][] buildMatrixThirdDegree(ParserNode tree) {
        return tree.streamChildren()
                .map(child -> child.streamChildren()
                        .map(grandChild -> grandChild.streamChildren()
                                .mapToInt(ParserNode::getVal)
                                .toArray()
                        ).toArray(int[][]::new)
                ).toArray(int[][][]::new);
    }

    /**
     * @param tree the parsing tree generated from the property given in the above method
     * @return just like buildMatrixThirdDegree, but for 4th degree properties.
     */
    private int[][][][] buildMatrixFourthDegree(ParserNode tree) {
        return tree.streamChildren()
                .map(this::buildMatrixThirdDegree)
                .toArray(int[][][][]::new);
    }

    private <R> R collectIntInArray(String key, Function<ParserNode, R> supplier) throws InvalidKeyException {
        String property = getProperty(key);

        if (property == null) {
            throw new InvalidKeyException("property ".concat(key).concat(" is not in ").concat(fileName));
        } else {
            ParserNode tree = new ParserNode(property.replaceAll(COMPONENT_NAMES, ""));
            return supplier.apply(tree);
        }
    }
}