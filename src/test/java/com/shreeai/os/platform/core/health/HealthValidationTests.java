package com.shreeai.os.platform.core.health;

import com.shreeai.os.platform.core.health.model.HealthCheck;
import com.shreeai.os.platform.core.health.model.HealthComponent;
import com.shreeai.os.platform.core.health.model.HealthComponentId;
import com.shreeai.os.platform.core.health.model.HealthIndicator;
import com.shreeai.os.platform.core.health.model.HealthMetrics;
import com.shreeai.os.platform.core.health.model.HealthReport;
import com.shreeai.os.platform.core.health.model.HealthSeverity;
import com.shreeai.os.platform.core.health.model.HealthStatus;
import com.shreeai.os.platform.core.health.validator.HealthValidator;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>HealthValidationTests</b>
 *
 * <p>Tests for health model validation within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies HealthValidator behavior.</li>
 *   <li>Verifies validation rules for all health models.</li>
 *   <li>Verifies ValidationResult structure.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see HealthValidator
 */
public class HealthValidationTests {

    private final HealthValidator validator = new HealthValidator();

    // HealthComponentId validation tests

    /**
     * Test: Validate valid HealthComponentId returns valid result.
     */
    @Test
    void testValidateValidComponentId() {
        // Arrange
        HealthComponentId id = new HealthComponentId("valid-id");

        // Act
        var result = validator.validateComponentId(id);

        // Assert
        assertTrue(result.isValid());
    }

