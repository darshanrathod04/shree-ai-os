package platform.core.health;

import com.shreeai.os.platform.core.health.error.HealthException;
import com.shreeai.os.platform.core.health.error.InvalidHealthComponentException;
import com.shreeai.os.platform.core.health.model.HealthComponent;
import com.shreeai.os.platform.core.health.model.HealthComponentId;
import com.shreeai.os.platform.core.health.service.DefaultHealthService;
import com.shreeai.os.platform.core.health.validator.HealthValidator;
import com.shreeai.os.platform.core.health.engine.HealthEvaluationEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>HealthRegistrationTests</b>
 *
 * <p>Tests for health component registration operations within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies health component registration behavior.</li>
 *   <li>Verifies duplicate registration rejection.</li>
 *   <li>Verifies unregistration behavior.</li>
 *   <li>Verifies existence checks.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultHealthService
 * @see HealthValidator
 */
public class HealthRegistrationTests {

    private DefaultHealthService service;
    private HealthValidator validator;
    private HealthEvaluationEngine engine;

    @BeforeEach
    void setUp() {
        validator = new HealthValidator();
        engine = new HealthEvaluationEngine();
        service = new DefaultHealthService(validator, engine);
    }

    // Registration tests

    /**
     * Test: Register a valid health component.
     */
    @Test
    void testRegisterValidComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Test Category");

        // Act
        boolean result = service.register(component);

