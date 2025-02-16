package org.acme.opt;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
class FeasibilitySolver {
    private final List<Resource> resources;
    private final List<Project> projects;
    private static final double SLACK_PENALTY = 0.1;  // Reduced penalty to allow more slack
    private static final double SLACK_THRESHOLD = 0.001;

    private double calculateCompletionPercentage(Project project, Map<Resource, Double> slacks, Map<String, Integer> resourceAllocation) {
        double totalRequirements = 0;
        double totalFulfilled = 0;

        Map<String, Integer> requirements = project.getRequirements();

        for (Resource resource : resources) {
            int requirement = requirements.getOrDefault(resource.getName(), 0);
            if (requirement > 0) {
                totalRequirements += requirement;
                // Only count fulfilled if resource is allocated to this project
                if (resourceAllocation.getOrDefault(resource.getName(), -1) == projects.indexOf(project)) {
                    double slack = slacks.getOrDefault(resource, 0.0);
                    double fulfilled = Math.min(requirement, resource.getAvailableCapacity());
                    totalFulfilled += fulfilled;
                }
            }
        }

        return totalRequirements > 0 ? (totalFulfilled / totalRequirements) * 100 : 100.0;
    }

    public FeasibilityResult solve() {
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("GLOP");

        if (solver == null) {
            throw new RuntimeException("Could not create solver");
        }

        // Create decision variables for projects - now forced to 1
        Map<Project, MPVariable> projectVars = new HashMap<>();
        for (Project project : projects) {
            MPVariable var = solver.makeIntVar(1, 1, "project_" + project.getName());  // Force selection
            projectVars.put(project, var);
        }

        Map<Resource, Map<Project, MPVariable>> resourceAllocationVars = new HashMap<>();
        for (Resource resource : resources) {
            Map<Project, MPVariable> projectAllocation = new HashMap<>();
            for (Project project : projects) {
                projectAllocation.put(project,
                        solver.makeIntVar(0, 1,
                                "alloc_" + resource.getName() + "_" + project.getName()));
            }
            resourceAllocationVars.put(resource, projectAllocation);
        }

        // Create slack variables for resources
        Map<Resource, MPVariable> slackVars = new HashMap<>();
        for (Resource resource : resources) {
            slackVars.put(resource,
                    solver.makeNumVar(0, Double.POSITIVE_INFINITY, "slack_" + resource.getName()));
        }


        // Constraint 1: Each resource can only be allocated to one project
        for (Resource resource : resources) {
            MPConstraint singleAllocation = solver.makeConstraint(0, 1,
                    "single_alloc_" + resource.getName());
            Map<Project, MPVariable> allocVars = resourceAllocationVars.get(resource);
            for (MPVariable allocVar : allocVars.values()) {
                singleAllocation.setCoefficient(allocVar, 1);
            }
        }

        // Constraint 2: Resource capacity and requirements
        for (Resource resource : resources) {
            MPConstraint capacityConstraint = solver.makeConstraint(
                    -Double.POSITIVE_INFINITY,
                    resource.getAvailableCapacity(),
                    "capacity_" + resource.getName());

            Map<Project, MPVariable> allocVars = resourceAllocationVars.get(resource);
            for (Project project : projects) {
                int requirement = project.getRequirements().getOrDefault(resource.getName(), 0);
                capacityConstraint.setCoefficient(allocVars.get(project), requirement);
            }

            capacityConstraint.setCoefficient(slackVars.get(resource), -1);
        }


        // Constraint 3: Resource allocation only if project is selected
        for (Resource resource : resources) {
            for (Project project : projects) {
                MPConstraint allocationConstraint = solver.makeConstraint(
                        0, 0,
                        "alloc_requires_project_" + resource.getName() + "_" + project.getName());
                allocationConstraint.setCoefficient(resourceAllocationVars.get(resource).get(project), 1);
                allocationConstraint.setCoefficient(projectVars.get(project), -1);
            }
        }


        // Objective: maximize project selection and resource utilization
        MPObjective objective = solver.objective();
        for (Project project : projects) {
            objective.setCoefficient(projectVars.get(project), 1.0);
        }
        for (MPVariable slack : slackVars.values()) {
            objective.setCoefficient(slack, -SLACK_PENALTY);
        }
        objective.setMaximization();

        // Add resource constraints with slack
//        for (Resource resource : resources) {
//            MPConstraint constraint = solver.makeConstraint(
//                    -Double.POSITIVE_INFINITY,
//                    resource.getAvailableCapacity(),
//                    "constraint_" + resource.getName()
//            );
//            // Add project requirements
//            for (Project project : projects) {
//                int requirement = project.getRequirements().getOrDefault(resource.getName(), 0);
//                constraint.setCoefficient(projectVars.get(project), requirement);
//            }
//
//            // Subtract slack
//            constraint.setCoefficient(slackVars.get(resource), -1);
//        }

        // Set objective: minimize slack
//        MPObjective objective = solver.objective();
//        for (Map.Entry<Resource, MPVariable> entry : slackVars.entrySet()) {
//            objective.setCoefficient(entry.getValue(), SLACK_PENALTY);
//        }
//        objective.setMinimization();

        // Solve
        MPSolver.ResultStatus status = solver.solve();

//        if (status != MPSolver.ResultStatus.OPTIMAL) {
//            return new FeasibilityResult(false, Collections.emptyMap(),
//                    Collections.emptyMap(), Collections.emptyMap());
//        }

        // Process results
        Map<Project, Boolean> projectResults = new HashMap<>();
        Map<Resource, Double> slackResults = new HashMap<>();
        Map<Project, Double> completionPercentages = new HashMap<>();
        Map<String, Integer> resourceAllocation = new HashMap<>();

        // Calculate resource allocations
        for (Resource resource : resources) {
            Map<Project, MPVariable> allocVars = resourceAllocationVars.get(resource);
            for (Map.Entry<Project, MPVariable> entry : allocVars.entrySet()) {
                if (entry.getValue().solutionValue() > 0.5) {
                    resourceAllocation.put(resource.getName(), projects.indexOf(entry.getKey()));
                    break;
                }
            }
        }

        // Calculate slack results
        for (Map.Entry<Resource, MPVariable> entry : slackVars.entrySet()) {
            slackResults.put(entry.getKey(), entry.getValue().solutionValue());
        }

        // Calculate project selections and completion percentages
        for (Project project : projects) {
            boolean isSelected = projectVars.get(project).solutionValue() > 0.5;
            projectResults.put(project, isSelected);

            double completionPercentage = calculateCompletionPercentage(
                    project, slackResults, resourceAllocation);
            completionPercentages.put(project, completionPercentage);
        }

        return new FeasibilityResult(true, projectResults, slackResults, completionPercentages);
    }
}
