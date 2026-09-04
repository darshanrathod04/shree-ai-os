package com.shreeai.os.platform.kernels.project.analyzer;

import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Role;
import com.shreeai.os.platform.kernels.project.model.ProjectEndpoint;
import com.shreeai.os.platform.kernels.project.model.ProjectMethod;
import com.shreeai.os.platform.kernels.project.model.ProjectEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>SpringAnalyzer</b>
 *
 * <p>Analyzes a collection of project classes to:
 * <ul>
 *   <li>Extract REST endpoints from @RestController / @Controller methods</li>
 *   <li>Detect Spring Boot via classpath or annotation presence</li>
 *   <li>Link controllers → services → repositories → entities</li>
 *   <li>Count Spring beans (@Service, @Repository, @Component, @Configuration)</li>
 * </ul></p>
 *
 * <p><b>Ownership:</b> Project Intelligence (Sprint-13)</p>
 */
public final class SpringAnalyzer {

    private static final Set<String> SPRING_BOOT_INDICATORS = Set.of(
            "SpringBootApplication", "SpringApplication",
            "EnableAutoConfiguration", "ComponentScan"
    );

    private final List<ProjectClass> classes;

    public SpringAnalyzer(List<ProjectClass> classes) {
        this.classes = new ArrayList<>(classes);
    }

