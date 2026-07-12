package platform.core.discovery.validator;

import platform.core.discovery.model.CapabilityId;
import platform.core.discovery.model.ContractId;
import platform.core.discovery.model.DiscoveryResult;
import platform.core.discovery.model.ResolutionStatus;
import platform.core.registry.validator.ValidationResult;

import java.util.regex.Pattern;

/**
 * <b>DiscoveryValidator</b>
 *
 * <p>Stateless validator that ensures discovery requests satisfy all architectural
 * requirements before capability resolution.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that discovery requests meet all prerequisites.</li>
 *   <li>Answers the question: "Can this discovery request be processed?" — it never performs discovery.</li>
 *   <li>Returns structured {@link ValidationResult} supporting multiple errors in a single execution.</li>
 *   <li>Reuses the approved Registry validation architecture.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Stateless — all state is passed as method parameters.</li>
 *   <li>Deterministic — same inputs always produce the same result.</li>
 *   <li>No business logic — validation rules only.</li>
 *   <li>No model mutation — models are never modified.</li>
 *   <li>Never performs discovery — validation only.</li>
 *   <li>Never accesses Registry — independent validation layer.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006, ADD-PLT-202, ADD-PLT-205,
 * ADD-PLT-206</p>
 *
 * @see ValidationResult
 * @see CapabilityId
 * @see ContractId
 * @see DiscoveryResult
 */
public final class DiscoveryValidator {

    private static final Pattern CAPABILITY_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");
    private static final Pattern CONTRACT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

    /**
     * Validates a {@link CapabilityId} for discovery readiness.
     *
     * <p>Performs the following validations:</p>
     * <ul>
     *   <li>CapabilityId exists</li>
     *   <li>CapabilityId format is valid</li>
     * </ul>
     *
     * @param capabilityId the capability identifier to validate (must not be null)
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code capabilityId} is null
     */
    public ValidationResult validateCapabilityId(CapabilityId capabilityId) {
        if (capabilityId == null) {
            throw new NullPointerException("CapabilityId must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // CapabilityId format
        String idValue = capabilityId.value();
        if (idValue == null || idValue.isBlank()) {
            builder.addError("CapabilityId value must not be null or blank");
        } else if (!CAPABILITY_ID_PATTERN.matcher(idValue).matches()) {
            builder.addError("CapabilityId format is invalid: '" + idValue
                    + "'. Must contain only alphanumeric characters and hyphens");
        }

        return builder.build();
    }

    /**
     * Validates a {@link ContractId} for discovery readiness.
     *
     * <p>Performs the following validations:</p>
     * <ul>
     *   <li>ContractId exists</li>
     *   <li>ContractId format is valid</li>
     * </ul>
     *
     * @param contractId the contract identifier to validate (must not be null)
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code contractId} is null
     */
    public ValidationResult validateContractId(ContractId contractId) {
        if (contractId == null) {
            throw new NullPointerException("ContractId must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // ContractId format
        String idValue = contractId.value();
        if (idValue == null || idValue.isBlank()) {
            builder.addError("ContractId value must not be null or blank");
        } else if (!CONTRACT_ID_PATTERN.matcher(idValue).matches()) {
            builder.addError("ContractId format is invalid: '" + idValue
                    + "'. Must contain only alphanumeric characters and hyphens");
        }

        return builder.build();
    }

    /**
     * Validates a {@link DiscoveryResult} for consistency.
     *
     * <p>Performs the following validations:</p>
     * <ul>
     *   <li>DiscoveryResult exists</li>
     *   <li>ResolutionStatus is valid</li>
     *   <li>DiscoveryResult consistency</li>
     * </ul>
     *
     * @param result the discovery result to validate (must not be null)
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code result} is null
     */
    public ValidationResult validateDiscoveryResult(DiscoveryResult result) {
        if (result == null) {
            throw new NullPointerException("DiscoveryResult must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // ResolutionStatus validity
        ResolutionStatus status = result.status();
        if (status == null) {
            builder.addError("ResolutionStatus must not be null");
        }

        // DiscoveryResult consistency
        if (result.capabilityId() == null) {
            builder.addError("DiscoveryResult capabilityId must not be null");
        }
        if (result.kernelId() == null) {
            builder.addError("DiscoveryResult kernelId must not be null");
        }
        if (result.contractId() == null) {
            builder.addError("DiscoveryResult contractId must not be null");
        }

        // Consistency check: if status is FOUND, all fields should be present
        if (status == ResolutionStatus.FOUND) {
            if (result.capabilityId() == null || result.kernelId() == null || result.contractId() == null) {
                builder.addWarning("DiscoveryResult with FOUND status should have all fields populated");
            }
        }

        return builder.build();
    }

    /**
     * Validates capability metadata consistency.
     *
     * <p>This is a method stub for capability metadata validation. The actual
     * metadata validation logic depends on the discovery implementation and is
     * not implemented in this validation layer.</p>
     *
     * <p>When the discovery implementation is available, callers should pass
     * the capability metadata to this method for validation.</p>
     *
     * @param capabilityId the capability identifier to validate (must not be null)
     * @param metadata     optional capability metadata (may be null)
     * @return a {@link ValidationResult} with validation results
     * @throws NullPointerException if {@code capabilityId} is null
     */
    public ValidationResult validateCapabilityMetadata(CapabilityId capabilityId, Object metadata) {
        if (capabilityId == null) {
            throw new NullPointerException("CapabilityId must not be null");
        }

        // Method stub - metadata validation logic depends on implementation
        return ValidationResult.builder().build();
    }
}