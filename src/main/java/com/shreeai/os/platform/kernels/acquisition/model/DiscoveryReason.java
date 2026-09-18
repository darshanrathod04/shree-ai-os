package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.Objects;

/**
 * <b>DiscoveryReason</b>
 *
 * <p>Immutable explainability record describing why one knowledge topic was
 * discovered. Every discovered topic carries at least one reason so the
 * discovery outcome is fully auditable.</p>
 *
 * <p><b>Example:</b></p>
 * <table border="1">
 *   <caption>Discovery reason examples</caption>
 *   <tr><th>topicName</th><th>evidence</th><th>sourceArtifact</th></tr>
 *   <tr><td>Java</td><td>JAVA</td><td>DomainProfile</td></tr>
 *   <tr><td>Streams</td><td>Become Java Developer</td><td>GoalStructure</td></tr>
 *   <tr><td>Roadmap</td><td>30 days</td><td>UserConstraints</td></tr>
 * </table>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.1 Source Discovery</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param topicName      the canonical name of the discovered topic (must not
 *                       be null or blank)
 * @param evidence       the exact input evidence that triggered the discovery
 *                       (must not be null or blank)
 * @param sourceArtifact the canonical artifact the evidence came from
 *                       ({@code DomainProfile}, {@code GoalStructure} or
 *                       {@code UserConstraints}; must not be null or blank)
 */
public record DiscoveryReason(
        String topicName,
        String evidence,
        String sourceArtifact) {

    /** Locked source artifact name for evidence originating from the domain profile. */
    public static final String SOURCE_DOMAIN_PROFILE = "DomainProfile";

    /** Locked source artifact name for evidence originating from the goal structure. */
    public static final String SOURCE_GOAL_STRUCTURE = "GoalStructure";

    /** Locked source artifact name for evidence originating from the user constraints. */
    public static final String SOURCE_USER_CONSTRAINTS = "UserConstraints";

    /**
     * Creates a new DiscoveryReason with validation.
     *
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if any parameter is blank
     */
    public DiscoveryReason {
        Objects.requireNonNull(topicName, "topicName must not be null");
        Objects.requireNonNull(evidence, "evidence must not be null");
        Objects.requireNonNull(sourceArtifact, "sourceArtifact must not be null");
        if (topicName.isBlank()) {
            throw new IllegalArgumentException("topicName must not be blank");
        }
        if (evidence.isBlank()) {
            throw new IllegalArgumentException("evidence must not be blank");
        }
        if (sourceArtifact.isBlank()) {
            throw new IllegalArgumentException("sourceArtifact must not be blank");
        }
    }

    @Override
    public String toString() {
        return "DiscoveryReason{topic='" + topicName
                + "', evidence='" + evidence
                + "', source=" + sourceArtifact + "}";
    }
}
