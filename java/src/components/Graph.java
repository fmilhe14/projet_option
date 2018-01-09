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
    private double[][] latencies;
    private double[][] bandwidths;


    public Graph(List<Node> nodes, double[][] latencies, double[][] bandwidths){

        this.nodes = nodes;
        this.latencies = latencies;
        this.bandwidths = bandwidths;
    }
}
