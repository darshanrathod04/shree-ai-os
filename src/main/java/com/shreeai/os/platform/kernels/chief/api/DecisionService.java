package com.shreeai.os.platform.kernels.chief.api;

import com.shreeai.os.platform.kernels.chief.model.DecisionContext;
import com.shreeai.os.platform.kernels.chief.model.DecisionResult;

/**
 * <b>DecisionService</b>
 *
 * <p>Defines strategic decision coordination contracts for the Chief Kernel.
 * This interface provides contracts for coordinating cross-kernel decisions
 * without implementing any decision algorithms.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines strategic decision coordination contracts.</li>
 *   <li>Provides cross-kernel decision routing.</li>
 *   <li>Selects participating kernels for decisions.</li>
 *   <li>Contains no decision algorithms.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only decision contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-101, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public interface DecisionService {

    /**
     * Evaluates a coordination request and determines the appropriate routing.
     *
     * <p>This operation evaluates a strategic coordination request and determines
     * which kernels should participate in the decision.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link KernelCoordinationService} for kernel selection logic.</p>
     *
     * @param context the decision context (must not be {@code null})
     * @return the decision result with routing and selected kernels
     * @throws IllegalArgumentException if context is {@code null}
     */
    DecisionResult evaluateCoordinationRequest(DecisionContext context);

    /**
     * Coordinates a cross-kernel decision.
     *
     * <p>This operation coordinates decision-making across multiple kernels
     * and returns the consolidated decision result.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link KernelCoordinationService} for cross-kernel coordination.</p>
     *
     * @param context the decision context (must not be {@code null})
     * @return the decision result
     * @throws IllegalArgumentException if context is {@code null}
     */
    DecisionResult coordinateDecision(DecisionContext context);

    /**
     * Determines the execution routing for a decision.
     *
     * <p>This operation determines how a decision should be routed to the
     * appropriate execution path.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link KernelCoordinationService} for routing logic.</p>
     *
     * @param context the decision context (must not be {@code null})
     * @return the decision result containing the routing information
     * @throws IllegalArgumentException if context is {@code null}
     */
    DecisionResult determineExecutionRouting(DecisionContext context);

    /**
     * Selects participating kernels for a decision.
     *
     * <p>This operation selects which kernels should participate in executing
     * a strategic decision based on the provided context.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> This operation delegates to
     * {@link KernelCoordinationService} for kernel selection.</p>
     *
     * @param context the decision context (must not be {@code null})
     * @return the decision result with selected kernels
     * @throws IllegalArgumentException if context is {@code null}
     */
    DecisionResult selectParticipatingKernels(DecisionContext context);
}