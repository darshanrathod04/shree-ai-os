package platform.core.discovery.model;

import platform.core.registry.model.KernelId;

import java.util.Objects;

/**
 * <b>DiscoveryResult</b>
 *
 * <p>Represents the result of capability discovery within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Composes the capability, kernel, contract, and status into a single value object.</li>
 *   <li>Serves as the return type for discovery operations.</li>
 *   <li>Contains no behavior — it is a pure data carrier representing the discovery outcome.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields SHALL be non-null and validated at construction time.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see CapabilityId
 * @see ContractId
 * @see ResolutionStatus
 * @see KernelId
 */
public final class DiscoveryResult {

    private final CapabilityId capabilityId;
    private final KernelId kernelId;
    private final ContractId contractId;
    private final ResolutionStatus status;

    /**
     * Constructs a new {@code DiscoveryResult} with the given capability, kernel, contract, and status.
     *
     * @param capabilityId the capability identifier (must not be null)
     * @param kernelId     the kernel identifier (must not be null)
     * @param contractId   the contract identifier (must not be null)
     * @param status       the resolution status (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public DiscoveryResult(CapabilityId capabilityId, KernelId kernelId, ContractId contractId, ResolutionStatus status) {
        this.capabilityId = Objects.requireNonNull(capabilityId, "CapabilityId must not be null");
        this.kernelId = Objects.requireNonNull(kernelId, "KernelId must not be null");
        this.contractId = Objects.requireNonNull(contractId, "ContractId must not be null");
        this.status = Objects.requireNonNull(status, "ResolutionStatus must not be null");
    }

    /**
     * Returns the capability identifier.
     *
     * @return the capability identifier
     */
    public CapabilityId capabilityId() {
        return capabilityId;
    }

    /**
     * Returns the kernel identifier.
     *
     * @return the kernel identifier
     */
    public KernelId kernelId() {
        return kernelId;
    }

    /**
     * Returns the contract identifier.
     *
     * @return the contract identifier
     */
    public ContractId contractId() {
        return contractId;
    }

    /**
     * Returns the resolution status.
     *
     * @return the resolution status
     */
    public ResolutionStatus status() {
        return status;
    }

    /**
     * Compares this {@code DiscoveryResult} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code DiscoveryResult} with the same
     *         capability, kernel, contract, and status
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveryResult that = (DiscoveryResult) o;
        return capabilityId.equals(that.capabilityId)
                && kernelId.equals(that.kernelId)
                && contractId.equals(that.contractId)
                && status == that.status;
    }

    /**
     * Returns the hash code for this {@code DiscoveryResult}.
     *
     * @return the hash code based on all fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(capabilityId, kernelId, contractId, status);
    }

    /**
     * Returns a string representation of this {@code DiscoveryResult}.
     *
     * @return a string containing the capability, kernel, contract, and status
     */
    @Override
    public String toString() {
        return "DiscoveryResult{"
                + "capabilityId=" + capabilityId
                + ", kernelId=" + kernelId
                + ", contractId=" + contractId
                + ", status=" + status
                + '}';
    }
}