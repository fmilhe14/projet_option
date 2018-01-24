package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
public class Service {

    private List<Component> Components;
    private Map<Component[], Integer> requiredLatencies;
    private Map<Component[], Integer> requiredBandwidths;
    private Path[] paths;
    private Graph graph;


    public Service(List<Component> Components, Graph graph, Map<Component[], Integer> requiredLatencies,
                   Map<Component[], Integer> bandwidths, Solver solver) {

        this.Components = Components;
        this.requiredLatencies = requiredLatencies;
        this.requiredBandwidths = bandwidths;
        this.graph = graph;

        this.paths = initiatePaths(solver);
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
