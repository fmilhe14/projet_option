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
    private IntVar[] arcsPossibleSuccesseur;
    private SetVar noeudsVisites;
    private SetVar arcsVisites;


    public Path(Graph graph, Component component1, Component component2, int requiredLatency, Solver solver) {

        this.component1 = component1;
        this.component2 = component2;
        this.requiredLatency = requiredLatency;
        this.solver = solver;
        this.graph = graph;

        int nbNodes = this.getGraph().getNodes().size();
        int nbArcs = this.getGraph().getEdges().size();

        this.successeur = VariableFactory.boundedArray("successeur", nbNodes + 1, 0, nbNodes, getSolver());
        this.arcsPossibleSuccesseur = new IntVar[nbNodes + 1];

        for(int i = 1; i <successeur.length; i++){

            int[] potsuccs = convertIntegers(findPotentialSucc(i-1));
            int[] potarcs = convertIntegers(findPotentialArcs(i-1));

            IntVar indiceSuccesseurs = VariableFactory.integer("indsucc_"+i, 0, potsuccs.length, solver);
            arcsPossibleSuccesseur[i] = VariableFactory.integer("arcsucc_"+i, 0, nbArcs, solver);

            //Je crois qu'il faut créer des arcs qui vont du noeud à lui même

            solver.post(IntConstraintFactory.element(successeur[i], potsuccs, indiceSuccesseurs));
            solver.post(IntConstraintFactory.element(arcsPossibleSuccesseur[i], potarcs, indiceSuccesseurs));
        }


        int[] enveloppe1 = new int[nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            enveloppe1[i] = i;
        }

        this.noeudsVisites = VariableFactory.set("noeudsVisites", enveloppe1, new int[]{}, this.getSolver());

        int[] enveloppe2 = new int[nbArcs];

        for (int i = 0; i < nbArcs; i++) {
            enveloppe2[i] = i;
        }

        this.arcsVisites = VariableFactory.set("arcsVisites", enveloppe2, new int[]{}, this.getSolver());

        //Contraintes ALL DIFFERENT sur tous les successeurs : on ne repasse pas par un noeud
        solver.post(IntConstraintFactory.alldifferent(this.getSuccesseur()));


        solver.post(IntConstraintFactory.arithm(successeur[0], "=", component1.getPosition()));

        //Le dernier composant boucle sur l'indice 0
  //      solver.post(IntConstraintFactory.arithm(successeur[component2.getPosition().getValue()], "=", 0)); //Pas besoin de le mettre avec le all diff ?

        successeurArcsConstraints();

        //Si un noeud a un sucesseur, alors ce noeud doit être dans le SetVar des noeuds visités
        successeurNoeudsConstraints();

        //Contrainte de latence
   //     pathLatencyConstraint();

    }

    private void successeurNoeudsConstraints() {

        int n = this.getSuccesseur().length;

        for (int i = 0; i < n; i++) {

            Constraint different = IntConstraintFactory.arithm(this.successeur[i], "!=", i);
            Constraint differentOf0 = IntConstraintFactory.arithm(this.successeur[i], "!=", 0);

            Constraint contient = SetConstraintsFactory.member(this.getSuccesseur()[i], this.getNoeudsVisites());

            ifOnlyIf(different.reif(), contient.reif());
            ifOnlyIf(differentOf0.reif(), contient.reif());

        }

    }

    private void successeurArcsConstraints() {

        int n = this.getSuccesseur().length;

        for (int i = 1; i < n; i++) {

            Constraint different = IntConstraintFactory.arithm(this.successeur[i], "!=", i);
            Constraint contient = SetConstraintsFactory.member(this.arcsPossibleSuccesseur[i], this.arcsVisites);

            ifOnlyIf(different.reif(), contient.reif());
        }
    }

    private void pathLatencyConstraint() {

        this.getGraph().getEdges().sort(Comparator.comparing(Edge::getId));

        int[] latenciesEdges = new int[this.getGraph().getEdges().size()];

        for (int i = 0; i < this.getGraph().getEdges().size(); i++) latenciesEdges[i] = this.getGraph().getEdges().get(i).getLatency();

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

    public int[] convertIntegers(Set<Integer> integers)
    {
        int[] ints = new int[integers.size()];
        int index = 0;
        for(Integer i : integers){
            ints[index++] = i;
        }

        return ints;
    }

    public Set<Integer> findPotentialSucc(int i){

        Set<Integer> potentialSuccesseur = new HashSet<>();

        for(int j = 0; j<graph.getEdges().size(); j++){

            Node node1 = graph.getEdges().get(j).getNode1();
            Node node2 = graph.getEdges().get(j).getNode2();

            if(node1.getId() == i) {
                potentialSuccesseur.add(node2.getId());
            }


            if(node2.getId() == i){
                potentialSuccesseur.add(node1.getId());
            }
        }

        return potentialSuccesseur;
    }

    public Set<Integer> findPotentialArcs(int i){

        Set<Integer> res = new HashSet<>();

        Map<PairOfIndex, Integer> map = getEdgeIds(graph.getEdges());
        Iterator<PairOfIndex> pairs = map.keySet().iterator();

        while(pairs.hasNext()){

            PairOfIndex pairOfIndex = pairs.next();

            if(pairOfIndex.contains(i)) res.add(map.get(pairOfIndex)) ;
        }

        return  res;
    }
}
