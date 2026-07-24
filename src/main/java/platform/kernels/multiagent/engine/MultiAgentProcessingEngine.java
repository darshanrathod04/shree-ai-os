package platform.kernels.multiagent.engine;

import java.util.List;

import platform.kernels.multiagent.model.AgentCommunication;
import platform.kernels.multiagent.model.AgentDescriptor;
import platform.kernels.multiagent.model.AgentRegistration;
import platform.kernels.multiagent.model.AgentRequest;
import platform.kernels.multiagent.model.AgentResponse;
import platform.kernels.multiagent.model.MultiAgentMetrics;

/**
 * <b>MultiAgentProcessingEngine</b>
 *
 * <p>Canonical processing engine interface for the Multi-Agent Kernel.
 * Defines the contract for deterministic processing operations delegated by the Service Layer.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-106, EIO-ARCH-001</p>
 *
 * <p>MultiAgentProcessingEngine is the public processing contract for the Engine layer.
 * It defines operations corresponding to the existing Multi-Agent public/service contract.
 * The Engine evaluates and computes deterministic outcomes; it does not perform
 * infrastructure execution, persistence, networking, or agent lifecycle execution.</p>
 *
 * <p><b>Architectural Separation:</b></p>
 * <pre>
 * Service coordinates
 * Engine computes (deterministic outcomes)
 * </pre>
 *
 * @since 1.0
 */
public interface MultiAgentProcessingEngine {

    /**
     * Evaluates an agent registration request and produces a deterministic processing outcome.
     *
     * <p>The engine evaluates canonical registration input and interprets already
     * validated registration metadata. It does NOT persist agents, maintain a registry,
     * contact external systems, start agents, or execute agents.</p>
     *
     * @param registration the agent registration (must not be {@code null})
     * @return the registration response
     * @throws IllegalArgumentException if registration is {@code null}
     * @since 1.0
     */
    AgentResponse registerAgent(AgentRegistration registration);

    /**
     * Evaluates an agent unregistration request and produces a deterministic processing outcome.
     *
     * <p>The engine evaluates the unregistration request and determines a processing outcome.
     * It does NOT mutate persistent registration storage, terminate processes, perform
     * infrastructure cleanup, or communicate with external agents.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the unregistration response
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     * @since 1.0
     */
    AgentResponse unregisterAgent(String agentId);

    /**
     * Evaluates discovery criteria and produces a deterministic discovery outcome.
     *
     * <p>The engine evaluates discovery criteria and performs deterministic computation
     * against data explicitly provided through the approved contract. It does NOT query
     * databases, perform network discovery, call remote registries, or maintain hidden
     * mutable agent registries.</p>
     *
     * @param request the discovery criteria (must not be {@code null})
     * @return list of matching agent descriptors
     * @throws IllegalArgumentException if request is {@code null}
     * @since 1.0
     */
    List<AgentDescriptor> discoverAgents(AgentRequest request);

    /**
     * Evaluates a Chief-mediated communication request and produces a deterministic
     * communication processing outcome.
     *
     * <p>Critical architectural invariant: All communication must flow through the
     * Chief Kernel. The engine evaluates communication metadata and preserves
     * routing/orchestration constraints encoded by existing models. It does NOT send
     * messages, open sockets, call HTTP endpoints, use message brokers, transport payloads,
     * or directly invoke another agent.</p>
     *
     * @param communication the communication message (must not be {@code null})
     * @return the communication response
     * @throws IllegalArgumentException if communication is {@code null}
     * @since 1.0
     */
    AgentResponse communicate(AgentCommunication communication);

    /**
     * Evaluates the kernel health status using only deterministic information available
     * through existing approved contracts.
     *
     * <p>The engine produces health information without performing network health probes,
     * querying databases, inspecting external infrastructure, or introducing monitoring
     * frameworks.</p>
     *
     * @return kernel health metrics
     * @since 1.0
     */
    MultiAgentMetrics getKernelHealth();
}