/**
 * <b>Multi-Agent Kernel API Layer</b>
 *
 * <p>This package provides the public contracts for the Multi-Agent Kernel.
 * The API defines how agents register, unregister, advertise capabilities,
 * discover peers, manage lifecycle, and communicate.</p>
 *
 * <p><b>API Philosophy:</b></p>
 * <ul>
 *   <li><b>Contracts only</b> — defines interfaces, no implementation</li>
 *   <li><b>Chief Kernel coordination</b> — all coordination flows through Chief Kernel</li>
 *   <li><b>Immutable types</b> — bootstrap records are immutable</li>
 *   <li><b>No direct communication</b> — agent-to-agent communication is forbidden</li>
 * </ul>
 *
 * <p><b>Communication Architecture:</b></p>
 * <pre>
 * Agent A
 *    │
 *    ▼
 * Chief Kernel
 *    │
 *    ▼
 * Agent B
 * </pre>
 *
 * <p><b>API Components:</b></p>
 * <ul>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentService} — primary façade</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.AgentRegistryService} — agent registration</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.AgentDiscoveryService} — agent discovery</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.CapabilityRegistryService} — capability management</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.AgentLifecycleService} — lifecycle management</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.AgentCommunicationService} — communication</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentTypes} — bootstrap types</li>
 * </ul>
 *
 * <p><b>Bootstrap Records:</b></p>
 * <ul>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentTypes.AgentRequest}</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentTypes.AgentResponse}</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentTypes.AgentDescriptor}</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentTypes.AgentCapability}</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentTypes.AgentRegistration}</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentTypes.AgentStatus}</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentTypes.AgentCommunication}</li>
 *   <li>{@link com.shreeai.os.platform.kernels.multiagent.api.MultiAgentTypes.MultiAgentMetrics}</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li><b>Interface-only</b> — no implementation logic</li>
 *   <li><b>Immutable records</b> — all bootstrap types are immutable</li>
 *   <li><b>Constructor validation</b> — Objects.requireNonNull()</li>
 *   <li><b>Defensive copying</b> — List.copyOf(), Map.copyOf()</li>
 *   <li><b>No framework dependencies</b> — technology-agnostic</li>
 * </ul>
 *
 * <p><b>API Layer must never:</b></p>
 * <ul>
 *   <li>Implement services</li>
 *   <li>Implement networking</li>
 *   <li>Implement discovery</li>
 *   <li>Implement lifecycle</li>
 *   <li>Implement messaging</li>
 *   <li>Enable direct agent-to-agent communication</li>
 *   <li>Access persistence</li>
 *   <li>Access runtime</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — API Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-101, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
package com.shreeai.os.platform.kernels.multiagent.api;