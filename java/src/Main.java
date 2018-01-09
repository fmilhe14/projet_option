import components.*;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

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
                    (int) graph.getNodes().get(i).getCpu(), solver);
            cpus[i] = cpui;

        }

        //Créer les variables intermédiaires cpu(i) : quantité de cpu occu

        IntVar[] mems = new IntVar[N];

        for (int i = 0; i < N; i++) {

            IntVar memi = VariableFactory.bounded("mem used at node " + i, 0,
                    (int) graph.getNodes().get(i).getMem(), solver);
            mems[i] = memi;

        }

        IntVar[][] bps = new IntVar[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {


                //Si les noeuds i et j ne sont pas reliés, alors on dit que la quantité de bande passante occupé entre les deux noeuds est infinie
                if (graph.getBandwidths()[i][j] == 0) {

                    IntVar bp_i_j = VariableFactory.bounded("bp used between node " + i + " and node " + j, Integer.MAX_VALUE,
                            Integer.MAX_VALUE, solver);

                } else {

                    IntVar bp_i_j = VariableFactory.bounded("bp used between node " + i + " and node " + j, 0,
                            (int) graph.getBandwidths()[i][j], solver);

                }

            }
        }

    }
}
