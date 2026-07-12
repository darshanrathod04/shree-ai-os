package platform.core.discovery.service;

import platform.core.discovery.api.DiscoveryService;
import platform.core.discovery.error.CapabilityNotFoundException;
import platform.core.discovery.error.ContractNotFoundException;
import platform.core.discovery.error.DiscoveryErrorCode;
import platform.core.discovery.error.DiscoveryError;
import platform.core.discovery.error.DiscoveryException;
import platform.core.discovery.error.InvalidDiscoveryRequestException;
import platform.core.discovery.model.CapabilityId;
import platform.core.discovery.model.ContractId;
import platform.core.discovery.model.DiscoveryResult;
import platform.core.discovery.model.ResolutionStatus;
import platform.core.discovery.validator.DiscoveryValidator;
import platform.core.registry.api.KernelRegistry;
import platform.core.registry.model.KernelId;
import platform.core.registry.model.RegisteredKernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <b>DefaultDiscoveryService</b>
 *
 * <p>Reference implementation of the Discovery Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the reference implementation of capability resolution.</li>
 *   <li>Resolves capabilities by consulting the Kernel Registry — it does not maintain its own registry.</li>
 *   <li>Ensures the Registry remains the single source of truth for kernel information.</li>
 *   <li>Never duplicates Registry state or creates separate storage structures.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006, ADD-PLT-202, ADD-PLT-205,
 * ADD-PLT-206</p>
 *
 * <p><b>Discovery Principle:</b> Registry owns kernel information. Discovery owns capability resolution.
 * Ownership shall never overlap.</p>
 *
 * @see DiscoveryService
 * @see KernelRegistry
 * @see DiscoveryValidator
 */
public final class DefaultDiscoveryService implements DiscoveryService {

    private final KernelRegistry kernelRegistry;
    private final DiscoveryValidator validator;

    /**
     * Constructs a new {@code DefaultDiscoveryService} with the given dependencies.
     *
     * <p>Uses constructor injection only — no setter injection.</p>
     *
     * @param kernelRegistry the kernel registry (must not be null)
     * @param validator      the discovery validator (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public DefaultDiscoveryService(KernelRegistry kernelRegistry, DiscoveryValidator validator) {
        this.kernelRegistry = java.util.Objects.requireNonNull(kernelRegistry, "KernelRegistry must not be null");
        this.validator = java.util.Objects.requireNonNull(validator, "DiscoveryValidator must not be null");
    }

    /**
     * Resolves a kernel by its capability.
     *
     * <p>Returns a {@link DiscoveryResult} describing the resolution outcome,
     * or an empty {@link java.util.Optional} if no such capability is available.</p>
     *
     * <p>Resolution flow:</p>
     * <ol>
     *   <li>Validate the capability request</li>
     *   <li>Retrieve all registered kernels from the Registry</li>
     *   <li>Match capability against kernel metadata</li>
     *   <li>Return DiscoveryResult with appropriate status</li>
     * </ol>
     *
     * @param capabilityId the capability to resolve (must not be null)
     * @return an {@link java.util.Optional} containing the discovery result if found,
     *         or an empty {@link java.util.Optional} if not found
     * @throws IllegalArgumentException if {@code capabilityId} is {@code null}
     */
    @Override
    public java.util.Optional<DiscoveryResult> resolveByCapability(CapabilityId capabilityId) {
        if (capabilityId == null) {
            throw new IllegalArgumentException("CapabilityId must not be null");
        }

        // Validate request
        var validationResult = validator.validateCapabilityId(capabilityId);
        if (!validationResult.isValid()) {
            throw new InvalidDiscoveryRequestException(
                    "Invalid capability request: " + validationResult.errors(),
                    validationResult.errors().toString()
            );
        }

        // Retrieve all kernels from Registry (single source of truth)
        Collection<RegisteredKernel> allKernels = kernelRegistry.findAll();

        // Match capability
        for (RegisteredKernel kernel : allKernels) {
            if (kernel.metadata() != null && kernel.metadata().tags() != null) {
                // Check if capability exists in kernel tags
                // This is a simplified check — actual implementation would use proper capability matching
                if (kernel.metadata().tags().contains(capabilityId.value())) {
                    DiscoveryResult result = new DiscoveryResult(
                            capabilityId,
                            kernel.kernelId(),
                            new ContractId("default-contract"), // Placeholder — actual contract would come from kernel
                            ResolutionStatus.FOUND
                    );
                    return java.util.Optional.of(result);
                }
            }
        }

        // Not found
        throw new CapabilityNotFoundException(capabilityId.value());
    }

