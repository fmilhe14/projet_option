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
    private Map<Component[], Integer> requiredLatencies;
    private Map<Component[], Integer> requiredBandwidths;

    public Service(List<Component> Components, Map<Component[], Integer> requiredLatencies,
                   Map<Component[], Integer> bandwidths) {

        this.components = Components;
        this.requiredLatencies = requiredLatencies;
        this.requiredBandwidths = bandwidths;

    }

}
