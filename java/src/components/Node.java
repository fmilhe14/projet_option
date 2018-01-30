package components;

import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Created by francoismilhem on 09/01/2018.
 */
@Getter
@Setter
public class Node {

    private int id;
    private int cpu;
    private int mem;

    private Data data;

    private SetVar compSurNoeud;
    private IntVar cpuConso;
    private IntVar memConso;

    public Node(int id, Data data) {

        this.id = id;

        int nbComponent = data.getComponents().size();
        int[] enveloppe = new int[nbComponent];

        for (int i = 0; i < nbComponent; i++) {
            enveloppe[i] = i;
        }

        this.data = data;
        this.cpu = data.getNetworkCpus()[id];
        this.mem = data.getNetworkMem()[id];

        this.cpuConso = VariableFactory.bounded("CPU consommés sur le noeud "+id, 0, this.cpu, data.getSolver());
        this.memConso = VariableFactory.bounded("Mémoire consommée sur le noeud "+id, 0, this.mem, data.getSolver());
        this.compSurNoeud = VariableFactory.set("Composants Sur Le Noeud "+id, enveloppe, new int[]{}, data.getSolver());

        this.coherenceconstraints();
    }

    private void coherenceconstraints() {

        // contrainte qui fait le lien entre le liste des composants et le cpu consommé sur le noeud
        data.getSolver().post(SetConstraintsFactory.sum(this.getCompSurNoeud(), this.data.getComponentsRequiredCpu()
                , 0, this.getCpuConso(), false));

        // idem avec la mémoire
        data.getSolver().post(SetConstraintsFactory.sum(this.getCompSurNoeud(), this.data.getComponentsRequiredmem()
                , 0, this.getMemConso(), false));


    }
}
