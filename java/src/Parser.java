import components.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Parser {
    private Properties prop;

    public Parser(String fileName) {
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

    private Properties getProperties() {
        return prop;
    }

    private String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public List<Service> services() {
         getProperty("services.number");
         return null;
    }

    public int[][] networkLatencies() {
        return null;
    }

    public int[][] networkBandwidths() {
        return null;
    }

    public int[] networkCpus() {
        return null;
    }

    public int[] networkMem() {
        return null;
    }


    public static void main(String[] args) {
        Parser parser = new Parser("edge.properties");
        System.out.println(parser.services());
        System.out.println(parser.networkLatencies());
    }
}
