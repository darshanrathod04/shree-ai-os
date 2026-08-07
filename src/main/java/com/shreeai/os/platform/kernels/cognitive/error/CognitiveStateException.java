package com.shreeai.os.platform.kernels.cognitive.error;

/**
 * <b>CognitiveStateException</b>
 *
 * <p>Represents failures associated with cognitive state management.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies cognitive state management failures</li>
 *   <li>Encapsulates CognitiveError for consistent reporting</li>
 *   <li>Preserves the original cause where applicable</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This exception classifies only. It never modifies cognitive state,
 * evaluates state correctness, or performs state transitions.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-104, EIO-ARCH-001</p>
 *
 * <p><b>Classification:</b></p>
 * <p>This exception is used for:</p>
 * <ul>
 *   <li>Cognitive state transition errors</li>
 *   <li>State management operation failures</li>
 *   <li>State persistence communication failures</li>
 * </ul>
 *
 * <p><b>What This Exception Does NOT Do:</b></p>
 * <ul>
 *   <li>Does not modify cognitive state</li>
 *   <li>Does not evaluate state correctness</li>
 *   <li>Does not perform state transitions</li>
 *   <li>Does not assess cognitive performance</li>
 * </ul>
 *
 * @since 1.0
 */
public class CognitiveStateException extends CognitiveException {

    /**
     * Creates a new CognitiveStateException with the specified error.
     *
     * @param error the cognitive error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public CognitiveStateException(CognitiveError error) {
        super(error);
    }

    /**
     * Creates a new CognitiveStateException with the specified error and cause.
     *
     * @param error the cognitive error (must not be {@code null})
     * @param cause the original cause (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public CognitiveStateException(CognitiveError error, Throwable cause) {
        super(error, cause);
    }

    /**
     * Returns a string representation of this exception.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "CognitiveStateException{" +
                "error=" + error() +
                '}';
    }
}