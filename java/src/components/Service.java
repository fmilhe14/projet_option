package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;

@Builder
@Getter
@Setter
public class Service {

    private List<Component> fixedComponents;
    private List<Component> unfixedComponents;
    private double[][] latencies;
    private double[][] bandwidths;
    private IntVar[][][] paths;

    public Service(List<Component> fixedComponents, List<Component> unfixedComponents, double[][] latencies,
                   double[][] bandwidths, IntVar[][][] paths){

        this.fixedComponents = fixedComponents;
        this.unfixedComponents = unfixedComponents;
        this.latencies = latencies;
        this.bandwidths = bandwidths;
        this.paths = paths;

        }
}
