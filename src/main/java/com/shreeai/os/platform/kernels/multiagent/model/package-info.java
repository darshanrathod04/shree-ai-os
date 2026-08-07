/**
 * <b>Multi-Agent Kernel — Domain Model</b>
 *
 * <p>Canonical domain models defining the vocabulary of the Multi-Agent Kernel.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-102, EIO-ARCH-001</p>
 *
 * <p>This package contains immutable value objects that define the canonical domain language
 * for the Multi-Agent Kernel. These models describe:</p>
 * <ul>
 *   <li>Identity (AgentId)</li>
 *   <li>Registration (AgentRegistration)</li>
 *   <li>Discovery (AgentDescriptor)</li>
 *   <li>Lifecycle (AgentStatus)</li>
 *   <li>Communication metadata (AgentCommunication)</li>
 *   <li>Runtime metrics (MultiAgentMetrics)</li>
 * </ul>
 *
 * <p><b>Architectural Position:</b></p>
 * <pre>
 * Applications
 *      │
 *      ▼
 * Public API
 *      │
 *      ▼
 * Domain Models
 *      │
 *      ▼
 * Future Validation Layer
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
 *   <li>Domain models define language only — they never perform work</li>
 *   <li>All models are immutable value objects</li>
 *   <li>Metadata is never behavior</li>
 *   <li>Communication is always Chief-mediated</li>
 * </ul>
 *
 * <p><b>Value Object Rules:</b></p>
 * <ul>
 *   <li>Every class is final</li>
 *   <li>Every field is private final</li>
 *   <li>No mutable collections</li>
 *   <li>Constructor validation with Objects.requireNonNull()</li>
 *   <li>Defensive copying with List.copyOf() and Map.copyOf()</li>
 *   <li>Value semantics with equals(), hashCode(), toString()</li>
 *   <li>No setters</li>
 * </ul>
 *
 * <p><b>Dependencies:</b></p>
 * <ul>
 *   <li>Allowed: java.util.*, java.time.*, platform.common.*</li>
 *   <li>Forbidden: service, validation, engine, runtime, network, repository, database, memory, planning, knowledge, reasoning, framework annotations</li>
 * </ul>
 *
 * <p><b>Migration:</b></p>
 * <p>These models replace the temporary bootstrap records from MAGENT-101
 * (platform.kernels.multiagent.api package). The migration is transparent to API consumers.</p>
 *
 * @since 1.0
 */
package com.shreeai.os.platform.kernels.multiagent.model;