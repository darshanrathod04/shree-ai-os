package com.shreeai.os.platform.kernels.reasoning.model;

/**
 * <b>VerificationIssueType</b>
 *
 * <p>Enum identifying the kind of issue found during self-verification.
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R4 Self Verification</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum VerificationIssueType {

    /** Evidence item references a hypothesis but no supporting evidence exists. */
    MISSING_EVIDENCE(1.00),

    /** A causal edge is not backed by a concept-graph relationship.
     *  Severity 0.25 */
    CONTRADICTION(0.75),

    /** A synthesized fact is not part of any causal chain.
     *  Severity 0.50 */
    INCOMPLETE_CHAIN(0.50),

    /** Supporting evidence has trust below the locked threshold.
     *  Severity 0.25 */
    LOW_TRUST(0.25),

    /** A hypothesis has no supporting evidence ids.
     *  Severity 1.00 */
    UNSUPPORTED_HYPOTHESIS(1.00);

    private final double severity;

    VerificationIssueType(double severity) {
        this.severity = severity;
    }

    /**
     * Returns the locked deterministic severity for this issue type.
     */
    public double severity() { return severity; }
}
