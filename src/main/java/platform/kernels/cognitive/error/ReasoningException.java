package platform.kernels.cognitive.error;

/**
 * <b>ReasoningException</b>
 *
 * <p>Represents failures related to reasoning operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies reasoning operation failures</li>
 *   <li>Encapsulates CognitiveError for consistent reporting</li>
 *   <li>Preserves the original cause where applicable</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This exception classifies only. It never retries, recovers,
 * or invokes reasoning logic.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-104, EIO-ARCH-001</p>
 *
 * <p><b>Classification:</b></p>
 * <p>This exception is used for:</p>
 * <ul>
 *   <li>Reasoning operation failures</li>
 *   <li>Reasoning request processing errors</li>
 *   <li>Reasoning engine communication failures</li>
 * </ul>
 *
 * <p><b>What This Exception Does NOT Do:</b></p>
 * <ul>
 *   <li>Does not retry failed operations</li>
 *   <li>Does not recover automatically</li>
 *   <li>Does not invoke reasoning logic</li>
 *   <li>Does not evaluate reasoning quality</li>
 * </ul>
 *
 * @since 1.0
 */
public class ReasoningException extends CognitiveException {

    /**
     * Creates a new ReasoningException with the specified error.
     *
     * @param error the cognitive error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ReasoningException(CognitiveError error) {
        super(error);
    }

    /**
     * Creates a new ReasoningException with the specified error and cause.
     *
     * @param error the cognitive error (must not be {@code null})
     * @param cause the original cause (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ReasoningException(CognitiveError error, Throwable cause) {
        super(error, cause);
    }

    /**
     * Returns a string representation of this exception.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ReasoningException{" +
                "error=" + error() +
                '}';
    }
}