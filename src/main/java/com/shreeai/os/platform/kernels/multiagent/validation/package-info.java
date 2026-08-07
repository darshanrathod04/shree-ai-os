/**
 * <b>Multi-Agent Kernel — Validation Layer</b>
 *
 * <p>Structural validation for the Multi-Agent Kernel.
 * Ensures all requests, registrations, and communications are structurally valid
 * before entering the Service Layer.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-103, EIO-ARCH-001</p>
 *
 * <p>This package contains validators that perform structural validation only.
 * Validation protects the Service Layer but does not perform Service responsibilities.</p>
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
 * Future Service Layer
 *      │
 *      ▼
 * Future Processing Engine
 * </pre>
 *
 * <p><b>Key Principles:</b></p>
 * <ul>
 *   <li>Validation is structural only — no business logic</li>
 *   <li>All validators are stateless</li>
 *   <li>Validation never performs registration, discovery, scheduling, or routing</li>
 *   <li>Communication must be Chief-mediated (direct communication is rejected)</li>
 * </ul>
 *
 * <p><b>Validation Flow:</b></p>
 * <pre>
 * MultiAgentRequest
 *         │
 *         ▼
 * MultiAgentValidator
 *         │
 *  ┌──────┼─────────────────────────────────────────────┐
 *  ▼      ▼          ▼          ▼          ▼            ▼
 * Registration  Discovery  Capability  Lifecycle  Communication  Criteria
 *         │
 *         ▼
 * Aggregate Results
 *         │
 *         ▼
 * MultiAgentValidationResult
 * </pre>
 *
 * <p><b>Validators:</b></p>
 * <ul>
 *   <li>{@link MultiAgentValidator} — Validation façade</li>
 *   <li>{@link AgentRegistrationValidator} — Validates registration metadata</li>
 *   <li>{@link AgentDiscoveryValidator} — Validates discovery criteria</li>
 *   <li>{@link CapabilityValidator} — Validates capability definitions</li>
 *   <li>{@link LifecycleValidator} — Validates lifecycle requests</li>
 *   <li>{@link CommunicationValidator} — Validates communication metadata</li>
 *   <li>{@link MultiAgentCriteriaValidator} — Validates shared criteria</li>
 * </ul>
 *
 * <p><b>Communication Invariant:</b></p>
 * <p>All communication must flow through the Chief Kernel:</p>
 * <pre>
 * Agent
 *   ↓
 * Chief
 *   ↓
 * Agent
 * </pre>
 * <p>Direct agent-to-agent communication is architecturally invalid and is rejected by validation.</p>
 *
 * <p><b>Dependencies:</b></p>
 * <ul>
 *   <li>Allowed: platform.kernels.multiagent.api.*, platform.kernels.multiagent.model.*, java.util.*</li>
 *   <li>Forbidden: service, engine, runtime, repository, database, network, memory, planning, knowledge, reasoning, framework annotations</li>
 * </ul>
 *
 * <p><b>What Validation Can Do:</b></p>
 * <ul>
 *   <li>✓ Null checks</li>
 *   <li>✓ Required field validation</li>
 *   <li>✓ Immutable collection validation</li>
 *   <li>✓ Identifier validation</li>
 *   <li>✓ Metadata validation</li>
 *   <li>✓ Lifecycle consistency checks</li>
 *   <li>✓ Capability uniqueness checks</li>
 *   <li>✓ Chief-mediated routing validation</li>
 * </ul>
 *
 * <p><b>What Validation Cannot Do:</b></p>
 * <ul>
 *   <li>✗ Register agents</li>
 *   <li>✗ Discover agents</li>
 *   <li>✗ Schedule work</li>
 *   <li>✗ Route communications</li>
 *   <li>✗ Perform networking</li>
 *   <li>✗ Orchestrate agents</li>
 *   <li>✗ Persist data</li>
 *   <li>✗ Maintain mutable state</li>
 * </ul>
 *
 * @since 1.0
 */
package com.shreeai.os.platform.kernels.multiagent.validation;