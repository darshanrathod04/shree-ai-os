package com.shreeai.os.platform.kernels.cognitive.error;

/**
 * <b>ReflectionException</b>
 *
 * <p>Represents failures related to reflective analysis operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies reflective analysis operation failures</li>
 *   <li>Encapsulates CognitiveError for consistent reporting</li>
 *   <li>Preserves the original cause where applicable</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This exception classifies only. It never performs reflection,
 * evaluates reflection outcomes, or assesses reflection quality.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-104, EIO-ARCH-001</p>
 *
 * <p><b>Classification:</b></p>
 * <p>This exception is used for:</p>
 * <ul>
 *   <li>Reflection scope processing errors</li>
 *   <li>Reflective analysis failures</li>
 *   <li>Reflection service communication failures</li>
 * </ul>
 *
 * <p><b>What This Exception Does NOT Do:</b></p>
 * <ul>
 *   <li>Does not perform reflective analysis</li>
 *   <li>Does not evaluate reflection outcomes</li>
 *   <li>Does not assess reflection quality</li>
 *   <li>Does not modify cognitive state</li>
 * </ul>
 *
 * @since 1.0
 */
public class ReflectionException extends CognitiveException {

    /**
     * Creates a new ReflectionException with the specified error.
     *
     * @param error the cognitive error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ReflectionException(CognitiveError error) {
        super(error);
    }

    /**
     * Creates a new ReflectionException with the specified error and cause.
     *
     * @param error the cognitive error (must not be {@code null})
     * @param cause the original cause (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ReflectionException(CognitiveError error, Throwable cause) {
        super(error, cause);
    }

    /**
     * Returns a string representation of this exception.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ReflectionException{" +
                "error=" + error() +
                '}';
    }
}