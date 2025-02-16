package org.acme.opt;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

@AllArgsConstructor
public class MaximizeResourceUsage {
    private final List<Resource> resources;
    private final List<Project> projects;
    public Map<Project, List<Resource>> solve() {
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("GLOP");

        // Create decision variables x[i][j] representing the quantity of resource i assigned to project j
        Map<Resource, Map<Project, MPVariable>> x = new HashMap<>();
        for (Resource resource : resources) {
            Map<Project, MPVariable> projectVars = new HashMap<>();
            for (Project project : projects) {
                String varName = String.format("x_%s_%s", resource.getName(), project.getName());
                // Upper bound is the minimum between resource capacity and project requirement
                int upperBound = Math.min(
                        resource.getAvailableCapacity(),
                        project.getRequirements().getOrDefault(resource.getName(), 0)
                );
                projectVars.put(project, solver.makeIntVar(0, upperBound, varName));
            }
            x.put(resource, projectVars);
        }

        // Objective: Minimize cost while maximizing resource utilization
        MPObjective objective = solver.objective();

        // Cost minimization component
        for (Resource resource : resources) {
            for (Project project : projects) {
                // Assuming Resource class has a getCost() method that returns cost per unit
                objective.setCoefficient(x.get(resource).get(project), resource.getCost());
            }
        }

        // Resource utilization component (negative coefficient to maximize)
        for (Resource resource : resources) {
            for (Project project : projects) {
                // Add a small negative weight to encourage resource utilization
                objective.setCoefficient(x.get(resource).get(project), -0.1);
            }
        }

        objective.setMinimization();

        // Constraint 1: Don't exceed resource capacity
        for (Resource resource : resources) {
            MPConstraint capacityConstraint = solver.makeConstraint(
                    0,
                    resource.getAvailableCapacity(),
                    "capacity_" + resource.getName()
            );
            for (Project project : projects) {
                capacityConstraint.setCoefficient(x.get(resource).get(project), 1);
            }
        }

        // Constraint 2: Don't exceed project requirements
        for (Project project : projects) {
            for (Resource resource : resources) {
                int requirement = project.getRequirements().getOrDefault(resource.getName(), 0);
                if (requirement > 0) {
                    MPConstraint requirementConstraint = solver.makeConstraint(
                            0,
                            requirement,
                            String.format("requirement_%s_%s", project.getName(), resource.getName())
                    );
                    requirementConstraint.setCoefficient(x.get(resource).get(project), 1);
                }
            }
        }

        // Solve the problem
        MPSolver.ResultStatus status = solver.solve();

        // Process results
        Map<Project, List<Resource>> assignments = new HashMap<>();
        if (status == MPSolver.ResultStatus.OPTIMAL || status == MPSolver.ResultStatus.FEASIBLE) {
            for (Project project : projects) {
                List<Resource> assignedResources = new ArrayList<>();
                for (Resource resource : resources) {
                    double quantity = x.get(resource).get(project).solutionValue();
                    // If any quantity of this resource is assigned to this project
                    if (quantity > 0) {
                        // Create new resource instances with the assigned quantity
                        for (int i = 0; i < (int)quantity; i++) {
                            assignedResources.add(new Resource(
                                    resource.getName(),
                                    1,  // One unit per instance
                                    resource.getCost()
                            ));
                        }
                    }
                }
                if (!assignedResources.isEmpty()) {
                    assignments.put(project, assignedResources);
                }
            }
        }

        return assignments;
    }

    // Helper method to calculate project completion percentage
    public double calculateProjectCompletion(Project project, List<Resource> assignedResources) {
        Map<String, Integer> requirements = project.getRequirements();
        Map<String, Integer> fulfilled = new HashMap<>();

        // Count assigned resources
        for (Resource resource : assignedResources) {
            fulfilled.merge(resource.getName(), 1, Integer::sum);
        }

        // Calculate completion percentage
        double totalRequirements = 0;
        double totalFulfilled = 0;

        for (Map.Entry<String, Integer> requirement : requirements.entrySet()) {
            String resourceName = requirement.getKey();
            int required = requirement.getValue();
            int fulfilled_amount = fulfilled.getOrDefault(resourceName, 0);

            totalRequirements += required;
            totalFulfilled += Math.min(fulfilled_amount, required);
        }

        return totalRequirements > 0 ? (totalFulfilled / totalRequirements) * 100 : 100.0;
    }
}
