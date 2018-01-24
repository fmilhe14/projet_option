import components.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

public class Parser {
    private Properties prop;
    private String fileName;
    private int nbNodes;
    private List<Integer> nodeModels;
    private List<Integer> modelValues;
    private List<Integer> networkTopology;

    public Parser(String fileName) {
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

            if (nodeModels.size() != nbNodes) {
                throw new InvalidPropertiesFormatException("Number of models in host.hostmodelpersite differs from sites.number");
            } else if (networkTopology.size() != nbNodes * nbNodes * 2) {
                throw new InvalidPropertiesFormatException("Matrix given in network.topology has the wrong size");
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
        return null;
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
        LinkedList<Integer> values = new LinkedList<>();

        if (property == null) {
            throw new InvalidKeyException("property ".concat(key).concat(" is not in ".concat(fileName)));
        } else {
            Matcher m = Pattern.compile("\\-?[0-9]+").matcher(property);
            while (m.find()) {
                values.add(parseInt(m.group()));
            }
            return values;
        }
    }

    /**
     * Ex: Call this with modelValueIndex = 3 to get the array of RAM per node in the model.
     * @param modelValueIndex the index in which, for each model in host.models, the required value is stored.
     * @return an array containing the value of the required property for each node.
     */
    private int[] modelValuePerNode(int modelValueIndex) {
        return nodeModels.stream().mapToInt(m -> modelValues.get((m - 1) * 5 + modelValueIndex)).toArray();
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



    public static void main(String[] args) throws InvalidPropertiesFormatException, InvalidKeyException {
        Parser parser = new Parser("edge.properties");
        try {
            System.out.println(makeString(parser.networkMem()));
            System.out.println(makeString(parser.networkCpus()));
            System.out.println(Arrays.stream(parser.networkBandwidths()).map(Parser::makeString).collect(joining(", ", "[", "]")));
            System.out.println(Arrays.stream(parser.networkLatencies()).map(Parser::makeString).collect(joining(", ", "[", "]")));
        } catch (Exception ignored) {
            ignored.printStackTrace();
            System.out.println("toto");
        }
    }

    private static String makeString(int[] array) {
        return Arrays.stream(array)
                .mapToObj(i -> "" + i)
                .collect(joining(", ", "[", "]"));
    }
}
