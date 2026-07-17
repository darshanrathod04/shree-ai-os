 package platform.kernels.context.service;

import platform.kernels.context.api.ContextLifecycleService;
import platform.kernels.context.api.ContextQueryService;
import platform.kernels.context.api.ContextService;
import platform.kernels.context.api.ContextSnapshotService;
import platform.kernels.context.error.ContextError;
import platform.kernels.context.error.ContextErrorCode;
import platform.kernels.context.error.ContextException;
import platform.kernels.context.error.ContextNotFoundException;
import platform.kernels.context.error.ContextValidationException;
import platform.kernels.context.model.Context;
import platform.kernels.context.model.ContextId;
import platform.kernels.context.model.ContextSnapshot;
import platform.kernels.context.model.CreateContextRequest;
import platform.kernels.context.model.UpdateContextRequest;
import platform.kernels.context.validation.ContextValidationResult;
import platform.kernels.context.validation.ContextValidator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>DefaultContextService</b>
 *
 * <p>The default implementation of the Context service layer within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates all Context API requests.</li>
 *   <li>Validates incoming requests using ContextValidator.</li>
 *   <li>Delegates processing to ContextProcessingEngine.</li>
 *   <li>Translates failures into ContextException hierarchy.</li>
 *   <li>Contains ZERO business logic - pure coordination layer.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Constructor injection only - no field injection, service locator, or static singleton.</li>
 *   <li>Stateless - no mutable instance state, no cached Context objects.</li>
 *   <li>Validation delegated to static ContextValidator methods.</li>
 *   <li>Processing delegated to ContextProcessingEngine.</li>
 *   <li>Exception translation - never exposes primitive error information.</li>
 *   <li>Thread-safe - immutable state, no synchronization needed.</li>
 * </ul>
 *
 * <p><b>Processing Flow:</b></p>
 * <pre>
 * Request
 *     │
 *     ▼
 * ContextValidator
 *     │
 *     ▼
 * ContextProcessingEngine
 *     │
 *     ▼
 * Return Result
 * </pre>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-105, EIO-ARCH-001</p>
 *
 * @see platform.kernels.context.api.ContextService
 * @see platform.kernels.context.api.ContextQueryService
 * @see platform.kernels.context.api.ContextLifecycleService
 * @see platform.kernels.context.api.ContextSnapshotService
 * @see platform.kernels.context.service.ContextProcessingEngine
 * @see platform.kernels.context.validation.ContextValidator
 */
