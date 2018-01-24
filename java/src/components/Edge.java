package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

@Builder
@Getter
@Setter
public class Edge {

    private int id;
    private Node node1;
    private Node node2;
    private int bandwidth;
    private Data data;
    private Solver solver;
    private IntVar bandwidthConso;
    private SetVar coupleComposantSurArc;

    public Edge(int id, Node node1, Node node2, int bandwidth, Data data, Solver solver) {

        this.node1 = node1;
        this.node2 = node2;

        int nbCoupleComponents = 0;

        for(Service s: data.getServices()) nbCoupleComponents += s.getRequiredLatencies().keySet().size();

        int[] enveloppe = new int[nbCoupleComponents];

        for (int i = 0; i < nbCoupleComponents; i++) {

            enveloppe[i] = i;
        }

        this.id = id;
        this.bandwidth = bandwidth;
        this.solver = solver;
        this.data = data;

        this.bandwidthConso = VariableFactory.bounded("bandePassanteConsommée", 0, this.getBandwidth(),
                this.getSolver());

        this.coupleComposantSurArc = VariableFactory.set("coupleComposantSurArc", enveloppe, new int[]{},
                this.getSolver());

        this.coherenceconstraints();
    }

    private void coherenceconstraints() {

        // contrainte qui fait le lien entre les couples de composant utilisant l'arc et la bande passante consommée
        solver.post(SetConstraintsFactory.sum(this.coupleComposantSurArc, this.data.getCoupleComponentsRequiredBandwidth()
                , 0, this.bandwidthConso, true));

        // les contraites vérifiant "bandwidthConso"<="bandwidthDispo" sont comprises dans la définition des variables bandwidthConso
    }

}
