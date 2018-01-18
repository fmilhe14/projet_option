package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

@Builder
@Getter
@Setter
public class Arc {

    private int id;
    private int bandwidth;
    private Graph graph;
    private Solver solver;
    private IntVar bandwidthConso;
    private SetVar coupleComposantSurArc;

    public Arc(int id,int bandwidth,Graph graph,Solver solver){

        int nbCoupleComponents = graph.getCoupleComponentes().length;
        int[] enveloppe = new int[nbCoupleComponents];
        for (int i = 0; i <nbCoupleComponents ; i++) {
            enveloppe[i]=i;
        }

        this.id=id;
        this.bandwidth=bandwidth;
        this.solver=solver;
        this.graph=graph;

        this.bandwidthConso = VariableFactory.bounded("bandePassanteConsommée",0,this.getBandwidth(),
                this.getSolver());

        this.coupleComposantSurArc = VariableFactory.set("coupleComposantSurArc",enveloppe,new int[]{},
                this.getSolver());

        this.coherenceconstraints();
    }

    private void coherenceconstraints(){

        // contrainte qui fait le lien entre les couples de composant utilisant l'arc et la bande passante consommée
        solver.post(SetConstraintsFactory.sum(this.coupleComposantSurArc,this.graph.getCoupleComponentesRequiredBandwidth()
                ,0,this.bandwidthConso,true));

        // les contraites vérifiant "bandwidthConso"<="bandwidthDispo" sont comprises dans la définition des variables bandwidthConso
    }

}
