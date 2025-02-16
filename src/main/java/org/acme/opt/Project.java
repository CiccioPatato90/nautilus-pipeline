package org.acme.opt;

import io.quarkus.arc.All;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
@AllArgsConstructor
@Getter
public class Project {
    private final String name;
    private final Map<String, Integer> resourceRequirements;

    public Map<String, Integer> getRequirements() { return Collections.unmodifiableMap(resourceRequirements); }
}
