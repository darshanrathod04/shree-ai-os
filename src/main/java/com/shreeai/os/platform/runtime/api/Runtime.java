package com.shreeai.os.platform.runtime.api;

import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.execution.ExecutionPipeline;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeLifecycle;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeState;

/**
 * <b>Runtime</b>
 *
 * <p>The primary Runtime interface for Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the public API for interacting with the Runtime Kernel.</li>
 *   <li>Owns the Runtime lifecycle and state transitions.</li>
 *   <li>Orchestrates execution sessions through the ExecutionPipeline.</li>
 *   <li>Enforces the RuntimeContract on all execution.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Invariant:</b> A Runtime instance MUST only accept new execution requests
 * when its state is {@link RuntimeState#READY}.</p>
 *
 * @see RuntimeBuilder
 * @see RuntimeConfiguration
 * @see RuntimeLifecycle
 * @see ExecutionPipeline
 */
public interface Runtime {

    /**
     * Returns the RuntimeConfiguration used to configure this Runtime instance.
     *
     * @return the runtime configuration
     */
    RuntimeConfiguration configuration();

    /**
     * Returns the RuntimeLifecycle that manages lifecycle transitions.
     *
     * @return the runtime lifecycle manager
     */
    RuntimeLifecycle lifecycle();

    /**
     * Returns the RuntimeContract that governs all execution within this Runtime.
     *
     * @return the runtime contract
     */
    RuntimeContract contract();

    /**
     * Returns the ExecutionPipeline used to process execution requests.
     *
     * @return the execution pipeline
     */
    ExecutionPipeline pipeline();

    /**
     * Submits an ExecutionRequest for processing.
     *
     * <p>The Runtime MUST be in {@link RuntimeState#READY} state to accept requests.
     *
     * @param request the execution request to process
     * @return the session created for tracking this execution
     * @throws IllegalStateException if Runtime is not in READY state
     */
    ExecutionSession submit(ExecutionRequest request);

    /**
     * Begins the Runtime lifecycle, transitioning from INITIALIZING to READY.
     *
     * @throws IllegalStateException if starting is not possible from the current state
     */
    void start();

    /**
     * Gracefully stops the Runtime, completing all active sessions.
     *
     * @throws IllegalStateException if stopping is not possible from the current state
     */
    void stop();

    /**
     * Forces an immediate shutdown of the Runtime, aborting active sessions.
     *
     * @throws IllegalStateException if shutdown is not possible from the current state
     */
    void shutdown();

    /**
     * Binds the given SDK event bus to this Runtime so runtime-side event
     * consumers (e.g. the knowledge ingestion consumer) can act on SDK
     * published events.
     *
     * <p>This is an additive, non-breaking extension point: the default
     * implementation is a no-op, so every existing Runtime implementation
     * remains source- and binary-compatible. The default
     * {@code DefaultRuntimeService} subscribes its event-driven kernel
     * consumers here.</p>
     *
     * @param eventBus the SDK event bus to bind (may be null; implementations
     *                 MUST tolerate null by doing nothing)
     */
    default void bindEventBus(com.shreeai.os.platform.sdk.events.RuntimeEventBus eventBus) {
        // no-op by default — additive extension point
    }

