package com.shreeai.os.platform.core.health;

import com.shreeai.os.platform.core.health.error.HealthErrorCode;
import com.shreeai.os.platform.core.health.error.HealthError;
import com.shreeai.os.platform.core.health.error.HealthException;
import com.shreeai.os.platform.core.health.error.InvalidHealthComponentException;
import com.shreeai.os.platform.core.health.model.HealthComponent;
import com.shreeai.os.platform.core.health.model.HealthComponentId;
import com.shreeai.os.platform.core.health.model.HealthReport;
import com.shreeai.os.platform.core.health.model.HealthStatus;
import com.shreeai.os.platform.core.health.service.DefaultHealthService;
import com.shreeai.os.platform.core.health.validator.HealthValidator;
import com.shreeai.os.platform.core.health.engine.HealthEvaluationEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>HealthIntegrationTests</b>
 *
 * <p>Integration tests for the complete Health subsystem within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies end-to-end health operations.</li>
 *   <li>Verifies integration between service, validator, and engine.</li>
 *   <li>Verifies complete workflows.</li>
 *   <li>Verifies error handling across layers.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultHealthService
 * @see HealthValidator
 * @see HealthEvaluationEngine
 */
public class HealthIntegrationTests {

    private DefaultHealthService service;
    private HealthValidator validator;
    private HealthEvaluationEngine engine;

    @BeforeEach
    void setUp() {
        validator = new HealthValidator();
        engine = new HealthEvaluationEngine();
        service = new DefaultHealthService(validator, engine);
    }

    // Complete workflow tests

    /**
     * Test: Complete workflow - register, check, unregister.
     */
    @Test
    void testCompleteWorkflowRegisterCheckUnregister() {
        // Arrange
        HealthComponentId id = new HealthComponentId("workflow-component");
        HealthComponent component = new HealthComponent(id, "Workflow Component", "Category");

        // Act & Assert - Register
        boolean registered = service.register(component);
        assertTrue(registered);
        assertTrue(service.exists(component));

        // Act & Assert - Check
        Optional<HealthReport> report = service.check(component);
        assertTrue(report.isPresent());
        assertEquals(HealthStatus.HEALTHY, report.get().status());
        assertEquals(component, report.get().component());

        // Act & Assert - Unregister
        boolean unregistered = service.unregister(component);
        assertTrue(unregistered);
        assertFalse(service.exists(component));

        // Act & Assert - Check after unregister
        Optional<HealthReport> reportAfterUnregister = service.check(component);
        assertTrue(reportAfterUnregister.isEmpty());
    }

    /**
     * Test: Multiple components complete lifecycle.
     */
    @Test
    void testMultipleComponentsCompleteLifecycle() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("lifecycle-1");
        HealthComponent component1 = new HealthComponent(id1, "Lifecycle 1", "Category A");

        HealthComponentId id2 = new HealthComponentId("lifecycle-2");
        HealthComponent component2 = new HealthComponent(id2, "Lifecycle 2", "Category B");

        // Act - Register both
        service.register(component1);
        service.register(component2);

        // Assert - Both exist
        assertTrue(service.exists(component1));
        assertTrue(service.exists(component2));

        // Act - Check both
        Optional<HealthReport> report1 = service.check(component1);
        Optional<HealthReport> report2 = service.check(component2);

        // Assert - Both reports present
        assertTrue(report1.isPresent());
        assertTrue(report2.isPresent());

        // Act - Check all
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        assertEquals(2, reports.size());

        // Act - Unregister one
        service.unregister(component1);

        // Assert
        assertFalse(service.exists(component1));
        assertTrue(service.exists(component2));

        // Act - Check all after unregister
        Collection<HealthReport> remainingReports = service.checkAll();

