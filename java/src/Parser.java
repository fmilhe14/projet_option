import components.Component;
import components.Service;
import org.chocosolver.solver.Solver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.*;
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
            components = collectIntInArrays("services.components");

            if (nodeModels.size() != nbNodes) {
                throw new InvalidPropertiesFormatException("Number of models in host.hostmodelpersite differs from sites.number");
            } else if (networkTopology.size() != nbNodes * nbNodes * 2) {
                throw new InvalidPropertiesFormatException("Matrix given in network.topology has the wrong size");
            } else if (components.length != nbServices) {
                throw new InvalidPropertiesFormatException("Matrix given in services.components has the wrong size");
            }

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

    private String getProperty(String key) {
        return prop.getProperty(key);
    }

    public List<Service> services() {
        List<Service> rep = new ArrayList<>();

        int nbServices = this.components.length;
        int[][] service;

        for (int i = 0; i < nbServices; i++) {
            service = this.components[i];

            List<Component> components = new ArrayList<>();
            Map<Component[], Integer> latencies = new HashMap<>();
            Map<Component[], Integer> bandwidths = new HashMap<>();

            int[] component;
            int id;
            String variableName;

            for (int j=0; j<service.length; j++) {
                component = service[j];
                id = i * nbServices + j;
                variableName = "position_component_" + id;

                if (component.length == 1) {
                    components.add(new Component(id, 0, 0,
                            bounded(variableName, component[0], component[0], solver)));
                } else {
                    components.add(new Component(id, component[0], component[1],
                            bounded(variableName, 0, nbNodes, solver)));
                }
            }

            rep.add(new Service(components, latencies, bandwidths));
        }
        return rep;
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
     * @param key the property in which to collect.
     * @return all the integer values stored in the given property
     * @throws InvalidKeyException if the given property key is not in the properties file
     */
    private List<Integer> collectIntValues(String key) throws InvalidKeyException {
        String property = getProperty(key);
        ArrayList<Integer> values = new ArrayList<>();

        if (property == null) {
            throw new InvalidKeyException("property ".concat(key).concat(" is not in ").concat(fileName));
        } else {
            Matcher m = compile("-?[0-9]+").matcher(property);
            while (m.find()) {
                values.add(parseInt(m.group()));
            }
            return values;
        }
    }

    /**
     * Ex: Call this with modelValueIndex = 2 to get the array of RAM per node in the model.
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
                .mapToObj(
                        i -> range(0, nbNodes)
                                .map(j -> networkTopology.get((i * 5 + j) * 2 + modelValueIndex))
                                .toArray()
                ).toArray(int[][]::new);
    }

    private int[][][] collectIntInArrays(String key) throws InvalidKeyException {
        String property = getProperty(key);

        if (property == null) {
            throw new InvalidKeyException("property ".concat(key).concat(" is not in ").concat(fileName));
        } else {
            return buildMatrixThirdDegree(property.replaceAll(" |([A-Za-z]+[0-9]+(-[a-z])? ?,)", ""));
        }
    }


    private int[][][] buildMatrixThirdDegree(String s) {
        ParserNode tree = new ParserNode(s);

        int nbFirstDegree = tree.nbChildren();
        int[][][] rep = new int[nbFirstDegree][][];

        ParserNode firstDegreeChild;
        int nbSecondDegreeChildren;

        for (int i = 0; i < nbFirstDegree; i++) {
            firstDegreeChild = tree.children.get(i);
            nbSecondDegreeChildren = firstDegreeChild.nbChildren();

            int[][] secondDegree = new int[nbSecondDegreeChildren][];

            ParserNode secondDegreeChild;
            int nbThirdDegreeChildren;

            for (int j = 0; j < nbSecondDegreeChildren; j++) {
                secondDegreeChild = firstDegreeChild.children.get(j);
                nbThirdDegreeChildren = secondDegreeChild.nbChildren();

                int[] thirdDegree = new int[nbThirdDegreeChildren];

                for (int k = 0; k < nbThirdDegreeChildren; k++) {
                    thirdDegree[k] = secondDegreeChild.children.get(k).val;
                }

                secondDegree[j] = thirdDegree;
            }

            rep[i] = secondDegree;
        }
        return rep;
    }









    public static void main(String[] args) {
        try {
            Parser parser = new Parser(null, "edge.properties");
//
//            System.out.println(Arrays.toString(parser.networkMem()));
//
//            System.out.println(Arrays.toString(parser.networkCpus()));
//
//            System.out.println(
//                    Arrays.stream(parser.networkBandwidths())
//                            .map(Arrays::toString)
//                            .collect(joining(", ", "[", "]")));
//
//            System.out.println(
//                    Arrays.stream(parser.networkLatencies())
//                            .map(Arrays::toString)
//                            .collect(joining(", ", "[", "]")));
//
            System.out.println(parser.services());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class ParserNode {
    private String prop;
    List<ParserNode> children;
    int val;

    public ParserNode(int val) {
        this.val = val;
    }

    public ParserNode(String s) {
        this.prop = s;
        this.children = new LinkedList<>();

        if (prop.charAt(0) == '{') {
            int nbOpen = 1;
            int ind = 1;
            char c;

            while (prop.length() != 0) {
                while (nbOpen != 0) {
                    c = prop.charAt(ind);

                    if (c == '{') {
                        nbOpen++;
                    } else if (c == '}') {
                        nbOpen--;
                    }
                    ind++;
                }

                this.children.add(new ParserNode(prop.substring(1, ind - 1)));
                prop = prop.substring(ind);

                if (prop.length() != 0) {
                    if (prop.charAt(0) == ',') {
                        prop = prop.substring(1);
                    }

                    if (prop.charAt(0) == '{') {
                        ind = 1;
                        nbOpen = 1;
                    }
                }
            }

        } else {
            Matcher matcher = compile("-?[0-9]+").matcher(prop);
            while (matcher.find()) {
                children.add(new ParserNode(parseInt(matcher.group())));
            }
            prop = "";
        }
    }

    public int nbChildren() {
        return children.size();
    }
}
