package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Edge {


    private Node nodeI;
    private Node nodeJ;
    private double bandwidth;
    private double latency;

    public Edge(Node nodeI, Node nodeJ, double bandwidth, double latency){

        this.nodeI = nodeI;
        this.nodeJ = nodeJ;
        this.bandwidth = bandwidth;
        this.latency = latency;
    }

}
