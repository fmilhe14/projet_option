import components.*;
import org.chocosolver.solver.Solver;

import java.util.List;

public class Main {


    public static void main(String[] args){


        double[][] latencies = Parser.parseLatencies();
        double[][] bandwidths = Parser.parseBandwidths();

        List<Service> serviceList = Parser.parseServices();
        Graph graph = Parser.parseGraph();


    }
}
