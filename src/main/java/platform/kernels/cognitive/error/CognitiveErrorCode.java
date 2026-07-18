package platform.kernels.cognitive.error;

/**
 * <b>CognitiveErrorCode</b>
 *
 * <p>Platform-standard error classification for Cognitive Kernel failures.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies cognitive failures into stable categories</li>
 *   <li>Provides framework-independent error identifiers</li>
 *   <li>Maintains no behavior - pure classification only</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an enum with no behavior. Each constant represents
 * a distinct category of cognitive failure.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-104, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public enum CognitiveErrorCode {

    /**
     * Validation failed - structural or construction invariant violation.
     */
    VALIDATION_ERROR,

    /**
     * Reasoning operation failed.
     */
    REASONING_ERROR,

    /**
     * Decision support operation failed.
     */
    DECISION_ERROR,

    /**
     * Reflective analysis operation failed.
     */
    REFLECTION_ERROR,

    /**
     * Cognitive state management operation failed.
     */
    COGNITIVE_STATE_ERROR,

    /**
     * Invalid request structure or content.
     */
    INVALID_REQUEST,

    /**
     * Invalid configuration or setup.
     */
    INVALID_CONFIGURATION,

    /**
     * Internal system error.
     */
    INTERNAL_ERROR
}