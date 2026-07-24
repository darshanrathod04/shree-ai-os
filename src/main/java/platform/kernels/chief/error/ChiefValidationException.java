package platform.kernels.chief.error;

import java.util.Objects;

/**
 * <b>ChiefValidationException</b>
 *
 * <p>Represents validation failures produced by the Validation Layer.
 * This exception extends ChiefException.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents validation failures.</li>
 *   <li>Classifies validation errors.</li>
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
public class ChiefValidationException extends ChiefException {

    /**
     * Constructs a {@code ChiefValidationException} with the specified error.
     *
     * @param error the chief error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ChiefValidationException(ChiefError error) {
        super(Objects.requireNonNull(error, "ChiefError must not be null"));
    }

    /**
     * Constructs a {@code ChiefValidationException} with the specified error and cause.
     *
     * @param error the chief error (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ChiefValidationException(ChiefError error, Throwable cause) {
        super(Objects.requireNonNull(error, "ChiefError must not be null"), cause);
    }
}