package platform.kernels.knowledge.validation;

import platform.kernels.knowledge.model.KnowledgeConcept;
import platform.kernels.knowledge.model.KnowledgeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>KnowledgeConceptValidator</b>
 *
 * <p>Validates the structural integrity of {@link KnowledgeConcept} instances.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates concept structure — ensures the underlying node is valid.</li>
 *   <li>Validates concept metadata — ensures canonical name, synonyms, and domain are present.</li>
 *   <li>Validates concept classification — ensures concept-specific fields are structurally sound.</li>
 *   <li>Validates required semantic fields — ensures mandatory concept fields are present.</li>
 *   <li>Performs structural validation only — no inference or truth evaluation.</li>
 *   <li>Does not mutate the concept — inspection only.</li>
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
 * @see KnowledgeConcept
 * @see KnowledgeNodeValidator
 * @see KnowledgeValidator
 */
public final class KnowledgeConceptValidator {

    private KnowledgeConceptValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Validates the structural integrity of a {@link KnowledgeConcept}.
     *
     * <p>Checks that the concept has a valid underlying node, a non-blank canonical name,
     * non-null synonyms, a non-blank domain, and non-null concept metadata.</p>
     *
     * @param concept the knowledge concept to validate (must not be null)
     * @return a list of violation messages (empty if the concept is structurally valid)
     * @throws NullPointerException if {@code concept} is null
     */
    public static List<String> validate(KnowledgeConcept concept) {
        Objects.requireNonNull(concept, "concept must not be null");

        List<String> violations = new ArrayList<>();

        // Validate underlying node
        KnowledgeNode node = concept.getNode();
        if (node == null) {
            violations.add("Concept node must not be null");
        } else {
            violations.addAll(KnowledgeNodeValidator.validate(node));
        }

        // Validate canonical name
        String canonicalName = concept.getCanonicalName();
        if (canonicalName == null || canonicalName.isBlank()) {
            violations.add("Concept canonicalName must not be null or blank");
        }

        // Validate synonyms
        String[] synonyms = concept.getSynonyms();
        if (synonyms == null) {
            violations.add("Concept synonyms must not be null");
        }

        // Validate domain
        String domain = concept.getDomain();
        if (domain == null || domain.isBlank()) {
            violations.add("Concept domain must not be null or blank");
        }

        // Validate concept metadata
        if (concept.getConceptMetadata() == null) {
            violations.add("Concept conceptMetadata must not be null");
        }

        return violations;
    }
}