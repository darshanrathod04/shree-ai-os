package platform.kernels.multiagent.engine;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>MultiAgentProcessingResult</b>
 *
 * <p>Immutable value object representing the result of deterministic engine processing
 * in the Multi-Agent Kernel.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-106, EIO-ARCH-001</p>
 *
 * <p>MultiAgentProcessingResult represents the processing outcome of a deterministic
 * evaluation by the Engine layer. It does NOT represent persistent registration state,
 * network delivery receipts, agent execution results, or transport acknowledgements.</p>
 *
 * <p>This class follows the same immutability pattern as {@code ChiefProcessingResult}
 * in the Chief Kernel's engine layer.</p>
 *
 * @param succeeded    whether the processing succeeded
 * @param outcome      a descriptive outcome of the processing (must not be {@code null})
 * @param processedAt  when the processing was completed (must not be {@code null})
 * @param metadata     additional processing metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class MultiAgentProcessingResult {
    private final boolean succeeded;
    private final String outcome;
    private final Instant processedAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new MultiAgentProcessingResult with the specified parameters.
     *
     * @param succeeded   whether the processing succeeded
     * @param outcome     a descriptive outcome of the processing (must not be {@code null} or blank)
     * @param processedAt when the processing was completed (must not be {@code null})
     * @param metadata    additional processing metadata (must not be {@code null})
     * @throws NullPointerException     if outcome, processedAt, or metadata is {@code null}
     * @throws IllegalArgumentException if outcome is blank
     * @since 1.0
     */
    public MultiAgentProcessingResult(
            boolean succeeded,
            String outcome,
            Instant processedAt,
            Map<String, Object> metadata) {
        this.succeeded = succeeded;
        this.outcome = validateOutcome(outcome);
        this.processedAt = Objects.requireNonNull(processedAt, "MultiAgentProcessingResult processedAt must not be null");
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "MultiAgentProcessingResult metadata must not be null"));
    }

    private static String validateOutcome(String outcome) {
        Objects.requireNonNull(outcome, "MultiAgentProcessingResult outcome must not be null");
        if (outcome.isBlank()) {
            throw new IllegalArgumentException("MultiAgentProcessingResult outcome must not be blank");
        }
        return outcome;
    }

    /**
     * Returns whether the processing succeeded.
     *
     * @return {@code true} if processing succeeded
     * @since 1.0
     */
    public boolean succeeded() {
        return succeeded;
    }

    /**
     * Returns a descriptive outcome of the processing.
     *
     * @return the processing outcome
     * @since 1.0
     */
    public String outcome() {
        return outcome;
    }

    /**
     * Returns when the processing was completed.
     *
     * @return the processing timestamp
     * @since 1.0
     */
    public Instant processedAt() {
        return processedAt;
    }

    /**
     * Returns additional processing metadata.
     *
     * @return an unmodifiable view of the processing metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the obj argument
     * @since 1.0
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MultiAgentProcessingResult that = (MultiAgentProcessingResult) obj;
        return succeeded == that.succeeded &&
               outcome.equals(that.outcome) &&
               processedAt.equals(that.processedAt) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the MultiAgentProcessingResult.
     *
     * @return a hash code value
     * @since 1.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(succeeded, outcome, processedAt, metadata);
    }

    /**
     * Returns a string representation of the MultiAgentProcessingResult.
     *
     * @return a string representation
     * @since 1.0
     */
    @Override
    public String toString() {
        return "MultiAgentProcessingResult{" +
                "succeeded=" + succeeded +
                ", outcome='" + outcome + '\'' +
                ", processedAt=" + processedAt +
                ", metadata=" + metadata +
                '}';
    }
}