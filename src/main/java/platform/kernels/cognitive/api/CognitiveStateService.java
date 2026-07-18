package platform.kernels.cognitive.api;

/**
 * <b>CognitiveStateService</b>
 *
 * <p>Defines contracts for cognitive state management, including cognitive state
 * retrieval, state transitions, attention management, focus management,
 * and reasoning lifecycle.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for cognitive state retrieval.</li>
 *   <li>Specifies state transition contracts.</li>
 *   <li>Defines attention management contracts.</li>
 *   <li>Specifies focus management contracts.</li>
 *   <li>Defines reasoning lifecycle contracts.</li>
 *   <li>Contains no state implementation — contracts only.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Business-focused — exposes only business-level contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-101, EIO-ARCH-001</p>
 *
 * @see CognitiveService
 * @see ReasoningService
 * @see DecisionService
 * @see ReflectionService
 */
public interface CognitiveStateService {

    /**
     * Retrieves the current cognitive state.
     *
     * <p>This operation returns the current state of cognitive processes including
     * attention, focus, and reasoning lifecycle.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param stateQuery the state query parameters (must not be {@code null})
     * @return the current cognitive state
     * @throws IllegalArgumentException if stateQuery is {@code null}
     */
    CognitiveState getState(StateQuery stateQuery);

    /**
     * Transitions the cognitive state.
     *
     * <p>This operation initiates a state transition from the current cognitive
     * state to a new state.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param stateTransition the state transition to perform (must not be {@code null})
     * @return a state transition result identifier
     * @throws IllegalArgumentException if stateTransition is {@code null}
     */
    String transitionState(StateTransition stateTransition);

    /**
     * Manages attention allocation.
     *
     * <p>This operation controls how cognitive resources are allocated across
     * different tasks and knowledge areas.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param attentionManagement the attention management parameters (must not be {@code null})
     * @return an attention management result identifier
     * @throws IllegalArgumentException if attentionManagement is {@code null}
     */
    String manageAttention(AttentionManagement attentionManagement);

    /**
     * Manages cognitive focus.
     *
     * <p>This operation controls the focus of cognitive processes, directing
     * attention to specific knowledge areas or reasoning tasks.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param focusManagement the focus management parameters (must not be {@code null})
     * @return a focus management result identifier
     * @throws IllegalArgumentException if focusManagement is {@code null}
     */
    String manageFocus(FocusManagement focusManagement);

    /**
     * Manages the reasoning lifecycle.
     *
     * <p>This operation controls the lifecycle of reasoning processes, including
     * initiation, suspension, resumption, and termination.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param lifecycleManagement the lifecycle management parameters (must not be {@code null})
     * @return a lifecycle management result identifier
     * @throws IllegalArgumentException if lifecycleManagement is {@code null}
     */
    String manageReasoningLifecycle(LifecycleManagement lifecycleManagement);
}