/**
 * Execution Contract Package - Runtime Layer ABI.
 *
 * <h2>Purpose</h2>
 * <p>This package defines the stable execution contract (Application Binary Interface)
 * for the Shree AI OS Runtime Layer. Every capability in the system must eventually
 * implement this contract to be executable by the Runtime Layer.</p>
 *
 * <p>This contract is designed to remain stable for years, providing a reliable
 * foundation for all future capability implementations.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Define immutable execution request and result models</li>
 *   <li>Define execution status and type enumerations</li>
 *   <li>Provide execution metadata for tracing and monitoring</li>
 *   <li>Define the ExecutableCapability interface contract</li>
 *   <li>Provide execution context for capability coordination</li>
 *   <li>Ensure thread-safety and immutability across all models</li>
 * </ul>
 *
 * <h2>Non-Responsibilities</h2>
 * <ul>
 *   <li>This package does NOT execute capabilities</li>
 *   <li>This package does NOT implement retry logic</li>
 *   <li>This package does NOT implement timeout logic</li>
 *   <li>This package does NOT implement circuit breaker pattern</li>
 *   <li>This package does NOT call skills, LLM, network, database, or filesystem</li>
 *   <li>This package does NOT contain business logic</li>
 * </ul>
 *
 * <h2>Package Structure</h2>
 * <ul>
 *   <li>{@link com.darshan.agent.execution.ExecutionRequest} - Immutable request model</li>
 *   <li>{@link com.darshan.agent.execution.ExecutionResult} - Immutable result model</li>
 *   <li>{@link com.darshan.agent.execution.ExecutionStatus} - Execution status enum</li>
 *   <li>{@link com.darshan.agent.execution.ExecutionType} - Execution type enum</li>
 *   <li>{@link com.darshan.agent.execution.ExecutionMetadata} - Immutable metadata model</li>
 *   <li>{@link com.darshan.agent.execution.ExecutableCapability} - Core capability interface</li>
 *   <li>{@link com.darshan.agent.execution.ExecutionContext} - Immutable context helper</li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><strong>Immutability:</strong> All models are immutable and thread-safe</li>
 *   <li><strong>Constructor Injection:</strong> No setters, only constructors and builders</li>
 *   <li><strong>No Static State:</strong> No mutable static state anywhere</li>
 *   <li><strong>Null Safety:</strong> Proper null handling and validation</li>
 *   <li><strong>Serialization Friendly:</strong> Models designed for JSON/XML serialization</li>
 *   <li><strong>ABI Stability:</strong> Interface designed for long-term stability</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Create execution metadata
 * ExecutionMetadata metadata = ExecutionMetadata.builder()
 *     .executionSource("AgentBrain")
 *     .traceId("trace-123")
 *     .sessionId("session-456")
 *     .build();
 *
 * // Create execution request
 * ExecutionRequest request = ExecutionRequest.builder()
 *     .decisionId("decision-789")
 *     .capabilityName("LearningSessionEngine")
 *     .intent("START_COURSE")
 *     .userInput("start course java")
 *     .session(session)
 *     .resolvedContext(resolvedContext)
 *     .metadata(metadata)
 *     .build();
 *
 * // Execute capability
 * ExecutableCapability capability = capabilityRegistry.getCapability("LearningSessionEngine");
 * ExecutionResult result = capability.execute(request);
 *
 * // Check result
 * if (result.isSuccess()) {
 *     System.out.println("Execution completed: " + result.getResponse());
 * } else {
 *     System.err.println("Execution failed: " + result.getErrorMessage());
 * }
 * </pre>
 *
 * <h2>Future Evolution</h2>
 * <p>This package is designed for extensibility:</p>
 * <ul>
 *   <li>New execution statuses can be added to ExecutionStatus enum</li>
 *   <li>New execution types can be added to ExecutionType enum</li>
 *   <li>ExecutionMetadata supports custom values for future expansion</li>
 *   <li>ExecutionContext supports future execution info for coordination</li>
 *   <li>Builder pattern allows adding new fields without breaking existing code</li>
 * </ul>
 *
 * <p><strong>Important:</strong> Any changes to this package must maintain backward
 * compatibility. This is the ABI of Shree AI OS Runtime.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.1
 */
package com.darshan.agent.execution;