package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;

import java.util.*;

@Builder
@Getter
@Setter
public class Graph {

    private List<Node> nodes;
    private List<Edge> edges;

    public Graph(List<Node> nodes, List<Edge> edges) {

        this.edges = edges;
        this.nodes = nodes;

    }
}
