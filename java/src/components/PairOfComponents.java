package components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class PairOfComponents {

    private Component component1;
    private Component component2;

    public PairOfComponents(Component component1, Component component2){

        this.component1 = component1;
        this.component2 = component2;
    }

}
