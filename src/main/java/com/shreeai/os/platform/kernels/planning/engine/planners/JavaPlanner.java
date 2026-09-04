package com.shreeai.os.platform.kernels.planning.engine.planners;

import com.shreeai.os.platform.kernels.planning.engine.MilestoneGenerator;
import com.shreeai.os.platform.kernels.planning.engine.TaskGraphBuilder;
import com.shreeai.os.platform.kernels.planning.model.Milestone;
import com.shreeai.os.platform.kernels.planning.model.Phase;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.Domain;

import java.util.List;
import java.util.Map;

/**
 * Domain planner for JAVA. Generates a structured Java developer roadmap
 * with phases for fundamentals, OOP, collections, Spring Boot, projects,
 * and portfolio.
 */
public final class JavaPlanner implements DomainPlanner {

    @Override
    public Domain domain() { return Domain.JAVA; }

    @Override
    public PlanBlueprint buildPlan(PlanningAnalysisResult analysis) {
        String goal = deriveGoal(analysis);

        List<Phase> phases = TaskGraphBuilder.buildChain(
                new String[]{
                        "Java Fundamentals",
                        "Object-Oriented Programming",
                        "Collections & Streams",
                        "Exception Handling & I/O",
                        "Concurrency Basics",
                        "Build Tools & Testing",
                        "Spring Boot Foundations",
                        "Database Integration",
                        "REST API Development",
                        "Project Portfolio"
                },
                new String[]{
                        "Master the Java language syntax, types, and control flow",
                        "Apply OOP principles: inheritance, polymorphism, encapsulation, abstraction",
                        "Use Collections framework and Stream API for data processing",
                        "Handle errors, files, and I/O streams idiomatically",
                        "Understand threads, executors, and synchronization",
                        "Use Maven/Gradle, JUnit, and mocking frameworks",
                        "Build applications with Spring Boot and dependency injection",
                        "Integrate relational databases with JPA/Hibernate",
                        "Develop production-grade REST APIs with validation and security",
                        "Build, document, and ship a portfolio of 3+ Java projects"
                },
                new int[]{2, 2, 2, 1, 2, 2, 3, 2, 2, 4},
                new String[][]{
                        {"JDK setup", "Variables and types", "Control flow"},
                        {"Classes and objects", "Inheritance", "Polymorphism", "Interfaces"},
                        {"List, Set, Map", "Stream operations", "Optional"},
                        {"Try-with-resources", "Custom exceptions", "NIO basics"},
                        {"Thread lifecycle", "ExecutorService", "CompletableFuture"},
                        {"Maven lifecycle", "JUnit 5", "Mockito"},
                        {"Spring Boot starters", "Beans and DI", "Application properties"},
                        {"Spring Data JPA", "Hibernate mappings", "Transactions"},
                        {"REST controllers", "DTOs and validation", "Spring Security basics"},
                        {"CLI app", "REST service", "Full-stack project"}
                },
                new String[][]{
                        {"Write a basic calculator CLI"},
                        {"Build a banking account hierarchy"},
                        {"Process a CSV with Streams"},
                        {"Build a file search utility"},
                        {"Implement a producer-consumer example"},
                        {"Unit-test a service with mocks"},
                        {"Create a REST hello-world service"},
                        {"Persist and query entities"},
                        {"Ship an API with auth"},
                        {"Deploy 3 projects to GitHub"}
                }
        );

        List<Milestone> milestones = MilestoneGenerator.generateSpaced(
                TaskGraphBuilder.totalWeeks(phases), 4,
                List.of("Java Core", "OOP Mastery", "Spring Boot Ready", "API Developer", "Project Portfolio Ready")
        );

        List<String> risks = List.of(
                "Skipping Data Structures and Algorithms",
                "No real projects or GitHub presence",
                "Inconsistent daily practice",
                "Avoiding concurrency fundamentals",
                "Neglecting testing and tooling"
        );

        List<String> successMetrics = List.of(
                "3+ projects on GitHub",
                "Spring Boot application deployed",
                "JUnit test suite with >80% coverage",
                "Comfortable with concurrency basics",
                "Portfolio website live"
        );

        List<String> recommendations = List.of(
                "Code daily — at least 1 hour per day",
                "Read 'Effective Java' by Joshua Bloch",
                "Solve LeetCode problems weekly",
                "Contribute to one open-source Java project",
                "Build a full-stack project with Spring Boot + React"
        );

        return new PlanBlueprint(
                "Roadmap for " + goal,
                "Become a job-ready " + goal,
                TaskGraphBuilder.totalWeeks(phases),
                phases,
                milestones,
                risks,
                successMetrics,
                recommendations,
                Map.of("domain", "JAVA", "version", "1.0")
        );
    }

    private String deriveGoal(PlanningAnalysisResult analysis) {
        for (String kw : analysis.keywords()) {
            if (kw.equalsIgnoreCase("java") || kw.equalsIgnoreCase("developer")) {
                if (kw.equalsIgnoreCase("developer")) return "Java Developer";
            }
        }
        return "Java Developer";
    }
}
