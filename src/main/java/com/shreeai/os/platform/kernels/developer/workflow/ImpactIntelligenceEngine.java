package com.shreeai.os.platform.kernels.developer.workflow;

import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntentType;
import com.shreeai.os.platform.kernels.developer.workflow.model.WorkflowImpactReport;
import com.shreeai.os.platform.kernels.developer.workflow.model.WorkflowImpactReport.RiskLevel;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Role;
import com.shreeai.os.platform.kernels.project.model.ProjectDependency;
import com.shreeai.os.platform.kernels.project.model.ProjectGraph;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * <b>ImpactIntelligenceEngine</b>
 *
 * <p>Deterministic, LLM-free impact analyzer for the Sprint-16 developer
 * workflow. Computes the affected files, impacted classes, risk level, and
 * dependency warnings for a given intent and target entity using
 * the project graph.</p>
 *
 * <p><b>Rules (deterministic, no LLM):</b></p>
 * <ul>
 *   <li>SECURITY → look for SecurityConfig, *Filter, *Controller, *Service, TokenService</li>
 *   <li>ADD_ENTITY → the entity class, its Repository, Service, Controller, DTO</li>
 *   <li>CREATE_API → the controller, service, repository, DTO for the entity</li>
 *   <li>REFACTOR → rename / extract: target class + all dependents</li>
 *   <li>FIX_BUG / OPTIMIZE / DATABASE → target class + its 1-hop incoming edges</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-16)</p>
 *
 * @since Sprint-16
 */
public final class ImpactIntelligenceEngine {

    /**
     * Computes the impact report for the given intent.
     *
     * @param intent     the analyzed developer intent
     * @param graph      the project graph (may be null for empty projects)
     * @param classes    the list of project classes (may be null/empty)
     * @return a non-null WorkflowImpactReport
     */
    public WorkflowImpactReport compute(DeveloperIntent intent,
                                        ProjectGraph graph,
                                        List<ProjectClass> classes) {
        Objects.requireNonNull(intent, "intent must not be null");
        List<ProjectClass> safeClasses = classes == null ? List.of() : classes;

        String entity = intent.entity() == null ? "" : intent.entity().trim();
        DeveloperIntentType type = intent.intent();

        // 1) Find candidate classes in the project graph by entity name (case-insensitive).
        List<ProjectClass> targetClasses = findTargetClasses(safeClasses, entity, type);

        // 2) Gather affected files (FQN-based).
        Set<String> affected = new LinkedHashSet<>();
        for (ProjectClass c : targetClasses) {
            affected.add(c.fullyQualifiedName());
        }
        // 3) For each target class, walk incoming edges to find dependents.
        if (graph != null && graph.size() > 0) {
            for (ProjectClass c : targetClasses) {
                for (ProjectDependency incoming : graph.incoming(c.fullyQualifiedName())) {
                    affected.add(incoming.source());
                }
            }
            // 4) Add intent-specific companion files (e.g. filters, security configs).
            List<ProjectClass> companions = findCompanionClasses(safeClasses, type, entity);
            for (ProjectClass c : companions) {
                affected.add(c.fullyQualifiedName());
            }
        }

        // 5) Build impacted class list (short, simple names).
        List<String> impacted = new ArrayList<>();
        for (String fqn : affected) {
            ProjectClass cls = graph == null ? null : graph.findClass(fqn);
            if (cls != null) {
                impacted.add(cls.name());
            } else {
                int lastDot = fqn.lastIndexOf('.');
                impacted.add(lastDot >= 0 ? fqn.substring(lastDot + 1) : fqn);
            }
        }

        // 6) Determine risk level.
        RiskLevel risk = determineRisk(type, targetClasses, impacted);

        // 7) Compute estimated changes (number of patches the pipeline will produce).
        int estimatedChanges = estimateChanges(type, targetClasses, companions(safeClasses, type, entity));

        // 8) Compute dependency warnings.
        List<String> warnings = determineWarnings(graph, type, targetClasses, impacted);

        return WorkflowImpactReport.builder()
                .totalFiles(safeClasses.size())
                .affectedFiles(new ArrayList<>(affected))
                .impactedClasses(impacted)
                .riskLevel(risk)
                .estimatedChanges(estimatedChanges)
                .dependencyWarnings(warnings)
                .build();
    }

    /**
     * Finds the target classes in the project that match the entity name,
     * falling back to intent-type-based discovery if no name is found.
     */
    private List<ProjectClass> findTargetClasses(List<ProjectClass> classes,
                                                 String entity,
                                                 DeveloperIntentType type) {
        if (classes.isEmpty()) return List.of();
        List<ProjectClass> matches = new ArrayList<>();
        if (!entity.isEmpty()) {
            String needle = entity.toLowerCase(Locale.ROOT);
            for (ProjectClass c : classes) {
                String name = c.name().toLowerCase(Locale.ROOT);
                if (name.equals(needle) || name.equalsIgnoreCase(needle)) {
                    matches.add(c);
                } else if (name.contains(needle) || needle.contains(name)) {
                    matches.add(c);
                }
            }
        }
        // If no entity-based matches, infer candidates by intent type.
        if (matches.isEmpty()) {
            if (type == DeveloperIntentType.SECURITY) {
                for (ProjectClass c : classes) {
                    if (isSecurityRelated(c)) matches.add(c);
                }
            } else if (type == DeveloperIntentType.ADD_FEATURE || type == DeveloperIntentType.CREATE_API) {
                for (ProjectClass c : classes) {
                    if (c.role() == Role.REST_CONTROLLER || c.role() == Role.CONTROLLER) {
                        matches.add(c);
                    }
                }
            }
        }
        return matches;
    }

