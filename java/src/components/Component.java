package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.variables.IntVar;

@Builder
@Getter
@Setter
public class Component {

    private int id;
    private double cpu;
    private double mem;
    private IntVar position;

    public Component(int id, double cpu, double mem, IntVar position){

        this.id = id;
        this.cpu = cpu;
        this.mem = mem;
        this.position = position;
    }
}
