package com.shreeai.os.platform.kernels.factory;

import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.execution.api.ExecutionService;
import com.shreeai.os.platform.kernels.chief.api.ChiefService;

/**
 * <b>DefaultKernelFactory</b>
 *
 * <p>Default implementation of KernelFactory that creates kernel service instances
 * with their required dependencies (validators, engines, etc.).</p>
 *
 * <p>This factory lives in the Kernel layer and handles the construction details,
 * allowing the Runtime layer to obtain kernel services through the KernelFactory
 * interface without knowing the internal construction details.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Creates kernel service instances with proper dependencies</li>
 *   <li>Hides validator/engine construction from Runtime layer</li>
 *   <li>Maintains kernel layer encapsulation</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Kernel Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @author Shree AI OS Team
 * @since 1.0
 */
public final class DefaultKernelFactory implements KernelFactory {

    /**
     * Creates a new DefaultKernelFactory.
     *
     * <p>This factory is stateless and thread-safe.</p>
     */
    public DefaultKernelFactory() {
    }

    @Override
    public PlanningService createPlanningService() {
        throw new UnsupportedOperationException(
                "PlanningService construction requires a PlanningValidator instance, " +
                "but PlanningValidator is a static utility class with no public constructor. " +
                "PlanningStage contract does not support PlanningService injection."
        );
    }

    @Override
    public ExecutionService createExecutionService() {
        throw new UnsupportedOperationException(
                "ExecutionService construction requires ExecutionValidator and " +
                "DefaultExecutionProcessingEngine instances, but both are static utility " +
                "classes with no public constructors. ActionExecutionStage contract does " +
                "not support ExecutionService injection."
        );
    }

    @Override
    public ChiefService createChiefService() {
        throw new UnsupportedOperationException(
                "ChiefService construction requires a ChiefValidator instance, " +
                "but ChiefValidator is a static utility class with no public constructor. " +
                "ChiefReviewStage contract does not support ChiefService injection."
        );
    }
}