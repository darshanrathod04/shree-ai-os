 package platform.core.health.validator;

import platform.core.health.model.HealthCheck;
import platform.core.health.model.HealthComponent;
import platform.core.health.model.HealthComponentId;
import platform.core.health.model.HealthIndicator;
import platform.core.health.model.HealthMetrics;
import platform.core.health.model.HealthReport;
import platform.core.health.model.HealthSeverity;
import platform.core.health.model.HealthStatus;
import platform.core.registry.validator.ValidationResult;

/**
 * <b>HealthValidator</b>
 *
 * <p>Stateless validator that ensures every Health model satisfies the architectural
 * requirements before being used by the Health Engine.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that Health models meet all structural requirements.</li>
 *   <li>Answers the question: "Is this Health model valid?" — it never evaluates health.</li>
 *   <li>Returns structured {@link ValidationResult} supporting multiple errors in a single execution.</li>
 *   <li>Enforces the architectural invariants defined in ADD-PLT-202, ADD-PLT-205, ADD-PLT-206.</li>
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
 *   <li>No health evaluation — never pings services, monitors CPU, or evaluates health.</li>
 *   <li>No external access — never accesses Registry, Lifecycle, Event Bus, Configuration, or databases.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see ValidationResult
 * @see platform.core.health.model.HealthComponentId
 * @see platform.core.health.model.HealthComponent
 * @see platform.core.health.model.HealthIndicator
 * @see platform.core.health.model.HealthMetrics
 * @see platform.core.health.model.HealthCheck
 * @see platform.core.health.model.HealthReport
 */
public final class HealthValidator {

    /**
     * Private constructor to prevent instantiation.
     * This is a static utility class.
     */
    private HealthValidator() {
    }

    /**
     * Validates a {@link HealthComponentId}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>ComponentId must not be null</li>
     *   <li>Value must not be null or blank</li>
     * </ul>
     *
     * @param componentId the component identifier to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code componentId} is null
     */
    public static ValidationResult validateComponentId(HealthComponentId componentId) {
        if (componentId == null) {
            throw new NullPointerException("HealthComponentId must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        String value = componentId.value();
        if (value == null || value.isBlank()) {
            builder.addError("HealthComponentId value must not be null or blank");
        }

        return builder.build();
    }

    /**
     * Validates a {@link HealthComponent}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Component must not be null</li>
     *   <li>Id must not be null</li>
     *   <li>Name must not be null or blank</li>
     *   <li>Category must not be null or blank</li>
     * </ul>
     *
     * @param component the health component to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code component} is null
     */
    public static ValidationResult validateComponent(HealthComponent component) {
        if (component == null) {
            throw new NullPointerException("HealthComponent must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Id must not be null
        HealthComponentId id = component.id();
        if (id == null) {
            builder.addError("HealthComponentId must not be null");
        }

        // Name must not be null or blank
        String name = component.name();
        if (name == null || name.isBlank()) {
            builder.addError("HealthComponent name must not be null or blank");
        }

        // Category must not be null or blank
        String category = component.category();
        if (category == null || category.isBlank()) {
            builder.addError("HealthComponent category must not be null or blank");
        }

        return builder.build();
    }

    /**
     * Validates a {@link HealthIndicator}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Indicator must not be null</li>
     *   <li>Name must not be null or blank</li>
     *   <li>Status must not be null</li>
     *   <li>Severity must not be null</li>
     *   <li>Message must not be null or blank</li>
     * </ul>
     *
     * @param indicator the health indicator to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code indicator} is null
     */
    public static ValidationResult validateIndicator(HealthIndicator indicator) {
        if (indicator == null) {
            throw new NullPointerException("HealthIndicator must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Name must not be null or blank
        String name = indicator.name();
        if (name == null || name.isBlank()) {
            builder.addError("HealthIndicator name must not be null or blank");
        }

        // Status must not be null
        HealthStatus status = indicator.status();
        if (status == null) {
            builder.addError("HealthIndicator status must not be null");
        }

        // Severity must not be null
        HealthSeverity severity = indicator.severity();
        if (severity == null) {
            builder.addError("HealthIndicator severity must not be null");
        }

        // Message must not be null or blank
        String message = indicator.message();
        if (message == null || message.isBlank()) {
            builder.addError("HealthIndicator message must not be null or blank");
        }

        return builder.build();
    }

    /**
     * Validates a {@link HealthMetrics}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Metrics must not be null</li>
     *   <li>Availability must be greater than or equal to 0</li>
     *   <li>Response time must be greater than or equal to 0</li>
     *   <li>Uptime must be greater than or equal to 0</li>
     *   <li>Values map must not be null</li>
     * </ul>
     *
     * <p><b>Note:</b> This validator does not calculate metrics. It only validates
     * that the provided values meet the structural requirements.</p>
     *
     * @param metrics the health metrics to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code metrics} is null
     */
    public static ValidationResult validateMetrics(HealthMetrics metrics) {
        if (metrics == null) {
            throw new NullPointerException("HealthMetrics must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Availability must be >= 0
        double availability = metrics.availability();
        if (availability < 0) {
            builder.addError("HealthMetrics availability must be greater than or equal to 0");
        }

        // Response time must be >= 0
        double responseTime = metrics.responseTime();
        if (responseTime < 0) {
            builder.addError("HealthMetrics responseTime must be greater than or equal to 0");
        }

        // Uptime must be >= 0
        double uptime = metrics.uptime();
        if (uptime < 0) {
            builder.addError("HealthMetrics uptime must be greater than or equal to 0");
        }

        // Values map must not be null
        if (metrics.values() == null) {
            builder.addError("HealthMetrics values map must not be null");
        }

        return builder.build();
    }

    /**
     * Validates a {@link HealthCheck}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Check must not be null</li>
     *   <li>Component must not be null</li>
     * </ul>
     *
     * <p><b>Note:</b> This validator does not execute health checks. It only validates
     * the structural integrity of the request.</p>
     *
     * @param check the health check to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code check} is null
     */
    public static ValidationResult validateCheck(HealthCheck check) {
        if (check == null) {
            throw new NullPointerException("HealthCheck must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Component must not be null
        if (check.component() == null) {
            builder.addError("HealthCheck component must not be null");
        }

        return builder.build();
    }

    /**
     * Validates a {@link HealthReport}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Report must not be null</li>
     *   <li>Component must not be null</li>
     *   <li>Status must not be null</li>
     *   <li>Indicators list must not be null</li>
     *   <li>Metrics must not be null</li>
     *   <li>Timestamp must not be null</li>
     * </ul>
     *
     * <p><b>Note:</b> This validator does not inspect health logic. It only validates
     * the structural integrity of the report.</p>
     *
     * @param report the health report to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code report} is null
     */
    public static ValidationResult validateReport(HealthReport report) {
        if (report == null) {
            throw new NullPointerException("HealthReport must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Component must not be null
        if (report.component() == null) {
            builder.addError("HealthReport component must not be null");
        }

        // Status must not be null
        if (report.status() == null) {
            builder.addError("HealthReport status must not be null");
        }

        // Indicators list must not be null
        if (report.indicators() == null) {
            builder.addError("HealthReport indicators list must not be null");
        }

        // Metrics must not be null
        if (report.metrics() == null) {
            builder.addError("HealthReport metrics must not be null");
        }

        // Timestamp must not be null
        if (report.timestamp() == null) {
            builder.addError("HealthReport timestamp must not be null");
        }

        return builder.build();
    }
}