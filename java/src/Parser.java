import components.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Parser {
    private String propertiesURL;

    public Parser(String fileName) {
        this.propertiesURL = System.getProperty("user.dir").concat("/java/ressources/").concat(fileName);
    }

    public static List<Service> services() {

        Properties prop = new Properties();
        InputStream input = null;
        List<Service> res = new LinkedList<>();

        try {
            input = new FileInputStream(propertiesURL);
            prop.load(input);

            System.out.println(prop.getProperty("services.number"));

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
        return res;
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
