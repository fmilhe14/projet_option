package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.solver.variables.impl.BoolVarImpl;

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.ifOnlyIf;

@Builder
@Getter
@Setter
public class Path {

    private Graph graph;
    private int indiceComponent1;
    private int indiceComponent2;
    private int requiredLatencie;
    private Solver solver ;

    private IntVar[] successeur ;
    private SetVar noeudsVisités ;
    private SetVar arcsVisités ;



public Path(Graph graph,int indiceComponent1,int indiceComponent2,int requiredLatencie,Solver solver){



    this.graph = graph ;
    this.indiceComponent1=indiceComponent1;
    this.indiceComponent2 = indiceComponent2;
    this.requiredLatencie = requiredLatencie;
    this.solver = solver ;

    int nbNodes = this.getGraph().getNodes().size();
    int nbArc = this.getGraph().getArcs().size();

    this.successeur = VariableFactory.boundedArray("successeur",nbNodes+1,0,nbNodes.size(),getSolver());


    int[] enveloppe1 = new int[nbNodes];
    for (int i = 0; i <nbNodes ; i++) {
        enveloppe1[i]=i;
    }

    this.noeudsVisités = VariableFactory.set("noeudsVisités",enveloppe1,new int[]{},this.getSolver());

    int[] enveloppe2 = new int[nbArc];
    for (int i = 0; i <nbArc ; i++) {
        enveloppe1[i]=i;
    }

    this.arcsVisités = VariableFactory.set("arcsVisités",enveloppe2,new int[]{},this.getSolver());




}

private void successeurNoeudsConstraints(){

    int n = this.getSuccesseur().length;
    for (int i = 0; i < n; i++) {

       Constraint different = IntConstraintFactory.arithm(this.successeur[i],"=!",i);
       Constraint contient = SetConstraintsFactory.member(this.getSuccesseur()[i],this.getNoeudsVisités());

       solver.post(ifOnlyIf(IntConstraintFactory.arithm(this.successeur[i],"=!",i).reif(),
               SetConstraintsFactory.member(this.getSuccesseur()[i],this.getNoeudsVisités())));
    }

}



}



/* reste à écrire les contriantes qui maintiennent liés les 3 représentations des chemins
et les contraintes qui vérifient que la validité du chemin (latence etc)
*/