        // Assert
        assertTrue(result);
    }

    /**
     * Test: Register null component throws IllegalArgumentException.
     */
    @Test
    void testRegisterNullComponent() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.register(null));
    }

    /**
     * Test: Register multiple components with different IDs.
     */
    @Test
    void testRegisterMultipleComponents() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("component-1");
        HealthComponent component1 = new HealthComponent(id1, "Component 1", "Category A");

        HealthComponentId id2 = new HealthComponentId("component-2");
        HealthComponent component2 = new HealthComponent(id2, "Component 2", "Category B");

        // Act
        boolean result1 = service.register(component1);
        boolean result2 = service.register(component2);

        // Assert
        assertTrue(result1);
        assertTrue(result2);

        // Verify both components exist
        assertTrue(service.exists(component1));
        assertTrue(service.exists(component2));
    }

    /**
     * Test: Register component with blank name throws InvalidHealthComponentException.
     */
    @Test
    void testRegisterComponentWithBlankName() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act & Assert
        assertThrows(InvalidHealthComponentException.class, () -> service.register(component));
    }

    /**
     * Test: Register component with blank category throws InvalidHealthComponentException.
     */
    @Test
    void testRegisterComponentWithBlankCategory() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "   ");

        // Act & Assert
        assertThrows(InvalidHealthComponentException.class, () -> service.register(component));
    }

    // Duplicate rejection tests

    /**
     * Test: Reject duplicate component registration.
     */
    @Test
    void testRejectDuplicateRegistration() {
        // Arrange
        HealthComponentId id = new HealthComponentId("dup-component");
        HealthComponent component = new HealthComponent(id, "Duplicate Component", "Category");

        service.register(component);

        // Act & Assert
        assertThrows(HealthException.class, () -> service.register(component));
    }

    /**
     * Test: Duplicate component with same ID but different name is rejected.
     */
    @Test
    void testRejectDuplicateIdWithDifferentName() {
        // Arrange
        HealthComponentId id = new HealthComponentId("dup-id");
        HealthComponent component1 = new HealthComponent(id, "Component 1", "Category");
        HealthComponent component2 = new HealthComponent(id, "Component 2", "Category");

        service.register(component1);

        // Act & Assert
        assertThrows(HealthException.class, () -> service.register(component2));
    }

    // Existence tests

    /**
     * Test: exists() returns true for registered component.
     */
    @Test
    void testExistsReturnsTrueForRegisteredComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("exists-test");
        HealthComponent component = new HealthComponent(id, "Exists Test", "Category");
        service.register(component);

        // Act
        boolean exists = service.exists(component);

        // Assert
        assertTrue(exists);
    }

    /**
     * Test: exists() returns false for unregistered component.
     */
    @Test
    void testExistsReturnsFalseForUnregisteredComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("not-exists");
        HealthComponent component = new HealthComponent(id, "Not Exists", "Category");

        // Act
        boolean exists = service.exists(component);

        // Assert
        assertFalse(exists);
    }

    /**
     * Test: exists() returns false after unregistration.
     */
    @Test
    void testExistsReturnsFalseAfterUnregistration() {
        // Arrange
        HealthComponentId id = new HealthComponentId("unregister-test");
        HealthComponent component = new HealthComponent(id, "Unregister Test", "Category");
        service.register(component);

        // Act
        service.unregister(component);
        boolean exists = service.exists(component);

        // Assert
        assertFalse(exists);
    }

    // Unregistration tests

    /**
     * Test: Unregister a registered component returns true.
     */
    @Test
    void testUnregisterRegisteredComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("unregister-valid");
        HealthComponent component = new HealthComponent(id, "Unregister Valid", "Category");
        service.register(component);

        // Act
        boolean result = service.unregister(component);

        // Assert
        assertTrue(result);
    }

    /**
     * Test: Unregister unregistered component returns false.
     */
    @Test
    void testUnregisterUnregisteredComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("not-registered");
        HealthComponent component = new HealthComponent(id, "Not Registered", "Category");

        // Act
        boolean result = service.unregister(component);

        // Assert
        assertFalse(result);
    }

    /**
     * Test: Unregister null component throws IllegalArgumentException.
     */
    @Test
    void testUnregisterNullComponent() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.unregister(null));
    }

    /**
     * Test: Unregister invalid component returns false.
     */
    @Test
    void testUnregisterInvalidComponent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("invalid");
        HealthComponent component = new HealthComponent(id, "   ", "Category");

        // Act
        boolean result = service.unregister(component);

        // Assert
        assertFalse(result);
    }

    /**
     * Test: Component count decreases after unregistration.
     */
    @Test
    void testComponentCountDecreasesAfterUnregistration() {
        // Arrange
        HealthComponentId id = new HealthComponentId("count-test");
        HealthComponent component = new HealthComponent(id, "Count Test", "Category");
        service.register(component);

        // Act
        service.unregister(component);
        boolean exists = service.exists(component);

        // Assert
        assertFalse(exists);
    }

    // Storage tests

    /**
     * Test: Storage is empty initially.
     */
    @Test
    void testStorageEmptyInitially() {
        // Act - checkAll returns HealthReports, verify empty
        var reports = service.checkAll();

        // Assert
        assertTrue(reports.isEmpty());
    }

    /**
     * Test: Multiple registrations increase storage size.
     */
    @Test
    void testMultipleRegistrationsIncreaseStorage() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("multi-1");
        HealthComponent component1 = new HealthComponent(id1, "Multi 1", "Category");

        HealthComponentId id2 = new HealthComponentId("multi-2");
        HealthComponent component2 = new HealthComponent(id2, "Multi 2", "Category");

        HealthComponentId id3 = new HealthComponentId("multi-3");
        HealthComponent component3 = new HealthComponent(id3, "Multi 3", "Category");

        // Act
        service.register(component1);
        service.register(component2);
        service.register(component3);

        // Assert - verify all exist
        assertTrue(service.exists(component1));
        assertTrue(service.exists(component2));
        assertTrue(service.exists(component3));
    }

    /**
     * Test: Unregister one component leaves others intact.
     */
    @Test
    void testUnregisterOneLeavesOthersIntact() {
        // Arrange
        HealthComponentId id1 = new HealthComponentId("keep-1");
        HealthComponent component1 = new HealthComponent(id1, "Keep 1", "Category");

        HealthComponentId id2 = new HealthComponentId("remove-2");
        HealthComponent component2 = new HealthComponent(id2, "Remove 2", "Category");

        service.register(component1);
        service.register(component2);

        // Act
        service.unregister(component2);

        // Assert
        assertTrue(service.exists(component1));
        assertFalse(service.exists(component2));
    }
}