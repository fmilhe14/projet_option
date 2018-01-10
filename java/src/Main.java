import components.Component;
import components.Graph;
import components.Service;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        //Parsing des données
        List<Service> serviceList = Parser.parseServices();
        Graph graph = Parser.parseGraph();

        int N = graph.getNodes().size();

        Solver solver = new Solver("");

        //Déclaration des variables intermédiaires

        //Quantité de cpu occupés dans un noeud

        IntVar[] cpus = new IntVar[N];

        for (int i = 0; i < N; i++) {

            IntVar cpui = VariableFactory.bounded("cpu used at node " + i, 0,
                    graph.getNodes().get(i).getCpu(), solver);
            cpus[i] = cpui;

        }

        //Quantité de mémoire utilisée dans un noeud

        IntVar[] mems = new IntVar[N];

        for (int i = 0; i < N; i++) {

            IntVar memi = VariableFactory.bounded("mem used at node " + i, 0,
                    graph.getNodes().get(i).getMem(), solver);
            mems[i] = memi;

        }

        //Quantité de bande passante occupée entre deux noeuds

        IntVar[][] bps = new IntVar[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {

                //Si les noeuds i et j ne sont pas reliés, alors on dit que la quantité de bande passante occupé entre les deux noeuds est infinie
                bps[i][j] = graph.getBandwidths()[i][j] == 0

                        ? VariableFactory.bounded("bp used between node " + i + " and node " + j, Integer.MAX_VALUE,
                        Integer.MAX_VALUE, solver)

                        : VariableFactory.bounded("bp used between node " + i + " and node " + j, 0,
                        graph.getBandwidths()[i][j], solver);
            }
        }

        //Contraintes

        for (int i = 0; i < N; i++) {

            for (Service s : serviceList) {

                //Contraintes sur les Noeuds

                cpuConstraintOnComponent(graph, solver, cpus, i, s.getFixedComponents());
                cpuConstraintOnComponent(graph, solver, cpus, i, s.getUnfixedComponents());
                memoryConstraintOnComponent(graph, solver, mems, i, s.getFixedComponents());
                memoryConstraintOnComponent(graph, solver, mems, i, s.getUnfixedComponents());


                //Contraintes sur les services
                for (Component cf : s.getFixedComponents()) {
                    for (Component cm : s.getUnfixedComponents()) {

                        pathContraints(graph, N, solver, s, cf, cm);
                    }
                }
            }
        }

    }

    private static void pathContraints(Graph graph, int n, Solver solver, Service s, Component cf, Component cm) {

        IntVar[] path = s.getPaths()[cf.getPosition().getValue()][cm.getPosition().getValue()];

        //On impose qu'un chemin ne repasse pas deux fois par le même noeud et que le premier élément corresponde au composant fixe
        //et que le composant mobile ait pour successeur le composant fixe

        solver.post(IntConstraintFactory.alldifferent(path));
        solver.post(IntConstraintFactory.arithm(path[0], "=", VariableFactory.zero(solver)));
        solver.post(IntConstraintFactory.arithm(path[n - 1], "=", VariableFactory.bounded("CF", path[0].getValue(), path[0].getValue(), solver)));

        //Calcul de la latence du chemin avec la variable tmp
        int tmp = 0;

        //On impose que la latence entre deux noeuds successifs d'un chemin ne soit pas infinie (c'est à dire qu'ils sont bien reliés dans le graphe)
        for (int i = 0; i < path.length; i++) {

            int succ = path[i].getValue();

            if (i != succ) {

                int l = graph.getLatencies()[i][succ];

                IntVar latency = VariableFactory.bounded("", l, l, solver);
                IntVar infinite = VariableFactory.bounded("", Integer.MAX_VALUE, Integer.MAX_VALUE, solver);

                solver.post(IntConstraintFactory.arithm(latency, "<", infinite));

                tmp += latency.getValue();

            }

        }

        int requiredLatence = s.getRequiredLatencies()[cf.getId()][cm.getId()]; //TODO Faut qu'on en parle pour se mettre ok, mais en gros c'est bien si dans chaque service les index des composants fixes vont de 0 à NB_COMPOSANTS_FIXES et pareil pour les composants mobiles

        IntVar rL = VariableFactory.bounded("Required Latency", requiredLatence, requiredLatence, solver);
        IntVar currentLatency = VariableFactory.bounded("Current Latency", tmp, tmp, solver);

        solver.post(IntConstraintFactory.arithm(currentLatency, "<=", rL));
    }

    private static void cpuConstraintOnComponent(Graph graph, Solver solver, IntVar[] cpus, int i, List<Component> components) {

        for (Component component : components) {

            int cpuUsed = cpus[component.getPosition().getValue()].getValue();
            int cpuRequired = component.getCpu();
            int cpuMax = graph.getNodes().get(i).getCpu();

            int value = cpuUsed + cpuRequired - cpuMax;

            IntVar var1 = VariableFactory.bounded("", value, value, solver);
            IntVar var2 = VariableFactory.bounded("", 0, 0, solver);

            solver.post(IntConstraintFactory.arithm(var1, ">=", var2));

        }
    }

    private static void memoryConstraintOnComponent(Graph graph, Solver solver, IntVar[] mems, int i, List<Component> components) {

        for (Component component : components) {

            int memUsed = mems[component.getPosition().getValue()].getValue();
            int memRequired = component.getMem();
            int memMax = graph.getNodes().get(i).getMem();

            int value = memUsed + memRequired - memMax;

            IntVar var1 = VariableFactory.bounded("", value, value, solver);
            IntVar var2 = VariableFactory.bounded("", 0, 0, solver);

            solver.post(IntConstraintFactory.arithm(var1, ">=", var2));

        }
    }

}

