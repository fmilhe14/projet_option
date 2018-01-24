package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Created by francoismilhem on 09/01/2018.
 */
@Builder
@Getter
@Setter
public class Node {

    private int id;
    private int cpu;
    private int mem;

    private Data data;

    private SetVar CompSurNoeud;
    private IntVar cpuConso;
    private IntVar memConso;
    private Solver solver;

    public Node(int cpu, int mem, int id, Solver solver, Data data) {

        int nbComponent = data.getComponents().size();
        int[] enveloppe = new int[nbComponent];

        for (int i = 0; i < nbComponent; i++) {
            enveloppe[i] = i;
        }

        this.data = data;
        this.cpu = cpu;
        this.mem = mem;
        this.solver = solver;
        this.cpuConso = VariableFactory.bounded("cpuconsomé", 0, this.cpu, this.solver);
        this.memConso = VariableFactory.bounded("memconsomé", 0, this.mem, this.solver);
        this.CompSurNoeud = VariableFactory.set("ComposantSurLeNoeud", enveloppe, new int[]{}, this.solver);

        this.coherenceconstraints();
    }

    private void coherenceconstraints() {

        // contrainte qui fait le lien entre le liste des composants et le cpu consommé sur le noeud
        solver.post(SetConstraintsFactory.sum(this.getCompSurNoeud(), this.data.getComponentsRequiredCpu()
                , 0, this.getCpuConso(), true));

        // idem avec la mémoire
        solver.post(SetConstraintsFactory.sum(this.getCompSurNoeud(), this.getData().getComponentsRequiredmem()
                , 0, this.getMemConso(), true));

        // les contraites vérifiant "ressourceConso"<="ressourceDispo" sont comprises dans la définition des variables "ressourceConso"

    }
}
