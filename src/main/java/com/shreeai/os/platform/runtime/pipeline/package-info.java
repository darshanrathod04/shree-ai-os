 /**
 * Runtime Pipeline Package - Execution Backbone.
 *
 * <h2>Purpose</h2>
 * <p>This package provides the pipeline-based execution backbone for Shree AI OS Runtime.
 * The pipeline is the foundation for all future runtime features, replacing the engine-based approach.</p>
 *
 * <p>Every runtime behavior (validation, retry, timeout, audit, metrics, etc.) is implemented
 * as a pipeline stage. The pipeline orchestrates these stages in order.</p>
 *
 * <h2>Architecture Philosophy (ADR-002)</h2>
 * <p><strong>Runtime owns execution state.</strong></p>
 *
 * <p>Execution history is recorded by Runtime, never inferred from PipelineResult.</p>
 *
 * <p>The flow is:</p>
 * <ol>
 *   <li><strong>Input:</strong> PipelineContext (immutable)</li>
 *   <li><strong>Execution State:</strong> PipelineExecutionState (runtime-owned, mutable, internal)</li>
 *   <li><strong>Output:</strong> PipelineResult (immutable snapshot)</li>
 * </ol>
 *
 * <p>PipelineExecutionState is the single source of truth for execution history.
 * It tracks:</p>
 * <ul>
 *   <li>Current stage index</li>
 *   <li>Visited stages</li>
 *   <li>Completed stages</li>
 *   <li>Messages</li>
 *   <li>Metadata</li>
 *   <li>Timing (start, end, duration)</li>
 *   <li>Failed flag</li>
 *   <li>Short-circuited flag</li>
 *   <li>Terminated flag</li>
 * </ul>
 *
 * <p>PipelineResult is an immutable snapshot created exactly once at the end of execution.
 * It must never be used as working memory or mutated during execution.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Define the ExecutionPipeline contract for pipeline orchestration</li>
 *   <li>Define the ExecutionStage contract for individual stages</li>
 *   <li>Define the ExecutionChain contract for stage progression</li>
 *   <li>Provide immutable PipelineContext for passing data through stages</li>
 *   <li>Provide immutable PipelineResult for stage/pipeline results</li>
 *   <li>Provide PipelineStageDescriptor for stage metadata</li>
 *   <li>Provide PipelineExecutionState for runtime-owned execution state</li>
 *   <li>Implement DefaultExecutionPipeline with stage ordering and validation</li>
 *   <li>Implement DefaultExecutionChain for sequential stage invocation</li>
 * </ul>
 *
 * <h2>Non-Responsibilities</h2>
 * <ul>
 *   <li>This package does NOT execute capabilities</li>
 *   <li>This package does NOT implement retry logic</li>
 *   <li>This package does NOT implement timeout logic</li>
 *   <li>This package does NOT implement circuit breaker pattern</li>
 *   <li>This package does NOT implement audit or metrics</li>
 *   <li>This package does NOT call skills, LLM, network, database, or filesystem</li>
 *   <li>This package does NOT contain business logic</li>
 * </ul>
 *
 * <h2>Package Structure</h2>
 * <ul>
 *   <li>{@link com.shreeai.os.platform.runtime.pipeline.ExecutionPipeline} - Pipeline contract</li>
 *   <li>{@link com.shreeai.os.platform.runtime.pipeline.ExecutionStage} - Stage contract</li>
 *   <li>{@link com.shreeai.os.platform.runtime.pipeline.ExecutionChain} - Chain contract</li>
 *   <li>{@link com.shreeai.os.platform.runtime.pipeline.DefaultExecutionPipeline} - Default pipeline implementation</li>
 *   <li>{@link com.shreeai.os.platform.runtime.pipeline.DefaultExecutionChain} - Default chain implementation</li>
 *   <li>{@link com.shreeai.os.platform.runtime.pipeline.PipelineContext} - Immutable context</li>
 *   <li>{@link com.shreeai.os.platform.runtime.pipeline.PipelineResult} - Immutable result</li>
 *   <li>{@link com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState} - Runtime-owned execution state</li>
 *   <li>{@link com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor} - Stage metadata</li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><strong>Pipeline-Based:</strong> Not engine-based. The pipeline is the execution backbone.</li>
 *   <li><strong>Runtime Ownership:</strong> Runtime owns execution state, not PipelineResult</li>
 *   <li><strong>No Inference:</strong> Execution history is recorded, never inferred</li>
 *   <li><strong>Immutability:</strong> PipelineContext and PipelineResult are immutable and thread-safe</li>
 *   <li><strong>Constructor Injection:</strong> No setters, only constructors and builders</li>
 *   <li><strong>No Static State:</strong> No mutable static state anywhere</li>
 *   <li><strong>Stage Ordering:</strong> Stages ordered by priority, validated at startup</li>
 *   <li><strong>Shadow Mode:</strong> Pipeline can operate in shadow mode without executing stages</li>
 *   <li><strong>ABI Stability:</strong> Interface designed for long-term stability</li>
 *   <li><strong>Single Creation:</strong> PipelineResult created exactly once via freeze()</li>
 * </ul>
 *
 * <h2>Execution Flow</h2>
 * <pre>
 * // 1. Create PipelineContext (immutable input)
 * PipelineContext context = PipelineContext.builder()...build();
 *
 * // 2. Create PipelineExecutionState (runtime-owned)
 * PipelineExecutionState state = new PipelineExecutionState(stages);
 *
 * // 3. Execute stages (runtime records state)
 * state.markStageStarted(stageName);
 * stage.process(context, chain, state);
 * state.markStageCompleted(stageName);
 *
 * // 4. Freeze to immutable PipelineResult (created once)
 * PipelineResult result = state.freeze();
 *
 * // 5. Return immutable result
 * return result;
 * </pre>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Create stages
 * ExecutionStage validationStage = new ValidationStage();
 * ExecutionStage retryStage = new RetryStage();
 * ExecutionStage auditStage = new AuditStage();
 *
 * // Create pipeline with stages (automatically ordered by priority)
 * ExecutionPipeline pipeline = new DefaultExecutionPipeline(
 *     List.of(validationStage, retryStage, auditStage)
 * );
 *
 * // Create pipeline context
 * PipelineContext context = PipelineContext.builder()
 *     .executionRequest(executionRequest)
 *     .decision(decision)
 *     .validationResult(validationResult)
 *     .build();
 *
 * // Execute pipeline
 * PipelineResult result = pipeline.execute(context);
 *
 * // Check result
 * if (result.isSuccess()) {
 *     System.out.println("Pipeline completed: " + result.getCompletedStages());
 * } else {
 *     System.err.println("Pipeline failed at stage: " + result.getCurrentStage());
 * }
 * </pre>
 *
 * <h2>Future Evolution</h2>
 * <p>This package is designed for extensibility:</p>
 * <ul>
 *   <li>New stages can be added by implementing ExecutionStage</li>
 *   <li>Stages are automatically ordered by priority</li>
 *   <li>PipelineStageDescriptor supports future extensibility fields</li>
 *   <li>PipelineContext attributes map allows stages to share data</li>
 *   <li>Builder pattern allows adding new fields without breaking existing code</li>
 *   <li>PipelineExecutionState can be extended with new tracking fields</li>
 * </ul>
 *
 * <h2>Architectural Decision Record (ADR-002)</h2>
 * <p><strong>Decision:</strong> Runtime owns execution state.</p>
 *
 * <p><strong>Context:</strong> Sprint 6.2A exposed a fundamental issue where DefaultExecutionChain
 * was attempting to determine whether a stage had executed by inspecting PipelineResult.
 * This is incorrect because execution history must never be inferred.</p>
 *
 * <p><strong>Consequences:</strong></p>
 * <ul>
 *   <li>PipelineExecutionState is the single source of truth for execution history</li>
 *   <li>PipelineResult is an immutable snapshot created exactly once</li>
 *   <li>Execution history is recorded by Runtime, never inferred from PipelineResult</li>
 *   <li>DefaultExecutionChain becomes simpler and more maintainable</li>
 *   <li>Runtime becomes easier to extend with Retry, Timeout, Audit, Metrics, etc.</li>
 * </ul>
 *
 * <p><strong>Important:</strong> Any changes to this package must maintain backward
 * compatibility. This is the execution backbone of Shree AI OS Runtime.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.2A-R1
 */
package com.shreeai.os.platform.runtime.pipeline;
