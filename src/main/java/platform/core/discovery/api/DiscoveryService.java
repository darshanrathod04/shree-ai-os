package platform.core.discovery.api;

import platform.core.discovery.model.CapabilityId;
import platform.core.discovery.model.ContractId;
import platform.core.discovery.model.DiscoveryResult;

import java.util.Collection;
import java.util.Optional;

/**
 * <b>DiscoveryService</b>
 *
 * <p>The official Platform contract for capability resolution within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Enables kernels to discover other kernels by capability or contract without compile-time dependencies.</li>
 *   <li>Hides deployment details from requesting kernels.</li>
 *   <li>Ensures discovery remains independent of registry implementation.</li>
 *   <li>Supports version compatibility resolution (KD-004).</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> Discovery only returns available kernels unless explicitly requested otherwise (KD-005).</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006, ADD-PLT-202, ADD-PLT-205,
 * ADD-PLT-206</p>
 *
 * <p><b>Discovery Principle:</b> Kernels discover capabilities, not implementations.</p>
 *
 * @see platform.core.discovery.api package-info
 */
public interface DiscoveryService {

    /**
     * Resolves a kernel by its capability.
     *
     * <p>Returns a {@link DiscoveryResult} describing the resolution outcome,
     * or an empty {@link Optional} if no such capability is available.</p>
     *
     * <p>Discovery SHALL return only kernels that are:</p>
     * <ul>
     *   <li>Registered (KD-001)</li>
     *   <li>Available (not suspended or retired)</li>
     *   <li>Compatible with the requested capability</li>
     * </ul>
     *
     * @param capabilityId the capability to resolve (must not be null)
     * @return an {@link Optional} containing the discovery result if found,
     *         or an empty {@link Optional} if not found
     * @throws IllegalArgumentException if {@code capabilityId} is {@code null}
     */
    Optional<DiscoveryResult> resolveByCapability(CapabilityId capabilityId);

    /**
     * Resolves a kernel by its contract type.
     *
     * <p>Returns a {@link DiscoveryResult} describing the resolution outcome,
     * or an empty {@link Optional} if no such contract is available.</p>
     *
     * <p>Discovery SHALL return only kernels that are:</p>
     * <ul>
     *   <li>Registered (KD-001)</li>
     *   <li>Available (not suspended or retired)</li>
     *   <li>Compatible with the requested contract version</li>
     * </ul>
     *
     * <p>Version compatibility is enforced per KD-004.</p>
     *
     * @param contractId the contract to resolve (must not be null)
     * @return an {@link Optional} containing the discovery result if found,
     *         or an empty {@link Optional} if not found
     * @throws IllegalArgumentException if {@code contractId} is {@code null}
     */
    Optional<DiscoveryResult> resolveByContract(ContractId contractId);

    /**
     * Determines whether a capability exists in the platform.
     *
     * <p>Returns {@code true} if at least one registered kernel provides
     * the requested capability and is available.</p>
     *
     * <p>This is a convenience method equivalent to checking
     * {@code resolveByCapability(capabilityId).isPresent()} but may be more
     * efficient in implementations that support direct existence checks.</p>
     *
     * @param capabilityId the capability to check (must not be null)
     * @return {@code true} if the capability exists, {@code false} otherwise
     * @throws IllegalArgumentException if {@code capabilityId} is {@code null}
     */
    boolean supports(CapabilityId capabilityId);

    /**
     * Lists all available capabilities in the platform.
     *
     * <p>Returns a snapshot of all capabilities provided by registered,
     * available kernels. The returned collection is unmodifiable and reflects
     * the state at the time of the call.</p>
     *
     * @return an unmodifiable collection of all available capabilities;
     *         returns an empty collection if no capabilities are available
     */
    Collection<CapabilityId> availableCapabilities();
}
