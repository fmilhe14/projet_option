import components.*;
import org.chocosolver.solver.Solver;
import parser.Parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Solver solver = new Solver("");

        Parser parser = new Parser(solver, "edge.properties");

        List<Service> services = parser.services();
        int[][] networkLatencies = parser.networkLatencies();
        int[][] networkBandwidths = parser.networkBandwidths();
        int[] networkCpus = parser.networkCpus();
        int[] networkMem = parser.networkMem();
        Data data = new Data(services, networkLatencies, networkBandwidths, networkCpus, networkMem, solver);

        List<Node> nodes = data.buildNodes();
        List<Edge> edges = data.buildEdges(nodes);

        Graph graph = new Graph(nodes, edges);

        ArrayList<Path[]> allPaths = initiatePaths(services, graph, solver);

        solver.findSolution();
    }


    private static ArrayList<Path[]> initiatePaths(List<Service> services, Graph graph, Solver solver) {

        ArrayList<Path[]> allPaths = new ArrayList<>();

        for(Service s: services) {

            Iterator<Component[]> pairsOfComponents = s.getRequiredLatencies().keySet().iterator();

            ArrayList<Component[]> effectivePairs = new ArrayList<>();

            while (pairsOfComponents.hasNext()) {

                Component[] pair = pairsOfComponents.next();
                if (s.getRequiredLatencies().get(pair) != -1) effectivePairs.add(pair);
            }

            Path[] paths = new Path[effectivePairs.size()];

            int i = 0;
            for (Component[] pair : effectivePairs) {

                int indiceComponent1 = pair[0].getId();
                int indiceComponent2 = pair[1].getId();

                paths[i] = new Path(graph, indiceComponent1, indiceComponent2, s.getRequiredLatencies().get(pair), solver);
                i++;
            }

            allPaths.add(paths);
        }

        return allPaths;
    }
}
