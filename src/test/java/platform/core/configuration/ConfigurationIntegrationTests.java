package platform.core.configuration;

import platform.core.configuration.engine.ConfigurationResolutionEngine;
import platform.core.configuration.engine.ResolutionResult;
import platform.core.configuration.error.DuplicateConfigurationException;
import platform.core.configuration.error.InvalidConfigurationException;
import platform.core.configuration.model.ConfigurationEntry;
import platform.core.configuration.model.ConfigurationKey;
import platform.core.configuration.model.ConfigurationNamespace;
import platform.core.configuration.model.ConfigurationType;
import platform.core.configuration.service.DefaultConfigurationService;
import platform.core.configuration.validator.ConfigurationValidator;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * <b>ConfigurationIntegrationTests</b>
 *
 * <p>Integration tests for Configuration Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies complete configuration lifecycle.</li>
 *   <li>Verifies validator integration.</li>
 *   <li>Verifies engine integration.</li>
 *   <li>Verifies service integration.</li>
 *   <li>Verifies error integration.</li>
 *   <li>Verifies read-only handling.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultConfigurationService
 * @see ConfigurationValidator
 * @see ConfigurationResolutionEngine
 */
public class ConfigurationIntegrationTests {

    private final DefaultConfigurationService service = new DefaultConfigurationService();

    // Complete configuration lifecycle

    /**
     * Test: Complete configuration lifecycle - register, get, update, remove.
     */
    @org.junit.jupiter.api.Test
    void testCompleteConfigurationLifecycle() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("lifecycle.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "initial value",
                "Test description",
                false,
                Instant.now()
        );

        // Act - Register
        boolean registered = service.register(entry);

        // Assert - Register
        org.junit.jupiter.api.Assertions.assertTrue(registered);

        // Act - Get
        Optional<ConfigurationEntry> retrieved = service.get(new ConfigurationKey("lifecycle.key"));

        // Assert - Get
        org.junit.jupiter.api.Assertions.assertTrue(retrieved.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals("initial value", retrieved.get().value());

        // Act - Exists
        boolean exists = service.exists(new ConfigurationKey("lifecycle.key"));

        // Assert - Exists
        org.junit.jupiter.api.Assertions.assertTrue(exists);

        // Act - List
        Collection<ConfigurationEntry> all = service.list();

        // Assert - List
        org.junit.jupiter.api.Assertions.assertEquals(1, all.size());

        // Act - Remove
        boolean removed = service.remove(new ConfigurationKey("lifecycle.key"));

        // Assert - Remove
        org.junit.jupiter.api.Assertions.assertTrue(removed);
        org.junit.jupiter.api.Assertions.assertFalse(service.exists(new ConfigurationKey("lifecycle.key")));
    }

    /**
     * Test: Multiple configurations lifecycle.
     */
    @org.junit.jupiter.api.Test
    void testMultipleConfigurationsLifecycle() {
        // Arrange & Act - Register multiple
        for (int i = 0; i < 5; i++) {
            ConfigurationEntry entry = new ConfigurationEntry(
                    new ConfigurationKey("lifecycle.key" + i),
                    new ConfigurationNamespace("test.namespace"),
                    ConfigurationType.STRING,
                    "value" + i,
                    "Description " + i,
                    false,
                    Instant.now()
            );
            service.register(entry);
        }

        // Assert - All registered
        org.junit.jupiter.api.Assertions.assertEquals(5, service.list().size());

        // Act - Get each
        for (int i = 0; i < 5; i++) {
            Optional<ConfigurationEntry> result = service.get(new ConfigurationKey("lifecycle.key" + i));
            org.junit.jupiter.api.Assertions.assertTrue(result.isPresent());
        }

        // Act - Remove all
        for (int i = 0; i < 5; i++) {
            service.remove(new ConfigurationKey("lifecycle.key" + i));
        }

        // Assert - All removed
        org.junit.jupiter.api.Assertions.assertTrue(service.list().isEmpty());
    }

    // Validator integration

