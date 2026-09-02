package com.shreeai.os.platform.kernels.project.analyzer;

import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Role;
import com.shreeai.os.platform.kernels.project.model.ProjectDependency;
import com.shreeai.os.platform.kernels.project.model.ProjectEndpoint;
import com.shreeai.os.platform.kernels.project.model.ProjectField;
import com.shreeai.os.platform.kernels.project.model.ProjectMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>DependencyGraphBuilder</b>
 *
 * <p>Walks a list of {@link ProjectClass} objects and produces a set of
 * {@link ProjectDependency} edges. Resolves the type references between
 * classes, fields, and methods to build the architecture graph.</p>
 *
 * <p><b>Ownership:</b> Project Intelligence (Sprint-13)</p>
 */
public final class DependencyGraphBuilder {

    private final List<ProjectClass> classes;
    private final Map<String, ProjectClass> byFqn;
    private final Map<String, ProjectClass> bySimpleName;

    public DependencyGraphBuilder(List<ProjectClass> classes) {
        this.classes = classes;
        this.byFqn = new HashMap<>();
        this.bySimpleName = new HashMap<>();
        for (ProjectClass c : classes) {
            byFqn.put(c.fullyQualifiedName(), c);
            bySimpleName.put(c.name(), c);
        }
    }

    /**
     * Builds the full set of dependency edges.
     */
    public List<ProjectDependency> buildEdges(List<ProjectEndpoint> endpoints) {
        List<ProjectDependency> edges = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (ProjectClass c : classes) {
            // EXTENDS
            if (c.superClass() != null && !c.superClass().isEmpty()) {
                String target = resolveType(c.superClass());
                if (target != null) {
                    addEdge(edges, seen, c.fullyQualifiedName(), target,
                            ProjectDependency.Type.EXTENDS, "");
                }
            }

            // IMPLEMENTS
            for (String iface : c.interfaces()) {
                String target = resolveType(iface);
                if (target != null) {
                    addEdge(edges, seen, c.fullyQualifiedName(), target,
                            ProjectDependency.Type.IMPLEMENTS, "");
                }
            }

            // Field-level dependencies
            for (ProjectField field : c.fields()) {
                String fieldType = resolveType(field.type());
                if (fieldType == null) continue;

                if (hasInjection(field)) {
                    addEdge(edges, seen, c.fullyQualifiedName(), fieldType,
                            ProjectDependency.Type.INJECTS, field.name());
                }

                // Spring role-based edges
                if (c.role() == Role.SERVICE && fieldType.endsWith("Repository")) {
                    addEdge(edges, seen, c.fullyQualifiedName(), fieldType,
                            ProjectDependency.Type.HAS_REPOSITORY, field.name());
                }
                if (c.role() == Role.REPOSITORY) {
                    ProjectClass target = byFqn.get(fieldType);
                    if (target != null && target.role() == Role.ENTITY) {
                        addEdge(edges, seen, c.fullyQualifiedName(), fieldType,
                                ProjectDependency.Type.MAPS_TO_ENTITY, field.name());
                    }
                }

                addEdge(edges, seen, c.fullyQualifiedName(), fieldType,
                        ProjectDependency.Type.DEPENDS_ON, field.name());
            }

            // Method-level dependencies
            for (ProjectMethod method : c.methods()) {
                // Return type
                String returnType = resolveType(method.returnType());
                if (returnType != null) {
                    addEdge(edges, seen, c.fullyQualifiedName(), returnType,
                            ProjectDependency.Type.RETURNS, method.name());
                }

                // Parameter types
                for (String param : method.parameterTypes()) {
                    String paramType = resolveType(param);
                    if (paramType != null) {
                        addEdge(edges, seen, c.fullyQualifiedName(), paramType,
                                ProjectDependency.Type.DEPENDS_ON,
                                method.name() + "(" + param + ")");
                    }
                }
            }
        }

        // EXPOSES edges from controller method → endpoint signature
        for (ProjectEndpoint endpoint : endpoints) {
            addEdge(edges, seen,
                    endpoint.controllerClass(),
                    endpoint.signature(),
                    ProjectDependency.Type.EXPOSES,
                    endpoint.methodName());
        }

        return edges;
    }

    private boolean hasInjection(ProjectField field) {
        for (String ann : field.annotations()) {
            if (Set.of("Autowired", "Inject", "Resource").contains(ann)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves a possibly-simple type name to a fully-qualified name
     * present in the parsed class set. Returns null if unresolvable.
     */
    private String resolveType(String type) {
        if (type == null || type.isBlank()) return null;
        // Strip generics
        int genIdx = type.indexOf('<');
        String base = genIdx < 0 ? type : type.substring(0, genIdx).trim();
        // Strip array brackets
        int arrIdx = base.indexOf('[');
        if (arrIdx >= 0) base = base.substring(0, arrIdx).trim();
        if (base.isEmpty()) return null;
        // Already FQN
        if (base.contains(".")) {
            if (byFqn.containsKey(base)) return base;
            // Try matching the simple name part
            String simple = base.substring(base.lastIndexOf('.') + 1);
            ProjectClass match = bySimpleName.get(simple);
            if (match != null) return match.fullyQualifiedName();
            return null;
        }
        ProjectClass match = bySimpleName.get(base);
        return match != null ? match.fullyQualifiedName() : null;
    }

    private void addEdge(List<ProjectDependency> edges, Set<String> seen,
                         String source, String target,
                         ProjectDependency.Type type, String context) {
        if (source == null || target == null) return;
        if (source.equals(target)) return;
        String key = source + "|" + target + "|" + type + "|" + context;
        if (seen.add(key)) {
            edges.add(ProjectDependency.builder()
                    .source(source)
                    .target(target)
                    .type(type)
                    .context(context)
                    .build());
        }
    }
}
