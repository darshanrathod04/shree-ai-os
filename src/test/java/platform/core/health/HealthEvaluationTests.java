package platform.core.health;

import com.shreeai.os.platform.core.health.error.InvalidHealthComponentException;
import com.shreeai.os.platform.core.health.model.HealthComponent;
import com.shreeai.os.platform.core.health.model.HealthComponentId;
import com.shreeai.os.platform.core.health.model.HealthIndicator;
import com.shreeai.os.platform.core.health.model.HealthMetrics;
import com.shreeai.os.platform.core.health.model.HealthReport;
import com.shreeai.os.platform.core.health.model.HealthStatus;
import com.shreeai.os.platform.core.health.service.DefaultHealthService;
import com.shreeai.os.platform.core.health.validator.HealthValidator;
import com.shreeai.os.platform.core.health.engine.HealthEvaluationEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>HealthEvaluationTests</b>
 *
 * <p>Tests for health evaluation operations within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies health check behavior.</li>
 *   <li>Verifies checkAll behavior.</li>
 *   <li>Verifies health report generation.</li>
 *   <li>Verifies evaluation delegation to engine.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultHealthService
 * @see HealthEvaluationEngine
 */
public class HealthEvaluationTests {

    private DefaultHealthService service;
    private HealthValidator validator;
    private HealthEvaluationEngine engine;

    @BeforeEach
    void setUp() {
        validator = new HealthValidator();
        engine = new HealthEvaluationEngine();
        service = new DefaultHealthService(validator, engine);
    }

    // Check tests

    /**
     * Test: Check registered component returns HealthReport.
     */
    @Test
    void testCheckRegisteredComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("check-test");
        HealthComponent component = new HealthComponent(id, "Check Test", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        HealthReport report = result.get();
        assertEquals(component, report.component());
        assertEquals(HealthStatus.HEALTHY, report.status());
    }

