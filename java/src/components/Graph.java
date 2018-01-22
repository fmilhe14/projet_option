package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class Graph {

    private List<Node> nodes ;
    private List<Arc> arcs ;

    public Graph(List<Node> nodes,List<Arc> arcs){
        this.arcs = arcs ;
        this.nodes =nodes ;
    }
}
