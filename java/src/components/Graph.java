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
    private double[][] latencies;
    private double[][] bandwidths;


    public Graph(List<Node> nodes, List<Service> services, double[][] latencies, double[][] bandwidths){

        this.nodes = nodes;
        this.services = services;
        this.latencies = latencies;
        this.bandwidths = bandwidths;
    }
}
