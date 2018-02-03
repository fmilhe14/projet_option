package components;

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

import java.util.*;


@Getter
@Setter
public class Path {

    private Graph graph;
    private int id;

    private Component componentA;
    private Component componentB;

    private int requiredLatency;
    private Solver solver;

    private IntVar[] successeur;
    private IntVar[] arcsPossibleSuccesseur;
    private SetVar noeudsVisites;
    private SetVar arcsVisites;


    public Path(Graph graph, Component componentA, Component componentB, int requiredLatency, Solver solver, int id) {


        this.id = id;
        this.componentA = componentA;
        this.componentB = componentB;
        this.requiredLatency = requiredLatency;
        this.solver = solver;
        this.graph = graph;

        int nbNodes = this.getGraph().getNodes().size();
        int nbArcs = this.getGraph().getEdges().size();

        this.successeur = new IntVar[nbNodes + 1];
        this.arcsPossibleSuccesseur = new IntVar[nbNodes + 1];

        for (int i = 0; i < nbNodes + 1; i++) {

            this.successeur[i] = VariableFactory.integer("successeur de " + i, 0, nbNodes, solver);
        }

        solver.post(IntConstraintFactory.arithm(successeur[0], "=", componentA.getPosition()));
        solver.post(IntConstraintFactory.alldifferent(successeur));

        buildConstraintsBetweenSuccesseurNodesAndSuccesseurArc(nbArcs);

        int[] enveloppe1 = buildEnveloppe(nbNodes + 1);
        this.noeudsVisites = VariableFactory.set("noeudsVisites", enveloppe1, new int[]{}, this.getSolver());

        int[] enveloppe2 = buildEnveloppe(nbArcs);
        this.arcsVisites = VariableFactory.set("arcsVisites", enveloppe2, new int[]{}, this.getSolver());

        //Si un noeud a un successeur, alors l'arc qui relie les deux noeuds doit être dans le SetVar des arcs visités
        for (int i = 0; i < successeur.length; i++) {

            BoolVar different = IntConstraintFactory.arithm(this.successeur[i], "!=", i).reif();

            //Si un noeud a un successeur, alors ce noeud doit être dans le SetVar des noeuds visités
            successeurNoeudsConstraints(different, i);
            //Si un noeud a un successeur, alors l'arc qui relie les deux noeuds doit être dans le SetVar des arcs visités
            successeurArcsConstraints(different, i);
        }

        //Contrainte de latence sur les deux composants
        pathLatencyConstraint();

        //On s'assure avec cette contrainte que si un composant du chemin est sur un noeud, alors le noeud contient bien le composant
        nodeSetContainsComponent();

        //Pour prendre en compte les arcs empruntés et ajuster la bande passante disponuble
        edgeSetContainsCoupleComponents();
    }

    private void buildConstraintsBetweenSuccesseurNodesAndSuccesseurArc(int nbArcs) {


        solver.post(IntConstraintFactory.arithm(successeur[0], "=", componentA.getPosition()));
        solver.post(IntConstraintFactory.alldifferent(successeur));

        for (int i = 0; i < successeur.length; i++) {

            int[] potsuccs = convertIntegers(findPotentialSucc(i));
            int[] potarcs = findPotentialArcs(i, potsuccs);

            //Cas du noeud fictif 0
            if (i == 0) {
                potsuccs = new int[]{componentA.getPosition().getValue()};
                potarcs = new int[]{0};
            }

            IntVar indiceSuccesseurs = VariableFactory.bounded("indsucc_" + i, 0, potsuccs.length - 1, solver);
            arcsPossibleSuccesseur[i] = VariableFactory.bounded("arcsucc_" + i, 0, nbArcs, solver);

            solver.post(IntConstraintFactory.element(successeur[i], potsuccs, indiceSuccesseurs));
            solver.post(IntConstraintFactory.element(arcsPossibleSuccesseur[i], potarcs, indiceSuccesseurs));

            Constraint successeurDeIestZero = IntConstraintFactory.arithm(successeur[i], "=", 0);
            Constraint indiceOfComponent2 = IntConstraintFactory.arithm(componentB.getPosition(), "=", i);

            solver.post(IntConstraintFactory.arithm(successeurDeIestZero.reif(), "=", indiceOfComponent2.reif()));

        }
    }

    private void successeurNoeudsConstraints(BoolVar different, int i) {

        BoolVar contient = SetConstraintsFactory.member(this.successeur[i], this.noeudsVisites).reif();
        solver.post(IntConstraintFactory.arithm(different, "=", contient));
    }

