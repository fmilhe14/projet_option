import components.*;
import org.chocosolver.solver.Solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Solver solver = new Solver("");

        List<Service> services = Parser;
        int[][] networkLatencies = Parser.getNetworkLatencies();
        int[][] networkBandwidths = Parser.getNetworkBandwidths();
        int[] networkCpus = Parser.getNetworkCpus();
        int[] networkMem = Parser.getNetworkMem();

        Data data = new Data(services, networkLatencies, networkBandwidths, networkCpus, networkMem, solver);

        List<Node> nodes = data.buildNodes();
        List<Edge> edges = data.buildEdges(nodes);

        Graph graph = new Graph(nodes, edges);

        solver.findSolution();

    }


    private ArrayList<Path[]> initiatePaths(List<Service> services, Graph graph, Solver solver) {

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
