package com.shreeai.os.platform.kernels.chief.api;

import com.shreeai.os.platform.kernels.chief.model.ChiefRequest;
import com.shreeai.os.platform.kernels.chief.model.CoordinationState;

/**
 * <b>KernelCoordinationService</b>
 *
 * <p>Defines contracts for coordinating the completed kernels of Shree AI OS.
 * This interface provides contracts for cross-kernel coordination without
 * implementing any orchestration logic.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines cross-kernel coordination contracts.</li>
 *   <li>Provides kernel routing and dependency coordination.</li>
 *   <li>Coordinates interactions among all completed kernels.</li>
 *   <li>Contains no orchestration implementation.</li>
 * </ul>
 *
 * <p><b>Kernel Coordination Boundaries:</b></p>
 * <ul>
 *   <li>Identity Kernel — identity and platform identity management</li>
 *   <li>Memory Kernel — persistent conversational memory</li>
 *   <li>Context Kernel — context lifecycle and contextual awareness</li>
 *   <li>Knowledge Kernel — knowledge representation and retrieval</li>
 *   <li>Cognitive Kernel — reasoning and cognitive processing</li>
 *   <li>Planning Kernel — goal decomposition, planning, scheduling, prioritization</li>
 *   <li>Execution Kernel — deterministic execution of validated plans</li>
 *   <li>Chief Kernel — strategic orchestration across kernels</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only coordination contracts.</li>
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
public interface KernelCoordinationService {

    /**
     * Submits a coordination request to the specified kernel.
     *
     * <p>This operation submits a coordination request to a target kernel
     * and returns the coordination state.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> Routes requests to the appropriate kernel through
     * public kernel contracts.</p>
     *
     * @param kernelName the target kernel name (must not be {@code null} or empty)
     * @param request    the coordination request (must not be {@code null})
     * @return the coordination state
     * @throws IllegalArgumentException if kernelName or request is {@code null} or empty
     */
    CoordinationState submitCoordinationRequest(String kernelName, ChiefRequest request);

    /**
     * Routes a request to the appropriate kernel.
     *
     * <p>This operation determines which kernel should handle a given request
     * and routes it accordingly.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param request the coordination request (must not be {@code null})
     * @return the coordination state with routing information
     * @throws IllegalArgumentException if request is {@code null}
     */
    CoordinationState routeToKernel(ChiefRequest request);

    /**
     * Coordinates dependencies between kernels.
     *
     * <p>This operation manages dependency coordination between kernels,
     * ensuring that dependent operations are properly sequenced.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param coordinationId the coordination identifier (must not be {@code null} or empty)
     * @return the coordination state with dependency information
     * @throws IllegalArgumentException if coordinationId is {@code null} or empty
     */
    CoordinationState coordinateDependencies(String coordinationId);

    /**
     * Retrieves the current orchestration state.
     *
     * <p>This operation retrieves the current state of cross-kernel
     * orchestration for a given coordination identifier.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param coordinationId the coordination identifier (must not be {@code null} or empty)
     * @return the coordination state
     * @throws IllegalArgumentException if coordinationId is {@code null} or empty
     */
    CoordinationState getOrchestrationState(String coordinationId);
}