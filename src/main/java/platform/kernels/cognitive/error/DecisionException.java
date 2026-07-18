package platform.kernels.cognitive.error;

/**
 * <b>DecisionException</b>
 *
 * <p>Represents failures related to decision support operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies decision support operation failures</li>
 *   <li>Encapsulates CognitiveError for consistent reporting</li>
 *   <li>Preserves the original cause where applicable</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This exception classifies only. It never contains decision logic,
 * evaluates decision quality, or ranks alternatives.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-104, EIO-ARCH-001</p>
 *
 * <p><b>Classification:</b></p>
 * <p>This exception is used for:</p>
 * <ul>
 *   <li>Decision context processing errors</li>
 *   <li>Decision evaluation failures</li>
 *   <li>Decision support service communication failures</li>
 * </ul>
 *
 * <p><b>What This Exception Does NOT Do:</b></p>
 * <ul>
 *   <li>Does not compare alternatives</li>
 *   <li>Does not evaluate decision quality</li>
 *   <li>Does not rank or score decisions</li>
 *   <li>Does not contain decision logic</li>
 * </ul>
 *
 * @since 1.0
 */
public class DecisionException extends CognitiveException {

    /**
     * Creates a new DecisionException with the specified error.
     *
     * @param error the cognitive error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public DecisionException(CognitiveError error) {
        super(error);
    }

    /**
     * Creates a new DecisionException with the specified error and cause.
     *
     * @param error the cognitive error (must not be {@code null})
     * @param cause the original cause (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public DecisionException(CognitiveError error, Throwable cause) {
        super(error, cause);
    }

    /**
     * Returns a string representation of this exception.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "DecisionException{" +
                "error=" + error() +
                '}';
    }
}