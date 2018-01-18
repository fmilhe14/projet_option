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



    this.graph=graph;
    this.indiceComponent1=indiceComponent1;
    this.indiceComponent2 = indiceComponent2;
    this.requiredLatencie = requiredLatencie;
    this.solver = solver ;




}
}



/* reste à écrire les contriantes qui maintiennent liés les 3 représentations des chemins
et les contraintes qui vérifient que la validité du chemin (latence etc)
*/