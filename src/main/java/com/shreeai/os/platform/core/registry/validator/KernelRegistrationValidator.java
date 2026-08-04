package com.shreeai.os.platform.core.registry.validator;

import com.shreeai.os.platform.core.registry.model.KernelId;
import com.shreeai.os.platform.core.registry.model.KernelMetadata;
import com.shreeai.os.platform.core.registry.model.KernelVersion;
import com.shreeai.os.platform.core.registry.model.RegisteredKernel;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * <b>KernelRegistrationValidator</b>
 *
 * <p>Stateless validator that ensures every Kernel satisfies the architectural
 * requirements before registration.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that a {@link RegisteredKernel} meets all registration prerequisites.</li>
 *   <li>Answers the question: "Can this Kernel be registered?" — it never registers the kernel.</li>
 *   <li>Returns structured {@link ValidationResult} supporting multiple errors in a single execution.</li>
 *   <li>Enforces the architectural invariants defined in KERNEL-007.</li>
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
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005, KERNEL-007</p>
 *
 * @see ValidationResult
 * @see RegisteredKernel
 */
public final class KernelRegistrationValidator {

    private static final Pattern KERNEL_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

    /**
     * Validates a {@link RegisteredKernel} for registration readiness.
     *
     * <p>Performs the following validations:</p>
     * <ul>
     *   <li>KernelId exists</li>
     *   <li>KernelId format is valid</li>
     *   <li>KernelVersion exists</li>
     *   <li>KernelMetadata exists</li>
     *   <li>Kernel name is not blank</li>
     *   <li>Description is not blank</li>
     *   <li>Category exists</li>
     *   <li>Metadata consistency</li>
     * </ul>
     *
     * @param kernel the registered kernel to validate (must not be null)
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code kernel} is null
     */
    public ValidationResult validate(RegisteredKernel kernel) {
        if (kernel == null) {
            throw new NullPointerException("RegisteredKernel must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // KernelId exists
        KernelId kernelId = kernel.kernelId();
        if (kernelId == null) {
            builder.addError("KernelId must not be null");
        } else {
            // KernelId format
            String idValue = kernelId.value();
            if (idValue == null || idValue.isBlank()) {
                builder.addError("KernelId value must not be null or blank");
            } else if (!KERNEL_ID_PATTERN.matcher(idValue).matches()) {
                builder.addError("KernelId format is invalid: '" + idValue
                        + "'. Must contain only alphanumeric characters and hyphens");
            }
        }

        // KernelVersion exists
        KernelVersion version = kernel.version();
        if (version == null) {
            builder.addError("KernelVersion must not be null");
        }

        // KernelMetadata exists
        KernelMetadata metadata = kernel.metadata();
        if (metadata == null) {
            builder.addError("KernelMetadata must not be null");
        } else {
            // Kernel name not blank
            String name = metadata.name();
            if (name == null || name.isBlank()) {
                builder.addError("Kernel name must not be null or blank");
            }

            // Description not blank
            String description = metadata.description();
            if (description == null || description.isBlank()) {
                builder.addError("Kernel description must not be null or blank");
            }

            // Category exists
            String category = metadata.category();
            if (category == null || category.isBlank()) {
                builder.addError("Kernel category must not be null or blank");
            }

            // Metadata consistency
            if (name != null && !name.isBlank() && category != null && !category.isBlank()) {
                if (name.equals(category)) {
                    builder.addWarning("Kernel name and category should not be identical");
                }
            }
        }

        return builder.build();
    }

    /**
     * Validates that the given {@link KernelId} is not already registered.
     *
     * <p>This is a method stub for duplicate KernelId detection. The actual
     * lookup mechanism depends on the registry implementation and is not
     * implemented in this validation layer.</p>
     *
     * <p>When the registry implementation is available, callers should pass
     * the set of currently registered kernel identifiers to this method.</p>
     *
     * @param kernelId       the kernel identifier to check for duplicates (must not be null)
     * @param registeredIds  the collection of currently registered kernel identifiers
     *                       (must not be null)
     * @return a {@link ValidationResult} with an error if the kernelId is already registered,
     *         or a valid result otherwise
     * @throws NullPointerException if {@code kernelId} or {@code registeredIds} is null
     */
    public ValidationResult validateNoDuplicate(KernelId kernelId, Collection<KernelId> registeredIds) {
        if (kernelId == null) {
            throw new NullPointerException("KernelId must not be null");
        }
        if (registeredIds == null) {
            throw new NullPointerException("RegisteredIds must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        if (registeredIds.contains(kernelId)) {
            builder.addError("KernelId '" + kernelId.value() + "' is already registered");
        }

        return builder.build();
    }
}