package platform.kernels.identity.api;

/**
 * <b>IdentityContract</b>
 *
 * <p>Groups all public contracts exposed by the Identity Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Aggregates all Identity Kernel public contracts into a single dependency.</li>
 *   <li>Represents the official API surface that other kernels depend upon.</li>
 *   <li>Enforces the principle that other kernels depend only on this contract,
 *       not on individual interfaces or internal implementations.</li>
 *   <li>Provides a stable, versioned API that shields consumers from internal changes.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All Identity operations accessible to other kernels
 * MUST be exposed through this contract. No kernel may access Identity
 * internals directly (KERNEL-ISO-001).</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-104, ADD-105, ADD-106, KERNEL-ISO-001</p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * // Other kernels depend only on IdentityContract
 * public class PlanningKernel {
 *     private final IdentityContract identity;
 *
 *     public PlanningKernel(IdentityContract identity) {
 *         this.identity = identity;
 *     }
 *
 *     public void execute() {
 *         // Access commands through the unified contract
 *         identity.commands().createIdentity(request);
 *
 *         // Access queries through the unified contract
 *         IdentityResult result = identity.queries().findIdentity(id);
 *
 *         // Access events through the unified contract
 *         String event = identity.events().IDENTITY_CREATED;
 *     }
 * }
 * </pre>
 *
 * @see platform.kernels.identity.api.IdentityKernel
 * @see platform.kernels.identity.api.IdentityCommands
 * @see platform.kernels.identity.api.IdentityQueries
 * @see platform.kernels.identity.api.IdentityEvents
 */
public interface IdentityContract {

    /**
     * Returns the primary kernel interface providing access to all Identity operations.
     *
     * <p>This is the main entry point for interacting with the Identity Kernel.
     * It provides access to commands, queries, and events through a unified interface.</p>
     *
     * @return the IdentityKernel interface
     */
    IdentityKernel kernel();
}