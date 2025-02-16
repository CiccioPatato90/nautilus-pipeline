package org.acme.opt;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;
import java.util.*;

public class OrTools {
    public static void main(String[] args) {
        // LOAD NATIVE LIBRARIES
        Loader.loadNativeLibraries();

        // --- Generate random input data ---
        int numProjects = 1;  // Only one project to test feasibility.
        Random rand = new Random(42);
        int numResources = 3000;

        // Generate resource names: "Resource0", "Resource1", ..., etc.
        List<String> resourceNames = new ArrayList<>();
        for (int i = 0; i < numResources; i++) {
            resourceNames.add("Resource" + i);
        }

        // Create a list to store the project's resource requirements.
        List<Map<String, Integer>> projectRequirements = new ArrayList<>();
        // Initialize total requirements map.
        Map<String, Integer> totalRequirements = new HashMap<>();
        for (String resource : resourceNames) {
            totalRequirements.put(resource, 0);
        }

        // Generate random requirements (0 to 19) for the project and accumulate totals.
        for (int p = 0; p < numProjects; p++) {
            Map<String, Integer> req = new HashMap<>();
            for (String resource : resourceNames) {
                int requirement = rand.nextInt(20);
                req.put(resource, requirement);
                totalRequirements.put(resource, totalRequirements.get(resource) + requirement);
            }
            projectRequirements.add(req);
        }

        // Define available capacity for each resource: roughly 50% of total requirements.
        Map<String, Integer> available = new HashMap<>();
        for (String resource : resourceNames) {
            int capacity = totalRequirements.get(resource) / 2;
            if (capacity < 10){
                capacity = 15;
            }
            available.put(resource, capacity);
        }

        // Print generated data.
        System.out.println("Available Capacities:");
        for (String resource : resourceNames) {
            System.out.println(resource + ": " + available.get(resource));
        }
        System.out.println("\nProject Requirements:");
        for (int p = 0; p < numProjects; p++) {
            System.out.println("Project " + p + ": " + projectRequirements.get(p));
        }

        // Create the MPSolver model using CBC (we need MIP capabilities).
        MPSolver solver = MPSolver.createSolver("CBC_MIXED_INTEGER_PROGRAMMING");
        if (solver == null) {
            System.out.println("Could not create solver.");
            return;
        }

        // Start timing the model building and solving.
        long startTime = System.nanoTime();

        // Decision variable: x[p] is 1 if project p is completed.
        MPVariable[] x = new MPVariable[numProjects];
        for (int p = 0; p < numProjects; p++) {
            x[p] = solver.makeIntVar(0, 1, "x_" + p);
        }

        // Create a map to store slack variables for each resource.
        Map<String, MPVariable> slackVars = new HashMap<>();

        // For each resource, add a constraint:
        //   x[0] * requirement - slack <= available
        // which is equivalent to: x[0]*requirement <= available + slack.
        for (Map.Entry<String, Integer> resourceEntry : available.entrySet()) {
            String resource = resourceEntry.getKey();
            int capacity = resourceEntry.getValue();

            // Create a slack variable for this resource.
            MPVariable slack = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "s_" + resource);
            slackVars.put(resource, slack);

            // Get the requirement for the single project.
            int req = projectRequirements.get(0).getOrDefault(resource, 0);
            // Create constraint: x[0]*req - slack <= capacity.
            MPConstraint constraint = solver.makeConstraint(-Double.POSITIVE_INFINITY, capacity, resource + "_constraint");
            constraint.setCoefficient(x[0], req);
            constraint.setCoefficient(slack, -1);
        }

        // Define a penalty for using slack.
        double penalty = 0.1;

        // Objective: maximize x[0] - penalty * (sum of slack variables).
        MPObjective objective = solver.objective();
        objective.setCoefficient(x[0], 1);
        for (MPVariable slack : slackVars.values()) {
            objective.setCoefficient(slack, -penalty);
        }
        objective.setMaximization();

        // Solve the model.
        MPSolver.ResultStatus resultStatus = solver.solve();

        long endTime = System.nanoTime();
        double elapsedSeconds = (endTime - startTime) / 1e9;

        if (resultStatus != MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("No optimal solution found.");
        } else {
            System.out.println("Optimal solution:");
            double xVal = x[0].solutionValue();
            System.out.println("Project selection (x): " + xVal);
            double totalSlack = 0;
            for (Map.Entry<String, MPVariable> entry : slackVars.entrySet()) {
                double sVal = entry.getValue().solutionValue();
                totalSlack += sVal;
                System.out.println("Slack for " + entry.getKey() + ": " + sVal);
            }
            System.out.println("Objective value: " + objective.value());
            System.out.printf("Time taken: %.3f seconds\n", elapsedSeconds);

            // Post-solve: if x = 1 but some slack is used (below a threshold), we flag it as ALMOST COMPLETED.
            if (xVal == 1) {
                double slackThreshold = 0.01;  // Adjust threshold as needed.
                if (totalSlack <= slackThreshold) {
                    System.out.println("Project is ALMOST COMPLETED (total slack = " + totalSlack + ")");
                } else {
                    System.out.println("Project is selected but requires significant slack (total slack = " + totalSlack + ")");
                }
            } else {
                System.out.println("Project is not selected.");
            }
        }
    }
}
