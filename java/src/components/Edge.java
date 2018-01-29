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

    private int id;
    private Node node1;
    private Node node2;

    private int bandwidth;
    private int latency;

    private Data data;

    private IntVar bandwidthConso;
    private SetVar coupleComposantSurArc;

    public Edge(int id, Node node1, Node node2, Data data) {

        this.data = data;


        this.node1 = node1;
        this.node2 = node2;

        int nbCoupleComponents = 0;

        for(Service s: data.getServices()) nbCoupleComponents += s.getRequiredLatencies().keySet().size();

        int[] enveloppe = new int[nbCoupleComponents];

        for (int i = 0; i < nbCoupleComponents; i++) {

            enveloppe[i] = i;
        }

        this.id = id;
        this.bandwidth = data.getNetworkBandwidths()[node1.getId()][node2.getId()];
        this.latency = data.getNetworkLatencies()[node1.getId()][node2.getId()];

        this.bandwidthConso = VariableFactory.bounded("bandePassanteConsommée", 0, this.getBandwidth(),
                data.getSolver());

        this.coupleComposantSurArc = VariableFactory.set("coupleComposantSurArc", enveloppe, new int[]{},
                data.getSolver());

        this.coherenceconstraints();
    }

    private void coherenceconstraints() {

        // contrainte qui fait le lien entre les couples de composant utilisant l'arc et la bande passante consommée

        data.getSolver().post(SetConstraintsFactory.sum(this.coupleComposantSurArc, this.data.getCoupleComponentsRequiredBandwidth()
                , 0, this.bandwidthConso, true));

    }

}