package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.Objects;

/**
 * <b>KnowledgeTopic</b>
 *
 * <p>One immutable, deterministically-discovered knowledge requirement topic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Names one piece of knowledge the OS must acquire for this request.</li>
 *   <li>Carries the locked priority assigned by the discovery pipeline.</li>
 *   <li>Carries a deterministic confidence within {@code [0.0, 1.0]}.</li>
 *   <li>Identified by a deterministic SHA-256 {@code topicId} derived from the
 *       canonical topic name ({@code SHA-256("TOPIC|" + name)}).</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.1 Source Discovery</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param topicId    the deterministic SHA-256 identifier (must not be null or blank)
 * @param name       the canonical topic name (must not be null or blank)
 * @param priority   the locked requirement priority (must not be null)
 * @param confidence the deterministic confidence within {@code [0.0, 1.0]}
 */
public record KnowledgeTopic(
        String topicId,
        String name,
        RequirementPriority priority,
        double confidence) {

    /**
     * Creates a new KnowledgeTopic with validation.
     *
     * @throws NullPointerException     if topicId, name or priority is null
     * @throws IllegalArgumentException if topicId or name is blank, or
     *                                  confidence is outside {@code [0.0, 1.0]}
     */
    public KnowledgeTopic {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        if (topicId.isBlank()) {
            throw new IllegalArgumentException("topicId must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be within [0.0, 1.0]: " + confidence);
        }
    }

    /**
     * Computes the deterministic topic identifier for a canonical topic name:
     * {@code SHA-256("TOPIC|" + name)} as lowercase hex.
     *
     * <p>This is the single locked topicId derivation rule for the entire
     * kernel - identical names always produce identical identifiers on every
     * machine and every run.</p>
     *
     * @param name the canonical topic name (must not be null)
     * @return the deterministic lowercase-hex SHA-256 topic identifier
     */
    public static String deterministicTopicId(String name) {
        Objects.requireNonNull(name, "name must not be null");
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    ("TOPIC|" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }

    @Override
    public String toString() {
        return String.format("KnowledgeTopic{name='%s', priority=%s, confidence=%.4f}",
                name, priority, confidence);
    }
}
