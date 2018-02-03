package components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.variables.IntVar;

@EqualsAndHashCode
@Getter
@Setter
public class Component {

    private int id;
    private int cpu;
    private int mem;
    private IntVar position;

    public Component(int id, int cpu, int mem, IntVar position) {

        this.id = id;
        this.cpu = cpu;
        this.mem = mem;
        this.position = position;
    }

    @Override
    public String toString() {
        return "Component n°" + id + "(" + cpu + ", " + mem + ")";
    }
}
