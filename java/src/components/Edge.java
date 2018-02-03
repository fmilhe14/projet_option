package components;

import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
public class Edge {

    //Edge ID
    private int id;

    private Node node1;
    private Node node2;

    private int bandwidth;
    private int latency;

    private Data data;

    private IntVar bandwidthConso;
    private SetVar coupleComponentsOnEdge;

    public Edge(int id, Node node1, Node node2, Data data) {

        this.data = data;

        this.node1 = node1;
        this.node2 = node2;

        int nbCoupleComponents = 0;

        for (Service s : data.getServices()) nbCoupleComponents += s.getRequiredLatencies().keySet().size();

        int[] enveloppe = new int[nbCoupleComponents];

        for (int i = 0; i < nbCoupleComponents; i++) {

            enveloppe[i] = i;
        }

        this.id = id;

        this.bandwidth = data.getNetworkBandwidths()[node1.getId() - 1][node2.getId() - 1];

        //Si c'est un arc fictif (qui relie le noeud a lui même)
        if (node1 == node2) bandwidth = 0;

        this.latency = data.getNetworkLatencies()[node1.getId() - 1][node2.getId() - 1];

        this.bandwidthConso = VariableFactory.bounded("bandePassanteConsommée sur l'arc " + id, 0, this.getBandwidth(),
                data.getSolver());

        this.coupleComponentsOnEdge = VariableFactory.set("couple de composants sur l'arc " + id, enveloppe, new int[]{},
                data.getSolver());

        //Si l'arc n'est pas fictif
        if (node1 != node2) {
            this.coherenceconstraints();
        }
    }

    private void coherenceconstraints() {

        // contrainte qui fait le lien entre les couples de composant utilisant l'arc et la bande passante consommée

        data.getSolver().post(SetConstraintsFactory.sum(this.coupleComponentsOnEdge, this.data.getCoupleComponentsRequiredBandwidth()
                , 0, this.bandwidthConso, false));


    }

}