    /**
     * Returns true if the project appears to be a Spring Boot application,
     * based on class annotations.
     */
    public boolean isSpringBoot() {
        for (ProjectClass c : classes) {
            for (String ann : c.annotations()) {
                if (SPRING_BOOT_INDICATORS.contains(ann)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns all REST endpoints discovered across all controllers.
     * Attempts to resolve the service, repository, and entity chain.
     */
    public List<ProjectEndpoint> extractEndpoints() {
        List<ProjectEndpoint> endpoints = new ArrayList<>();

        // Build quick lookup maps
        Map<String, ProjectClass> byName = buildNameMap();
        Map<String, ProjectClass> byRole = new HashMap<>();

        for (ProjectClass c : classes) {
            if (c.role() == Role.CONTROLLER || c.role() == Role.SERVICE
                    || c.role() == Role.REPOSITORY || c.role() == Role.ENTITY) {
                byRole.put(c.name(), c);
            }
        }

        for (ProjectClass c : classes) {
            if (c.role() == Role.CONTROLLER) {
                String basePath = extractClassPath(c);
                for (ProjectMethod m : c.methods()) {
                    String httpMethod = m.httpMethod();
                    String path = m.httpPath();
                    if (httpMethod == null || httpMethod.isBlank()) continue;
                    if (path == null || path.isBlank()) continue;

                    String fullPath = combinePath(basePath, path);
                    String service = resolveService(m, byName, byRole);
                    String repository = resolveRepository(service, byName, byRole);
                    String entity = resolveEntity(repository, byName, byRole);

                    endpoints.add(ProjectEndpoint.builder()
                            .httpMethod(httpMethod)
                            .path(fullPath)
                            .controllerClass(c.fullyQualifiedName())
                            .methodName(m.name())
                            .responseDto(m.returnType())
                            .service(service)
                            .repository(repository)
                            .entity(entity)
                            .build());
                }
            }
        }

        return endpoints;
    }

    private String extractClassPath(ProjectClass controller) {
        for (String ann : controller.annotations()) {
            if (ann.equals("RestController") || ann.equals("Controller")) {
                // Check if there's a @RequestMapping at class level
                // (would be stored in a class-level annotation)
                // For now return ""
                return "";
            }
        }
        return "";
    }

    private String combinePath(String base, String methodPath) {
        if (base.isEmpty()) return methodPath;
        if (methodPath.startsWith("/")) return base + methodPath;
        return base + "/" + methodPath;
    }

    /**
     * Attempts to resolve the service used by a controller method.
     * Looks at method parameters for service-type parameters, or
     * at injected fields in the controller.
     */
    private String resolveService(ProjectMethod method,
                                  Map<String, ProjectClass> byName,
                                  Map<String, ProjectClass> byRole) {
        // Look for a service type in parameters
        for (String paramType : method.parameterTypes()) {
            String simpleName = stripPackage(paramType);
            ProjectClass svc = byRole.get(simpleName);
            if (svc != null && svc.role() == Role.SERVICE) {
                return svc.fullyQualifiedName();
            }
            // Try by name: UserService, XxxService pattern
            if (simpleName.endsWith("Service")) {
                return simpleName;
            }
        }
        return null;
    }

    /**
     * Attempts to resolve the repository used by a service.
     */
    private String resolveRepository(String serviceName,
                                     Map<String, ProjectClass> byName,
                                     Map<String, ProjectClass> byRole) {
        if (serviceName == null) return null;
        String simpleService = stripSimpleName(serviceName);
        ProjectClass svc = byName.get(serviceName);
        if (svc == null) svc = byRole.get(simpleService);
        if (svc == null) return null;

        // Look for injected repository fields in the service class
        for (var field : svc.fields()) {
            String fieldType = stripSimpleName(field.type());
            ProjectClass repo = byRole.get(fieldType);
            if (repo != null && repo.role() == Role.REPOSITORY) {
                return repo.fullyQualifiedName();
            }
            if (fieldType.endsWith("Repository")) {
                return fieldType;
            }
        }
        return null;
    }

    /**
     * Attempts to resolve the entity used by a repository.
     */
    private String resolveEntity(String repositoryName,
                                Map<String, ProjectClass> byName,
                                Map<String, ProjectClass> byRole) {
        if (repositoryName == null) return null;
        String simpleRepo = stripSimpleName(repositoryName);
        ProjectClass repo = byName.get(repositoryName);
        if (repo == null) repo = byRole.get(simpleRepo);
        if (repo == null) return null;

        for (var field : repo.fields()) {
            String fieldType = stripSimpleName(field.type());
            ProjectClass entity = byRole.get(fieldType);
            if (entity != null && entity.role() == Role.ENTITY) {
                return entity.fullyQualifiedName();
            }
            if (fieldType.endsWith("Entity") || fieldType.endsWith("Document")) {
                return fieldType;
            }
        }
        return null;
    }

    /**
     * Returns all classes with a given Spring role.
     */
    public List<ProjectClass> findByRole(Role role) {
        List<ProjectClass> result = new ArrayList<>();
        for (ProjectClass c : classes) {
            if (c.role() == role) result.add(c);
        }
        return result;
    }

    /**
     * Returns all entities (@Entity or @Document annotated classes).
     */
    public List<ProjectEntity> extractEntities() {
        List<ProjectEntity> result = new ArrayList<>();
        for (ProjectClass c : classes) {
            if (c.role() == Role.ENTITY) {
                result.add(ProjectEntity.builder()
                        .name(c.name())
                        .fullyQualifiedName(c.fullyQualifiedName())
                        .fields(c.fields())
                        .build());
            }
        }
        return result;
    }

    /**
     * Counts all Spring beans (classes annotated with @Service, @Repository,
     * @Component, @Configuration, @Bean).
     */
    public int countBeans() {
        int count = 0;
        for (ProjectClass c : classes) {
            if (c.role() != Role.NONE && c.role() != Role.ENTITY) {
                count++;
            }
            // Also count @Bean methods inside @Configuration classes
            if (c.role() == Role.CONFIGURATION) {
                count += (int) c.methods().stream()
                        .filter(m -> m.annotations().contains("Bean"))
                        .count();
            }
        }
        return count;
    }

    private Map<String, ProjectClass> buildNameMap() {
        Map<String, ProjectClass> map = new HashMap<>();
        for (ProjectClass c : classes) {
            map.put(c.fullyQualifiedName(), c);
            map.put(c.name(), c);
        }
        return map;
    }

    private String stripPackage(String fqn) {
        int idx = fqn.lastIndexOf('.');
        return idx < 0 ? fqn : fqn.substring(idx + 1);
    }

    private String stripSimpleName(String name) {
        if (name == null) return "";
        return stripPackage(name);
    }
}
