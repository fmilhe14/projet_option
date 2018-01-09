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


    private double cpu;
    private double mem;
    private double netIn;
    private double netOut;

    public Node(double cpu, double mem, double netIn, double netOut){

        this.cpu = cpu;
        this.mem = mem;
        this.netIn = netIn;
        this.netOut = netOut;

    }

}
