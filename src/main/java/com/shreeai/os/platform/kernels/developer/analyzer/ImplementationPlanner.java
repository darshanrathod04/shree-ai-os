package com.shreeai.os.platform.kernels.developer.analyzer;

import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntentType;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;

import java.util.*;

/**
 * <b>ImplementationPlanner</b>
 *
 * <p>Generates a structured, phased implementation plan based on the
 * developer intent, impact report, and validation issues. Uses a
 * deterministic, rules-based approach without LLM calls.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-14)</p>
 *
 * @since Sprint-14
 */
public final class ImplementationPlanner {

    private final List<ProjectClass> allClasses;

    public ImplementationPlanner(List<ProjectClass> allClasses) {
        this.allClasses = new ArrayList<>(Objects.requireNonNull(allClasses, "allClasses must not be null"));
    }

    /**
     * Generates a structured implementation plan for the given intent
     * and impact report.
     */
    public ImplementationPlan plan(DeveloperIntent intent, ImpactReport impact,
                                   List<ValidationIssue> issues) {
        List<ImplementationPlan.Phase> phases = buildPhases(intent, impact, issues);
        List<String> risks = buildRisks(intent, impact, issues);
        double confidence = computeConfidence(intent, impact, phases);

        return ImplementationPlan.builder()
                .request(intent.originalRequest())
                .phases(phases)
                .metadata(buildMetadata(intent, impact))
                .confidence(confidence)
                .risks(risks)
                .build();
    }

    // ─── Phase building ──────────────────────────────────────────────────────

    private List<ImplementationPlan.Phase> buildPhases(DeveloperIntent intent,
                                                        ImpactReport impact,
                                                        List<ValidationIssue> issues) {
        List<ImplementationPlan.Phase> phases = new ArrayList<>();

        switch (intent.intent()) {
            case ADD_FEATURE -> phases.addAll(planAddFeature(intent, impact));
            case SECURITY -> phases.addAll(planSecurity(intent, impact));
            case CREATE_API -> phases.addAll(planCreateApi(intent, impact));
            case ADD_ENTITY -> phases.addAll(planAddEntity(intent, impact));
            case REFACTOR -> phases.addAll(planRefactor(intent, impact));
            case FIX_BUG -> phases.addAll(planFixBug(intent, impact));
            case OPTIMIZE -> phases.addAll(planOptimize(intent, impact));
            case DATABASE -> phases.addAll(planDatabase(intent, impact));
        }

        // Add validation phase if issues exist
        if (!issues.isEmpty()) {
            phases.add(buildValidationPhase(issues));
        }

        // Always add a testing phase
        phases.add(buildTestingPhase(intent, impact));

        // Number phases sequentially
        for (int i = 0; i < phases.size(); i++) {
            final int num = i + 1;
            phases.set(i, ImplementationPlan.Phase.builder()
                    .number(num)
                    .objective(phases.get(i).objective())
                    .description(phases.get(i).description())
                    .affectedFiles(phases.get(i).affectedFiles())
                    .dependencies(phases.get(i).dependencies())
                    .verificationCriteria(phases.get(i).verificationCriteria())
                    .riskNotes(phases.get(i).riskNotes())
                    .build());
        }

        return phases;
    }

    // ─── Intent-specific planners ───────────────────────────────────────────

