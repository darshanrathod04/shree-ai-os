package platform.core.health.model;

/**
 * <b>HealthSeverity</b>
 *
 * <p>Represents the severity of health findings within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines severity levels for health indicators.</li>
 *   <li>Provides a type-safe enumeration for severity classification.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum HealthSeverity {

    /**
     * Informational finding — no action required.
     */
    INFO,

    /**
     * Warning finding — attention may be required.
     */
    WARNING,

    /**
     * Error finding — action is required.
     */
    ERROR,

    /**
     * Critical finding — immediate action is required.
     */
    CRITICAL
}