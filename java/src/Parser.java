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

public class Parser {
    private Properties prop;
    private String fileName;

    public Parser(String fileName) {
        this.fileName = fileName;
        String propertiesURL = System.getProperty("user.dir").concat("/java/ressources/").concat(fileName);
        InputStream input = null;

        prop = new Properties();

        try {
            input = new FileInputStream(propertiesURL);
            prop.load(input);
        } catch (IOException e) {
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
        int nbServices = parseInt(getProperty("services.number"));
        String comp = getProperty("services.components");
        return null;
    }

    public int[][] networkLatencies() {
        return null;
    }

    public int[][] networkBandwidths() {
        return null;
    }

    public int[] networkCpus() throws InvalidPropertiesFormatException, InvalidKeyException {
        return modelValuePerNode(1);
    }

    public int[] networkMem() throws InvalidPropertiesFormatException, InvalidKeyException {
        return modelValuePerNode(2);
    }

    /**
     * Ex: Call this with modelValueIndex = 3 to get the array of RAM per node in the model.
     * @param modelValueIndex the index in which, for each model, the required value is stored.
     * @return an array containing the value of the required property for each node.
     * @throws InvalidKeyException
     * @throws InvalidPropertiesFormatException
     */
    private int[] modelValuePerNode(int modelValueIndex) throws InvalidKeyException, InvalidPropertiesFormatException {
        int nbNodes = parseInt(getProperty("sites.number"));

        List<Integer> nodeModels = collectIntValues("sites.hostsmodelpersite");
        if (nodeModels.size() != nbNodes) {
            throw new InvalidPropertiesFormatException("Number of models in host.hostmodelpersite differs from sites.number");
        }

        List<Integer> modelValues = collectIntValues("host.models");

        return nodeModels.stream().mapToInt(m -> modelValues.get((m - 1) * 5 + modelValueIndex)).toArray();
    }

    /**
     * @param key the property in which to collect.
     * @return all the integer values stored in the given property
     * @throws InvalidKeyException
     */
    private List<Integer> collectIntValues(String key) throws InvalidKeyException {
        List<Integer> nodeModels = new LinkedList<>();
        String property = getProperty(key);

        if (property == null) {
            throw new InvalidKeyException("property ".concat(key).concat(" is not in ".concat(fileName)));
        } else {
            Matcher m = Pattern.compile("[0-9]+").matcher(property);
            while (m.find()) {
                nodeModels.add(parseInt(m.group()));
            }
            return nodeModels;
        }
    }






    public static void main(String[] args) throws InvalidPropertiesFormatException, InvalidKeyException {
        Parser parser = new Parser("edge.properties");
        try {
            System.out.println(makeString(parser.networkMem()));
            System.out.println(makeString(parser.networkCpus()));
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