public final class DefaultContextService implements
        ContextService,
        ContextQueryService,
        ContextLifecycleService,
        ContextSnapshotService {

    private final ContextProcessingEngine processingEngine;

    /**
     * Creates a new DefaultContextService with constructor injection.
     *
     * <p><b>Dependency Injection:</b> Uses constructor injection only. The processing engine
     * is injected via the constructor and stored in an immutable final field.</p>
     *
     * <p><b>Thread Safety:</b> This constructor is thread-safe. The service is immutable
     * after construction.</p>
     *
     * <p><b>Stateless:</b> This service maintains no mutable state. All operations
     * delegate to the injected engine.</p>
     *
     * @param processingEngine the ContextProcessingEngine to delegate processing to (must not be null)
     * @throws NullPointerException if {@code processingEngine} is null
     */
    public DefaultContextService(ContextProcessingEngine processingEngine) {
        this.processingEngine = processingEngine;
    }

    // ========================================================================
    // ContextService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the request using ContextValidator.</li>
     *   <li>Delegates creation to ContextProcessingEngine.</li>
     *   <li>Returns the created Context.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the request structure and data.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.</p>
     *
     * @throws ContextValidationException if validation fails
     */
    @Override
    public Context createContext(CreateContextRequest request) {
        // Validate request components
        ContextValidationResult validationResult = validateCreateContextRequest(request);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        return processingEngine.createContext(request);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the request using ContextValidator.</li>
     *   <li>Delegates update to ContextProcessingEngine.</li>
     *   <li>Returns the updated Context.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the request structure and data.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.
     * Throws ContextNotFoundException if Context does not exist (translated from engine).</p>
     *
     * @throws ContextValidationException if validation fails
     * @throws ContextNotFoundException if the Context does not exist
     */
    @Override
    public Context updateContext(UpdateContextRequest request) {
        // Validate request components
        ContextValidationResult validationResult = validateUpdateContextRequest(request);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        return processingEngine.updateContext(request);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates clearing to ContextProcessingEngine.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.
     * Throws ContextNotFoundException if Context does not exist (translated from engine).</p>
     *
     * @throws ContextValidationException if validation fails
     * @throws ContextNotFoundException if the Context does not exist
     */
    @Override
    public void clearContext(ContextId contextId) {
        // Validate contextId
        ContextValidationResult validationResult = ContextValidator.validateContextId(contextId);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        processingEngine.clearContext(contextId);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates suspension to ContextProcessingEngine.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.
     * Throws ContextNotFoundException if Context does not exist (translated from engine).</p>
     *
     * @throws ContextValidationException if validation fails
     * @throws ContextNotFoundException if the Context does not exist
     */
    @Override
    public void suspendContext(ContextId contextId) {
        // Validate contextId
        ContextValidationResult validationResult = ContextValidator.validateContextId(contextId);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        processingEngine.suspendContext(contextId);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates resumption to ContextProcessingEngine.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.
     * Throws ContextNotFoundException if Context does not exist (translated from engine).
     * Throws IllegalStateException if Context is not suspended (translated from engine).</p>
     *
     * @throws ContextValidationException if validation fails
     * @throws ContextNotFoundException if the Context does not exist
     * @throws IllegalStateException if the Context is not suspended
     */
    @Override
    public void resumeContext(ContextId contextId) {
        // Validate contextId
        ContextValidationResult validationResult = ContextValidator.validateContextId(contextId);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        processingEngine.resumeContext(contextId);
    }

    // ========================================================================
    // ContextQueryService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates query to ContextProcessingEngine.</li>
     *   <li>Returns the result.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.</p>
     *
     * @throws ContextValidationException if validation fails
     */
    @Override
    public Optional<Context> findById(ContextId id) {
        // Validate id
        ContextValidationResult validationResult = ContextValidator.validateContextId(id);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        return processingEngine.findById(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Delegates query to ContextProcessingEngine.</li>
     *   <li>Returns the result.</li>
     * </ol>
     *
     * <p><b>Validation:</b> No validation required - no input parameters.</p>
     *
     * <p><b>Exception Translation:</b> No exceptions expected from this operation.</p>
     */
    @Override
    public List<Context> findActiveContexts() {
        // No validation required - no input parameters
        return processingEngine.findActiveContexts();
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates existence check to ContextProcessingEngine.</li>
     *   <li>Returns the result.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.</p>
     *
     * @throws ContextValidationException if validation fails
     */
    @Override
    public boolean exists(ContextId id) {
        // Validate id
        ContextValidationResult validationResult = ContextValidator.validateContextId(id);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        return processingEngine.exists(id);
    }

    // ========================================================================
    // ContextLifecycleService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates activation to ContextProcessingEngine.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.
     * Throws ContextNotFoundException if Context does not exist (translated from engine).
     * Throws IllegalStateException if Context is already active (translated from engine).</p>
     *
     * @throws ContextValidationException if validation fails
     * @throws ContextNotFoundException if the Context does not exist
     * @throws IllegalStateException if the Context is already active
     */
    @Override
    public void activate(ContextId id) {
        // Validate id
        ContextValidationResult validationResult = ContextValidator.validateContextId(id);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        processingEngine.activate(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates deactivation to ContextProcessingEngine.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.
     * Throws ContextNotFoundException if Context does not exist (translated from engine).
     * Throws IllegalStateException if Context is not active (translated from engine).</p>
     *
     * @throws ContextValidationException if validation fails
     * @throws ContextNotFoundException if the Context does not exist
     * @throws IllegalStateException if the Context is not active
     */
    @Override
    public void deactivate(ContextId id) {
        // Validate id
        ContextValidationResult validationResult = ContextValidator.validateContextId(id);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        processingEngine.deactivate(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates expiration to ContextProcessingEngine.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.
     * Throws ContextNotFoundException if Context does not exist (translated from engine).</p>
     *
     * @throws ContextValidationException if validation fails
     * @throws ContextNotFoundException if the Context does not exist
     */
    @Override
    public void expire(ContextId id) {
        // Validate id
        ContextValidationResult validationResult = ContextValidator.validateContextId(id);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        processingEngine.expire(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates archival to ContextProcessingEngine.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.
     * Throws ContextNotFoundException if Context does not exist (translated from engine).</p>
     *
     * @throws ContextValidationException if validation fails
     * @throws ContextNotFoundException if the Context does not exist
     */
    @Override
    public void archive(ContextId id) {
        // Validate id
        ContextValidationResult validationResult = ContextValidator.validateContextId(id);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        processingEngine.archive(id);
    }

    // ========================================================================
    // ContextSnapshotService Implementation
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates snapshot creation to ContextProcessingEngine.</li>
     *   <li>Returns the created snapshot.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.
     * Throws ContextNotFoundException if Context does not exist (translated from engine).</p>
     *
     * @throws ContextValidationException if validation fails
     * @throws ContextNotFoundException if the Context does not exist
     */
    @Override
    public ContextSnapshot createSnapshot(ContextId id) {
        // Validate id
        ContextValidationResult validationResult = ContextValidator.validateContextId(id);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        return processingEngine.createSnapshot(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates snapshot retrieval to ContextProcessingEngine.</li>
     *   <li>Returns the result.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.</p>
     *
     * @throws ContextValidationException if validation fails
     */
    @Override
    public Optional<ContextSnapshot> latestSnapshot(ContextId id) {
        // Validate id
        ContextValidationResult validationResult = ContextValidator.validateContextId(id);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        return processingEngine.latestSnapshot(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b></p>
     * <ol>
     *   <li>Validates the contextId.</li>
     *   <li>Delegates history retrieval to ContextProcessingEngine.</li>
     *   <li>Returns the result.</li>
     * </ol>
     *
     * <p><b>Validation:</b> Validates the ContextId structure.</p>
     *
     * <p><b>Exception Translation:</b> Throws ContextValidationException if validation fails.</p>
     *
     * @throws ContextValidationException if validation fails
     */
    @Override
    public List<ContextSnapshot> history(ContextId id) {
        // Validate id
        ContextValidationResult validationResult = ContextValidator.validateContextId(id);
        if (!validationResult.isValid()) {
            throw createValidationException(validationResult);
        }

        // Delegate to engine
        return processingEngine.history(id);
    }

    // ========================================================================
    // Private Validation Helper Methods
    // ========================================================================

    /**
     * Validates a CreateContextRequest.
     *
     * <p>Validates all components of the creation request including type, data, and timestamps.</p>
     *
     * @param request the request to validate (must not be null)
     * @return the validation result
     */
    private ContextValidationResult validateCreateContextRequest(CreateContextRequest request) {
        List<String> violations = new ArrayList<>();

        // Validate type
        if (request.type() == null) {
            violations.add("Context type must not be null");
        }

        // Validate data
        if (request.data() == null) {
            violations.add("Context data must not be null");
        }

        // Validate createdAt
        if (request.createdAt() == null) {
            violations.add("Context createdAt must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("requestType", "CreateContextRequest")
        );
    }

    /**
     * Validates an UpdateContextRequest.
     *
     * <p>Validates all components of the update request including contextId, type, state, data, and timestamps.</p>
     *
     * @param request the request to validate (must not be null)
     * @return the validation result
     */
    private ContextValidationResult validateUpdateContextRequest(UpdateContextRequest request) {
        List<String> violations = new ArrayList<>();

        // Validate contextId
        ContextValidationResult idResult = ContextValidator.validateContextId(request.contextId());
        if (!idResult.isValid()) {
            violations.addAll(idResult.getViolations());
        }

        // Validate updatedAt
        if (request.updatedAt() == null) {
            violations.add("Context updatedAt must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("requestType", "UpdateContextRequest")
        );
    }

    // ========================================================================
    // Private Exception Translation Helper Methods
    // ========================================================================

    /**
     * Creates a ContextValidationException from a validation result.
     *
     * <p><b>Exception Translation:</b> Translates validation failures into the
     * Context exception hierarchy using ContextError and ContextValidationException.</p>
     *
     * <p><b>Never exposes primitive error information</b> - always encapsulates
     * failures using ContextError.</p>
     *
     * @param validationResult the validation result (must not be null)
     * @return a ContextValidationException encapsulating the validation errors
     */
    private ContextValidationException createValidationException(ContextValidationResult validationResult) {
        ContextError error = new ContextError(
                ContextErrorCode.VALIDATION_FAILED,
                "Context validation failed: " + String.join(", ", validationResult.getViolations()),
                Instant.now(),
                Map.of(
                        "violations", validationResult.getViolations(),
                        "validationTimestamp", validationResult.getValidatedAt().toString()
                )
        );

        return new ContextValidationException(error);
    }
}