    /**
     * Returns the list of intent-specific companion files (e.g. security filters
     * for SECURITY, repositories for ADD_ENTITY).
     */
    private List<ProjectClass> findCompanionClasses(List<ProjectClass> classes,
                                                    DeveloperIntentType type,
                                                    String entity) {
        if (classes.isEmpty()) return List.of();
        List<ProjectClass> companions = new ArrayList<>();
        switch (type) {
            case SECURITY -> {
                for (ProjectClass c : classes) {
                    if (isSecurityRelated(c)) companions.add(c);
                }
            }
            case ADD_ENTITY, CREATE_API -> {
                String needle = entity == null ? "" : entity.toLowerCase(Locale.ROOT);
                for (ProjectClass c : classes) {
                    String name = c.name().toLowerCase(Locale.ROOT);
                    if (c.role() == Role.REPOSITORY && (needle.isEmpty() || name.contains(needle))) {
                        companions.add(c);
                    }
                    if (c.role() == Role.SERVICE && (needle.isEmpty() || name.contains(needle))) {
                        companions.add(c);
                    }
                    if ((c.role() == Role.REST_CONTROLLER || c.role() == Role.CONTROLLER)
                            && (needle.isEmpty() || name.contains(needle))) {
                        companions.add(c);
                    }
                }
            }
            default -> { /* no companion for FIX_BUG, OPTIMIZE, REFACTOR, DATABASE */ }
        }
        return companions;
    }

    private List<ProjectClass> companions(List<ProjectClass> classes,
                                          DeveloperIntentType type,
                                          String entity) {
        return findCompanionClasses(classes, type, entity);
    }

    /**
     * Returns true if a class is security-related (e.g. SecurityConfig, JwtFilter).
     */
    private boolean isSecurityRelated(ProjectClass c) {
        String name = c.name();
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.contains("securityconfig") || lower.contains("security")) return true;
        if (lower.contains("filter") && (lower.contains("auth") || lower.contains("jwt"))) return true;
        if (lower.contains("tokenservice") || lower.contains("jwt")) return true;
        if (lower.endsWith("auth") || lower.endsWith("authservice")) return true;
        if (c.role() == Role.CONFIGURATION && lower.contains("security")) return true;
        return false;
    }

    /**
     * Computes the risk level using deterministic rules.
     */
    private RiskLevel determineRisk(DeveloperIntentType type,
                                    List<ProjectClass> targetClasses,
                                    List<String> impacted) {
        int count = impacted.size();
        if (type == DeveloperIntentType.SECURITY) {
            return count >= 3 ? RiskLevel.HIGH : RiskLevel.MEDIUM;
        }
        if (type == DeveloperIntentType.REFACTOR) {
            return count > 5 ? RiskLevel.HIGH : (count > 2 ? RiskLevel.MEDIUM : RiskLevel.LOW);
        }
        if (type == DeveloperIntentType.ADD_ENTITY || type == DeveloperIntentType.CREATE_API) {
            return count > 6 ? RiskLevel.HIGH : RiskLevel.MEDIUM;
        }
        if (type == DeveloperIntentType.FIX_BUG || type == DeveloperIntentType.OPTIMIZE) {
            return count > 3 ? RiskLevel.MEDIUM : RiskLevel.LOW;
        }
        if (type == DeveloperIntentType.DATABASE) {
            return count > 4 ? RiskLevel.HIGH : RiskLevel.MEDIUM;
        }
        return count > 5 ? RiskLevel.HIGH : RiskLevel.MEDIUM;
    }

    /**
     * Estimates the number of file changes the pipeline will produce.
     */
    private int estimateChanges(DeveloperIntentType type,
                                List<ProjectClass> targetClasses,
                                List<ProjectClass> companions) {
        int baseline = companions.isEmpty() ? 1 : companions.size();
        return switch (type) {
            case SECURITY -> Math.max(3, baseline + 2);          // service, filter, config
            case ADD_ENTITY -> Math.max(2, baseline + 1);         // entity + repository
            case CREATE_API -> Math.max(2, baseline + 1);         // controller + DTO
            case REFACTOR -> Math.max(1, baseline);
            case FIX_BUG -> 1;
            case OPTIMIZE -> 1;
            case DATABASE -> Math.max(1, baseline);
            case ADD_FEATURE -> Math.max(2, baseline + 1);
        };
    }

    /**
     * Detects dependency warnings (cycles, missing deps, etc.).
     */
    private List<String> determineWarnings(ProjectGraph graph,
                                           DeveloperIntentType type,
                                           List<ProjectClass> targetClasses,
                                           List<String> impacted) {
        List<String> warnings = new ArrayList<>();
        if (graph == null || graph.size() == 0) return warnings;

        if (!graph.detectCycles().isEmpty()) {
            warnings.add("Project contains circular dependencies — refactor carefully");
        }

        if (type == DeveloperIntentType.SECURITY) {
            boolean hasConfig = impacted.stream().anyMatch(n -> n.toLowerCase(Locale.ROOT).contains("securityconfig"));
            boolean hasFilter = impacted.stream().anyMatch(n -> n.toLowerCase(Locale.ROOT).contains("filter"));
            if (!hasConfig) {
                warnings.add("No SecurityConfig found — will need a new configuration class");
            }
            if (!hasFilter) {
                warnings.add("No auth filter found — will add a JWT filter");
            }
        }

        if (type == DeveloperIntentType.ADD_ENTITY || type == DeveloperIntentType.CREATE_API) {
            if (targetClasses.isEmpty()) {
                warnings.add("No matching class found in project — new files will be created");
            }
        }

        if (impacted.size() > 8) {
            warnings.add("High-blast-radius change: " + impacted.size() + " files will be affected");
        }

        return warnings;
    }
}
