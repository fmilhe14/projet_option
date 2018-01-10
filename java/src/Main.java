import components.*;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.math.BigInteger;
import java.util.List;

public class Main {


    public static void main(String[] args) {

        List<Service> serviceList = Parser.parseServices();
        Graph graph = Parser.parseGraph();

        int N = graph.getNodes().size();

        Solver solver = new Solver("");

        //Créer les variables intermédiaires cpu(i) : quantité de cpu occu

        IntVar[] cpus = new IntVar[N];

        for (int i = 0; i < N; i++) {

            IntVar cpui = VariableFactory.bounded("cpu used at node " + i, 0,
                     graph.getNodes().get(i).getCpu(), solver);
            cpus[i] = cpui;

        }

        //Créer les variables intermédiaires cpu(i) : quantité de cpu occupée au noeud i

        IntVar[] mems = new IntVar[N];

        for (int i = 0; i < N; i++) {

            IntVar memi = VariableFactory.bounded("mem used at node " + i, 0,
                     graph.getNodes().get(i).getMem(), solver);
            mems[i] = memi;

        }

        // Créer les variables bp(i, j) : quantité de bande passant occupée entre i et j

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

        //Contraintes sur les Noeuds

        for (int i = 0; i < N; i++) {

            for (Service s : serviceList) {

                cpuConstraintOnComponent(graph, solver, cpus, i, s.getFixedComponents());
                cpuConstraintOnComponent(graph, solver, cpus, i, s.getUnfixedComponents());
                memoryConstraintOnComponent(graph, solver, mems, i, s.getFixedComponents());
                memoryConstraintOnComponent(graph, solver, mems, i, s.getUnfixedComponents());


            }
        }

        //Contraintes sur les services

        for(Service s : serviceList){

            for(Component cf: s.getFixedComponents()){

                for(Component cm: s.getUnfixedComponents()){


                    IntVar[] path = s.getPaths()[cf.getPosition().getValue()][cm.getPosition().getValue()];

                    //On impose qu'un chemin ne repasse pas deux fois par le même noeud et que le premier élément corresponde au composant fixe
                    //et que le composant mobile ait pour successeur le composant fixe

                    solver.post(IntConstraintFactory.alldifferent(path));
                    solver.post(IntConstraintFactory.arithm(path[0], "=", VariableFactory.zero(solver)));
                    solver.post(IntConstraintFactory.arithm(path[N-1], "=", VariableFactory.bounded("CF", path[0].getValue(), path[0].getValue(), solver));

                    
                    //On impose que la latence entre deux noeuds successifs d'un chemin ne soit pas infinie (c'est à dire qu'ils sont bien reliés dans le graphe)
                    for(int i = 0; i<path.length; i++){

                        int succ = path[i].getValue();

                        if(i != succ) {

                           int l =  graph.getLatencies()[i][succ];

                           IntVar latency = VariableFactory.bounded("", l, l, solver);
                           IntVar infinite = VariableFactory.bounded("", Integer.MAX_VALUE, Integer.MAX_VALUE, solver);

                           solver.post(IntConstraintFactory.arithm(latency, "<", infinite));

                        }

                    }
                }
            }
        }
    }

    private static void cpuConstraintOnComponent(Graph graph, Solver solver, IntVar[] cpus, int i, List<Component> components) {
        for (int k = 0; k < components.size(); k++) {

            Component cf = components.get(k);

            int cpuUsed = cpus[cf.getPosition().getValue()].getValue();
            int cpuRequired = cf.getCpu();
            int cpuMax = graph.getNodes().get(i).getCpu();

            int value = cpuUsed + cpuRequired - cpuMax;

            IntVar var1 = VariableFactory.bounded("", value, value, solver);
            IntVar var2 = VariableFactory.bounded("", 0, 0, solver);

            solver.post(IntConstraintFactory.arithm(var1, ">=", var2));

        }
    }

    private static void memoryConstraintOnComponent(Graph graph, Solver solver, IntVar[] mems, int i, List<Component> components) {

        for (int k = 0; k < components.size(); k++) {

            Component cf = components.get(k);

            int memUsed = mems[cf.getPosition().getValue()].getValue();
            int memRequired = cf.getMem();
            int memMax = graph.getNodes().get(i).getMem();

            int value = memUsed + memRequired - memMax;

            IntVar var1 = VariableFactory.bounded("",  value,  value, solver);
            IntVar var2 = VariableFactory.bounded("", 0, 0, solver);

            solver.post(IntConstraintFactory.arithm(var1, ">=", var2));

        }
    }

}

