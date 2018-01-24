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

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.ifOnlyIf;

@Builder
@Getter
@Setter
public class Path {

    private Graph graph;
    private int indiceComponent1;
    private int indiceComponent2;
    private int requiredLatencie;
    private Solver solver;

    private IntVar[] successeur;
    private SetVar noeudsVisites;
    private SetVar arcsVisites;


    public Path(Graph graph, int indiceComponent1, int indiceComponent2, int requiredLatencie, Solver solver) {


        this.graph = graph;
        this.indiceComponent1 = indiceComponent1;
        this.indiceComponent2 = indiceComponent2;
        this.requiredLatencie = requiredLatencie;
        this.solver = solver;

        int nbNodes = this.getGraph().getNodes().size();
        int nbArc = this.getGraph().getEdges().size();

        this.successeur = VariableFactory.boundedArray("successeur", nbNodes + 1, 0, nbNodes, getSolver());


        int[] enveloppe1 = new int[nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            enveloppe1[i] = i;
        }

        this.noeudsVisites = VariableFactory.set("noeudsVisites", enveloppe1, new int[]{}, this.getSolver());

        int[] enveloppe2 = new int[nbArc];
        for (int i = 0; i < nbArc; i++) {
            enveloppe1[i] = i;
        }

        this.arcsVisites = VariableFactory.set("arcsVisites", enveloppe2, new int[]{}, this.getSolver());


        solver.post();

        //Contraintes ALL DIFFERENT sur tous les successeurs : on ne repasse pas par un noeud
        solver.post(IntConstraintFactory.alldifferent(this.getSuccesseur()));

        //La liste successeur comment par le premier composant
        solver.post(IntConstraintFactory.arithm(successeur[0], "=", indiceComponent1));

        //Le dernier composant boucle sur l'indice 0
        solver.post(IntConstraintFactory.arithm(successeur[indiceComponent2], "=", 0));


        successeurArcsConstraints();

        //Si un noeud a un sucesseur, alors ce noeud doit être dans le SetVar des noeuds visités
        successeurNoeudsConstraints();

    }

    private void successeurNoeudsConstraints() {

        int n = this.getSuccesseur().length;

        for (int i = 0; i < n; i++) {

            Constraint different = IntConstraintFactory.arithm(this.successeur[i], "!=", VariableFactory.bounded("", i, i, solver), "!=", 0);
            Constraint contient = SetConstraintsFactory.member(this.getSuccesseur()[i], this.getNoeudsVisites());
            ifOnlyIf(different.reif(), contient.reif());
        }

    }

    private void successeurArcsConstraints() {

        int n = this.getSuccesseur().length;

        for (int i = 0; i < n; i++) {

            Constraint different = IntConstraintFactory.arithm(this.successeur[i], "!=", VariableFactory.bounded("", i, i, solver), "!=", 0);

            Constraint contient = SetConstraintsFactory.member(VariableFactory.bounded("", this.graph.getEdgeIds().get(new int[]{i, this.successeur[i].getLB()}),
                    this.graph.getEdgeIds().get(new int[]{i, this.successeur[i].getLB()}), solver), this.getArcsVisites());

            ifOnlyIf(different.reif(), contient.reif());

        }
    }

    private void pathLatencyConstraint() {

        int currentLatency = 0;
        int[] visitedEdges = this.getArcsVisites().getValues();

    }

}



/* reste à écrire les contriantes qui maintiennent liés les 3 représentations des chemins
et les contraintes qui vérifient que la validité du chemin (latence etc)
*/