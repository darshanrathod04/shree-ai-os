package com.shreeai.os.platform.kernels.developer.codegen;

import com.shreeai.os.platform.kernels.developer.analyzer.ImpactReport;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntentType;
import com.shreeai.os.platform.kernels.developer.codegen.model.*;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;

import java.time.Instant;
import java.util.*;

/**
 * <b>PatchPlanner</b> — produces a deterministic {@link PatchPlan} for a
 * developer intent, taking into account the impact report and any existing
 * project classes.
 *
 * <p>The planner never writes any files; it only emits a structured
 * description of the changes. Each {@link FilePatch} carries its
 * own reason, dependencies, and ordered operations.</p>
 *
 * <p>The planner's behavior is purely rule-based — no LLM calls, no random
 * elements — so two calls with the same inputs will always produce the
 * same plan.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class PatchPlanner {

    private final List<ProjectClass> existingClasses;
    private final String defaultPackage;

    public PatchPlanner() {
        this(List.of(), "com.example");
    }

    public PatchPlanner(List<ProjectClass> existingClasses, String defaultPackage) {
        this.existingClasses = existingClasses == null ? List.of() : new ArrayList<>(existingClasses);
        this.defaultPackage = defaultPackage == null || defaultPackage.isEmpty()
                ? "com.example" : defaultPackage;
    }

    /**
     * Builds a {@link PatchPlan} for the given developer request.
     */
    public PatchPlan plan(DeveloperIntent intent,
                          ImpactReport impact,
                          List<ProjectClass> allClasses) {
        Objects.requireNonNull(intent, "intent");

        List<FilePatch> patches = switch (intent.intent()) {
            case SECURITY -> planSecurity(intent, impact == null ? emptyImpact() : impact, allClasses);
            case ADD_FEATURE -> planAddFeature(intent, impact, allClasses);
            case CREATE_API -> planCreateApi(intent, impact, allClasses);
            case ADD_ENTITY -> planAddEntity(intent, impact, allClasses);
            case REFACTOR -> planRefactor(intent, impact, allClasses);
            case FIX_BUG -> planFixBug(intent, impact, allClasses);
            case OPTIMIZE -> planOptimize(intent, impact, allClasses);
            case DATABASE -> planDatabase(intent, impact, allClasses);
        };

        List<String> newFiles = patches.stream()
                .filter(FilePatch::isNewFile)
                .map(FilePatch::targetFile)
                .toList();
        List<String> modifiedFiles = patches.stream()
                .filter(p -> !p.isNewFile())
                .map(FilePatch::targetFile)
                .toList();

        return PatchPlan.builder()
                .request(intent.originalRequest())
                .intent(intent.intent().name())
                .entity(intent.entity())
                .patches(patches)
                .newFiles(newFiles)
                .modifiedFiles(modifiedFiles)
                .status(PatchPlan.Status.READY)
                .createdAt(Instant.now())
                .metadata(buildMetadata(intent, impact, patches.size()))
                .build();
    }

    // ─── Intent-specific planners ──────────────────────────────────────────────

    private List<FilePatch> planSecurity(DeveloperIntent intent, ImpactReport impact,
                                         List<ProjectClass> allClasses) {
        String entity = capFirst(intent.entity().isEmpty() ? "Jwt" : intent.entity());
        List<FilePatch> patches = new ArrayList<>();

        // 1) Create JwtTokenService
        patches.add(buildCreateServicePatch(
                "JwtTokenService", defaultPackage + ".security",
                "Token generation and validation for JWT authentication",
                List.of("io.jsonwebtoken.Jwts",
                        "io.jsonwebtoken.SignatureAlgorithm",
                        "io.jsonwebtoken.Claims",
                        "io.jsonwebtoken.JwtException")));

        // 2) Create JwtAuthenticationFilter
        patches.add(buildCreateClassPatch(
                "JwtAuthenticationFilter", defaultPackage + ".security",
                "OncePerRequestFilter that extracts and validates JWT tokens",
                List.of("jakarta.servlet.FilterChain",
                        "jakarta.servlet.http.HttpServletRequest",
                        "jakarta.servlet.http.HttpServletResponse",
                        "org.springframework.web.filter.OncePerRequestFilter"),
                true /* abstract */));

        // 3) Create SecurityConfig (modify or create)
        patches.add(buildCreateConfigPatch(
                "SecurityConfig", defaultPackage + ".config",
                "Spring Security configuration with JWT filter chain",
                List.of("org.springframework.context.annotation.Bean",
                        "org.springframework.security.config.annotation.web.builders.HttpSecurity",
                        "org.springframework.security.web.SecurityFilterChain")));

        // 4) Modify the affected services / controllers to use the new components
        if (impact != null) {
            for (ProjectClass affected : impact.affectedServices()) {
                if (isNewClass(affected)) continue;
                patches.add(buildModifyForSecurityPatch(affected));
            }
        }

        return patches;
    }

    private List<FilePatch> planAddFeature(DeveloperIntent intent, ImpactReport impact,
                                          List<ProjectClass> allClasses) {
        String entity = capFirst(intent.entity().isEmpty() ? "Feature" : intent.entity());
        List<FilePatch> patches = new ArrayList<>();

        patches.add(buildCreateServicePatch(
                entity + "Service", defaultPackage + ".service",
                "Service layer for " + entity + " feature",
                List.of()));

        patches.add(buildCreateControllerPatch(
                entity + "Controller", defaultPackage + ".controller",
                "REST controller exposing " + entity + " endpoints",
                List.of()));

        return patches;
    }

    private List<FilePatch> planCreateApi(DeveloperIntent intent, ImpactReport impact,
                                          List<ProjectClass> allClasses) {
        String entity = capFirst(intent.entity().isEmpty() ? "Resource" : intent.entity());
        List<FilePatch> patches = new ArrayList<>();

        // Create DTO
        patches.add(buildCreateDtoPatch(
                entity + "Dto", defaultPackage + ".dto",
                "Data transfer object for " + entity + " API"));

        // Create Service
        patches.add(buildCreateServicePatch(
                entity + "Service", defaultPackage + ".service",
                "Business logic for " + entity + " operations",
                List.of()));

        // Create Controller with endpoints
        patches.add(buildCreateControllerWithEndpointsPatch(
                entity + "Controller", defaultPackage + ".controller",
                "REST controller for " + entity + " CRUD operations",
                entity));

        return patches;
    }

    private List<FilePatch> planAddEntity(DeveloperIntent intent, ImpactReport impact,
                                          List<ProjectClass> allClasses) {
        String entity = capFirst(intent.entity().isEmpty() ? "Entity" : intent.entity());
        List<FilePatch> patches = new ArrayList<>();

        // Entity
        patches.add(buildCreateEntityPatch(entity, defaultPackage + ".entity",
                "JPA entity representing " + entity));

        // Repository
        patches.add(buildCreateRepositoryPatch(entity + "Repository",
                defaultPackage + ".repository",
                "Spring Data repository for " + entity));

        // Service
        patches.add(buildCreateServicePatch(entity + "Service",
                defaultPackage + ".service",
                "Service for " + entity + " operations",
                List.of()));

        // Controller
        patches.add(buildCreateControllerPatch(entity + "Controller",
                defaultPackage + ".controller",
                "REST controller for " + entity,
                List.of()));

        return patches;
    }

    private List<FilePatch> planRefactor(DeveloperIntent intent, ImpactReport impact,
                                         List<ProjectClass> allClasses) {
        List<FilePatch> patches = new ArrayList<>();
        String target = intent.entity().isEmpty() ? "UserService" : capFirst(intent.entity());

        // For refactoring we always try to modify the existing target class
        ProjectClass existing = findClass(allClasses, target);
        if (existing != null) {
            patches.add(buildRefactorPatch(existing));
        } else {
            // Fall back to creating a refactored version
            patches.add(buildCreateServicePatch(
                    target + "Refactored", defaultPackage + ".service",
                    "Refactored version of " + target,
                    List.of()));
        }
        return patches;
    }

    private List<FilePatch> planFixBug(DeveloperIntent intent, ImpactReport impact,
                                       List<ProjectClass> allClasses) {
        List<FilePatch> patches = new ArrayList<>();
        String target = intent.entity().isEmpty() ? "Bug" : capFirst(intent.entity());
        ProjectClass existing = findClass(allClasses, target);

        if (existing != null) {
            patches.add(buildBugfixPatch(existing, intent.originalRequest()));
        } else {
            // Create a defensive helper class
            patches.add(buildCreateServicePatch(
                    target + "Fixer", defaultPackage + ".fix",
                    "Helper class to fix " + target,
                    List.of()));
        }
        return patches;
    }

    private List<FilePatch> planOptimize(DeveloperIntent intent, ImpactReport impact,
                                          List<ProjectClass> allClasses) {
        List<FilePatch> patches = new ArrayList<>();
        String target = intent.entity().isEmpty() ? "Service" : capFirst(intent.entity());
        ProjectClass existing = findClass(allClasses, target);
        if (existing != null) {
            patches.add(buildOptimizePatch(existing));
        } else {
            patches.add(buildCreateServicePatch(
                    target + "Cache", defaultPackage + ".cache",
                    "Cached proxy for " + target,
                    List.of()));
        }
        return patches;
    }

    private List<FilePatch> planDatabase(DeveloperIntent intent, ImpactReport impact,
                                          List<ProjectClass> allClasses) {
        String entity = capFirst(intent.entity().isEmpty() ? "Record" : intent.entity());
        List<FilePatch> patches = new ArrayList<>();
        patches.add(buildCreateEntityPatch(entity, defaultPackage + ".entity",
                "JPA entity for " + entity));
        patches.add(buildCreateRepositoryPatch(entity + "Repository",
                defaultPackage + ".repository",
                "Repository for " + entity));
        return patches;
    }

    // ─── Patch builders ────────────────────────────────────────────────────────

    private FilePatch buildCreateServicePatch(String className, String pkg,
                                              String reason, List<String> imports) {
        String filePath = pkg.replace('.', '/') + "/" + className + ".java";
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.CREATE_CLASS, className,
                "public class " + className + " {}",
                "Create " + className + " service class",
                List.of()
        ));
        List<String> allImports = new ArrayList<>();
        allImports.add("org.springframework.stereotype.Service");
        allImports.addAll(imports);
        for (String imp : allImports) {
            ops.add(new FilePatch.Operation(
                    PatchOperation.ADD_IMPORT, imp, imp,
                    "Import " + imp, List.of()
            ));
        }
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(pkg + "." + className)
                .newFile(true)
                .operations(ops)
                .dependencies(List.of("org.springframework.stereotype.Service"))
                .reason(reason)
                .build();
    }

    private FilePatch buildCreateClassPatch(String className, String pkg,
                                             String reason, List<String> imports,
                                             boolean isAbstract) {
        String filePath = pkg.replace('.', '/') + "/" + className + ".java";
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.CREATE_CLASS, className,
                (isAbstract ? "public abstract class " : "public class ") + className + " {}",
                "Create " + className,
                List.of()
        ));
        for (String imp : imports) {
            ops.add(new FilePatch.Operation(
                    PatchOperation.ADD_IMPORT, imp, imp,
                    "Import " + imp, List.of()
            ));
        }
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(pkg + "." + className)
                .newFile(true)
                .operations(ops)
                .reason(reason)
                .build();
    }

    private FilePatch buildCreateConfigPatch(String className, String pkg,
                                              String reason, List<String> imports) {
        String filePath = pkg.replace('.', '/') + "/" + className + ".java";
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.CREATE_CLASS, className,
                "public class " + className + " {}",
                "Create " + className + " configuration class",
                List.of()
        ));
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_IMPORT, "org.springframework.context.annotation.Configuration",
                "org.springframework.context.annotation.Configuration",
                "Configuration annotation import", List.of()
        ));
        for (String imp : imports) {
            ops.add(new FilePatch.Operation(
                    PatchOperation.ADD_IMPORT, imp, imp,
                    "Import " + imp, List.of()
            ));
        }
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(pkg + "." + className)
                .newFile(true)
                .operations(ops)
                .dependencies(List.of("org.springframework.context.annotation.Configuration"))
                .reason(reason)
                .build();
    }

    private FilePatch buildCreateDtoPatch(String className, String pkg, String reason) {
        String filePath = pkg.replace('.', '/') + "/" + className + ".java";
        List<FilePatch.Operation> ops = List.of(
                new FilePatch.Operation(
                        PatchOperation.CREATE_CLASS, className,
                        "public class " + className + " {}",
                        "Create " + className + " DTO",
                        List.of()
                )
        );
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(pkg + "." + className)
                .newFile(true)
                .operations(ops)
                .reason(reason)
                .build();
    }

    private FilePatch buildCreateEntityPatch(String className, String pkg, String reason) {
        String filePath = pkg.replace('.', '/') + "/" + className + ".java";
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_ENTITY, className,
                "public class " + className + " {}",
                "Create JPA entity " + className,
                List.of()
        ));
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_IMPORT, "jakarta.persistence.Entity",
                "jakarta.persistence.Entity",
                "JPA Entity annotation", List.of()
        ));
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_IMPORT, "jakarta.persistence.Id",
                "jakarta.persistence.Id",
                "JPA Id annotation", List.of()
        ));
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_IMPORT, "jakarta.persistence.GeneratedValue",
                "jakarta.persistence.GeneratedValue",
                "JPA GeneratedValue annotation", List.of()
        ));
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(pkg + "." + className)
                .newFile(true)
                .operations(ops)
                .dependencies(List.of("jakarta.persistence.Entity",
                        "jakarta.persistence.Id",
                        "jakarta.persistence.GeneratedValue"))
                .reason(reason)
                .build();
    }

    private FilePatch buildCreateRepositoryPatch(String className, String pkg, String reason) {
        String filePath = pkg.replace('.', '/') + "/" + className + ".java";
        // Find the entity FQN (assumes naming convention ClassName -> ClassNameEntity)
        // We'll leave the entity FQN unknown at this stage - downstream validation will resolve
        String entityName = className.endsWith("Repository")
                ? className.substring(0, className.length() - "Repository".length())
                : "Entity";
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.CREATE_CLASS, className,
                "public interface " + className + " extends JpaRepository<" + entityName + ", Long> {}",
                "Create Spring Data repository " + className,
                List.of()
        ));
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_IMPORT, "org.springframework.data.jpa.repository.JpaRepository",
                "org.springframework.data.jpa.repository.JpaRepository",
                "JpaRepository import", List.of()
        ));
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_IMPORT, "org.springframework.stereotype.Repository",
                "org.springframework.stereotype.Repository",
                "Repository annotation import", List.of()
        ));
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(pkg + "." + className)
                .newFile(true)
                .operations(ops)
                .dependencies(List.of("org.springframework.data.jpa.repository.JpaRepository",
                        "org.springframework.stereotype.Repository"))
                .reason(reason)
                .build();
    }

    private FilePatch buildCreateControllerPatch(String className, String pkg,
                                                 String reason, List<String> imports) {
        String filePath = pkg.replace('.', '/') + "/" + className + ".java";
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.CREATE_CLASS, className,
                "public class " + className + " {}",
                "Create " + className + " REST controller",
                List.of()
        ));
        List<String> allImports = new ArrayList<>(List.of(
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.web.bind.annotation.RequestMapping"
        ));
        allImports.addAll(imports);
        for (String imp : allImports) {
            ops.add(new FilePatch.Operation(
                    PatchOperation.ADD_IMPORT, imp, imp,
                    "Import " + imp, List.of()
            ));
        }
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(pkg + "." + className)
                .newFile(true)
                .operations(ops)
                .dependencies(List.of("org.springframework.web.bind.annotation.RestController"))
                .reason(reason)
                .build();
    }

    private FilePatch buildCreateControllerWithEndpointsPatch(String className, String pkg,
                                                               String reason, String entity) {
        String filePath = pkg.replace('.', '/') + "/" + className + ".java";
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.CREATE_CLASS, className,
                "public class " + className + " {}",
                "Create " + className + " REST controller",
                List.of()
        ));
        // Add GET endpoint
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_ENDPOINT, "getAll()",
                "List<" + entity + "Dto> getAll()",
                "Add GET /" + lowerFirst(entity) + " endpoint", List.of()
        ));
        // Add POST endpoint
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_ENDPOINT, "create(" + entity + "Dto)",
                entity + "Dto create(" + entity + "Dto dto)",
                "Add POST /" + lowerFirst(entity) + " endpoint", List.of()
        ));
        // Imports
        List<String> imps = List.of(
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.web.bind.annotation.RequestMapping",
                "org.springframework.web.bind.annotation.GetMapping",
                "org.springframework.web.bind.annotation.PostMapping",
                "org.springframework.web.bind.annotation.RequestBody"
        );
        for (String imp : imps) {
            ops.add(new FilePatch.Operation(
                    PatchOperation.ADD_IMPORT, imp, imp,
                    "Import " + imp, List.of()
            ));
        }
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(pkg + "." + className)
                .newFile(true)
                .operations(ops)
                .reason(reason)
                .build();
    }

    private FilePatch buildModifyForSecurityPatch(ProjectClass affected) {
        String filePath = affected.filePath();
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.MODIFY_CLASS, affected.name(),
                "Add @PreAuthorize annotations and JWT user details extraction",
                "Wire JWT authentication into " + affected.name(),
                List.of("com.example.security.JwtTokenService")
        ));
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_IMPORT, "org.springframework.security.access.prepost.PreAuthorize",
                "org.springframework.security.access.prepost.PreAuthorize",
                "PreAuthorize annotation", List.of()
        ));
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(affected.fullyQualifiedName())
                .newFile(false)
                .operations(ops)
                .reason("Wire JWT authentication into " + affected.name())
                .build();
    }

    private FilePatch buildRefactorPatch(ProjectClass existing) {
        String filePath = existing.filePath();
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.MODIFY_CLASS, existing.name(),
                "Refactor for cleaner separation of concerns",
                "Refactor " + existing.name() + " to improve structure",
                List.of()
        ));
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(existing.fullyQualifiedName())
                .newFile(false)
                .operations(ops)
                .reason("Refactor " + existing.name() + " to improve structure")
                .build();
    }

    private FilePatch buildBugfixPatch(ProjectClass existing, String description) {
        String filePath = existing.filePath();
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.MODIFY_METHOD, "fixBug",
                "Apply fix: " + description,
                "Fix bug in " + existing.name(),
                List.of()
        ));
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(existing.fullyQualifiedName())
                .newFile(false)
                .operations(ops)
                .reason("Fix bug in " + existing.name() + ": " + description)
                .build();
    }

    private FilePatch buildOptimizePatch(ProjectClass existing) {
        String filePath = existing.filePath();
        List<FilePatch.Operation> ops = new ArrayList<>();
        ops.add(new FilePatch.Operation(
                PatchOperation.MODIFY_METHOD, "optimize",
                "Add caching layer to " + existing.name(),
                "Optimize " + existing.name() + " with caching",
                List.of()
        ));
        ops.add(new FilePatch.Operation(
                PatchOperation.ADD_IMPORT, "org.springframework.cache.annotation.Cacheable",
                "org.springframework.cache.annotation.Cacheable",
                "Cacheable annotation", List.of()
        ));
        return FilePatch.builder()
                .targetFile(filePath)
                .targetClass(existing.fullyQualifiedName())
                .newFile(false)
                .operations(ops)
                .reason("Optimize " + existing.name() + " with caching")
                .build();
    }

    // ─── Utilities ──────────────────────────────────────────────────────────────

    private ImpactReport emptyImpact() {
        return ImpactReport.builder()
                .targetClass("")
                .directlyAffected(List.of())
                .indirectlyAffected(List.of())
                .affectedEndpoints(List.of())
                .affectedControllers(List.of())
                .affectedServices(List.of())
                .affectedRepositories(List.of())
                .affectedEntities(List.of())
                .affectedConfigurations(List.of())
                .dependencyDepth(0)
                .build();
    }

    private ProjectClass findClass(List<ProjectClass> classes, String simpleName) {
        if (classes == null) return null;
        return classes.stream()
                .filter(c -> c.name().equals(simpleName) || c.fullyQualifiedName().endsWith("." + simpleName))
                .findFirst()
                .orElse(null);
    }

    private boolean isNewClass(ProjectClass c) {
        return c.filePath() == null || c.filePath().isEmpty();
    }

    private String capFirst(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String lowerFirst(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private Map<String, Object> buildMetadata(DeveloperIntent intent, ImpactReport impact,
                                              int patchCount) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("intent", intent.intent().name());
        meta.put("entity", intent.entity());
        meta.put("domain", intent.domain());
        meta.put("patchCount", patchCount);
        if (impact != null) {
            meta.put("affectedClassCount", impact.totalAffected());
        }
        return meta;
    }
}
