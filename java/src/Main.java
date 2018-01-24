import components.*;
import org.chocosolver.solver.Solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Solver solver = new Solver("");

        List<Service> services = Parser.getServices();
        int[][] networkLatencies = Parser.getNetworkLatencies();
        int[][] networkBandwidths = Parser.getNetworkBandwidths();
        int[] networkCpus = Parser.getNetworkCpus();
        int[] networkMem = Parser.getNetworkMem();

        Data data = new Data(services, networkLatencies, networkBandwidths, networkCpus, networkMem, solver);

        List<Node> nodes = data.buildNodes();
        List<Edge> edges = data.buildEdges();

        Graph graph = new Graph();


    }


    private Path[] initiatePaths(Solver solver) {

        Iterator<Component[]> pairsOfComponents = this.requiredLatencies.keySet().iterator();

        ArrayList<Component[]> effectivePairs = new ArrayList<>();

        while(pairsOfComponents.hasNext()){

            Component[] pair = pairsOfComponents.next();
            if(requiredLatencies.get(pair) != -1) effectivePairs.add(pair) ;
        }

        Path[] paths = new Path[effectivePairs.size()];

        int i = 0;
        for(Component[] pair : effectivePairs){

            int indiceComponent1 = pair[0].getId();
            int indiceComponent2 = pair[1].getId();

            paths[i] = new Path(graph, indiceComponent1, indiceComponent2, this.requiredLatencies.get(pair), solver);
            i ++;
        }

        return paths;
    }
}
