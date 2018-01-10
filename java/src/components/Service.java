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
    private int[][] requiredLatencies;
    private int[][] requiredBandwidths;
    private IntVar[][][] paths;

    public Service(List<Component> fixedComponents, List<Component> unfixedComponents, int[][] requiredLatencies,
                   int[][] bandwidths, IntVar[][][] paths){

        this.fixedComponents = fixedComponents;
        this.unfixedComponents = unfixedComponents;
        this.requiredLatencies = requiredLatencies;
        this.requiredBandwidths = bandwidths;
        this.paths = paths;

        }
}
