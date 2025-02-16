package org.acme.opt;

import java.util.*;
import java.util.function.Function;

class ProjectGenerator {
    public enum RequirementProfile {
        BALANCED,          // Similar requirements across resources
        SPARSE,           // Few high requirements, many low/zero
        COMPLEMENTARY,    // Projects tend to need different resources
        COMPETITIVE,      // Projects compete for same resources
        SEASONAL          // Requirements follow a pattern across resources
    }

    private final int numProjects;
    private final List<Resource> resources;
    private final RequirementProfile profile;
    private final double utilizationTarget;  // Target resource utilization (0.0 to 1.0)
    private final Random random;

    private ProjectGenerator(Builder builder) {
        this.numProjects = builder.numProjects;
        this.resources = builder.resources;
        this.profile = builder.profile;
        this.utilizationTarget = builder.utilizationTarget;
        this.random = new Random(builder.seed);
    }

    public List<Project> generate() {
        List<Project> projects = new ArrayList<>();

        switch (profile) {
            case BALANCED -> generateBalancedProjects(projects);
            case SPARSE -> generateSparseProjects(projects);
            case COMPLEMENTARY -> generateComplementaryProjects(projects);
            case COMPETITIVE -> generateCompetitiveProjects(projects);
            case SEASONAL -> generateSeasonalProjects(projects);
        }

        return projects;
    }

    private void generateBalancedProjects(List<Project> projects) {
        for (int i = 0; i < numProjects; i++) {
            Map<String, Integer> requirements = new HashMap<>();
            for (Resource resource : resources) {
                int capacity = resource.getAvailableCapacity();
                int maxReq = (int) (capacity * utilizationTarget / numProjects);
                requirements.put(resource.getName(), maxReq + random.nextInt(maxReq/2));
            }
            projects.add(new Project("Project" + i, requirements));
        }
    }

    private void generateSparseProjects(List<Project> projects) {
        for (int i = 0; i < numProjects; i++) {
            Map<String, Integer> requirements = new HashMap<>();
            int numRequiredResources = Math.max(1, resources.size() / 5);  // Use 20% of resources

            List<Resource> shuffledResources = new ArrayList<>(resources);
            Collections.shuffle(shuffledResources, random);

            for (int j = 0; j < numRequiredResources; j++) {
                Resource resource = shuffledResources.get(j);
                int capacity = resource.getAvailableCapacity();
                int maxReq = (int) (capacity * utilizationTarget);
                requirements.put(resource.getName(), maxReq + random.nextInt(maxReq/2));
            }
            projects.add(new Project("Project" + i, requirements));
        }
    }

    private void generateComplementaryProjects(List<Project> projects) {
        // Divide resources into groups
        List<List<Resource>> resourceGroups = new ArrayList<>();
        List<Resource> remainingResources = new ArrayList<>(resources);

        while (!remainingResources.isEmpty()) {
            int groupSize = Math.min(remainingResources.size(),
                    Math.max(1, resources.size() / numProjects));
            List<Resource> group = remainingResources.subList(0, groupSize);
            resourceGroups.add(new ArrayList<>(group));
            remainingResources = remainingResources.subList(groupSize, remainingResources.size());
        }

        // Generate projects that primarily use one group
        for (int i = 0; i < numProjects; i++) {
            Map<String, Integer> requirements = new HashMap<>();
            List<Resource> primaryGroup = resourceGroups.get(i % resourceGroups.size());

            // High requirements for primary group
            for (Resource resource : primaryGroup) {
                int capacity = resource.getAvailableCapacity();
                int maxReq = (int) (capacity * utilizationTarget);
                requirements.put(resource.getName(), maxReq + random.nextInt(maxReq/2));
            }

            // Low requirements for other resources
            for (Resource resource : resources) {
                if (!primaryGroup.contains(resource)) {
                    int capacity = resource.getAvailableCapacity();
                    int maxReq = (int) (capacity * utilizationTarget * 0.2);  // 20% of normal
                    requirements.put(resource.getName(), random.nextInt(maxReq));
                }
            }

            projects.add(new Project("Project" + i, requirements));
        }
    }

    private void generateCompetitiveProjects(List<Project> projects) {
        // Select highly contested resources
        int numContested = Math.max(1, resources.size() / 3);  // 33% of resources are contested
        List<Resource> contestedResources = new ArrayList<>(resources.subList(0, numContested));

        for (int i = 0; i < numProjects; i++) {
            Map<String, Integer> requirements = new HashMap<>();

            // High requirements for contested resources
            for (Resource resource : contestedResources) {
                int capacity = resource.getAvailableCapacity();
                int maxReq = (int) (capacity * utilizationTarget);
                requirements.put(resource.getName(), maxReq + random.nextInt(maxReq/2));
            }

            // Normal requirements for other resources
            for (Resource resource : resources) {
                if (!contestedResources.contains(resource)) {
                    int capacity = resource.getAvailableCapacity();
                    int maxReq = (int) (capacity * utilizationTarget / numProjects);
                    requirements.put(resource.getName(), random.nextInt(maxReq));
                }
            }

            projects.add(new Project("Project" + i, requirements));
        }
    }

    private void generateSeasonalProjects(List<Project> projects) {
        // Create a seasonal pattern
        double[] seasonalPattern = new double[resources.size()];
        int seasonLength = Math.max(1, resources.size() / 4);  // Four seasons

        for (int i = 0; i < resources.size(); i++) {
            seasonalPattern[i] = 0.5 + 0.5 * Math.sin(2 * Math.PI * i / seasonLength);
        }

        for (int i = 0; i < numProjects; i++) {
            Map<String, Integer> requirements = new HashMap<>();

            for (int j = 0; j < resources.size(); j++) {
                Resource resource = resources.get(j);
                int capacity = resource.getAvailableCapacity();
                double seasonalFactor = seasonalPattern[(j + i) % resources.size()];
                int maxReq = (int) (capacity * utilizationTarget * seasonalFactor);
                requirements.put(resource.getName(), maxReq + random.nextInt(maxReq/2));
            }

            projects.add(new Project("Project" + i, requirements));
        }
    }

    public static class Builder {
        private int numProjects = 5;
        private List<Resource> resources = new ArrayList<>();
        private RequirementProfile profile = RequirementProfile.BALANCED;
        private double utilizationTarget = 0.7;
        private long seed = 42;

        public Builder numProjects(int val) {
            numProjects = val;
            return this;
        }

        public Builder resources(List<Resource> val) {
            resources = val;
            return this;
        }

        public Builder profile(RequirementProfile val) {
            profile = val;
            return this;
        }

        public Builder utilizationTarget(double val) {
            utilizationTarget = val;
            return this;
        }

        public Builder seed(long val) {
            seed = val;
            return this;
        }

        public ProjectGenerator build() {
            return new ProjectGenerator(this);
        }
    }
}
