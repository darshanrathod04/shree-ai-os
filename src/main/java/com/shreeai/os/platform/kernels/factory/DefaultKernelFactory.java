package com.shreeai.os.platform.kernels.factory;

import com.shreeai.os.platform.kernels.chief.api.ChiefService;
import com.shreeai.os.platform.kernels.chief.engine.DefaultChiefProcessingEngine;
import com.shreeai.os.platform.kernels.chief.service.DefaultChiefService;
import com.shreeai.os.platform.kernels.chief.validation.ChiefValidator;
import com.shreeai.os.platform.kernels.execution.api.ExecutionService;
import com.shreeai.os.platform.kernels.execution.engine.DefaultExecutionProcessingEngine;
import com.shreeai.os.platform.kernels.execution.service.DefaultExecutionService;
import com.shreeai.os.platform.kernels.execution.validation.ExecutionValidator;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.engine.DefaultPlanningProcessingEngine;
import com.shreeai.os.platform.kernels.planning.service.DefaultPlanningService;
import com.shreeai.os.platform.kernels.planning.validation.PlanningValidator;

import java.util.Objects;

/**
 * Default composition root for all kernel services.
 *
 * Thread-safe singleton kernel construction.
 */
public final class DefaultKernelFactory implements KernelFactory {

    private final PlanningService planningService;
    private final ExecutionService executionService;
    private final ChiefService chiefService;

    public DefaultKernelFactory() {

        // Planning
        PlanningValidator planningValidator =
                new PlanningValidator();

        DefaultPlanningProcessingEngine planningEngine =
                new DefaultPlanningProcessingEngine();

        this.planningService =
                new DefaultPlanningService(
                        planningValidator,
                        planningEngine
                );

        // Execution
        ExecutionValidator executionValidator =
                new ExecutionValidator();

        DefaultExecutionProcessingEngine executionEngine =
                new DefaultExecutionProcessingEngine();

        this.executionService =
                new DefaultExecutionService(
                        executionValidator,
                        executionEngine
                );

        // Chief
        ChiefValidator chiefValidator =
                new ChiefValidator();

        DefaultChiefProcessingEngine chiefEngine =
                new DefaultChiefProcessingEngine();

        this.chiefService =
                new DefaultChiefService(
                        chiefValidator,
                        chiefEngine
                );

        validateComposition();
    }

    private void validateComposition() {

        Objects.requireNonNull(planningService);
        Objects.requireNonNull(executionService);
        Objects.requireNonNull(chiefService);
    }

    @Override
    public PlanningService createPlanningService() {
        return planningService;
    }

    @Override
    public ExecutionService createExecutionService() {
        return executionService;
    }

    @Override
    public ChiefService createChiefService() {
        return chiefService;
    }
}