    private List<ImplementationPlan.Phase> planAddFeature(DeveloperIntent intent, ImpactReport impact) {
        List<ImplementationPlan.Phase> phases = new ArrayList<>();
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Domain model")
                .description("Define or extend the domain model/entities")
                .affectedFiles(collectEntityNames(impact.affectedEntities()))
                .dependencies(List.of())
                .verificationCriteria(List.of(
                        "Entity compiles",
                        "All fields defined",
                        "Relationships mapped"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Repository layer")
                .description("Implement or extend the data access layer")
                .affectedFiles(collectNames(impact.affectedRepositories()))
                .dependencies(List.of("Domain model"))
                .verificationCriteria(List.of(
                        "Repository interface compiles",
                        "Query methods defined",
                        "Unit tests pass"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Service layer")
                .description("Implement or extend the business logic layer")
                .affectedFiles(collectNames(impact.affectedServices()))
                .dependencies(List.of("Repository layer"))
                .verificationCriteria(List.of(
                        "Service compiles",
                        "Business logic implemented",
                        "Unit tests pass"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("API/Controller layer")
                .description("Expose the feature via REST endpoints")
                .affectedFiles(collectNames(impact.affectedControllers()))
                .dependencies(List.of("Service layer"))
                .verificationCriteria(List.of(
                        "Endpoint compiles",
                        "HTTP method and path correct",
                        "Integration tests pass"
                ))
                .riskNotes(List.of(
                        "Existing endpoints must remain compatible"
                ))
                .build());
        return phases;
    }

    private List<ImplementationPlan.Phase> planSecurity(DeveloperIntent intent, ImpactReport impact) {
        List<ImplementationPlan.Phase> phases = new ArrayList<>();
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Security configuration")
                .description("Configure Spring Security and define access rules")
                .affectedFiles(collectNames(impact.affectedConfigurations()))
                .dependencies(List.of())
                .verificationCriteria(List.of(
                        "SecurityConfig compiles",
                        "Access rules defined",
                        "No endpoints left unprotected by default"
                ))
                .riskNotes(List.of(
                        "Filter ordering",
                        "Existing session authentication",
                        "CORS configuration"
                ))
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Authentication service")
                .description("Implement JWT token generation and validation")
                .affectedFiles(collectNames(impact.affectedServices()))
                .dependencies(List.of("Security configuration"))
                .verificationCriteria(List.of(
                        "Token generation works",
                        "Token validation works",
                        "Refresh token implemented"
                ))
                .riskNotes(List.of(
                        "Secret key management",
                        "Token expiration"
                ))
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Authentication endpoints")
                .description("Expose login/logout/refresh endpoints")
                .affectedFiles(collectNames(impact.affectedControllers()))
                .dependencies(List.of("Authentication service"))
                .verificationCriteria(List.of(
                        "POST /login works",
                        "POST /refresh works",
                        "Unauthorized access returns 401"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Apply security to existing endpoints")
                .description("Protect all existing endpoints with authentication")
                .affectedFiles(collectNames(impact.affectedControllers()))
                .dependencies(List.of("Authentication endpoints"))
                .verificationCriteria(List.of(
                        "All endpoints require auth",
                        "Existing integration tests updated",
                        "Security tests pass"
                ))
                .riskNotes(List.of(
                        "Breaking existing clients",
                        "Public endpoints must be explicitly permitted"
                ))
                .build());
        return phases;
    }

    private List<ImplementationPlan.Phase> planCreateApi(DeveloperIntent intent, ImpactReport impact) {
        List<ImplementationPlan.Phase> phases = new ArrayList<>();
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Define DTO/Request/Response models")
                .description("Create data transfer objects for the API")
                .affectedFiles(List.of())
                .dependencies(List.of())
                .verificationCriteria(List.of(
                        "DTOs compile",
                        "Validation annotations present",
                        "Jackson annotations correct"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Implement controller")
                .description("Create the REST controller with endpoints")
                .affectedFiles(collectNames(impact.affectedControllers()))
                .dependencies(List.of("Define DTO models"))
                .verificationCriteria(List.of(
                        "Endpoints defined",
                        "HTTP methods correct",
                        "Paths follow REST conventions"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Service layer")
                .description("Implement business logic for the API")
                .affectedFiles(collectNames(impact.affectedServices()))
                .dependencies(List.of("Implement controller"))
                .verificationCriteria(List.of(
                        "Service compiles",
                        "Error handling in place",
                        "Unit tests pass"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Integration tests")
                .description("End-to-end API integration tests")
                .affectedFiles(List.of())
                .dependencies(List.of("Service layer"))
                .verificationCriteria(List.of(
                        "All CRUD operations tested",
                        "Error responses tested",
                        "HTTP status codes correct"
                ))
                .riskNotes(List.of())
                .build());
        return phases;
    }

    private List<ImplementationPlan.Phase> planAddEntity(DeveloperIntent intent, ImpactReport impact) {
        List<ImplementationPlan.Phase> phases = new ArrayList<>();
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Create entity class")
                .description("Define the JPA entity with table mapping")
                .affectedFiles(collectEntityNames(impact.affectedEntities()).stream().map(s -> s + ".java").toList())
                .dependencies(List.of())
                .verificationCriteria(List.of(
                        "Entity compiles",
                        "@Table and @Column annotations correct",
                        "Relationships defined"
                ))
                .riskNotes(List.of(
                        "Database schema migration required"
                ))
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Create repository")
                .description("Define Spring Data JPA repository")
                .affectedFiles(collectNames(impact.affectedRepositories()))
                .dependencies(List.of("Create entity class"))
                .verificationCriteria(List.of(
                        "Repository compiles",
                        "Custom queries defined",
                        "Unit tests pass"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Database migration")
                .description("Create Flyway/Liquibase migration script")
                .affectedFiles(List.of())
                .dependencies(List.of("Create repository"))
                .verificationCriteria(List.of(
                        "Migration script created",
                        "Schema validated",
                        "Rollback tested"
                ))
                .riskNotes(List.of(
                        "Existing data migration",
                        "Backward compatibility"
                ))
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Service and controller")
                .description("Expose entity via service and API")
                .affectedFiles(mergeNames(collectNames(impact.affectedServices()), collectNames(impact.affectedControllers())))
                .dependencies(List.of("Database migration"))
                .verificationCriteria(List.of(
                        "Service and controller compile",
                        "Integration tests pass"
                ))
                .riskNotes(List.of())
                .build());
        return phases;
    }

    private List<ImplementationPlan.Phase> planRefactor(DeveloperIntent intent, ImpactReport impact) {
        List<ImplementationPlan.Phase> phases = new ArrayList<>();
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Identify affected files")
                .description("Map all classes that depend on the refactored code")
                .affectedFiles(impact.directlyAffected())
                .dependencies(List.of())
                .verificationCriteria(List.of(
                        "All affected files identified",
                        "No breaking changes documented"
                ))
                .riskNotes(List.of(
                        "Wide impact due to refactoring " + intent.entity()
                ))
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Implement refactoring")
                .description("Apply the refactoring change")
                .affectedFiles(impact.directlyAffected())
                .dependencies(List.of("Identify affected files"))
                .verificationCriteria(List.of(
                        "Code compiles",
                        "All callers updated",
                        "No logic changes"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Update all references")
                .description("Propagate refactoring to all affected classes")
                .affectedFiles(impact.indirectlyAffected())
                .dependencies(List.of("Implement refactoring"))
                .verificationCriteria(List.of(
                        "All references updated",
                        "Project compiles",
                        "All tests pass"
                ))
                .riskNotes(List.of(
                        "Missed reference",
                        "Breaking API change"
                ))
                .build());
        return phases;
    }

    private List<ImplementationPlan.Phase> planFixBug(DeveloperIntent intent, ImpactReport impact) {
        List<ImplementationPlan.Phase> phases = new ArrayList<>();
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Reproduce the bug")
                .description("Write a failing test that demonstrates the bug")
                .affectedFiles(List.of())
                .dependencies(List.of())
                .verificationCriteria(List.of(
                        "Failing test written",
                        "Test reproduces the bug"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Implement the fix")
                .description("Apply the fix to the affected code")
                .affectedFiles(impact.directlyAffected())
                .dependencies(List.of("Reproduce the bug"))
                .verificationCriteria(List.of(
                        "Code compiles",
                        "Bug test now passes"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Regression testing")
                .description("Run full test suite to ensure no regressions")
                .affectedFiles(List.of())
                .dependencies(List.of("Implement the fix"))
                .verificationCriteria(List.of(
                        "All existing tests pass",
                        "New test included in CI"
                ))
                .riskNotes(List.of(
                        "Hidden dependency on buggy behavior"
                ))
                .build());
        return phases;
    }

    private List<ImplementationPlan.Phase> planOptimize(DeveloperIntent intent, ImpactReport impact) {
        List<ImplementationPlan.Phase> phases = new ArrayList<>();
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Profile and identify bottleneck")
                .description("Identify the specific code to optimize")
                .affectedFiles(impact.directlyAffected())
                .dependencies(List.of())
                .verificationCriteria(List.of(
                        "Bottleneck identified",
                        "Performance baseline established"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Implement optimization")
                .description("Apply the optimization technique")
                .affectedFiles(impact.directlyAffected())
                .dependencies(List.of("Profile and identify bottleneck"))
                .verificationCriteria(List.of(
                        "Code compiles",
                        "Performance improved",
                        "Correctness preserved"
                ))
                .riskNotes(List.of(
                        "Premature optimization",
                        "Complexity trade-off"
                ))
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Benchmark and validate")
                .description("Verify the optimization with benchmarks")
                .affectedFiles(List.of())
                .dependencies(List.of("Implement optimization"))
                .verificationCriteria(List.of(
                        "Benchmark shows improvement",
                        "All tests pass"
                ))
                .riskNotes(List.of())
                .build());
        return phases;
    }

    private List<ImplementationPlan.Phase> planDatabase(DeveloperIntent intent, ImpactReport impact) {
        List<ImplementationPlan.Phase> phases = new ArrayList<>();
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Define schema changes")
                .description("Plan and implement database schema changes")
                .affectedFiles(List.of())
                .dependencies(List.of())
                .verificationCriteria(List.of(
                        "Migration script created",
                        "Schema validated"
                ))
                .riskNotes(List.of(
                        "Data loss risk",
                        "Backward compatibility"
                ))
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Update repository layer")
                .description("Adapt repository queries to new schema")
                .affectedFiles(collectNames(impact.affectedRepositories()))
                .dependencies(List.of("Define schema changes"))
                .verificationCriteria(List.of(
                        "Repository compiles",
                        "Queries validated"
                ))
                .riskNotes(List.of())
                .build());
        phases.add(ImplementationPlan.Phase.builder()
                .objective("Update service and controller")
                .description("Propagate changes to service and API layers")
                .affectedFiles(mergeNames(collectNames(impact.affectedServices()), collectNames(impact.affectedControllers())))
                .dependencies(List.of("Update repository layer"))
                .verificationCriteria(List.of(
                        "All affected layers compile",
                        "Integration tests pass"
                ))
                .riskNotes(List.of())
                .build());
        return phases;
    }

    // ─── Shared phases ──────────────────────────────────────────────────────

    private ImplementationPlan.Phase buildValidationPhase(List<ValidationIssue> issues) {
        List<String> critical = issues.stream()
                .filter(i -> i.severity() == ValidationIssue.Severity.HIGH)
                .map(ValidationIssue::message)
                .toList();
        return ImplementationPlan.Phase.builder()
                .objective("Architecture validation")
                .description("Verify no new architecture violations introduced")
                .affectedFiles(issues.stream()
                        .flatMap(i -> i.affectedFiles().stream())
                        .distinct()
                        .toList())
                .dependencies(List.of("Implementation complete"))
                .verificationCriteria(critical.isEmpty()
                        ? List.of("No HIGH severity issues", "No architectural violations")
                        : List.of("Critical issues resolved: " + String.join(", ", critical)))
                .riskNotes(List.of(
                        "Existing issues must be tracked separately",
                        "HIGH severity must be resolved before merge"
                ))
                .build();
    }

    private ImplementationPlan.Phase buildTestingPhase(DeveloperIntent intent, ImpactReport impact) {
        List<String> affectedFiles = new ArrayList<>();
        affectedFiles.addAll(impact.directlyAffected());
        return ImplementationPlan.Phase.builder()
                .objective("Comprehensive testing")
                .description("Run full test suite and verify all phases")
                .affectedFiles(affectedFiles)
                .dependencies(List.of("All previous phases"))
                .verificationCriteria(List.of(
                        "mvn test passes",
                        "All phases verified",
                        "No regressions"
                ))
                .riskNotes(List.of())
                .build();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private List<String> collectNames(List<? extends com.shreeai.os.platform.kernels.project.model.ProjectClass> classes) {
        return classes.stream().map(ProjectClass::name).toList();
    }

    private List<String> collectEntityNames(List<com.shreeai.os.platform.kernels.project.model.ProjectEntity> entities) {
        return entities.stream().map(com.shreeai.os.platform.kernels.project.model.ProjectEntity::name).toList();
    }

    private List<String> mergeNames(List<String> a, List<String> b) {
        List<String> merged = new ArrayList<>(a);
        for (String s : b) {
            if (!merged.contains(s)) merged.add(s);
        }
        return merged;
    }

    private List<String> buildRisks(DeveloperIntent intent, ImpactReport impact,
                                     List<ValidationIssue> issues) {
        List<String> risks = new ArrayList<>();
        if (impact.totalAffected() > 10) {
            risks.add("High change surface area (" + impact.totalAffected() + " affected classes)");
        }
        if (impact.dependencyDepth() > 3) {
            risks.add("Deep dependency chain (" + impact.dependencyDepth() + " levels)");
        }
        for (ValidationIssue issue : issues) {
            if (issue.severity() == ValidationIssue.Severity.HIGH) {
                risks.add("HIGH: " + issue.message());
            }
        }
        switch (intent.intent()) {
            case SECURITY -> {
                risks.add("Existing session authentication compatibility");
                risks.add("Bean/filter ordering");
                risks.add("Filter precedence");
            }
            case ADD_FEATURE -> {
                if (!impact.affectedEndpoints().isEmpty()) {
                    risks.add("Existing endpoint compatibility");
                }
            }
            case REFACTOR -> {
                risks.add("Breaking API changes for dependent services");
            }
            default -> { }
        }
        return risks;
    }

    private Map<String, Object> buildMetadata(DeveloperIntent intent, ImpactReport impact) {
        return Map.of(
                "intentType", intent.intent().name(),
                "domain", intent.domain(),
                "affectedClasses", impact.totalAffected(),
                "dependencyDepth", impact.dependencyDepth(),
                "endpoints", impact.affectedEndpoints().size(),
                "controllers", impact.affectedControllers().size(),
                "services", impact.affectedServices().size()
        );
    }

    private double computeConfidence(DeveloperIntent intent, ImpactReport impact,
                                     List<ImplementationPlan.Phase> phases) {
        double base = 0.85;
        if (impact.totalAffected() > 20) base -= 0.1;
        if (impact.dependencyDepth() > 4) base -= 0.1;
        if (phases.isEmpty()) base -= 0.2;
        return Math.max(0.3, base);
    }
}