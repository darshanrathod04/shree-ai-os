package platform.kernels.chief.error;

import java.util.Objects;

/**
 * <b>ChiefException</b>
 *
 * <p>Canonical base exception for the Chief Kernel.
 * This exception wraps ChiefError and provides the foundation for all
 * Chief Kernel exceptions.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Wraps ChiefError for exception handling.</li>
 *   <li>Provides base exception for all Chief Kernel exceptions.</li>
 *   <li>Encapsulates error information.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — ChiefError is immutable.</li>
 *   <li>No business logic — pure exception wrapper.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-104, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public class ChiefException extends RuntimeException {

    private final ChiefError error;

    /**
     * Constructs a {@code ChiefException} with the specified error.
     *
     * @param error the chief error (must not be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ChiefException(ChiefError error) {
        super(Objects.requireNonNull(error, "ChiefError must not be null").message());
        this.error = error;
    }

    /**
     * Constructs a {@code ChiefException} with the specified error and cause.
     *
     * @param error the chief error (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws IllegalArgumentException if error is {@code null}
     */
    public ChiefException(ChiefError error, Throwable cause) {
        super(Objects.requireNonNull(error, "ChiefError must not be null").message(), cause);
        this.error = error;
    }

    /**
     * Returns the chief error.
     *
     * @return the chief error
     */
    public ChiefError error() {
        return error;
    }
}