    /**
     * Streams a token-level response for {@code message} through the full
     * execution pipeline (memory recall, knowledge retrieval, reasoning, LLM
     * synthesis) and returns a live {@link java.util.stream.Stream} of token
     * fragments from the LLM provider.
     *
     * <p><b>Sprint-release-fix:</b> replaces the word-splitting simulation in
     * {@code ShreeClient.chatStream()} with true provider token streaming from
     * the LLM router's first-available provider.</p>
     *
     * <p>The caller is responsible for closing the returned stream so that
     * underlying HTTP connections are released.</p>
     *
     * @param message the user message to process (must not be null)
     * @return a closeable stream of token fragments (never null)
     * @throws IllegalArgumentException if message is null
     * @throws IllegalStateException   if no LLM provider is available
     */
    /**
     * Streams LLM token chunks for the given message.
     *
     * <p>Default implementation returns an empty stream — additive extension point.
     * The canonical {@code DefaultRuntimeService} overrides this to deliver provider token streaming from
     * the LLM router's first-available provider.</p>
     *
     * <p>The caller is responsible for closing the returned stream so that
     * underlying HTTP connections are released.</p>
     *
     * @param message the user message to process (must not be null)
     * @return a closeable stream of token fragments (never null)
     * @throws IllegalArgumentException if message is null
     * @throws IllegalStateException   if no LLM provider is available
     */
    default java.util.stream.Stream<String> streamText(String message) {
        return java.util.stream.Stream.empty();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Reflection Kernel — Phase 1.5
    // Additive extension points: default implementations are no-ops so all
    // existing Runtime implementations remain source- and binary-compatible.
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Reflects on a completed execution and produces a structured reflection report.
     *
     * <p>Delegates to {@link com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine}
     * to score quality, assign a verdict, and extract actionable lessons.
     * The result is scored for importance via
     * {@link com.shreeai.os.platform.runtime.reflection.ReflectionImportanceScorer}.</p>
     *
     * <p>Default implementation returns {@code null} — additive extension point.</p>
     *
     * @param executionId the execution identifier to reflect on (never null)
     * @param requestText the original user request text (may be null)
     * @param planStepCount number of planned steps (0 when unplanned)
     * @param actionStatus status reported by the action execution stage
     * @param executionSuccess whether execution completed successfully
     * @param responseSummary summary of the produced response / conclusion
     * @param confidence confidence of the produced conclusion (0.0–1.0)
     * @return the reflection report, or {@code null} when reflection is unavailable
     * @throws IllegalArgumentException if executionId is null
     */
    default com.shreeai.os.platform.sdk.ReflectionReport reflectOnExecution(
            String executionId,
            String requestText,
            int planStepCount,
            String actionStatus,
            boolean executionSuccess,
            String responseSummary,
            double confidence) {
        return null;
    }

    /**
     * Returns the most recent reflection history for a tenant.
     *
     * <p>Default implementation returns an empty list — additive extension point.</p>
     *
     * @param tenantId the tenant identifier (never null)
     * @param limit    maximum number of records to return (must be ≥ 1)
     * @return list of recent reflection history records (never null)
     */
    default java.util.List<com.shreeai.os.platform.runtime.reflection.ReflectionHistory> recentReflections(
            String tenantId, int limit) {
        return java.util.List.of();
    }

    /**
     * Searches reflection history for a tenant by keyword.
     *
     * <p>Default implementation returns an empty list — additive extension point.</p>
     *
     * @param tenantId the tenant identifier (never null)
     * @param keyword  the keyword to search in lessons and verdicts (never null)
     * @param limit    maximum number of records to return (must be ≥ 1)
     * @return list of matching reflection history records (never null)
     */
    default java.util.List<com.shreeai.os.platform.runtime.reflection.ReflectionHistory> searchReflections(
            String tenantId, String keyword, int limit) {
        return java.util.List.of();
    }

    /**
     * Returns reflection statistics (analytics summary) for a tenant.
     *
     * <p>Default implementation returns {@code null} — additive extension point.</p>
     *
     * @param tenantId the tenant identifier (never null)
     * @param window   number of recent records to analyze (must be ≥ 1)
     * @return the analytics summary, or {@code null} when analytics is unavailable
     */
    default com.shreeai.os.platform.sdk.ReflectionStatistics reflectionStatistics(
            String tenantId, int window) {
        return null;
    }

    /**
     * Returns the importance score for a reflection verdict.
     *
     * <p>Default implementation returns {@code 0} — additive extension point.</p>
     *
     * @param verdict the reflection verdict (never null)
     * @param score   the quality score (0.0–1.0)
     * @param lessons the extracted lessons (never null)
     * @return the importance score (0–100)
     */
    default int reflectionImportance(
            String verdict, double score, java.util.List<String> lessons) {
        return 0;
    }

    /**
     * Returns the {@link com.shreeai.os.platform.kernels.identity.api.IdentityService}
     * exposed by this Runtime, or {@code null} when the Runtime does not manage
     * an Identity Kernel (e.g. test or stub runtimes).
     *
     * <p>Default implementation returns {@code null} — additive extension point.</p>
     *
     * @return the identity service, or null when unavailable
     */
    default com.shreeai.os.platform.kernels.identity.api.IdentityService identityService() {
        return null;
    }

    /**
     * Resolves the Identity context for an incoming request.
     *
     * <p>Default implementation returns {@code null} — additive extension point.
     * Implementations delegate to the Identity Kernel's
     * {@link com.shreeai.os.platform.kernels.identity.api.IdentityService#resolveIdentity}
     * entry point.</p>
     *
     * @param requestId     the request identifier (never null)
     * @param sessionId     the session identifier (may be null)
     * @param applicationId the application identifier (may be null)
     * @param workspaceId   the workspace identifier (may be null)
     * @return the resolved identity context, or null when unavailable
     */
    default com.shreeai.os.platform.kernels.identity.model.IdentityContext resolveIdentity(
            String requestId,
            String sessionId,
            String applicationId,
            String workspaceId) {
        return null;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Sprint-Release-5: Real Tenant Isolation
    // Canonical access to the runtime's tenant infrastructure for SDK consumers
    // and internal pipeline stages that need to validate tenant boundaries.
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Returns the {@link com.shreeai.os.platform.runtime.tenant.TenantResolver}
     * used by this Runtime to resolve the current tenant identity, or
     * {@code null} when the Runtime does not manage multi-tenancy.
     *
     * <p>Default implementation returns {@code null} — additive extension point.</p>
     *
     * @return the tenant resolver, or null when unavailable
     */
    default com.shreeai.os.platform.runtime.tenant.TenantResolver tenantResolver() {
        return null;
    }

    /**
     * Returns the {@link com.shreeai.os.platform.runtime.tenant.TenantIsolationEnforcer}
     * used by this Runtime to validate cross-tenant access, or
     * {@code null} when the Runtime does not manage multi-tenancy.
     *
     * <p>Default implementation returns {@code null} — additive extension point.</p>
     *
     * @return the tenant isolation enforcer, or null when unavailable
     */
    default com.shreeai.os.platform.runtime.tenant.TenantIsolationEnforcer tenantIsolation() {
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Sprint-Release-6: Advanced Planning APIs
    // Canonical access to the Planning Kernel for SDK consumers that want typed,
    // strong-API access instead of the legacy string-routing path.
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Returns the {@link com.shreeai.os.platform.kernels.planning.api.PlanningService}
     * used by this Runtime for goal/task planning and plan validation, or
     * {@code null} when the Runtime does not host the Planning Kernel.
     *
     * <p>Default implementation returns {@code null} — additive extension point.
     * Production runtimes that initialize the Planning Kernel override this.</p>
     *
     * @return the planning service, or null when unavailable
     */
    default com.shreeai.os.platform.kernels.planning.api.PlanningService planningService() {
        return null;
    }
}