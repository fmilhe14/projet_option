package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class Graph {

    private List<Node> nodes;
    private List<Edge> edges;


    public Graph(List<Node> nodes, List<Edge> edges){

        this.nodes = nodes;
        this.edges = edges;
    }
}
