package org.acme.opt;

import java.util.*;
import java.util.stream.Collectors;

public class MaximizeResourceUsageEntrypoint {
    public static void main(String[] args) {
        ResourceGenerator resourceGen = new ResourceGenerator.Builder()
                .numResources(1000)
                .minCapacity(70)
                .distribution(ResourceGenerator.CapacityDistribution.NORMAL)
                .build();

        ProjectGenerator projectGen = new ProjectGenerator.Builder()
                .numProjects(800)
                .resources(resourceGen.generate())
                .profile(ProjectGenerator.RequirementProfile.BALANCED)
                .utilizationTarget(100)
                .build();

        List<Resource> resources = resourceGen.generate();
        List<Project> projects = projectGen.generate();

        // Solve feasibility
        var solver = new MaximizeResourceUsage(resources, projects);
        var result = solver.solve();

        // Added Code: Unified Statistics Summary
        System.out.println("\n=== FEASIBILITY STATS SUMMARY ===\n");

        // Per-Project Stats
        System.out.println("Per-Project Stats:");
        for (Map.Entry<Project, List<Resource>> entry : result.entrySet()) {
            Project project = entry.getKey();
            List<Resource> assigned = entry.getValue();
            double completion = solver.calculateProjectCompletion(project, assigned);

            Map<String, Integer> assignmentCount = assigned.stream()
                    .collect(Collectors.groupingBy(
                            Resource::getName,
                            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                    ));
            // Calculate missing resources to complete the project's requirements.
            Map<String, Integer> missingResources = new HashMap<>();
            Map<String, Integer> requirements = project.getRequirements();
            for (Map.Entry<String, Integer> reqEntry : requirements.entrySet()) {
                String resourceName = reqEntry.getKey();
                int required = reqEntry.getValue();
                int assignedCount = assignmentCount.getOrDefault(resourceName, 0);
                if (assignedCount < required) {
                    missingResources.put(resourceName, required - assignedCount);
                }
            }

            System.out.printf("Project %s: Completion = %.2f%%, Resources Assigned = %d%n",
                    project.getName(), completion, assigned.size());
            System.out.println("   Assigned Resources:");
            assignmentCount.forEach((res, count) ->
                    System.out.printf("      %s: %d%n", res, count));

            System.out.println("   Required Resources:");
            requirements.forEach((res, count) ->
                    System.out.printf("      %s: %d%n", res, count));

            if (!missingResources.isEmpty()) {
                System.out.println("   Missing Resources:");
                missingResources.forEach((res, count) ->
                        System.out.printf("      %s: %d%n", res, count));
            } else {
                System.out.println("   All resource requirements met.");
            }
        }

        // Global Stats
        int totalAvailable = resources.stream().mapToInt(Resource::getAvailableCapacity).sum();
        int totalUsed = result.values().stream().mapToInt(List::size).sum();
        double avgUsed = (double) totalUsed / projects.size();
        System.out.println("\nGlobal Stats:");
        System.out.printf("Total Resources Available: %d%n", totalAvailable);
        System.out.printf("Total Resources Used: %d%n", totalUsed);
        System.out.printf("Average Resources per Project: %.2f%n", avgUsed);
        System.out.printf("Unused Resources: %d%n", totalAvailable - totalUsed);

        // Global Resource Assignment Breakdown
        Map<String, Integer> globalAssignment = result.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        Resource::getName,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        System.out.println("\nGlobal Resource Assignment Breakdown:");
        globalAssignment.forEach((res, count) ->
                System.out.printf("Resource %s: Assigned %d times%n", res, count));

        // Additional Interesting Stats: Most/Least Assigned Resources
        Optional<Map.Entry<String, Integer>> mostAssigned = globalAssignment.entrySet().stream()
                .max(Map.Entry.comparingByValue());
        Optional<Map.Entry<String, Integer>> leastAssigned = globalAssignment.entrySet().stream()
                .min(Map.Entry.comparingByValue());

        mostAssigned.ifPresent(entry ->
                System.out.printf("\nMost Assigned Resource: %s (%d times)%n", entry.getKey(), entry.getValue()));
        leastAssigned.ifPresent(entry ->
                System.out.printf("Least Assigned Resource: %s (%d times)%n", entry.getKey(), entry.getValue()));

        // Per Resource Stats
        System.out.println("\nPer Resource Stats:");
        for (Resource resource : resources) {
            System.out.printf("Resource %s: Capacity = %d, Cost = %d, Assigned Count = %d%n",
                    resource.getName(),
                    resource.getAvailableCapacity(),
                    resource.getCost(),
                    globalAssignment.getOrDefault(resource.getName(), 0));
        }

    }
}
