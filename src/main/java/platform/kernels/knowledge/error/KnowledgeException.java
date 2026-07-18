package platform.kernels.knowledge.error;

import java.util.Objects;

/**
 * <b>KnowledgeException</b>
 *
 * <p>Base exception for all Knowledge Kernel errors within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Serves as the root of the Knowledge Kernel exception hierarchy.</li>
 *   <li>Encapsulates exactly one immutable {@link KnowledgeError}.</li>
 *   <li>Extends {@link RuntimeException} for unchecked exception handling.</li>
 *   <li>Preserves exception chaining where appropriate.</li>
 *   <li>Never duplicates primitive error fields — all error data is in the KnowledgeError.</li>
 * </ul>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * RuntimeException
 *         │
 *         ▼
 *   KnowledgeException
 *         │
 *    ├── KnowledgeValidationException
 *    ├── KnowledgeGraphException
 *    ├── KnowledgeExtractionException
 *    └── KnowledgeNotFoundException
 * </pre>
 *
 * <p><b>Immutability:</b> This class is immutable. The encapsulated KnowledgeError
 * is immutable by delegation.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-104, EIO-ARCH-001</p>
 *
 * @see KnowledgeError
 * @see KnowledgeValidationException
 * @see KnowledgeGraphException
 * @see KnowledgeExtractionException
 * @see KnowledgeNotFoundException
 */
public class KnowledgeException extends RuntimeException {

    private final KnowledgeError error;

    /**
     * Creates a new KnowledgeException with the specified error.
     *
     * @param error the immutable KnowledgeError describing the failure (must not be null)
     * @throws NullPointerException if {@code error} is null
     */
    public KnowledgeException(KnowledgeError error) {
        super(error.getMessage());
        this.error = Objects.requireNonNull(error, "error must not be null");
    }

    /**
     * Creates a new KnowledgeException with the specified error and cause.
     *
     * @param error the immutable KnowledgeError describing the failure (must not be null)
     * @param cause the cause of the exception (may be null)
     * @throws NullPointerException if {@code error} is null
     */
    public KnowledgeException(KnowledgeError error, Throwable cause) {
        super(error.getMessage(), cause);
        this.error = Objects.requireNonNull(error, "error must not be null");
    }

    /**
     * Returns the immutable KnowledgeError encapsulated by this exception.
     *
     * @return the KnowledgeError (never null)
     */
    public KnowledgeError getError() {
        return error;
    }
}