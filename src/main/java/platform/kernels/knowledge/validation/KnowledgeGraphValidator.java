package platform.kernels.knowledge.validation;

import platform.kernels.knowledge.model.KnowledgeGraph;
import platform.kernels.knowledge.model.KnowledgeId;
import platform.kernels.knowledge.model.KnowledgeNode;
import platform.kernels.knowledge.model.KnowledgeRelationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <b>KnowledgeGraphValidator</b>
 *
 * <p>Validates the structural integrity of {@link KnowledgeGraph} instances.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates graph integrity — ensures the graph structure is internally consistent.</li>
 *   <li>Validates node collection consistency — ensures all nodes are structurally valid.</li>
 *   <li>Validates relationship collection consistency — ensures all relationships are structurally valid.</li>
 *   <li>Validates graph invariants — ensures the graph satisfies structural invariants.</li>
 *   <li>Detects duplicate identifiers — ensures no duplicate node or relationship IDs.</li>
 *   <li>Detects orphan relationships — ensures all relationship endpoints reference known nodes.</li>
 *   <li>Inspects graph structure only — no graph mutation.</li>
 *   <li>Does not perform graph optimization or algorithm execution.</li>
 * </ul>
 *
 * <p><b>Validator Rules:</b></p>
 * <ul>
 *   <li>Static methods only</li>
 *   <li>Stateless</li>
 *   <li>Pure validation</li>
 *   <li>Thread-safe</li>
 *   <li>Deterministic</li>
 *   <li>No business logic</li>
 *   <li>No side effects</li>
 *   <li>No persistence</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-103, EIO-ARCH-001</p>
 *
 * @see KnowledgeGraph
 * @see KnowledgeValidator
 */
public final class KnowledgeGraphValidator {

    private KnowledgeGraphValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Validates the structural integrity of a {@link KnowledgeGraph}.
     *
     * <p>Checks that the graph has non-null node and relationship collections, all
     * nodes and relationships are individually valid, there are no duplicate identifiers,
     * and no relationships reference unknown node identifiers.</p>
     *
     * @param graph the knowledge graph to validate (must not be null)
     * @return a list of violation messages (empty if the graph is structurally valid)
     * @throws NullPointerException if {@code graph} is null
     */
    public static List<String> validate(KnowledgeGraph graph) {
        Objects.requireNonNull(graph, "graph must not be null");

        List<String> violations = new ArrayList<>();

        List<KnowledgeNode> nodes = graph.getNodes();
        List<KnowledgeRelationship> relationships = graph.getRelationships();

        // Validate node and relationship collections
        if (nodes == null) {
            violations.add("Graph nodes collection must not be null");
        }
        if (relationships == null) {
            violations.add("Graph relationships collection must not be null");
        }

        if (nodes == null || relationships == null) {
            return violations;
        }

        // Validate each node and check for duplicate identifiers
        Set<KnowledgeId> nodeIds = new HashSet<>();
        for (KnowledgeNode node : nodes) {
            if (node != null) {
                violations.addAll(KnowledgeNodeValidator.validate(node));
                if (node.getId() != null && !nodeIds.add(node.getId())) {
                    violations.add("Duplicate node id: " + node.getId());
                }
            } else {
                violations.add("Graph contains null node");
            }
        }

        // Validate each relationship and check for duplicate identifiers
        Set<KnowledgeId> relationshipIds = new HashSet<>();
        for (KnowledgeRelationship relationship : relationships) {
            if (relationship != null) {
                violations.addAll(KnowledgeRelationshipValidator.validate(relationship));

                // Check for duplicate relationship identifiers
                if (relationship.getId() != null && !relationshipIds.add(relationship.getId())) {
                    violations.add("Duplicate relationship id: " + relationship.getId());
                }

                // Check for orphan relationships (source node not in graph)
                if (relationship.getSourceNodeId() != null && !nodeIds.contains(relationship.getSourceNodeId())) {
                    violations.add("Orphan relationship source: " + relationship.getSourceNodeId()
                            + " not found in graph nodes");
                }

                // Check for orphan relationships (target node not in graph)
                if (relationship.getTargetNodeId() != null && !nodeIds.contains(relationship.getTargetNodeId())) {
                    violations.add("Orphan relationship target: " + relationship.getTargetNodeId()
                            + " not found in graph nodes");
                }
            } else {
                violations.add("Graph contains null relationship");
            }
        }

        return violations;
    }
}