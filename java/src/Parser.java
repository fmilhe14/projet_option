import components.Graph;
import components.Node;
import components.Service;
import org.chocosolver.solver.Solver;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    //TODO Implémenter le parser
    public static List<Service> parseServices(Solver solver) {

        return new ArrayList<Service>();
    }

    //TODO Implémenter le parser
    public static Graph parseGraph() {

        return new Graph(new ArrayList<Node>(), new ArrayList<Service>(), new int[1][1], new int[1][1]);
    }
}
