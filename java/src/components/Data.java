package components;

import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class Data {

    //Service and components to deploy
    private List<Service> services;
    private List<Component> components;

    //Network features
    private int[][] networkLatencies;
    private int[][] networkBandwidths;
    private int[] networkCpus;
    private int[] networkMem;

    //Requirements
    private int[] componentsRequiredCpu;
    private int[] componentsRequiredmem;
    private int[] coupleComponentsRequiredBandwidth;
    private int[] coupleComponentsRequiredLatency;

    //Solver
    private Solver solver;


    public Data(List<Service> services, int[][] networkLatencies, int[][] networkBandwidths, int[] networkCpus, int[] networkMem, Solver solver) {

        this.services = services;
        this.components = composantFactory(services);

        this.networkLatencies = networkLatencies;
        this.networkBandwidths = networkBandwidths;
        this.networkCpus = networkCpus;
        this.networkMem = networkMem;

        this.componentsRequiredCpu = componentsRequiredCpuFactory(this.components);
        this.componentsRequiredmem = componentsRequiredMemFactory(this.components);
        this.coupleComponentsRequirementsFactory();

        this.solver = solver;

    }

    private List<Component> composantFactory(List<Service> services) {

        this.components = new ArrayList<>();

        for (Service s : services) {

            components.addAll(s.getComponents());
        }

        components.sort(Comparator.comparing(Component::getId)); //Pour avoir la liste triée en fonction des indices des composants

        return components;
    }

    private int[] componentsRequiredCpuFactory(List<Component> componentList) {

        int n = componentList.size();
        int[] componentsRequiredCpu = new int[n];

        for (int i = 0; i < n; i++) {

            componentsRequiredCpu[i] = componentList.get(i).getCpu();
        }
        return componentsRequiredCpu;
    }

    private int[] componentsRequiredMemFactory(List<Component> componentList) {

        int n = componentList.size();
        int[] componentsRequiredMem = new int[n];

        for (int i = 0; i < n; i++) {

            componentsRequiredMem[i] = componentList.get(i).getMem();
        }
        return componentsRequiredMem;
    }

    private void coupleComponentsRequirementsFactory() {

        ArrayList<Integer> coupleComponentsRequiredBandwidth = new ArrayList<>();
        ArrayList<Integer> coupleComponentsRequiredLatency = new ArrayList<>();

        int bandwidths;
        int latency;

        for (Service s : this.getServices()) {

            int nbComponent = s.getComponents().size();

            for (int i = 0; i < nbComponent; i++) {

                Component c = s.getComponents().get(i);

                for (int j = i + 1; j < nbComponent; j++) {

                    Component c1 = s.getComponents().get(j);

                    PairOfComponents pairOfComponents = new PairOfComponents(c, c1);

                    bandwidths = s.getRequiredBandwidths().get(pairOfComponents);
                    latency = s.getRequiredLatencies().get(pairOfComponents);

                    if (latency > 0) {

                        coupleComponentsRequiredBandwidth.add(bandwidths);
                        coupleComponentsRequiredLatency.add(latency);
                    }
                }
            }
        }

        int nbCouple = coupleComponentsRequiredBandwidth.size();
        this.coupleComponentsRequiredBandwidth = new int[nbCouple];
        this.coupleComponentsRequiredLatency = new int[nbCouple];

        for (int i = 0; i < nbCouple; i++) {

            this.coupleComponentsRequiredBandwidth[i] = coupleComponentsRequiredBandwidth.get(i);
            this.coupleComponentsRequiredLatency[i] = coupleComponentsRequiredLatency.get(i);

        }

    }

    public List<Node> buildNodes() {

        ArrayList<Node> nodes = new ArrayList<>();

        for (int i = 0; i < networkCpus.length; i++) {

            nodes.add(new Node(i, this));
        }

        return nodes;
    }

    public List<Edge> buildEdges(List<Node> nodes) {

        nodes.sort(Comparator.comparing(Node::getId)); //Pour avoir la liste triée en fonction des indices des composants

        ArrayList<Edge> edges = new ArrayList<>();

        int edgeId = 1;

        for (int i = 0; i < networkLatencies.length; i++) {
            for (int j = i ; j < networkLatencies.length; j++) {

                if(i == j){

                    edges.add(new Edge(edgeId, nodes.get(i), nodes.get(j), this));
                    edgeId ++;
                }

                if (networkLatencies[i][j] != 0) {

                    edges.add(new Edge(edgeId, nodes.get(i), nodes.get(j), this));
                    edgeId ++ ;
                }
            }
        }

        return edges;
    }

}