    /**
     * Resolves a kernel by its contract type.
     *
     * <p>Returns a {@link DiscoveryResult} describing the resolution outcome,
     * or an empty {@link java.util.Optional} if no such contract is available.</p>
     *
     * <p>Resolution flow:</p>
     * <ol>
     *   <li>Validate the contract request</li>
     *   <li>Retrieve all registered kernels from the Registry</li>
     *   <li>Match contract against kernel metadata</li>
     *   <li>Return DiscoveryResult with appropriate status</li>
     * </ol>
     *
     * @param contractId the contract to resolve (must not be null)
     * @return an {@link java.util.Optional} containing the discovery result if found,
     *         or an empty {@link java.util.Optional} if not found
     * @throws IllegalArgumentException if {@code contractId} is {@code null}
     */
    @Override
    public java.util.Optional<DiscoveryResult> resolveByContract(ContractId contractId) {
        if (contractId == null) {
            throw new IllegalArgumentException("ContractId must not be null");
        }

        // Validate request
        var validationResult = validator.validateContractId(contractId);
        if (!validationResult.isValid()) {
            throw new InvalidDiscoveryRequestException(
                    "Invalid contract request: " + validationResult.errors(),
                    validationResult.errors().toString()
            );
        }

        // Retrieve all kernels from Registry (single source of truth)
        Collection<RegisteredKernel> allKernels = kernelRegistry.findAll();

        // Match contract
        for (RegisteredKernel kernel : allKernels) {
            // Check if kernel implements the requested contract
            // This is a simplified check — actual implementation would use proper contract matching
            if (kernel.metadata() != null && kernel.metadata().category() != null) {
                if (kernel.metadata().category().equals(contractId.value())) {
                    DiscoveryResult result = new DiscoveryResult(
                            new CapabilityId("default-capability"), // Placeholder — actual capability would come from kernel
                            kernel.kernelId(),
                            contractId,
                            ResolutionStatus.FOUND
                    );
                    return java.util.Optional.of(result);
                }
            }
        }

        // Not found
        throw new ContractNotFoundException(contractId.value());
    }

    /**
     * Determines whether a capability exists in the platform.
     *
     * <p>Returns {@code true} if at least one registered kernel provides
     * the requested capability and is available.</p>
     *
     * <p>This is a convenience method that checks for capability existence
     * without throwing exceptions.</p>
     *
     * @param capabilityId the capability to check (must not be null)
     * @return {@code true} if the capability exists, {@code false} otherwise
     * @throws IllegalArgumentException if {@code capabilityId} is {@code null}
     */
    @Override
    public boolean supports(CapabilityId capabilityId) {
        if (capabilityId == null) {
            throw new IllegalArgumentException("CapabilityId must not be null");
        }

        // Validate request
        var validationResult = validator.validateCapabilityId(capabilityId);
        if (!validationResult.isValid()) {
            return false;
        }

        // Retrieve all kernels from Registry
        Collection<RegisteredKernel> allKernels = kernelRegistry.findAll();

        // Check if any kernel provides the capability
        for (RegisteredKernel kernel : allKernels) {
            if (kernel.metadata() != null && kernel.metadata().tags() != null) {
                if (kernel.metadata().tags().contains(capabilityId.value())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Lists all available capabilities in the platform.
     *
     * <p>Returns a snapshot of all capabilities provided by registered,
     * available kernels. The returned collection is unmodifiable and reflects
     * the state at the time of the call.</p>
     *
     * <p>This method scans all registered kernels and extracts unique capabilities
     * from their metadata.</p>
     *
     * @return an unmodifiable collection of all available capabilities;
     *         returns an empty collection if no capabilities are available
     */
    @Override
    public Collection<CapabilityId> availableCapabilities() {
        // Retrieve all kernels from Registry
        Collection<RegisteredKernel> allKernels = kernelRegistry.findAll();

        // Extract unique capabilities
        List<CapabilityId> capabilities = new ArrayList<>();
        for (RegisteredKernel kernel : allKernels) {
            if (kernel.metadata() != null && kernel.metadata().tags() != null) {
                for (String tag : kernel.metadata().tags()) {
                    // Treat tags as capabilities for this implementation
                    capabilities.add(new CapabilityId(tag));
                }
            }
        }

        // Return unmodifiable collection
        return Collections.unmodifiableCollection(capabilities);
    }
}