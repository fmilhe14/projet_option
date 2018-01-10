package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.ReificationConstraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.List;

@Builder
@Getter
@Setter
public class Service {

    private List<Component> fixedComponents;
    private List<Component> unfixedComponents;
    private int[][] requiredCpus;
    private int[][] requiredMemory;
    private int[][] requiredLatencies;
    private int[][] requiredBandwidths;
    private IntVar[][][] paths;
    private BoolVar[][][] pathReification;

    public Service(List<Component> fixedComponents, List<Component> unfixedComponents, int[][] requiredLatencies,
                   int[][] requiredCpus, int[][] requiredMemory,
                   int[][] bandwidths, int N, Solver solver) {

        this.fixedComponents = fixedComponents;
        this.unfixedComponents = unfixedComponents;
        this.requiredLatencies = requiredLatencies;
        this.requiredBandwidths = bandwidths;
        this.requiredCpus = requiredCpus;
        this.requiredMemory = requiredMemory ;

        this.paths = new IntVar[fixedComponents.size()][unfixedComponents.size()][N+1];

        for(int i = 0; i < fixedComponents.size(); i++){

            for(int j = 0; j < unfixedComponents.size(); j++){

                for(int k = 0; k < N + 1; k++){

                    this.paths[i][j][k] = VariableFactory.bounded("", 0, N, solver);

                    for(int l = 0; l < N; l++) {

                        if(this.paths[i][j][k].getLB() == l && k != l) {

                            this.pathReification[i][j][k] = IntConstraintFactory.arithm(this.paths[i][j][k],
                                    "=", l).reif();
                        }
                    }
                }
            }
        }
    }
}
