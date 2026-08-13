package com.shreeai.os.platform.core.health;

import com.shreeai.os.platform.core.health.model.HealthCheck;
import com.shreeai.os.platform.core.health.model.HealthComponent;
import com.shreeai.os.platform.core.health.model.HealthComponentId;
import com.shreeai.os.platform.core.health.model.HealthIndicator;
import com.shreeai.os.platform.core.health.model.HealthMetrics;
import com.shreeai.os.platform.core.health.model.HealthReport;
import com.shreeai.os.platform.core.health.model.HealthSeverity;
import com.shreeai.os.platform.core.health.model.HealthStatus;
import com.shreeai.os.platform.core.health.engine.EvaluationResult;
import com.shreeai.os.platform.core.health.engine.HealthEvaluationEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>HealthEngineTests</b>
 *
 * <p>Tests for health evaluation engine within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies HealthEvaluationEngine behavior.</li>
 *   <li>Verifies EvaluationResult structure.</li>
 *   <li>Verifies basic health evaluation.</li>
 *   <li>Verifies engine is stateless.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see HealthEvaluationEngine
 * @see EvaluationResult
 */
public class HealthEngineTests {

    private HealthEvaluationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new HealthEvaluationEngine();
    }

    // Basic evaluation tests

    /**
     * Test: Evaluate valid component returns successful result.
     */
    @Test
    void testEvaluateValidComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        assertNotNull(result.report());
        assertNull(result.failureMessage());
    }

    /**
     * Test: Evaluate null component throws IllegalArgumentException.
     */
    @Test
    void testEvaluateNullComponent() {
        // Arrange - create a valid check first
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate(null, check));
    }

    /**
     * Test: Evaluate null check throws IllegalArgumentException.
     */
    @Test
    void testEvaluateNullCheck() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> engine.evaluate(component, null));
    }

    /**
     * Test: Evaluate returns HEALTHY status.
     */
    @Test
    void testEvaluateReturnsHealthyStatus() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        assertEquals(HealthStatus.HEALTHY, result.report().status());
    }

    /**
     * Test: Evaluate returns report with correct component.
     */
    @Test
    void testEvaluateReturnsReportWithCorrectComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        assertEquals(component, result.report().component());
    }

    /**
     * Test: Evaluate returns report with at least one indicator.
     */
    @Test
    void testEvaluateReturnsReportWithIndicator() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        List<HealthIndicator> indicators = result.report().indicators();
        assertFalse(indicators.isEmpty());
    }

    /**
     * Test: Evaluate returns report with metrics.
     */
    @Test
    void testEvaluateReturnsReportWithMetrics() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        assertNotNull(result.report().metrics());
    }

    /**
     * Test: Evaluate returns report with timestamp.
     */
    @Test
    void testEvaluateReturnsReportWithTimestamp() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        assertNotNull(result.report().timestamp());
    }

    /**
     * Test: Evaluate with deep check sets deepCheck metric.
     */
    @Test
    void testEvaluateWithDeepCheckSetsMetric() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, true);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        Map<String, Object> values = result.report().metrics().values();
        assertTrue(values.containsKey("deepCheck"));
        assertEquals(true, values.get("deepCheck"));
    }

    /**
     * Test: Evaluate with non-deep check sets deepCheck metric to false.
     */
    @Test
    void testEvaluateWithNonDeepCheckSetsMetric() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        Map<String, Object> values = result.report().metrics().values();
        assertTrue(values.containsKey("deepCheck"));
        assertEquals(false, values.get("deepCheck"));
    }

    // EvaluationResult tests

    /**
     * Test: EvaluationResult.success() creates valid result.
     */
    @Test
    void testEvaluationResultSuccess() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthIndicator indicator = new HealthIndicator(
                "CPU",
                HealthStatus.HEALTHY,
                HealthSeverity.INFO,
                "OK"
        );
        HealthMetrics metrics = new HealthMetrics(1.0, 0.0, 0.0, Map.of());
        HealthReport report = new HealthReport(component, HealthStatus.HEALTHY, List.of(indicator), metrics, Instant.now());

        // Act
        EvaluationResult result = EvaluationResult.success(report);

        // Assert
        assertTrue(result.success());
        assertNotNull(result.report());
        assertNull(result.failureMessage());
        assertNotNull(result.timestamp());
    }

    /**
     * Test: EvaluationResult.failure() creates valid result.
     */
    @Test
    void testEvaluationResultFailure() {
        // Act
        EvaluationResult result = EvaluationResult.failure("Test failure");

        // Assert
        assertFalse(result.success());
        assertNull(result.report());
        assertEquals("Test failure", result.failureMessage());
        assertNotNull(result.timestamp());
    }

    /**
     * Test: EvaluationResult.success() with null report throws NullPointerException.
     */
    @Test
    void testEvaluationResultSuccessWithNullReport() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> EvaluationResult.success(null));
    }

    /**
     * Test: EvaluationResult.failure() with null message throws IllegalArgumentException.
     */
    @Test
    void testEvaluationResultFailureWithNullMessage() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> EvaluationResult.failure(null));
    }

    /**
     * Test: EvaluationResult.failure() with blank message throws IllegalArgumentException.
     */
    @Test
    void testEvaluationResultFailureWithBlankMessage() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> EvaluationResult.failure("   "));
    }

    /**
     * Test: EvaluationResult equals and hashCode.
     */
    @Test
    void testEvaluationResultEqualsAndHashCode() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthIndicator indicator = new HealthIndicator(
                "CPU",
                HealthStatus.HEALTHY,
                HealthSeverity.INFO,
                "OK"
        );
        HealthMetrics metrics = new HealthMetrics(1.0, 0.0, 0.0, Map.of());
        HealthReport report = new HealthReport(component, HealthStatus.HEALTHY, List.of(indicator), metrics, Instant.now());

        EvaluationResult result1 = EvaluationResult.success(report);
        EvaluationResult result2 = EvaluationResult.success(report);

        // Assert
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    /**
     * Test: EvaluationResult toString for success.
     */
    @Test
    void testEvaluationResultToStringForSuccess() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthIndicator indicator = new HealthIndicator(
                "CPU",
                HealthStatus.HEALTHY,
                HealthSeverity.INFO,
                "OK"
        );
        HealthMetrics metrics = new HealthMetrics(1.0, 0.0, 0.0, Map.of());
        HealthReport report = new HealthReport(component, HealthStatus.HEALTHY, List.of(indicator), metrics, Instant.now());
        EvaluationResult result = EvaluationResult.success(report);

        // Act
        String toString = result.toString();

        // Assert
        assertTrue(toString.contains("success=true"));
    }

    /**
     * Test: EvaluationResult toString for failure.
     */
    @Test
    void testEvaluationResultToStringForFailure() {
        // Arrange
        EvaluationResult result = EvaluationResult.failure("Test failure");

        // Act
        String toString = result.toString();

        // Assert
        assertTrue(toString.contains("success=false"));
        assertTrue(toString.contains("Test failure"));
    }

    // Engine statelessness tests

    /**
     * Test: Engine has no public fields.
     */
    @Test
    void testEngineHasNoPublicFields() {
        // Assert
        assertEquals(0, HealthEvaluationEngine.class.getFields().length);
    }

    /**
     * Test: Multiple evaluations are independent.
     */
    @Test
    void testMultipleEvaluationsAreIndependent() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("component-1");
        HealthComponent component1 = new HealthComponent(id1, "Component 1", "Category");
        HealthCheck check1 = new HealthCheck(component1, false);

        HealthComponentId id2 = new HealthComponentId("component-2");
        HealthComponent component2 = new HealthComponent(id2, "Component 2", "Category");
        HealthCheck check2 = new HealthCheck(component2, true);

        // Act
        EvaluationResult result1 = engine.evaluate(component1, check1);
        EvaluationResult result2 = engine.evaluate(component2, check2);

        // Assert
        assertTrue(result1.success());
        assertTrue(result2.success());
        assertEquals(component1, result1.report().component());
        assertEquals(component2, result2.report().component());
    }

    /**
     * Test: Engine can be instantiated multiple times.
     */
    @Test
    void testEngineCanBeInstantiatedMultipleTimes() {
        // Act
        HealthEvaluationEngine engine1 = new HealthEvaluationEngine();
        HealthEvaluationEngine engine2 = new HealthEvaluationEngine();

        // Assert
        assertNotNull(engine1);
        assertNotNull(engine2);
        assertNotSame(engine1, engine2);
    }

    // Metrics validation tests

    /**
     * Test: Evaluated metrics have valid availability.
     */
    @Test
    void testEvaluatedMetricsHaveValidAvailability() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        HealthMetrics metrics = result.report().metrics();
        assertTrue(metrics.availability() >= 0);
        assertTrue(metrics.availability() <= 1.0);
    }

    /**
     * Test: Evaluated metrics have non-negative response time.
     */
    @Test
    void testEvaluatedMetricsHaveNonNegativeResponseTime() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        assertTrue(result.report().metrics().responseTime() >= 0);
    }

    /**
     * Test: Evaluated metrics have non-negative uptime.
     */
    @Test
    void testEvaluatedMetricsHaveNonNegativeUptime() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        assertTrue(result.report().metrics().uptime() >= 0);
    }

    /**
     * Test: Evaluated metrics have non-null values map.
     */
    @Test
    void testEvaluatedMetricsHaveNonNullValuesMap() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        assertNotNull(result.report().metrics().values());
    }

    // Indicator validation tests

    /**
     * Test: Evaluated report has at least one indicator.
     */
    @Test
    void testEvaluatedReportHasAtLeastOneIndicator() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        assertFalse(result.report().indicators().isEmpty());
    }

    /**
     * Test: Evaluated indicator has correct structure.
     */
    @Test
    void testEvaluatedIndicatorHasCorrectStructure() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        HealthCheck check = new HealthCheck(component, false);

        // Act
        EvaluationResult result = engine.evaluate(component, check);

        // Assert
        assertTrue(result.success());
        HealthIndicator indicator = result.report().indicators().get(0);
        assertNotNull(indicator.name());
        assertNotNull(indicator.status());
        assertNotNull(indicator.severity());
        assertNotNull(indicator.message());
    }
}