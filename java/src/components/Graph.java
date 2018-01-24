package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
public class Graph {

    private List<Node> nodes;
    private List<Edge> edges;
    private Map<int[], Integer> edgeIds;

    public Graph(List<Node> nodes, List<Edge> edges) {

        this.edges = edges;
        this.edgeIds = getEdgeIds(this.edges);
        this.nodes = nodes;
    }

    private Map<int[], Integer> getEdgeIds(List<Edge> edges){

        Map<int[], Integer> map = new HashMap<>();

        for (Edge edge : edges) {

            map.put(new int[]{edge.getNode1().getId(), edge.getNode2().getId()}, edge.getId());
        }

        return map;
    }
}
