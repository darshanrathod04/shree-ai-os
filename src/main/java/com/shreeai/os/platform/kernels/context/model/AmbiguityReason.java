package com.shreeai.os.platform.kernels.context.model;

import java.util.Objects;

/**
 * <b>AmbiguityReason</b>
 *
 * <p>Immutable description of a single ambiguity risk diagnosed from the
 * cognitive artifacts of a user request.</p>
 *
 * <p><b>Deterministic Severity:</b> The severity must be exactly one of the
 * four locked values {@value #SEVERITY_LOW} (LOW), {@value #SEVERITY_MEDIUM}
 * (MEDIUM), {@value #SEVERITY_HIGH} (HIGH) or {@value #SEVERITY_CRITICAL}
 * (CRITICAL). Arbitrary decimals are rejected by the canonical constructor.</p>
 *
 * <p><b>Architectural Responsibility:</b> Provides a traceable, explainable
 * reason for why a request is ambiguous. It carries no runtime decision.</p>
 *
 * <p><b>Ownership:</b> Context Kernel - P1 Context Intelligence</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param type the ambiguity category (must not be null)
 * @param explanation why this risk was diagnosed (must not be null)
 * @param severity the deterministic severity (one of the locked constants)
 */
public record AmbiguityReason(AmbiguityType type, String explanation, double severity) {

    /** Locked LOW severity level. */
    public static final double SEVERITY_LOW = 0.25;

    /** Locked MEDIUM severity level. */
    public static final double SEVERITY_MEDIUM = 0.50;

    /** Locked HIGH severity level. */
    public static final double SEVERITY_HIGH = 0.75;

    /** Locked CRITICAL severity level. */
    public static final double SEVERITY_CRITICAL = 1.00;

    /**
     * Compact constructor that validates the type, explanation and rejects
     * arbitrary severity decimals (the four locked values only).
     *
     * @throws NullPointerException if type or explanation is null
     * @throws IllegalArgumentException if severity is not a locked value
     */
    public AmbiguityReason {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
        if (!isLockedSeverity(severity)) {
            throw new IllegalArgumentException(
                    "severity must be one of 0.25, 0.50, 0.75 or 1.00 but was " + severity);
        }
    }

    private static boolean isLockedSeverity(double value) {
        return value == SEVERITY_LOW || value == SEVERITY_MEDIUM
                || value == SEVERITY_HIGH || value == SEVERITY_CRITICAL;
    }
}