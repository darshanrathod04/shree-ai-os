package platform.core.plugin.verification;

/**
 * <b>VerificationSeverity</b>
 *
 * <p>Severity level of a verification issue within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Classifies verification issues by severity.</li>
 *   <li>Contains no business logic.</li>
 *   <li>Immutable by design.</li>
 * </ul>
 *
 * <p><b>Values:</b></p>
 * <ul>
 *   <li>{@link #INFO} — Informational notice, does not affect validity.</li>
 *   <li>{@link #WARNING} — Potential concern, does not affect validity.</li>
 *   <li>{@link #ERROR} — Violates a requirement, makes the plugin invalid.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 */
public enum VerificationSeverity {

    /** Informational notice, does not affect validity. */
    INFO,

    /** Potential concern, does not affect validity. */
    WARNING,

    /** Violates a requirement, makes the plugin invalid. */
    ERROR
}