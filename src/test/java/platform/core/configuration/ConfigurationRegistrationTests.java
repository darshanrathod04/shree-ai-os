package platform.core.configuration;

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

/**
 * <b>ConfigurationRegistrationTests</b>
 *
 * <p>Tests for configuration registration operations within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies configuration registration behavior.</li>
 *   <li>Verifies duplicate key rejection.</li>
 *   <li>Verifies read-only configuration handling.</li>
 *   <li>Verifies multiple registration scenarios.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultConfigurationService
 * @see ConfigurationValidator
 */
public class ConfigurationRegistrationTests {

    private final ConfigurationValidator validator = new ConfigurationValidator();
    private final DefaultConfigurationService service = new DefaultConfigurationService(validator);

    // Register configuration

    /**
     * Test: Register a valid configuration entry.
     */
    @org.junit.jupiter.api.Test
    void testRegisterValidConfiguration() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "test value",
                "Test description",
                false,
                Instant.now()
        );

        // Act
        boolean result = service.register(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result);
    }

    /**
     * Test: Register multiple configurations.
     */
    @org.junit.jupiter.api.Test
    void testRegisterMultipleConfigurations() {
        // Arrange
        ConfigurationEntry entry1 = new ConfigurationEntry(
                new ConfigurationKey("test.key1"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value1",
                "Description 1",
                false,
                Instant.now()
        );

        ConfigurationEntry entry2 = new ConfigurationEntry(
                new ConfigurationKey("test.key2"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value2",
                "Description 2",
                false,
                Instant.now()
        );

        // Act
        boolean result1 = service.register(entry1);
        boolean result2 = service.register(entry2);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result1);
        org.junit.jupiter.api.Assertions.assertTrue(result2);

        Collection<ConfigurationEntry> all = service.list();
        org.junit.jupiter.api.Assertions.assertEquals(2, all.size());
    }

    // Duplicate rejection

    /**
     * Test: Reject duplicate configuration registration.
     */
    @org.junit.jupiter.api.Test
    void testRejectDuplicateConfiguration() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );

        service.register(entry);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                DuplicateConfigurationException.class,
                () -> service.register(entry)
        );
    }

    /**
     * Test: Duplicate key with different value is rejected.
     */
    @org.junit.jupiter.api.Test
    void testRejectDuplicateKeyWithDifferentValue() {
        // Arrange
        ConfigurationEntry entry1 = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value1",
                "Description",
                false,
                Instant.now()
        );

        ConfigurationEntry entry2 = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value2",
                "Description",
                false,
                Instant.now()
        );

        service.register(entry1);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                DuplicateConfigurationException.class,
                () -> service.register(entry2)
        );
    }

    // Read-only registration

    /**
     * Test: Register a read-only configuration.
     */
    @org.junit.jupiter.api.Test
    void testRegisterReadOnlyConfiguration() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                true,
                Instant.now()
        );

        // Act
        boolean result = service.register(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result);
    }

    /**
     * Test: Read-only configuration can be registered but not removed.
     */
    @org.junit.jupiter.api.Test
    void testReadOnlyConfigurationCannotBeRemoved() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                true,
                Instant.now()
        );

        service.register(entry);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                InvalidConfigurationException.class,
                () -> service.remove(new ConfigurationKey("test.key"))
        );
    }

    // Multiple registration

    /**
     * Test: Register configurations with different keys.
     */
    @org.junit.jupiter.api.Test
    void testRegisterMultipleDifferentKeys() {
        // Arrange & Act
        for (int i = 0; i < 10; i++) {
            ConfigurationEntry entry = new ConfigurationEntry(
                    new ConfigurationKey("test.key" + i),
                    new ConfigurationNamespace("test.namespace"),
                    ConfigurationType.STRING,
                    "value" + i,
                    "Description " + i,
                    false,
                    Instant.now()
            );
            service.register(entry);
        }

        // Assert
        Collection<ConfigurationEntry> all = service.list();
        org.junit.jupiter.api.Assertions.assertEquals(10, all.size());
    }

    // Storage size

    /**
     * Test: Verify storage size after registrations.
     */
    @org.junit.jupiter.api.Test
    void testStorageSizeAfterRegistrations() {
        // Arrange
        ConfigurationEntry entry1 = new ConfigurationEntry(
                new ConfigurationKey("test.key1"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value1",
                "Description 1",
                false,
                Instant.now()
        );

        ConfigurationEntry entry2 = new ConfigurationEntry(
                new ConfigurationKey("test.key2"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value2",
                "Description 2",
                false,
                Instant.now()
        );

        // Act
        service.register(entry1);
        service.register(entry2);

        // Assert
        Collection<ConfigurationEntry> all = service.list();
        org.junit.jupiter.api.Assertions.assertEquals(2, all.size());
    }

    /**
     * Test: Storage size is zero initially.
     */
    @org.junit.jupiter.api.Test
    void testStorageSizeInitiallyZero() {
        // Act
        Collection<ConfigurationEntry> all = service.list();

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(all.isEmpty());
    }
}