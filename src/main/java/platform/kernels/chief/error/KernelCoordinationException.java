package platform.kernels.chief.error;

import java.util.Objects;

/**
 * <b>KernelCoordinationException</b>
 *
 * <p>Represents failures related to orchestration, coordination,
 * and dependency graph. This exception extends ChiefException.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents kernel coordination failures.</li>
 *   <li>Classifies coordination orchestration errors.</li>
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
public class KernelCoordinationException extends ChiefException {

    /**
     * Constructs a {@code KernelCoordinationException} with the specified error.
     *
     * @param error the chief error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public KernelCoordinationException(ChiefError error) {
        super(Objects.requireNonNull(error, "ChiefError must not be null"));
    }

    /**
     * Constructs a {@code KernelCoordinationException} with the specified error and cause.
     *
     * @param error the chief error (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public KernelCoordinationException(ChiefError error, Throwable cause) {
        super(Objects.requireNonNull(error, "ChiefError must not be null"), cause);
    }
}