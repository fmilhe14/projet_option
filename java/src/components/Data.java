package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Builder
@Getter
@Setter
public class Data {

    //Service and components to deploy
    private List<Service> services;
    private List<Component> components;

    //Network features
    private int[][] networkLatencies;
    private int[][] networkBandwidths;

    //Requirements
    private int[] componentsRequiredCpu;
    private int[] componentsRequiredmem;
    private int[] coupleComponentsRequiredBandwidth;
    private int[] coupleComponentsRequiredLatency;


    public Data(List<Service> services, int[][] networkLatencies, int[][] networkBandwidths) {


        this.services = services;
        this.components = composantFactory(services);

        this.networkLatencies = networkLatencies;
        this.networkBandwidths = networkBandwidths;

        this.componentsRequiredCpu = componentsRequiredCpuFactory(this.components);
        this.componentsRequiredmem = componentsRequiredMemFactory(this.components);
        this.coupleComponentesFactory();

    }

    //Methode pour récupérer tous les
    private List<Component> composantFactory(List<Service> services) {

        this.components = new ArrayList<>();

        for (Service s : services) {

            for (Component c : s.getComponents()) {

                components.add(c);
            }
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

    private void coupleComponentesFactory() {

        ArrayList<Integer> coupleComponentsRequiredBandwidth = new ArrayList<>();

        int bandwidths = 0;

        for (Service s : this.getServices()) {

            int nbComponent = s.getComponents().size();

            for (int i = 0; i < nbComponent; i++) {

                Component c = s.getComponents().get(i);

                for (int j = i + 1; j < nbComponent; j++) {

                    Component c1 = s.getComponents().get(j);

                    bandwidths = s.getRequiredBandwidths().get(new Component[]{c, c1});

                    if (bandwidths != 0) {

                        coupleComponentsRequiredBandwidth.add(bandwidths);
                    }
                }
            }
        }

        int nbCouple = coupleComponentsRequiredBandwidth.size();
        this.coupleComponentsRequiredBandwidth = new int[nbCouple];

        for (int i = 0; i < nbCouple; i++) {

            this.coupleComponentsRequiredBandwidth[i] = coupleComponentsRequiredBandwidth.get(i);

        }

    }
}
