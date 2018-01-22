package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class Data {


    private List<Service> services;
    private int[][] latencies;
    private int[][] bandwidths;
    private List<Component> components ;
    private int[] componentsRequiredCpu ;
    private int[] componentsRequiredmem ;
    private Component[][] coupleComponentes ; // tableau des couples de composants en relation
    private int[] coupleComponentesRequiredBandwidth ; // bande passante consommée par couple


    public Data(List<Service> services, int[][] latencies, int[][] bandwidths) {


        this.services = services;
        this.latencies = latencies;
        this.bandwidths = bandwidths;
        this.components = composantFactory(services);
        this.componentsRequiredCpu = componentsRequiredCpuFactory(this.components);
        this.componentsRequiredmem = componentsRequiredMemFactory(this.components);
        this.coupleComponentesFactory();

    }

    private static List<Component> composantFactory(List<Service> services){
        List<Component> lcomponent = new ArrayList<Component>();
        for(Service s :services){
            for(Component c : s.getComponents()){
                lcomponent.add(c);
            }
        }
        return lcomponent ;
    }

    private static int[] componentsRequiredCpuFactory(List<Component> componentList){
        int n = componentList.size();
        int[] componentsRequiredCpu = new int[n];
        for (int i = 0; i < n; i++) {
            componentsRequiredCpu[i]=componentList.get(i).getCpu();
        }
        return componentsRequiredCpu;
    }

    private static int[] componentsRequiredMemFactory(List<Component> componentList){
        int n = componentList.size();
        int[] componentsRequiredMem = new int[n];
        for (int i = 0; i < n; i++) {
            componentsRequiredMem[i]=componentList.get(i).getMem();
        }
        return componentsRequiredMem;
    }

    private void coupleComponentesFactory(){
        ArrayList<Component[]> lcoupleComponentes = new ArrayList<Component[]>();
        ArrayList<Integer> lcoupleComponentesRequiredBandwidth = new ArrayList<Integer>();
        int bandwidths;
        for(Service s :this.getServices()){
            int nbComponent = s.getComponents().size();
            for (int i = 0; i < nbComponent; i++) {
                for (int j = i+1; j < nbComponent; j++) {
                    bandwidths = s.getRequiredBandwidths()[i][j];
                    if(bandwidths!=0){
                       lcoupleComponentes.add(new Component[]{s.getComponents().get(i),s.getComponents().get(j)});
                        lcoupleComponentesRequiredBandwidth.add(bandwidths);                    }
                }
            }
        }
        int nbCouple = lcoupleComponentesRequiredBandwidth.size();
        this.coupleComponentesRequiredBandwidth = new int[nbCouple];
        this.coupleComponentes = new Component[nbCouple][2];
        for (int i = 0; i < nbCouple; i++) {
            this.coupleComponentesRequiredBandwidth[i]=lcoupleComponentesRequiredBandwidth.get(i);
            this.coupleComponentes[i][0] = lcoupleComponentes.get(i)[0];
            this.coupleComponentes[i][1] = lcoupleComponentes.get(i)[1];
        }

    }
}
