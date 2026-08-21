package com.shreeai.os.platform.kernels.factory;

import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.execution.api.ExecutionService;
import com.shreeai.os.platform.kernels.chief.api.ChiefService;
import com.shreeai.os.platform.kernels.identity.api.IdentityService;

/**
 * <b>KernelFactory</b>
 *
 * <p>Factory interface for creating kernel service instances.
 *
 * <p>This factory provides a composition boundary between the Runtime layer
 * and the Kernel layer, allowing the Runtime to obtain kernel services
 * without knowing the internal construction details (validators, engines, etc.).</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides factory methods for kernel service creation</li>
 *   <li>Hides validator/engine construction details from Runtime</li>
 *   <li>Maintains kernel layer encapsulation</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Kernel Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @author Shree AI OS Team
 * @since 1.0
 */
public interface KernelFactory {

    /**
     * Creates a PlanningService instance.
     *
     * @return a PlanningService instance
     */
    PlanningService createPlanningService();

    IdentityService createIdentityService();
    /**
     * Creates an ExecutionService instance.
     *
     * @return an ExecutionService instance
     */
    ExecutionService createExecutionService();

    /**
     * Creates a ChiefService instance.
     *
     * @return a ChiefService instance
     */
    ChiefService createChiefService();
}