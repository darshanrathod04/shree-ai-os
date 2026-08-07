/**
 * <b>Multi-Agent Kernel — Error Layer</b>
 *
 * <p>Canonical exception hierarchy and immutable error model for the Multi-Agent Kernel.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-104, EIO-ARCH-001</p>
 *
 * <p>This package provides a consistent representation of failures across registration,
 * discovery, capabilities, lifecycle, communication, and validation.</p>
 *
 * <p><b>Architectural Position:</b></p>
 * <pre>
 * Applications
 *      │
 *      ▼
 * Multi-Agent API
 *      │
 *      ▼
 * Domain Models
 *      │
 *      ▼
 * Validation Layer
 *      │
 *      ▼
 * Error Layer
 *      │
 *      ▼
 * Future Service Layer
 *      │
 *      ▼
 * Future Processing Engine
 * </pre>
 *
 * <p><b>Key Principles:</b></p>
 * <ul>
 *   <li>Error Layer represents failures only — it never performs processing</li>
 *   <li>All error objects are immutable value objects</li>
 *   <li>Exception hierarchy is canonical and fixed</li>
 *   <li>No recovery, retry, or routing logic</li>
 * </ul>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * MultiAgentException (base runtime exception)
 * ├── AgentRegistrationException
 * ├── AgentDiscoveryException
 * ├── CapabilityException
 * ├── LifecycleException
 * ├── CommunicationException
 * └── MultiAgentValidationException
 * </pre>
 *
 * <p><b>Components:</b></p>
 * <ul>
 *   <li>{@link MultiAgentErrorCode} — Canonical error code enumeration</li>
 *   <li>{@link MultiAgentError} — Immutable error value object</li>
 *   <li>{@link MultiAgentException} — Base runtime exception</li>
 *   <li>{@link AgentRegistrationException} — Registration errors</li>
 *   <li>{@link AgentDiscoveryException} — Discovery errors</li>
 *   <li>{@link CapabilityException} — Capability errors</li>
 *   <li>{@link LifecycleException} — Lifecycle errors</li>
 *   <li>{@link CommunicationException} — Communication errors</li>
 *   <li>{@link MultiAgentValidationException} — Validation errors</li>
 * </ul>
 *
 * <p><b>Error Codes:</b></p>
 * <ul>
 *   <li>REGISTRATION_ERROR — Agent registration errors</li>
 *   <li>DISCOVERY_ERROR — Agent discovery errors</li>
 *   <li>CAPABILITY_ERROR — Capability management errors</li>
 *   <li>LIFECYCLE_ERROR — Agent lifecycle errors</li>
 *   <li>COMMUNICATION_ERROR — Agent communication errors</li>
 *   <li>VALIDATION_ERROR — Validation failures</li>
 *   <li>MULTI_AGENT_ERROR — General Multi-Agent Kernel errors</li>
 * </ul>
 *
 * <p><b>Immutable Error Model:</b></p>
 * <p>MultiAgentError is an immutable value object containing:</p>
 * <ul>
 *   <li>errorCode — The error category</li>
 *   <li>message — Human-readable error message</li>
 *   <li>agentId — Associated agent identifier (optional)</li>
 *   <li>timestamp — When the error occurred</li>
 *   <li>details — Additional error context</li>
 * </ul>
 *
 * <p><b>Dependencies:</b></p>
 * <ul>
 *   <li>Allowed: platform.kernels.multiagent.model.*, java.util.*, java.time.*</li>
 *   <li>Forbidden: service, validation, engine, runtime, repository, database, network, memory, planning, knowledge, reasoning, framework annotations</li>
 * </ul>
 *
 * <p><b>What Error Layer Can Do:</b></p>
 * <ul>
 *   <li>✓ Represent failures as immutable objects</li>
 *   <li>✓ Categorize errors with error codes</li>
 *   <li>✓ Throw typed exceptions</li>
 *   <li>✓ Expose error information</li>
 * </ul>
 *
 * <p><b>What Error Layer Cannot Do:</b></p>
 * <ul>
 *   <li>✗ Implement retry logic</li>
 *   <li>✗ Implement recovery logic</li>
 *   <li>✗ Perform routing</li>
 *   <li>✗ Perform scheduling</li>
 *   <li>✗ Register agents</li>
 *   <li>✗ Discover agents</li>
 *   <li>✗ Manage lifecycle</li>
 *   <li>✗ Execute agents</li>
 *   <li>✗ Maintain mutable state</li>
 * </ul>
 *
 * @since 1.0
 */
package com.shreeai.os.platform.kernels.multiagent.error;