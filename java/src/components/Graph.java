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

    private List<Node> nodes ;
    private List<Arc> arcs ;
    private Map<int[], Integer> retrieveArcId;

    public Graph(List<Node> nodes,List<Arc> arcs){
        this.arcs = arcs ;

        this.retrieveArcId = new HashMap<>();

        for(Arc arc: arcs){

            retrieveArcId.put(new int[]{arc.getNode1().getId(), arc.getNode2().getId()}, arc.getId());
        }

        this.nodes =nodes ;
    }
}
