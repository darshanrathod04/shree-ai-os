package platform.kernels.chief.error;

import java.util.Objects;

/**
 * <b>DecisionException</b>
 *
 * <p>Represents failures related to decision context, decision execution,
 * and decision validation. This exception extends ChiefException.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents decision-related failures.</li>
 *   <li>Classifies decision orchestration errors.</li>
 *   <li>No additional behavior.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-104, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public class DecisionException extends ChiefException {

    /**
     * Constructs a {@code DecisionException} with the specified error.
     *
     * @param error the chief error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public DecisionException(ChiefError error) {
        super(Objects.requireNonNull(error, "ChiefError must not be null"));
    }

    /**
     * Constructs a {@code DecisionException} with the specified error and cause.
     *
     * @param error the chief error (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public DecisionException(ChiefError error, Throwable cause) {
        super(Objects.requireNonNull(error, "ChiefError must not be null"), cause);
    }
}