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
import com.shreeai.os.platform.kernels.identity.api.IdentityService;
import com.shreeai.os.platform.kernels.identity.engine.DefaultIdentityProcessingEngine;
import com.shreeai.os.platform.kernels.identity.service.DefaultIdentityService;
import com.shreeai.os.platform.kernels.identity.validation.IdentityValidator;
import com.shreeai.os.platform.kernels.tool.api.ToolService;
import com.shreeai.os.platform.kernels.tool.engine.DefaultToolProcessingEngine;
import com.shreeai.os.platform.kernels.tool.service.DefaultToolService;
import com.shreeai.os.platform.kernels.tool.validation.ToolValidator;

import java.util.Objects;

/**
 * Default composition root for all kernel services.
 *
 * Thread-safe singleton kernel construction.
 */
public final class DefaultKernelFactory implements KernelFactory {

    private final IdentityService identityService;
    private final PlanningService planningService;
    private final ExecutionService executionService;
    private final ChiefService chiefService;
    private final ToolService toolService;

    public DefaultKernelFactory() {

        // Identity Kernel
        IdentityValidator identityValidator =
                new IdentityValidator();

        DefaultIdentityProcessingEngine identityEngine =
                new DefaultIdentityProcessingEngine(identityValidator);

        this.identityService =
                new DefaultIdentityService(identityEngine);

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

        // Tool Kernel
        ToolValidator toolValidator =
                new ToolValidator();

        DefaultToolProcessingEngine toolEngine =
                new DefaultToolProcessingEngine();

        this.toolService =
                new DefaultToolService(
                        toolValidator,
                        toolEngine
                );

        validateComposition();
    }

    @Override
    public IdentityService createIdentityService() {
        return identityService;
    }

    private void validateComposition() {

        Objects.requireNonNull(identityService);
        Objects.requireNonNull(planningService);
        Objects.requireNonNull(executionService);
        Objects.requireNonNull(chiefService);
        Objects.requireNonNull(toolService);
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

    @Override
    public ToolService createToolService() {
        return toolService;
    }
}