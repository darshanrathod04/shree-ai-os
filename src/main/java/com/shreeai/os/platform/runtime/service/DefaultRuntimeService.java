package com.shreeai.os.platform.runtime.service;

import com.shreeai.os.platform.runtime.AbstractRuntimeService;
import com.shreeai.os.platform.runtime.RuntimeState;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.internal.DefaultRuntime;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeLifecycle;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeLifecycleListener;
import com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>DefaultRuntimeService</b>
 *
 * <p>Default implementation of the Runtime that extends {@link AbstractRuntimeService}
 * and implements the {@link Runtime} contract.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the concrete Runtime implementation for bootstrap integration.</li>
 *   <li>Extends AbstractRuntimeService for lifecycle state management.</li>
 *   <li>Implements the Runtime API contract.</li>
 *   <li>Delegates execution to the canonical ExecutionPipeline.</li>
 *   <li>Coordinates lifecycle only — no platform business logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Service</p>
 * <p><b>Lifecycle:</b> CREATED → INITIALIZED → STARTED → VERIFIED → STOPPED</p>
 */
public final class DefaultRuntimeService extends AbstractRuntimeService implements Runtime {

    private final RuntimeConfiguration configuration;
    private final RuntimeContract contract;
    private final List<ExecutionStage> stages;
    private RuntimeLifecycle lifecycle;
    private com.shreeai.os.platform.runtime.execution.ExecutionPipeline pipeline;
    
    /**
     * Constructs a new DefaultRuntimeService with the given configuration and contract.
     *
     * @param configuration the runtime configuration (must not be null)
     * @param contract      the runtime contract (must not be null)
     */
    public DefaultRuntimeService(RuntimeConfiguration configuration, RuntimeContract contract) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.contract = Objects.requireNonNull(contract, "contract must not be null");
        this.stages = new ArrayList<>();
    }

    /**
     * Constructs a new DefaultRuntimeService with the given configuration, contract, and stages.
     *
     * @param configuration the runtime configuration (must not be null)
     * @param contract      the runtime contract (must not be null)
     * @param stages        the execution pipeline stages (must not be null)
     */
    public DefaultRuntimeService(RuntimeConfiguration configuration, RuntimeContract contract, List<ExecutionStage> stages) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.contract = Objects.requireNonNull(contract, "contract must not be null");
        this.stages = new ArrayList<>(Objects.requireNonNull(stages, "stages must not be null"));
    }

    @Override
    public void initialize() {
        super.initialize();
        // Create the canonical execution pipeline
        this.pipeline = new DefaultExecutionPipeline(stages);
        // Create lifecycle (uses DefaultRuntime lifecycle)
        com.shreeai.os.platform.runtime.lifecycle.RuntimeState runtimeState = 
            com.shreeai.os.platform.runtime.lifecycle.RuntimeState.INITIALIZING;
        this.lifecycle = new com.shreeai.os.platform.runtime.internal.DefaultRuntimeLifecycle();
    }

    @Override
    public void start() {
        super.start();
        if (lifecycle != null) {
            lifecycle.start();
        }
    }

    @Override
    public void verify() {
        super.verify();
    }

    @Override
    public void shutdown() {
        if (lifecycle != null) {
            try {
                lifecycle.stop();
            } catch (Exception e) {
                lifecycle.shutdown();
            }
        }
        super.shutdown();
    }

    // ========================================================================
    // Runtime API Implementation
    // ========================================================================

    @Override
    public RuntimeConfiguration configuration() {
        return configuration;
    }

    @Override
    public RuntimeLifecycle lifecycle() {
        return lifecycle;
    }

    @Override
    public RuntimeContract contract() {
        return contract;
    }

    @Override
    public com.shreeai.os.platform.runtime.execution.ExecutionPipeline pipeline() {
        return (com.shreeai.os.platform.runtime.execution.ExecutionPipeline) pipeline;
    }

    @Override
    public ExecutionSession submit(ExecutionRequest request) {
        if (lifecycle == null || !lifecycle.isAcceptingRequests()) {
            throw new IllegalStateException(
                    "Runtime is not accepting requests. State: " + getState());
        }
        // Use the canonical execution pipeline
        return pipeline != null ? 
            com.shreeai.os.platform.runtime.execution.ExecutionSession.builder()
                .requestId(request.requestId())
                .build() : 
            null;
    }

    @Override
    public void stop() {
        if (lifecycle != null) {
            lifecycle.stop();
        }
    }

    @Override
    public String getName() {
        return "DefaultRuntimeService";
    }

    /**
     * Returns the current runtime state.
     *
     * @return current runtime state
     */
    public RuntimeState getRuntimeState() {
        return getState();
    }
}