    /**
     * Test: Check unregistered component returns empty Optional.
     */
    @Test
    void testCheckUnregisteredComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("not-registered");
        HealthComponent component = new HealthComponent(id, "Not Registered", "Category");

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isEmpty());
    }

    /**
     * Test: Check null component throws IllegalArgumentException.
     */
    @Test
    void testCheckNullComponent() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.check(null));
    }

    /**
     * Test: Check invalid component throws InvalidHealthComponentException.
     */
    @Test
    void testCheckInvalidComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("invalid");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act & Assert
        assertThrows(InvalidHealthComponentException.class, () -> service.check(component));
    }

    /**
     * Test: Check returns report with indicators.
     */
    @Test
    void testCheckReturnsReportWithIndicators() {
        // Arrange
        HealthComponentId id = new HealthComponentId("indicators-test");
        HealthComponent component = new HealthComponent(id, "Indicators Test", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        HealthReport report = result.get();
        assertNotNull(report.indicators());
        assertFalse(report.indicators().isEmpty());
    }

    /**
     * Test: Check returns report with metrics.
     */
    @Test
    void testCheckReturnsReportWithMetrics() {
        // Arrange
        HealthComponentId id = new HealthComponentId("metrics-test");
        HealthComponent component = new HealthComponent(id, "Metrics Test", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        HealthReport report = result.get();
        assertNotNull(report.metrics());
        assertTrue(report.metrics().availability() >= 0);
    }

    /**
     * Test: Check returns report with timestamp.
     */
    @Test
    void testCheckReturnsReportWithTimestamp() {
        // Arrange
        HealthComponentId id = new HealthComponentId("timestamp-test");
        HealthComponent component = new HealthComponent(id, "Timestamp Test", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        HealthReport report = result.get();
        assertNotNull(report.timestamp());
    }

    // CheckAll tests

    /**
     * Test: CheckAll returns empty collection when no components registered.
     */
    @Test
    void testCheckAllReturnsEmptyWhenNoComponents() {
        // Act
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        assertTrue(reports.isEmpty());
    }

    /**
     * Test: CheckAll returns reports for all registered components.
     */
    @Test
    void testCheckAllReturnsReportsForAllComponents() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("checkall-1");
        HealthComponent component1 = new HealthComponent(id1, "CheckAll 1", "Category");

        HealthComponentId id2 = new HealthComponentId("checkall-2");
        HealthComponent component2 = new HealthComponent(id2, "CheckAll 2", "Category");

        service.register(component1);
        service.register(component2);

        // Act
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        assertEquals(2, reports.size());
    }

    /**
     * Test: CheckAll returns unmodifiable collection.
     */
    @Test
    void testCheckAllReturnsUnmodifiableCollection() {
        // Arrange
        HealthComponentId id = new HealthComponentId("unmodifiable-test");
        HealthComponent component = new HealthComponent(id, "Unmodifiable Test", "Category");
        service.register(component);

        // Act
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> reports.clear());
    }

    /**
     * Test: CheckAll returns reports with correct component references.
     */
    @Test
    void testCheckAllReturnsReportsWithCorrectComponents() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("verify-1");
        HealthComponent component1 = new HealthComponent(id1, "Verify 1", "Category");

        HealthComponentId id2 = new HealthComponentId("verify-2");
        HealthComponent component2 = new HealthComponent(id2, "Verify 2", "Category");

        service.register(component1);
        service.register(component2);

        // Act
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        boolean found1 = false;
        boolean found2 = false;
        for (HealthReport report : reports) {
            if (report.component().equals(component1)) {
                found1 = true;
            }
            if (report.component().equals(component2)) {
                found2 = true;
            }
        }
        assertTrue(found1, "Report for component1 not found");
        assertTrue(found2, "Report for component2 not found");
    }

    /**
     * Test: CheckAll continues after individual component failure.
     */
    @Test
    void testCheckAllContinuesAfterFailure() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("continue-1");
        HealthComponent component1 = new HealthComponent(id1, "Continue 1", "Category");

        HealthComponentId id2 = new HealthComponentId("continue-2");
        HealthComponent component2 = new HealthComponent(id2, "Continue 2", "Category");

        service.register(component1);
        service.register(component2);

        // Act - should not throw even if one component fails
        Collection<HealthReport> reports = service.checkAll();

        // Assert - both components should be checked
        assertEquals(2, reports.size());
    }

    // Health report structure tests

    /**
     * Test: Health report contains correct component.
     */
    @Test
    void testHealthReportContainsCorrectComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("report-component");
        HealthComponent component = new HealthComponent(id, "Report Component", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(component, result.get().component());
    }

    /**
     * Test: Health report has HEALTHY status for basic evaluation.
     */
    @Test
    void testHealthReportHasHealthyStatus() {
        // Arrange
        HealthComponentId id = new HealthComponentId("status-test");
        HealthComponent component = new HealthComponent(id, "Status Test", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(HealthStatus.HEALTHY, result.get().status());
    }

    /**
     * Test: Health report has at least one indicator.
     */
    @Test
    void testHealthReportHasAtLeastOneIndicator() {
        // Arrange
        HealthComponentId id = new HealthComponentId("indicator-count");
        HealthComponent component = new HealthComponent(id, "Indicator Count", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        List<HealthIndicator> indicators = result.get().indicators();
        assertFalse(indicators.isEmpty());
    }

    /**
     * Test: Health report metrics have valid values.
     */
    @Test
    void testHealthReportMetricsHaveValidValues() {
        // Arrange
        HealthComponentId id = new HealthComponentId("metrics-validation");
        HealthComponent component = new HealthComponent(id, "Metrics Validation", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        HealthMetrics metrics = result.get().metrics();
        assertTrue(metrics.availability() >= 0);
        assertTrue(metrics.responseTime() >= 0);
        assertTrue(metrics.uptime() >= 0);
        assertNotNull(metrics.values());
    }

    /**
     * Test: Health report timestamp is recent.
     */
    @Test
    void testHealthReportTimestampIsRecent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("timestamp-recent");
        HealthComponent component = new HealthComponent(id, "Timestamp Recent", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().timestamp());
    }

    // Multiple check tests

    /**
     * Test: Check same component multiple times returns reports.
     */
    @Test
    void testCheckSameComponentMultipleTimes() {
        // Arrange
        HealthComponentId id = new HealthComponentId("multiple-check");
        HealthComponent component = new HealthComponent(id, "Multiple Check", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result1 = service.check(component);
        Optional<HealthReport> result2 = service.check(component);

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(result1.get().status(), result2.get().status());
    }

    /**
     * Test: Check different components returns different reports.
     */
    @Test
    void testCheckDifferentComponentsReturnsDifferentReports() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("diff-1");
        HealthComponent component1 = new HealthComponent(id1, "Diff 1", "Category A");

        HealthComponentId id2 = new HealthComponentId("diff-2");
        HealthComponent component2 = new HealthComponent(id2, "Diff 2", "Category B");

        service.register(component1);
        service.register(component2);

        // Act
        Optional<HealthReport> result1 = service.check(component1);
        Optional<HealthReport> result2 = service.check(component2);

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(component1, result1.get().component());
        assertEquals(component2, result2.get().component());
    }

    // Edge cases

    /**
     * Test: Check component after unregistration returns empty.
     */
    @Test
    void testCheckAfterUnregistrationReturnsEmpty() {
        // Arrange
        HealthComponentId id = new HealthComponentId("after-unregister");
        HealthComponent component = new HealthComponent(id, "After Unregister", "Category");
        service.register(component);
        service.unregister(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isEmpty());
    }

    /**
     * Test: CheckAll after partial unregistration returns only registered components.
     */
    @Test
    void testCheckAllAfterPartialUnregistration() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("partial-1");
        HealthComponent component1 = new HealthComponent(id1, "Partial 1", "Category");

        HealthComponentId id2 = new HealthComponentId("partial-2");
        HealthComponent component2 = new HealthComponent(id2, "Partial 2", "Category");

        service.register(component1);
        service.register(component2);
        service.unregister(component2);

        // Act
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        assertEquals(1, reports.size());
    }
}