    private void successeurArcsConstraints(BoolVar different, int i) {

        BoolVar contient = SetConstraintsFactory.member(this.arcsPossibleSuccesseur[i], this.arcsVisites).reif();
        solver.post(IntConstraintFactory.arithm(different, "=", contient));
    }

    private void nodeSetContainsComponent() {

        for (int i = 0; i < graph.getNodes().size(); i++) {

            Node node = graph.getNodes().get(i);

            BoolVar componentAOnNodeI = IntConstraintFactory.arithm(componentA.getPosition(), "=", node.getId()).reif();
            BoolVar setOfNodeIContainsComponentA = SetConstraintsFactory.member(VariableFactory.fixed(componentA.getId(), solver), node.getCompSurNoeud()).reif();

            BoolVar componentBOnNodeI = IntConstraintFactory.arithm(componentB.getPosition(), "=", node.getId()).reif();
            BoolVar setOfNodeIContainsComponentB = SetConstraintsFactory.member(VariableFactory.fixed(componentB.getId(), solver), node.getCompSurNoeud()).reif();

            solver.post(IntConstraintFactory.arithm(componentAOnNodeI, "=", setOfNodeIContainsComponentA));
            solver.post(IntConstraintFactory.arithm(componentBOnNodeI, "=", setOfNodeIContainsComponentB));

        }
    }

    private void edgeSetContainsCoupleComponents() {

        List<Edge> edges = this.graph.getEdges();

        edges.sort(Comparator.comparing(Edge::getId));

        for (int i = 0; i < edges.size(); i++) {

            Edge edge = edges.get(i);

            BoolVar setOfEdgeContainsEdge = SetConstraintsFactory.member(VariableFactory.fixed(edge.getId(), solver), arcsVisites).reif();
            BoolVar setOfCoupleComponentsContainsPath = SetConstraintsFactory.member(VariableFactory.fixed(id, solver), edge.getCoupleComponentsOnEdge()).reif();

            solver.post(IntConstraintFactory.arithm(setOfEdgeContainsEdge, "=", setOfCoupleComponentsContainsPath));

        }
    }

    private void pathLatencyConstraint() {

        this.getGraph().getEdges().sort(Comparator.comparing(Edge::getId));

        int[] latenciesEdges = new int[this.getGraph().getEdges().size()];

        for (int i = 0; i < this.getGraph().getEdges().size(); i++)
            latenciesEdges[i] = this.getGraph().getEdges().get(i).getLatency();


        solver.post(SetConstraintsFactory.sum(this.arcsVisites, latenciesEdges
                , 0, VariableFactory.bounded("MAX LATENCY", 0, requiredLatency, solver), false));

    }

    private Map<PairOfIndex, Integer> getEdgeIds(List<Edge> edges) {

        Map<PairOfIndex, Integer> map = new HashMap<>();

        for (Edge edge : edges) {

            map.put(new PairOfIndex(edge.getNode1().getId(), edge.getNode2().getId()), edge.getId());
        }

        return map;
    }

    public int[] convertIntegers(Set<Integer> integers) {

        int[] ints = new int[integers.size()];
        int index = 0;
        for (Integer i : integers) {
            ints[index++] = i;
        }

        return ints;
    }

    public Set<Integer> findPotentialSucc(int i) {

        Set<Integer> potentialSuccesseur = new HashSet<>();

        potentialSuccesseur.add(0);

        for (int j = 0; j < graph.getEdges().size(); j++) {

            Node node1 = graph.getEdges().get(j).getNode1();
            Node node2 = graph.getEdges().get(j).getNode2();

            if (node1.getId() != node2.getId()) {

                if (node1.getId() == i) {
                    potentialSuccesseur.add(node2.getId());
                }


                if (node2.getId() == i) {
                    potentialSuccesseur.add(node1.getId());
                }
            }
        }

        potentialSuccesseur.add(i); //Cas où il est son propre successeur

        return potentialSuccesseur;
    }

    public int[] findPotentialArcs(int i, int[] potsuccs) {

        Map<PairOfIndex, Integer> map = getEdgeIds(graph.getEdges());

        int[] potentialArcs = new int[potsuccs.length];
        potentialArcs[0] = 0;

        for (int j = 1; j < potentialArcs.length; j++) {

            if (map.get(new PairOfIndex(i, potsuccs[j])) != null) {
                potentialArcs[j] = map.get(new PairOfIndex(i, potsuccs[j]));
            } else {
                potentialArcs[j] = map.get(new PairOfIndex(potsuccs[j], i));
            }

        }

        return potentialArcs;
    }

    private int[] buildEnveloppe(int size) {

        int[] enveloppe = new int[size];
        for (int i = 0; i < size; i++) {
            enveloppe[i] = i;
        }

        return enveloppe;
    }
}
