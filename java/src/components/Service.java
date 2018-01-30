package components;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Service {

    private List<Component> components;
    private Path[] path;
    private Map<PairOfComponents, Integer> requiredLatencies;
    private Map<PairOfComponents, Integer> requiredBandwidths;

    public Service(List<Component> components, Map<PairOfComponents, Integer> requiredLatencies,
                   Map<PairOfComponents, Integer> bandwidths) {

        this.components = components;
        this.requiredLatencies = requiredLatencies;
        this.requiredBandwidths = bandwidths;

    }
}
