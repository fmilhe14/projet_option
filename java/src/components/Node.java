package components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by francoismilhem on 09/01/2018.
 */
@Builder
@Getter
@Setter
public class Node {


    private int cpu;
    private int mem;
    private int netIn;
    private int netOut;

    public Node(int cpu, int mem, int netIn, int netOut){

        this.cpu = cpu;
        this.mem = mem;
        this.netIn = netIn;
        this.netOut = netOut;

    }

}
