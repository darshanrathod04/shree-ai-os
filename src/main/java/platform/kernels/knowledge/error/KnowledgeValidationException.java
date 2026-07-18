package platform.kernels.knowledge.error;

import java.util.Objects;

/**
 * <b>KnowledgeValidationException</b>
 *
 * <p>Exception thrown when a Knowledge validation operation fails.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Indicates that a knowledge entity, relationship, graph, or request failed structural validation.</li>
 *   <li>Encapsulates exactly one immutable {@link KnowledgeError} describing the validation failure.</li>
 *   <li>Extends {@link KnowledgeException} to maintain the kernel exception hierarchy.</li>
 *   <li>Contains no business logic, recovery logic, or reasoning.</li>
 * </ul>
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
 * @see KnowledgeException
 * @see KnowledgeError
 * @see KnowledgeErrorCode
 */
public class KnowledgeValidationException extends KnowledgeException {

    /**
     * Creates a new KnowledgeValidationException with the specified error.
     *
     * @param error the immutable KnowledgeError describing the validation failure
     *              (must not be null)
     * @throws NullPointerException if {@code error} is null
     */
    public KnowledgeValidationException(KnowledgeError error) {
        super(error);
    }

    /**
     * Creates a new KnowledgeValidationException with the specified error and cause.
     *
     * @param error the immutable KnowledgeError describing the validation failure
     *              (must not be null)
     * @param cause the cause of the exception (may be null)
     * @throws NullPointerException if {@code error} is null
     */
    public KnowledgeValidationException(KnowledgeError error, Throwable cause) {
        super(error, cause);
    }
}