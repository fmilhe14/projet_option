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
    private List<Service> services;
    private int[][] latencies;
    private int[][] bandwidths;


    public Graph(List<Node> nodes, List<Service> services, int[][] latencies, int[][] bandwidths) {

        this.nodes = nodes;
        this.services = services;
        this.latencies = latencies;
        this.bandwidths = bandwidths;
    }
}
