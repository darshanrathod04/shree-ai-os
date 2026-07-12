package platform.runtime.internal;

import platform.runtime.api.Runtime;
import platform.runtime.config.RuntimeConfiguration;
import platform.runtime.contracts.RuntimeContract;
import platform.runtime.execution.ExecutionContext;
import platform.runtime.execution.ExecutionPipeline;
import platform.runtime.execution.ExecutionRequest;
import platform.runtime.execution.ExecutionResult;
import platform.runtime.execution.ExecutionSession;
import platform.runtime.lifecycle.RuntimeLifecycle;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <b>DefaultRuntime</b>
 *
 * <p>Default implementation of the {@link Runtime} interface.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the concrete implementation of the Runtime API.</li>
 *   <li>Manages lifecycle state transitions through {@link DefaultRuntimeLifecycle}.</li>
 *   <li>Delegates execution to the configured {@link ExecutionPipeline}.</li>
 *   <li>Enforces the {@link RuntimeContract} on all execution requests.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Internal)</p>
 * <p><b>Invariant:</b> The Runtime MUST always be in exactly one valid RuntimeState.</p>
 */
public final class DefaultRuntime implements Runtime {

    private final RuntimeConfiguration configuration;
    private final RuntimeContract contract;
    private final DefaultRuntimeLifecycle lifecycle;
    private final AtomicReference<ExecutionPipeline> pipeline;

    /**
     * Constructs a new DefaultRuntime.
     *
     * @param configuration the runtime configuration
     * @param contract      the runtime contract
     */
    public DefaultRuntime(RuntimeConfiguration configuration, RuntimeContract contract) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.contract = Objects.requireNonNull(contract, "contract must not be null");
        this.lifecycle = new DefaultRuntimeLifecycle();
        this.pipeline = new AtomicReference<>(new DefaultExecutionPipeline());
    }

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
    public ExecutionPipeline pipeline() {
        return pipeline.get();
    }

    @Override
    public ExecutionSession submit(ExecutionRequest request) {
        if (!lifecycle.isAcceptingRequests()) {
            throw new IllegalStateException(
                    "Runtime is not accepting requests. Current state: " + lifecycle.currentState());
        }

        ExecutionSession session = ExecutionSession.builder()
                .requestId(request.requestId())
                .build();

        ExecutionContext context = ExecutionContext.builder()
                .session(session)
                .configuration(configuration)
                .contract(contract)
                .build();

        ExecutionResult result = pipeline.get().execute(request, context);

        // Session tracking and result handling will be implemented in Sprint 2
        return session;
    }

    @Override
    public void start() {
        lifecycle.start();
    }

    @Override
    public void stop() {
        lifecycle.stop();
    }

    @Override
    public void shutdown() {
        lifecycle.shutdown();
    }
}