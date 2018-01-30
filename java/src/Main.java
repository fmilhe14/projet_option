import components.*;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.VariableFactory;
import parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;
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


      //  Chatterbox.showSolutions(solver);
        solver.findSolution();
        Chatterbox.printStatistics(solver);

    }


    private static ArrayList<Path[]> initiatePaths(List<Service> services, Graph graph, Solver solver) {

        ArrayList<Path[]> allPaths = new ArrayList<>();

        for(Service s: services) {

            Iterator<PairOfComponents> pairsOfComponents = s.getRequiredLatencies().keySet().iterator();

            ArrayList<PairOfComponents> effectivePairOfComponents = new ArrayList<>();

            while (pairsOfComponents.hasNext()) {

                PairOfComponents pairOfComponents = pairsOfComponents.next();

                if (s.getRequiredLatencies().get(pairOfComponents) != -1) effectivePairOfComponents.add(pairOfComponents);
            }

            Path[] paths = new Path[effectivePairOfComponents.size()];

            int i = 0;
            for (PairOfComponents pairOfComponents : effectivePairOfComponents) {

                paths[i] = new Path(graph, pairOfComponents.getComponent1(), pairOfComponents.getComponent2(), s.getRequiredLatencies().get(pairOfComponents), solver);
                i++;
            }

            allPaths.add(paths);
        }

        return allPaths;
    }

}
