package components;

import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.*;

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.ifOnlyIf;

@Getter
@Setter
public class Path {

    private Graph graph;

    private Component component1;
    private Component component2;

    private int requiredLatency;
    private Solver solver;

    private IntVar[] successeur;
    private IntVar[] arcssucc;
    private SetVar noeudsVisites;
    private SetVar arcsVisites;


    public Path(Graph graph, Component component1, Component component2, int requiredLatency, Solver solver) {

        this.component1 = component1;
        this.component2 = component2;
        this.requiredLatency = requiredLatency;
        this.solver = solver;
        this.graph = graph;

        int nbNodes = this.getGraph().getNodes().size();
        int nbArc = this.getGraph().getEdges().size();

        this.successeur = VariableFactory.boundedArray("successeur", nbNodes + 1, 0, nbNodes, getSolver());
        this.arcssucc = new IntVar[nbNodes +1]; //Pas sûr de la taille

        for(int i = 0; i <successeur.length; i++){

            int[] potsuccs = convertIntegers(findPotentialSucc(i));
            int[] potarcs = convertIntegers(findPotentialArcs(i));

            IntVar indsucc = VariableFactory.integer("indsucc_"+i, 0, potsuccs.length, solver);
            arcssucc[i] = VariableFactory.integer("arcsucc_"+i, 0, nbArc, solver);

            IntConstraintFactory.element(successeur[i], potsuccs, indsucc);
            IntConstraintFactory.element(arcssucc[i], potarcs, indsucc);
        }


        int[] enveloppe1 = new int[nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            enveloppe1[i] = i;
        }

        this.noeudsVisites = VariableFactory.set("noeudsVisites", enveloppe1, new int[]{}, this.getSolver());

        int[] enveloppe2 = new int[nbArc];

        for (int i = 0; i < nbArc; i++) {
            enveloppe2[i] = i;
        }

        this.arcsVisites = VariableFactory.set("arcsVisites", enveloppe2, new int[]{}, this.getSolver());

        //Contraintes ALL DIFFERENT sur tous les successeurs : on ne repasse pas par un noeud
        solver.post(IntConstraintFactory.alldifferent(this.getSuccesseur()));

        //La liste successeur comment par le premier composant
        solver.post(IntConstraintFactory.arithm(successeur[0], "=", component1.getPosition()));

        //Le dernier composant boucle sur l'indice 0
  //      solver.post(IntConstraintFactory.arithm(successeur[component2.getPosition().getValue()], "=", 0)); //ToDO je suis pas sûr que ce soit bon

        successeurArcsConstraints();

        //Si un noeud a un sucesseur, alors ce noeud doit être dans le SetVar des noeuds visités
        successeurNoeudsConstraints();

        //Contrainte de latence
   //     pathLatencyConstraint();

    }

    private void successeurNoeudsConstraints() {

        int n = this.getSuccesseur().length;

        for (int i = 0; i < n; i++) {

            Constraint different = IntConstraintFactory.arithm(this.successeur[i], "!=", VariableFactory.bounded("", i, i, solver));
            Constraint differentOf0 = IntConstraintFactory.arithm(this.successeur[i], "!=", VariableFactory.bounded("", 0, 0, solver));
            Constraint contient = SetConstraintsFactory.member(this.getSuccesseur()[i], this.getNoeudsVisites());
            ifOnlyIf(different.reif(), contient.reif());
            ifOnlyIf(differentOf0.reif(), contient.reif());

        }

    }

    private void successeurArcsConstraints() {

        int n = this.getSuccesseur().length;

        for (int i = 0; i < n; i++) {

            Constraint different = IntConstraintFactory.arithm(this.successeur[i], "!=", i);
            Constraint contient = SetConstraintsFactory.member(this.arcssucc[i], this.arcsVisites);

            ifOnlyIf(different.reif(), contient.reif());
        }
    }

    private void pathLatencyConstraint() {

        this.getGraph().getEdges().sort(Comparator.comparing(Edge::getId));

        int[] latenciesEdges = new int[this.getGraph().getEdges().size()];

        for (int i = 0; i < this.getGraph().getEdges().size(); i++)

            latenciesEdges[i] = this.getGraph().getEdges().get(i).getLatency();

        solver.post(SetConstraintsFactory.sum(this.getArcsVisites(), latenciesEdges
                , 0, VariableFactory.bounded("", this.requiredLatency, this.requiredLatency, solver), true));

    }

    private Map<PairOfIndex, Integer> getEdgeIds(List<Edge> edges) {

        Map<PairOfIndex, Integer> map = new HashMap<>();

        for (Edge edge : edges) {

            map.put(new PairOfIndex(edge.getNode1().getId(), edge.getNode2().getId()), edge.getId());
        }

        return map;
    }

    public int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    public ArrayList<Integer> findPotentialSucc(int i){

        ArrayList<Integer> potentialSuccesseur = new ArrayList<>();

        for(int j = 0; j<graph.getEdges().size(); j++){

            Node node1 = graph.getEdges().get(j).getNode1();

            if(node1.getId() == i) {
                potentialSuccesseur.add(node1.getId());
            }

            Node node2 = graph.getEdges().get(j).getNode1();

            if(node2.getId() == i){
                potentialSuccesseur.add(node2.getId());
            }
        }

        return potentialSuccesseur;
    }

    public ArrayList<Integer> findPotentialArcs(int i){

        ArrayList<Integer> res = new ArrayList<>();

        Map<PairOfIndex, Integer> map = getEdgeIds(graph.getEdges());
        Iterator<PairOfIndex> pairs = map.keySet().iterator();

        while(pairs.hasNext()){

            PairOfIndex pairOfIndex = pairs.next();

            if(pairOfIndex.contains(i)) res.add(i) ;
        }

        return  res;
    }

}
