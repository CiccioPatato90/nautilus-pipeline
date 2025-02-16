package org.acme.opt;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
@AllArgsConstructor
public class FeasibilityResult {
    private final boolean isFeasible;
    private final Map<Project, Boolean> projectSelections;
    private final Map<Resource, Double> resourceSlacks;
    private final Map<Project, Double> completionPercentages;

    public Map<Project, Boolean> getProjectSelections() { return Collections.unmodifiableMap(projectSelections); }
    public Map<Resource, Double> getResourceSlacks() { return Collections.unmodifiableMap(resourceSlacks); }
    public Map<Project, Double> getCompletionPercentages() { return Collections.unmodifiableMap(completionPercentages); }
}

