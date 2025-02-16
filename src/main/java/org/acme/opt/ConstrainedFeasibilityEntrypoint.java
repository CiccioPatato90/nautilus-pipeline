package org.acme.opt;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConstrainedFeasibilityEntrypoint {
    public static void main(String[] args) {
        // Create a smaller test case for clearer demonstration
//        List<Resource> resources = generateConstrainedResources(5);
//        List<Project> projects = generateDemandingProject(resources);
        ResourceGenerator resourceGen = new ResourceGenerator.Builder()
                .numResources(5)
                .minCapacity(10)
                .distribution(ResourceGenerator.CapacityDistribution.UNIFORM)
                .build();

        ProjectGenerator projectGen = new ProjectGenerator.Builder()
                .numProjects(4)
                .resources(resourceGen.generate())
                .profile(ProjectGenerator.RequirementProfile.COMPETITIVE)
                .utilizationTarget(0.9)
                .build();

        List<Resource> resources = resourceGen.generate();
        List<Project> projects = projectGen.generate();
//        List<Resource> resources = generateConstrainedResources(5);
//        List<Project> projects = generateDemandingProject(resources);
        // Print input data
        System.out.println("Input Data:");
        for (Project proj : projects) {
            System.out.printf("Project %s:%n",
                    proj.getName());
            for (Resource resource : resources) {
                System.out.printf(" --> Resource %s: Required = %d%n, ",
                        resource.getName(),
                        proj.getRequirements().getOrDefault(resource.getName(), 0));
            }
        }

        // Solve feasibility
        FeasibilitySolver solver = new FeasibilitySolver(resources, projects);
        FeasibilityResult result = solver.solve();


        // Print results
        System.out.println("\nFeasibility Analysis Results:");
        for (Map.Entry<Project, Boolean> entry : result.getProjectSelections().entrySet()) {
            Project p = entry.getKey();
            double completionPercentage = result.getCompletionPercentages().get(p);

            System.out.printf("Project %s: Selected (Completion: %.2f%%)%n",
                    p.getName(),
                    completionPercentage);
        }

        System.out.println("\nResource Details:");
        for (Project proj : projects) {
            System.out.printf("Project %s:%n",
                    proj.getName());
            for (Resource resource : resources) {
                int required = proj.getRequirements().getOrDefault(resource.getName(), 0);
                double slack = result.getResourceSlacks().getOrDefault(resource, 0.0);
                double fulfilled = Math.min(required, resource.getAvailableCapacity());
                double fulfillmentPercentage = (fulfilled / required) * 100;

                System.out.printf("%s: Required=%d, Capacity=%d, Fulfilled=%.1f (%.1f%%)%n",
                        resource.getName(),
                        required,
                        resource.getAvailableCapacity(),
                        fulfilled,
                        fulfillmentPercentage);
            }
        }
    }

    private static List<Resource> generateConstrainedResources(int count) {
        List<Resource> resources = new ArrayList<>();

        // Create resources with limited capacity
        for (int i = 0; i < count; i++) {
            // Deliberately set low capacity
            int capacity = 10;  // Fixed capacity for all resources
            resources.add(new Resource("Resource" + i, capacity, capacity % i));
        }

        return resources;
    }

    private static List<Project> generateDemandingProject(List<Resource> resources) {
        List<Project> projects = new ArrayList<>();
        Map<String, Integer> requirements = new HashMap<>();

        // Create varying requirements, some exceeding capacity
        for (Resource resource : resources) {
            // Requirements will vary from 8 to 15 units
            int requirement = 8 + (resources.indexOf(resource) * 2);  // Gradually increasing requirements
            requirements.put(resource.getName(), requirement);
        }

        projects.add(new Project("Project0", requirements));
        projects.add(new Project("Project1", requirements));
        projects.add(new Project("Project2", requirements));
        return projects;
    }
}
