package com.shreeai.os.platform.resolver;

import java.util.Objects;

/**
 * <b>CapabilityRequirement</b>
 *
 * <p>Immutable declaration that a single {@link CapabilityType} is required
 * for a request, along with its priority and the reason it was selected.</p>
 *
 * <p><b>Ownership:</b> Platform Resolver</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param capability the required capability
 * @param priority   how strongly the capability is required
 * @param reason     human-readable justification for the requirement
 */
public record CapabilityRequirement(
        CapabilityType capability,
        Priority priority,
        String reason
) {

    /**
     * Relative strength of a capability requirement.
     */
    public enum Priority {
        /** The capability must run to fulfil the request. */
        REQUIRED,

        /** The capability should run when available. */
        RECOMMENDED,

        /** The capability may run when beneficial. */
        OPTIONAL
    }

    public CapabilityRequirement {
        Objects.requireNonNull(capability, "capability must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }
}