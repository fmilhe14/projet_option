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
    private Path[] path;
    private Map<Component[], Integer> requiredLatencies;
    private Map<Component[], Integer> requiredBandwidths;

    public Service(List<Component> Components, Graph graph, Map<Component[], Integer> requiredLatencies,
                   Map<Component[], Integer> bandwidths, Solver solver) {

        this.Components = Components;
        this.requiredLatencies = requiredLatencies;
        this.requiredBandwidths = bandwidths;

    }

}