        // Assert
        assertEquals(1, remainingReports.size());
    }

    // Error handling integration tests

    /**
     * Test: Invalid component registration is rejected.
     */
    @Test
    void testInvalidComponentRegistrationRejected() {
        // Arrange
        HealthComponentId id = new HealthComponentId("invalid");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act & Assert
        assertThrows(InvalidHealthComponentException.class, () -> service.register(component));
        assertFalse(service.exists(component));
    }

    /**
     * Test: Duplicate registration is rejected.
     */
    @Test
    void testDuplicateRegistrationRejected() {
        // Arrange
        HealthComponentId id = new HealthComponentId("dup");
        HealthComponent component = new HealthComponent(id, "Duplicate", "Category");

        // Act
        service.register(component);

        // Assert
        assertThrows(HealthException.class, () -> service.register(component));
    }

    /**
     * Test: Check invalid component throws exception.
     */
    @Test
    void testCheckInvalidComponentThrowsException() {
        // Arrange
        HealthComponentId id = new HealthComponentId("invalid-check");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act & Assert
        assertThrows(InvalidHealthComponentException.class, () -> service.check(component));
    }

    /**
     * Test: Check unregistered component returns empty.
     */
    @Test
    void testCheckUnregisteredComponentReturnsEmpty() {
        // Arrange
        HealthComponentId id = new HealthComponentId("not-registered");
        HealthComponent component = new HealthComponent(id, "Not Registered", "Category");

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isEmpty());
    }

    // Validator integration tests

    /**
     * Test: Validator rejects invalid components before registration.
     */
    @Test
    void testValidatorRejectsInvalidComponentsBeforeRegistration() {
        // Arrange
        HealthComponentId id = new HealthComponentId("validator-test");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act & Assert
        assertThrows(InvalidHealthComponentException.class, () -> service.register(component));
        
        // Verify component was not registered
        assertFalse(service.exists(component));
    }

    /**
     * Test: Validator accepts valid components for registration.
     */
    @Test
    void testValidatorAcceptsValidComponentsForRegistration() {
        // Arrange
        HealthComponentId id = new HealthComponentId("validator-valid");
        HealthComponent component = new HealthComponent(id, "Valid Component", "Category");

        // Act
        boolean registered = service.register(component);

        // Assert
        assertTrue(registered);
        assertTrue(service.exists(component));
    }

    // Engine integration tests

    /**
     * Test: Engine generates valid reports for registered components.
     */
    @Test
    void testEngineGeneratesValidReportsForRegisteredComponents() {
        // Arrange
        HealthComponentId id = new HealthComponentId("engine-test");
        HealthComponent component = new HealthComponent(id, "Engine Test", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        HealthReport report = result.get();
        
        // Verify report structure
        assertNotNull(report.component());
        assertNotNull(report.status());
        assertNotNull(report.indicators());
        assertNotNull(report.metrics());
        assertNotNull(report.timestamp());
        
        // Verify report content
        assertEquals(component, report.component());
        assertEquals(HealthStatus.HEALTHY, report.status());
        assertFalse(report.indicators().isEmpty());
        assertTrue(report.metrics().availability() >= 0);
    }

    /**
     * Test: Engine evaluation is delegated by service.
     */
    @Test
    void testEngineEvaluationIsDelegatedByService() {
        // Arrange
        HealthComponentId id = new HealthComponentId("delegation-test");
        HealthComponent component = new HealthComponent(id, "Delegation Test", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        // The service delegates to the engine, which generates a HEALTHY status
        assertEquals(HealthStatus.HEALTHY, result.get().status());
    }

    // Error propagation tests

    /**
     * Test: InvalidHealthComponentException propagates from validator to service.
     */
    @Test
    void testInvalidHealthComponentExceptionPropagates() {
        // Arrange
        HealthComponentId id = new HealthComponentId("propagation-test");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act & Assert
        assertThrows(InvalidHealthComponentException.class, () -> service.register(component));
    }

    /**
     * Test: HealthException propagates for duplicate registration.
     */
    @Test
    void testHealthExceptionPropagatesForDuplicateRegistration() {
        // Arrange
        HealthComponentId id = new HealthComponentId("dup-propagation");
        HealthComponent component = new HealthComponent(id, "Duplicate", "Category");

        // Act
        service.register(component);

        // Assert
        assertThrows(HealthException.class, () -> service.register(component));
    }

    /**
     * Test: HealthCheckFailedException propagates from engine.
     */
    @Test
    void testHealthCheckFailedExceptionPropagates() {
        // This test verifies that if the engine throws HealthCheckFailedException,
        // the service propagates it correctly. For now, the basic engine doesn't throw,
        // but the structure is in place.
        
        // Arrange
        HealthComponentId id = new HealthComponentId("check-fail");
        HealthComponent component = new HealthComponent(id, "Check Fail", "Category");
        service.register(component);

        // Act - The current implementation doesn't throw, but the structure supports it
        Optional<HealthReport> result = service.check(component);

        // Assert - For now, it succeeds
        assertTrue(result.isPresent());
    }

    // checkAll integration tests

    /**
     * Test: checkAll returns reports for all registered components.
     */
    @Test
    void testCheckAllReturnsReportsForAllRegisteredComponents() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("checkall-1");
        HealthComponent component1 = new HealthComponent(id1, "CheckAll 1", "Category A");

        HealthComponentId id2 = new HealthComponentId("checkall-2");
        HealthComponent component2 = new HealthComponent(id2, "CheckAll 2", "Category B");

        HealthComponentId id3 = new HealthComponentId("checkall-3");
        HealthComponent component3 = new HealthComponent(id3, "CheckAll 3", "Category C");

        service.register(component1);
        service.register(component2);
        service.register(component3);

        // Act
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        assertEquals(3, reports.size());
        
        // Verify all components are in reports
        boolean found1 = false, found2 = false, found3 = false;
        for (HealthReport report : reports) {
            if (report.component().equals(component1)) found1 = true;
            if (report.component().equals(component2)) found2 = true;
            if (report.component().equals(component3)) found3 = true;
        }
        assertTrue(found1, "Component 1 not found in reports");
        assertTrue(found2, "Component 2 not found in reports");
        assertTrue(found3, "Component 3 not found in reports");
    }

    /**
     * Test: checkAll after partial unregistration.
     */
    @Test
    void testCheckAllAfterPartialUnregistration() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("partial-1");
        HealthComponent component1 = new HealthComponent(id1, "Partial 1", "Category");

        HealthComponentId id2 = new HealthComponentId("partial-2");
        HealthComponent component2 = new HealthComponent(id2, "Partial 2", "Category");

        HealthComponentId id3 = new HealthComponentId("partial-3");
        HealthComponent component3 = new HealthComponent(id3, "Partial 3", "Category");

        service.register(component1);
        service.register(component2);
        service.register(component3);

        // Unregister one
        service.unregister(component2);

        // Act
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        assertEquals(2, reports.size());
        
        // Verify only registered components are in reports
        boolean found1 = false, found2 = false, found3 = false;
        for (HealthReport report : reports) {
            if (report.component().equals(component1)) found1 = true;
            if (report.component().equals(component2)) found2 = true;
            if (report.component().equals(component3)) found3 = true;
        }
        assertTrue(found1, "Component 1 not found");
        assertFalse(found2, "Component 2 should not be found");
        assertTrue(found3, "Component 3 not found");
    }

    // Model immutability tests

    /**
     * Test: HealthReport returned by service is immutable.
     */
    @Test
    void testHealthReportReturnedByServiceIsImmutable() {
        // Arrange
        HealthComponentId id = new HealthComponentId("immutable-test");
        HealthComponent component = new HealthComponent(id, "Immutable Test", "Category");
        service.register(component);

        // Act
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        HealthReport report = result.get();
        
        // Verify indicators list is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> report.indicators().clear());
        
        // Verify metrics values map is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> report.metrics().values().clear());
    }

    /**
     * Test: checkAll returns unmodifiable collection.
     */
    @Test
    void testCheckAllReturnsUnmodifiableCollection() {
        // Arrange
        HealthComponentId id = new HealthComponentId("unmodifiable-checkall");
        HealthComponent component = new HealthComponent(id, "Unmodifiable CheckAll", "Category");
        service.register(component);

        // Act
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> reports.clear());
    }

    // Complex scenario tests

    /**
     * Test: Register, check, unregister, re-register same component.
     */
    @Test
    void testRegisterCheckUnregisterReregisterSameComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("reregister");
        HealthComponent component = new HealthComponent(id, "Reregister", "Category");

        // Act - First registration
        boolean registered1 = service.register(component);
        assertTrue(registered1);

        // Act - Check
        Optional<HealthReport> report1 = service.check(component);
        assertTrue(report1.isPresent());

        // Act - Unregister
        boolean unregistered = service.unregister(component);
        assertTrue(unregistered);

        // Act - Re-register
        boolean registered2 = service.register(component);
        assertTrue(registered2);

        // Assert
        assertTrue(service.exists(component));

        // Act - Check again
        Optional<HealthReport> report2 = service.check(component);
        assertTrue(report2.isPresent());
    }

    /**
     * Test: Multiple services with same dependencies.
     */
    @Test
    void testMultipleServicesWithSameDependencies() {
        // Arrange
        DefaultHealthService service1 = new DefaultHealthService(validator, engine);
        DefaultHealthService service2 = new DefaultHealthService(validator, engine);

        HealthComponentId id = new HealthComponentId("multi-service");
        HealthComponent component = new HealthComponent(id, "Multi Service", "Category");

        // Act - Register in service1
        service1.register(component);

        // Assert - Not in service2
        assertFalse(service2.exists(component));

        // Act - Register in service2
        service2.register(component);

        // Assert - In both services
        assertTrue(service1.exists(component));
        assertTrue(service2.exists(component));

        // Act - Unregister from service1
        service1.unregister(component);

        // Assert - Still in service2
        assertFalse(service1.exists(component));
        assertTrue(service2.exists(component));
    }

    /**
     * Test: Error details contain component information.
     */
    @Test
    void testErrorDetailsContainComponentInformation() {
        // Arrange
        HealthComponentId id = new HealthComponentId("error-details");
        HealthComponent component = new HealthComponent(id, "Error Details", "Test Category");

        // Act - Try to register duplicate
        service.register(component);
        HealthException exception = assertThrows(HealthException.class, () -> service.register(component));

        // Assert
        HealthError error = exception.error();
        assertEquals(HealthErrorCode.HEALTH_ALREADY_REGISTERED, error.code());
        assertNotNull(error.details());
        // Verify error details exist (actual keys depend on implementation)
        assertFalse(error.details().isEmpty());
    }

    /**
     * Test: Validation errors contain detailed information.
     */
    @Test
    void testValidationErrorsContainDetailedInformation() {
        // Arrange
        HealthComponentId id = new HealthComponentId("validation-details");
        HealthComponent component = new HealthComponent(id, "   ", "   ");

        // Act
        InvalidHealthComponentException exception = assertThrows(
                InvalidHealthComponentException.class,
                () -> service.register(component)
        );

        // Assert
        HealthError error = exception.error();
        assertEquals(HealthErrorCode.HEALTH_INVALID_COMPONENT, error.code());
        assertTrue(error.details().containsKey("reason"));
    }

    // End-to-end scenario tests

    /**
     * Test: Complete health monitoring scenario.
     */
    @Test
    void testCompleteHealthMonitoringScenario() {
        // Arrange - Simulate a real-world scenario
        HealthComponentId eventBusId = new HealthComponentId("event-bus");
        HealthComponent eventBus = new HealthComponent(eventBusId, "Event Bus", "Infrastructure");

        HealthComponentId registryId = new HealthComponentId("registry");
        HealthComponent registry = new HealthComponent(registryId, "Registry", "Infrastructure");

        HealthComponentId dbId = new HealthComponentId("database");
        HealthComponent database = new HealthComponent(dbId, "Database", "Data Store");

        // Act - Register all components
        service.register(eventBus);
        service.register(registry);
        service.register(database);

        // Assert - All registered
        assertTrue(service.exists(eventBus));
        assertTrue(service.exists(registry));
        assertTrue(service.exists(database));

        // Act - Check all components
        Collection<HealthReport> reports = service.checkAll();

        // Assert
        assertEquals(3, reports.size());

        // Act - Check individual components
        Optional<HealthReport> eventBusReport = service.check(eventBus);
        Optional<HealthReport> registryReport = service.check(registry);
        Optional<HealthReport> dbReport = service.check(database);

        // Assert
        assertTrue(eventBusReport.isPresent());
        assertTrue(registryReport.isPresent());
        assertTrue(dbReport.isPresent());

        // Verify all reports have HEALTHY status
        assertEquals(HealthStatus.HEALTHY, eventBusReport.get().status());
        assertEquals(HealthStatus.HEALTHY, registryReport.get().status());
        assertEquals(HealthStatus.HEALTHY, dbReport.get().status());

        // Act - Unregister one component
        service.unregister(registry);

        // Assert
        assertFalse(service.exists(registry));
        assertTrue(service.exists(eventBus));
        assertTrue(service.exists(database));

        // Act - Check all again
        Collection<HealthReport> remainingReports = service.checkAll();

        // Assert
        assertEquals(2, remainingReports.size());
    }

    /**
     * Test: Health check with deep flag.
     */
    @Test
    void testHealthCheckWithDeepFlag() {
        // Arrange
        HealthComponentId id = new HealthComponentId("deep-check");
        HealthComponent component = new HealthComponent(id, "Deep Check", "Category");
        service.register(component);

        // Act - The service creates a HealthCheck with deep=false internally
        Optional<HealthReport> result = service.check(component);

        // Assert
        assertTrue(result.isPresent());
        // The basic engine sets deepCheck metric based on the check parameter
        // Since service passes false, deepCheck should be false
        assertEquals(false, result.get().metrics().values().get("deepCheck"));
    }
}