    /**
     * Test: Validator rejects invalid configurations during registration.
     */
    @org.junit.jupiter.api.Test
    void testValidatorRejectsInvalidConfigurations() {
        // Arrange
        ConfigurationEntry invalidEntry = new ConfigurationEntry(
                new ConfigurationKey("invalid key!"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                InvalidConfigurationException.class,
                () -> service.register(invalidEntry)
        );
    }

    /**
     * Test: Validator allows valid configurations during registration.
     */
    @org.junit.jupiter.api.Test
    void testValidatorAllowsValidConfigurations() {
        // Arrange
        ConfigurationEntry validEntry = new ConfigurationEntry(
                new ConfigurationKey("valid.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );

        // Act
        boolean result = service.register(validEntry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result);
    }

    // Engine integration

    /**
     * Test: Engine resolves valid configurations.
     */
    @org.junit.jupiter.api.Test
    void testEngineResolvesValidConfigurations() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("engine.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "resolved value",
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);

        // Act
        Optional<ConfigurationEntry> result = service.get(new ConfigurationKey("engine.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isPresent());
    }

    /**
     * Test: Engine handles null values correctly.
     */
    @org.junit.jupiter.api.Test
    void testEngineHandlesNullValues() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("null.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                null,
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);

        // Act
        Optional<ConfigurationEntry> result = service.get(new ConfigurationKey("null.key"));

        // Assert - Engine returns empty for null values
        org.junit.jupiter.api.Assertions.assertFalse(result.isPresent());
    }

    // Service integration

    /**
     * Test: Service coordinates validator and engine correctly.
     */
    @org.junit.jupiter.api.Test
    void testServiceCoordinatesValidatorAndEngine() {
        // Arrange
        ConfigurationEntry validEntry = new ConfigurationEntry(
                new ConfigurationKey("coord.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.INTEGER,
                42,
                "Description",
                false,
                Instant.now()
        );

        // Act - Register (validator validates)
        boolean registered = service.register(validEntry);
        org.junit.jupiter.api.Assertions.assertTrue(registered);

        // Act - Get (engine resolves)
        Optional<ConfigurationEntry> retrieved = service.get(new ConfigurationKey("coord.key"));
        org.junit.jupiter.api.Assertions.assertTrue(retrieved.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals(42, retrieved.get().value());
    }

    /**
     * Test: Service prevents duplicate registrations.
     */
    @org.junit.jupiter.api.Test
    void testServicePreventsDuplicateRegistrations() {
        // Arrange
        ConfigurationEntry entry1 = new ConfigurationEntry(
                new ConfigurationKey("dup.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value1",
                "Description",
                false,
                Instant.now()
        );

        ConfigurationEntry entry2 = new ConfigurationEntry(
                new ConfigurationKey("dup.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value2",
                "Description",
                false,
                Instant.now()
        );

        // Act
        service.register(entry1);

        // Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                DuplicateConfigurationException.class,
                () -> service.register(entry2)
        );
    }

    // Error integration

    /**
     * Test: Error architecture is used correctly during service operations.
     */
    @org.junit.jupiter.api.Test
    void testErrorArchitectureIsUsedCorrectly() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("error.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);

        // Act & Assert - Duplicate registration throws DuplicateConfigurationException
        org.junit.jupiter.api.Assertions.assertThrows(
                DuplicateConfigurationException.class,
                () -> service.register(entry)
        );

        // Act & Assert - Read-only removal throws InvalidConfigurationException
        ConfigurationEntry readOnlyEntry = new ConfigurationEntry(
                new ConfigurationKey("readonly-error.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                true,
                Instant.now()
        );
        service.register(readOnlyEntry);

        org.junit.jupiter.api.Assertions.assertThrows(
                InvalidConfigurationException.class,
                () -> service.remove(new ConfigurationKey("readonly-error.key"))
        );
    }

    // Read-only handling

    /**
     * Test: Read-only configurations can be registered and retrieved but not removed.
     */
    @org.junit.jupiter.api.Test
    void testReadOnlyHandling() {
        // Arrange
        ConfigurationEntry readOnlyEntry = new ConfigurationEntry(
                new ConfigurationKey("readonly.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "readonly value",
                "Read-only description",
                true,
                Instant.now()
        );

        // Act - Register
        boolean registered = service.register(readOnlyEntry);
        org.junit.jupiter.api.Assertions.assertTrue(registered);

        // Act - Get
        Optional<ConfigurationEntry> retrieved = service.get(new ConfigurationKey("readonly.key"));
        org.junit.jupiter.api.Assertions.assertTrue(retrieved.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals("readonly value", retrieved.get().value());

        // Act - Exists
        boolean exists = service.exists(new ConfigurationKey("readonly.key"));
        org.junit.jupiter.api.Assertions.assertTrue(exists);

        // Act & Assert - Remove throws exception
        org.junit.jupiter.api.Assertions.assertThrows(
                InvalidConfigurationException.class,
                () -> service.remove(new ConfigurationKey("readonly.key"))
        );

        // Assert - Still exists after failed removal
        org.junit.jupiter.api.Assertions.assertTrue(service.exists(new ConfigurationKey("readonly.key")));
    }

    /**
     * Test: Non-read-only configurations can be removed.
     */
    @org.junit.jupiter.api.Test
    void testNonReadOnlyCanBeRemoved() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("removable.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);

        // Act
        boolean removed = service.remove(new ConfigurationKey("removable.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(removed);
        org.junit.jupiter.api.Assertions.assertFalse(service.exists(new ConfigurationKey("removable.key")));
    }

    // Complex scenarios

    /**
     * Test: Complex scenario with multiple types and operations.
     */
    @org.junit.jupiter.api.Test
    void testComplexScenario() {
        // Arrange - Register multiple configurations
        ConfigurationEntry entry1 = new ConfigurationEntry(
                new ConfigurationKey("complex.key1"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value1",
                "Description 1",
                false,
                Instant.now()
        );

        ConfigurationEntry entry2 = new ConfigurationEntry(
                new ConfigurationKey("complex.key2"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.INTEGER,
                42,
                "Description 2",
                false,
                Instant.now()
        );

        ConfigurationEntry readOnlyEntry = new ConfigurationEntry(
                new ConfigurationKey("complex.key3"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.BOOLEAN,
                true,
                "Description 3",
                true,
                Instant.now()
        );

        // Act
        service.register(entry1);
        service.register(entry2);
        service.register(readOnlyEntry);

        // Assert - All registered
        org.junit.jupiter.api.Assertions.assertEquals(3, service.list().size());

        // Act - Get each
        Optional<ConfigurationEntry> result1 = service.get(new ConfigurationKey("complex.key1"));
        Optional<ConfigurationEntry> result2 = service.get(new ConfigurationKey("complex.key2"));
        Optional<ConfigurationEntry> result3 = service.get(new ConfigurationKey("complex.key3"));

        // Assert - All retrieved
        org.junit.jupiter.api.Assertions.assertTrue(result1.isPresent());
        org.junit.jupiter.api.Assertions.assertTrue(result2.isPresent());
        org.junit.jupiter.api.Assertions.assertTrue(result3.isPresent());

        // Act - Remove non-read-only
        boolean removed = service.remove(new ConfigurationKey("complex.key1"));

        // Assert - Removed successfully
        org.junit.jupiter.api.Assertions.assertTrue(removed);
        org.junit.jupiter.api.Assertions.assertFalse(service.exists(new ConfigurationKey("complex.key1")));

        // Act - Try to remove read-only
        org.junit.jupiter.api.Assertions.assertThrows(
                InvalidConfigurationException.class,
                () -> service.remove(new ConfigurationKey("complex.key3"))
        );

        // Assert - Read-only still exists
        org.junit.jupiter.api.Assertions.assertTrue(service.exists(new ConfigurationKey("complex.key3")));

        // Assert - Final count
        org.junit.jupiter.api.Assertions.assertEquals(2, service.list().size());
    }

    /**
     * Test: Configuration types are preserved correctly.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationTypesPreserved() {
        // Arrange
        ConfigurationEntry stringEntry = new ConfigurationEntry(
                new ConfigurationKey("type.string"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "string value",
                "Description",
                false,
                Instant.now()
        );

        ConfigurationEntry intEntry = new ConfigurationEntry(
                new ConfigurationKey("type.int"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.INTEGER,
                42,
                "Description",
                false,
                Instant.now()
        );

        ConfigurationEntry boolEntry = new ConfigurationEntry(
                new ConfigurationKey("type.bool"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.BOOLEAN,
                true,
                "Description",
                false,
                Instant.now()
        );

        // Act
        service.register(stringEntry);
        service.register(intEntry);
        service.register(boolEntry);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(ConfigurationType.STRING,
                service.get(new ConfigurationKey("type.string")).get().type());
        org.junit.jupiter.api.Assertions.assertEquals(ConfigurationType.INTEGER,
                service.get(new ConfigurationKey("type.int")).get().type());
        org.junit.jupiter.api.Assertions.assertEquals(ConfigurationType.BOOLEAN,
                service.get(new ConfigurationKey("type.bool")).get().type());
    }

    /**
     * Test: Null key operations throw IllegalArgumentException.
     */
    @org.junit.jupiter.api.Test
    void testNullKeyOperationsThrowException() {
        // Act & Assert - get(null)
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.get(null)
        );

        // Act & Assert - exists(null)
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.exists(null)
        );

        // Act & Assert - remove(null)
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.remove(null)
        );

        // Act & Assert - register(null)
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.register(null)
        );
    }
}