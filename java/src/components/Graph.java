package components;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
