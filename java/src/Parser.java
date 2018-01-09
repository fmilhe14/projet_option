import components.Edge;
import components.Graph;
import components.Node;
import components.Service;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static List<Service> parseServices(){

        return new ArrayList<Service>();
    }

    public static double[][] parseLatencies(){

        return new double[1][1];
    }

    public static double[][] parseBandwidths(){

        return new double[1][1];
    }

    public static Graph parseGraph(){

        return new Graph(new ArrayList<Node>(), new ArrayList<Edge>());
    }

}
