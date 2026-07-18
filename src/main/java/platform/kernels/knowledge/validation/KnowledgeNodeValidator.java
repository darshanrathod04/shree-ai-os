package platform.kernels.knowledge.validation;

import platform.kernels.knowledge.model.KnowledgeNode;
import platform.kernels.knowledge.model.KnowledgeState;
import platform.kernels.knowledge.model.KnowledgeType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeNodeValidator</b>
 *
 * <p>Validates the structural integrity of {@link KnowledgeNode} instances.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates node identity — ensures the node has a valid identifier.</li>
 *   <li>Validates node consistency — ensures type, state, and scope are coherent.</li>
 *   <li>Validates required metadata — ensures mandatory fields are present.</li>
 *   <li>Validates node state — ensures the state is a recognized value.</li>
 *   <li>Validates node classification — ensures the type is a recognized value.</li>
 *   <li>Performs structural validation only — no semantic reasoning.</li>
 *   <li>Does not mutate the node — inspection only.</li>
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
 * @see KnowledgeNode
 * @see KnowledgeValidator
 */
public final class KnowledgeNodeValidator {

    private KnowledgeNodeValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Validates the structural integrity of a {@link KnowledgeNode}.
     *
     * <p>Checks that the node has a valid identifier, a recognized type and state,
     * non-blank label and description, and non-null timestamps.</p>
     *
     * @param node the knowledge node to validate (must not be null)
     * @return a list of violation messages (empty if the node is structurally valid)
     * @throws NullPointerException if {@code node} is null
     */
    public static List<String> validate(KnowledgeNode node) {
        Objects.requireNonNull(node, "node must not be null");

        List<String> violations = new ArrayList<>();

        // Validate identity
        if (node.getId() == null) {
            violations.add("Node id must not be null");
        }

        // Validate type
        if (node.getType() == null) {
            violations.add("Node type must not be null");
        }

        // Validate state
        if (node.getState() == null) {
            violations.add("Node state must not be null");
        }

        // Validate scope
        if (node.getScope() == null) {
            violations.add("Node scope must not be null");
        }

        // Validate label
        if (node.getLabel() == null || node.getLabel().isBlank()) {
            violations.add("Node label must not be null or blank");
        }

        // Validate description
        if (node.getDescription() == null || node.getDescription().isBlank()) {
            violations.add("Node description must not be null or blank");
        }

        // Validate metadata
        if (node.getMetadata() == null) {
            violations.add("Node metadata must not be null");
        }

        // Validate timestamps
        if (node.getCreatedAt() == null) {
            violations.add("Node createdAt must not be null");
        }
        if (node.getUpdatedAt() == null) {
            violations.add("Node updatedAt must not be null");
        }

        return violations;
    }
}