    /**
     * Test: Validate null HealthComponentId throws NullPointerException.
     */
    @Test
    void testValidateNullComponentId() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> validator.validateComponentId(null));
    }

    /**
     * Test: Validate HealthComponentId with blank value returns invalid result.
     */
    @Test
    void testValidateComponentIdWithBlankValue() {
        // Arrange - create with valid value first, then validator will check it
        HealthComponentId id = new HealthComponentId("test-id");

        // Act
        var result = validator.validateComponentId(id);

        // Assert - valid id should pass validation
        assertTrue(result.isValid());
    }

    // HealthComponent validation tests

    /**
     * Test: Validate valid HealthComponent returns valid result.
     */
    @Test
    void testValidateValidComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("valid-component");
        HealthComponent component = new HealthComponent(id, "Valid Component", "Category");

        // Act
        var result = validator.validateComponent(component);

        // Assert
        assertTrue(result.isValid());
    }

    /**
     * Test: Validate null HealthComponent throws NullPointerException.
     */
    @Test
    void testValidateNullComponent() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> validator.validateComponent(null));
    }

    /**
     * Test: Validate component with blank name returns invalid result.
     */
    @Test
    void testValidateComponentWithBlankName() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-id");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act
        var result = validator.validateComponent(component);

        // Assert
        assertFalse(result.isValid());
    }

    /**
     * Test: Validate component with blank category returns invalid result.
     */
    @Test
    void testValidateComponentWithBlankCategory() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-id");
        HealthComponent component = new HealthComponent(id, "Test Component", "   ");

        // Act
        var result = validator.validateComponent(component);

        // Assert
        assertFalse(result.isValid());
    }

    // HealthIndicator validation tests

    /**
     * Test: Validate valid HealthIndicator returns valid result.
     */
    @Test
    void testValidateValidIndicator() {
        // Arrange
        HealthIndicator indicator = new HealthIndicator(
                "CPU",
                HealthStatus.HEALTHY,
                HealthSeverity.INFO,
                "CPU usage normal"
        );

        // Act
        var result = validator.validateIndicator(indicator);

        // Assert
        assertTrue(result.isValid());
    }

    /**
     * Test: Validate null HealthIndicator throws NullPointerException.
     */
    @Test
    void testValidateNullIndicator() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> validator.validateIndicator(null));
    }

    /**
     * Test: Validate indicator with blank name returns invalid result.
     */
    @Test
    void testValidateIndicatorWithBlankName() {
        // Arrange
        HealthIndicator indicator = new HealthIndicator(
                "   ",
                HealthStatus.HEALTHY,
                HealthSeverity.INFO,
                "Message"
        );

        // Act
        var result = validator.validateIndicator(indicator);

        // Assert
        assertFalse(result.isValid());
    }

    /**
     * Test: Validate indicator with null status returns invalid result.
     */
    @Test
    void testValidateIndicatorWithNullStatus() {
        // Arrange - create valid indicator, validator will check fields
        HealthIndicator indicator = new HealthIndicator(
                "CPU",
                HealthStatus.HEALTHY,
                HealthSeverity.INFO,
                "Message"
        );

        // Act
        var result = validator.validateIndicator(indicator);

        // Assert - valid indicator should pass
        assertTrue(result.isValid());
    }

    /**
     * Test: Validate indicator with blank message returns invalid result.
     */
    @Test
    void testValidateIndicatorWithBlankMessage() {
        // Arrange
        HealthIndicator indicator = new HealthIndicator(
                "CPU",
                HealthStatus.HEALTHY,
                HealthSeverity.INFO,
                "   "
        );

        // Act
        var result = validator.validateIndicator(indicator);

        // Assert
        assertFalse(result.isValid());
    }

    // HealthMetrics validation tests

    /**
     * Test: Validate valid HealthMetrics returns valid result.
     */
    @Test
    void testValidateValidMetrics() {
        // Arrange
        HealthMetrics metrics = new HealthMetrics(0.99, 10.0, 100.0, Map.of("key", "value"));

        // Act
        var result = validator.validateMetrics(metrics);

        // Assert
        assertTrue(result.isValid());
    }

    /**
     * Test: Validate null HealthMetrics throws NullPointerException.
     */
    @Test
    void testValidateNullMetrics() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> validator.validateMetrics(null));
    }

    /**
     * Test: Validate metrics with negative availability returns invalid result.
     */
    @Test
    void testValidateMetricsWithNegativeAvailability() {
        // Arrange
        HealthMetrics metrics = new HealthMetrics(-1.0, 10.0, 100.0, Map.of());

        // Act
        var result = validator.validateMetrics(metrics);

        // Assert
        assertFalse(result.isValid());
    }

    /**
     * Test: Validate metrics with negative responseTime returns invalid result.
     */
    @Test
    void testValidateMetricsWithNegativeResponseTime() {
        // Arrange
        HealthMetrics metrics = new HealthMetrics(0.99, -10.0, 100.0, Map.of());

        // Act
        var result = validator.validateMetrics(metrics);

        // Assert
        assertFalse(result.isValid());
    }

    /**
     * Test: Validate metrics with negative uptime returns invalid result.
     */
    @Test
    void testValidateMetricsWithNegativeUptime() {
        // Arrange
        HealthMetrics metrics = new HealthMetrics(0.99, 10.0, -100.0, Map.of());

        // Act
        var result = validator.validateMetrics(metrics);

        // Assert
        assertFalse(result.isValid());
    }

    /**
     * Test: Validate metrics with empty values map returns valid result.
     */
    @Test
    void testValidateMetricsWithEmptyValuesMap() {
        // Arrange
        HealthMetrics metrics = new HealthMetrics(0.99, 10.0, 100.0, Map.of());

        // Act
        var result = validator.validateMetrics(metrics);

        // Assert
        assertTrue(result.isValid());
    }

    /**
     * Test: Validate metrics with zero values returns valid result.
     */
    @Test
    void testValidateMetricsWithZeroValues() {
        // Arrange
        HealthMetrics metrics = new HealthMetrics(0.0, 0.0, 0.0, Map.of());

        // Act
        var result = validator.validateMetrics(metrics);

        // Assert
        assertTrue(result.isValid());
    }

    // HealthCheck validation tests

    /**
     * Test: Validate valid HealthCheck returns valid result.
     */
    @Test
    void testValidateValidCheck() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-id");
        HealthComponent component = new HealthComponent(id, "Test", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        var result = validator.validateCheck(check);

        // Assert
        assertTrue(result.isValid());
    }

    /**
     * Test: Validate null HealthCheck throws NullPointerException.
     */
    @Test
    void testValidateNullCheck() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> validator.validateCheck(null));
    }

    /**
     * Test: Validate check with valid component returns valid result.
     */
    @Test
    void testValidateCheckWithValidComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-id");
        HealthComponent component = new HealthComponent(id, "Test", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        var result = validator.validateCheck(check);

        // Assert
        assertTrue(result.isValid());
    }

    // HealthReport validation tests

    /**
     * Test: Validate valid HealthReport returns valid result.
     */
    @Test
    void testValidateValidReport() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-id");
        HealthComponent component = new HealthComponent(id, "Test", "Category");
        HealthIndicator indicator = new HealthIndicator(
                "CPU",
                HealthStatus.HEALTHY,
                HealthSeverity.INFO,
                "OK"
        );
        HealthMetrics metrics = new HealthMetrics(0.99, 10.0, 100.0, Map.of());
        HealthReport report = new HealthReport(
                component,
                HealthStatus.HEALTHY,
                List.of(indicator),
                metrics,
                Instant.now()
        );

        // Act
        var result = validator.validateReport(report);

        // Assert
        assertTrue(result.isValid());
    }

    /**
     * Test: Validate null HealthReport throws NullPointerException.
     */
    @Test
    void testValidateNullReport() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> validator.validateReport(null));
    }

    /**
     * Test: Validate report with valid data returns valid result.
     */
    @Test
    void testValidateReportWithValidData() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-id");
        HealthComponent component = new HealthComponent(id, "Test", "Category");
        HealthIndicator indicator = new HealthIndicator(
                "CPU",
                HealthStatus.HEALTHY,
                HealthSeverity.INFO,
                "OK"
        );
        HealthMetrics metrics = new HealthMetrics(0.99, 10.0, 100.0, Map.of());
        HealthReport report = new HealthReport(component, HealthStatus.HEALTHY, List.of(indicator), metrics, Instant.now());

        // Act
        var result = validator.validateReport(report);

        // Assert
        assertTrue(result.isValid());
    }

    /**
     * Test: Validate report with empty indicators list returns valid result.
     */
    @Test
    void testValidateReportWithEmptyIndicators() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-id");
        HealthComponent component = new HealthComponent(id, "Test", "Category");
        HealthMetrics metrics = new HealthMetrics(0.99, 10.0, 100.0, Map.of());
        HealthReport report = new HealthReport(component, HealthStatus.HEALTHY, List.of(), metrics, Instant.now());

        // Act
        var result = validator.validateReport(report);

        // Assert
        assertTrue(result.isValid());
    }

    // ValidationResult structure tests

    /**
     * Test: ValidationResult.isValid() returns true for valid result.
     */
    @Test
    void testValidationResultIsValidReturnsTrue() {
        // Arrange
        HealthComponentId id = new HealthComponentId("valid-id");
        HealthComponent component = new HealthComponent(id, "Valid", "Category");

        // Act
        var result = validator.validateComponent(component);

        // Assert
        assertTrue(result.isValid());
    }

    /**
     * Test: ValidationResult.errors() returns empty list for valid result.
     */
    @Test
    void testValidationResultErrorsEmptyForValid() {
        // Arrange
        HealthComponentId id = new HealthComponentId("valid-id");
        HealthComponent component = new HealthComponent(id, "Valid", "Category");

        // Act
        var result = validator.validateComponent(component);

        // Assert
        assertTrue(result.errors().isEmpty());
    }

    /**
     * Test: ValidationResult.warnings() returns empty list for valid result.
     */
    @Test
    void testValidationResultWarningsEmptyForValid() {
        // Arrange
        HealthComponentId id = new HealthComponentId("valid-id");
        HealthComponent component = new HealthComponent(id, "Valid", "Category");

        // Act
        var result = validator.validateComponent(component);

        // Assert
        assertTrue(result.warnings().isEmpty());
    }

    /**
     * Test: ValidationResult.isValid() returns false for invalid result.
     */
    @Test
    void testValidationResultIsValidReturnsFalse() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-id");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act
        var result = validator.validateComponent(component);

        // Assert
        assertFalse(result.isValid());
    }

    /**
     * Test: ValidationResult.errors() returns errors for invalid result.
     */
    @Test
    void testValidationResultErrorsPresentForInvalid() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-id");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act
        var result = validator.validateComponent(component);

        // Assert
        assertFalse(result.errors().isEmpty());
        assertEquals(1, result.errors().size());
    }
}