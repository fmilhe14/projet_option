package components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class PairOfIndex {


    private int i1;
    private int i2;


    public PairOfIndex(int i1, int i2) {

        this.i1 = i1;
        this.i2 = i2;
    }

    public boolean contains(int i) {

        return i1 == i || i2 == i;
    }
}
