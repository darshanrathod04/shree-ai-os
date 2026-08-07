package platform.core.configuration;

import com.shreeai.os.platform.core.configuration.model.ConfigurationEntry;
import com.shreeai.os.platform.core.configuration.model.ConfigurationKey;
import com.shreeai.os.platform.core.configuration.model.ConfigurationNamespace;
import com.shreeai.os.platform.core.configuration.model.ConfigurationType;
import com.shreeai.os.platform.core.configuration.service.DefaultConfigurationService;
import com.shreeai.os.platform.core.configuration.validator.ConfigurationValidator;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * <b>ConfigurationLookupTests</b>
 *
 * <p>Tests for configuration lookup operations within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies configuration retrieval behavior.</li>
 *   <li>Verifies missing configuration handling.</li>
 *   <li>Verifies exists() method behavior.</li>
 *   <li>Verifies list() method behavior.</li>
 *   <li>Verifies remove() method behavior.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultConfigurationService
 * @see ConfigurationValidator
 */
public class ConfigurationLookupTests {

    private final DefaultConfigurationService service = new DefaultConfigurationService();

    // Existing configuration

    /**
     * Test: Retrieve an existing configuration.
     */
    @org.junit.jupiter.api.Test
    void testGetExistingConfiguration() {
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
        service.register(entry);

        // Act
        Optional<ConfigurationEntry> result = service.get(new ConfigurationKey("test.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals("test value", result.get().value());
    }

    /**
     * Test: Retrieve configuration with correct key.
     */
    @org.junit.jupiter.api.Test
    void testGetConfigurationWithCorrectKey() {
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

        service.register(entry1);
        service.register(entry2);

        // Act
        Optional<ConfigurationEntry> result = service.get(new ConfigurationKey("test.key1"));

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals("value1", result.get().value());
    }

    // Missing configuration

    /**
     * Test: Retrieve a non-existing configuration returns empty Optional.
     */
    @org.junit.jupiter.api.Test
    void testGetMissingConfiguration() {
        // Act
        Optional<ConfigurationEntry> result = service.get(new ConfigurationKey("nonexistent.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isPresent());
    }

    /**
     * Test: Retrieve configuration with wrong key returns empty Optional.
     */
    @org.junit.jupiter.api.Test
    void testGetConfigurationWithWrongKey() {
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

        // Act
        Optional<ConfigurationEntry> result = service.get(new ConfigurationKey("wrong.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isPresent());
    }

    /**
     * Test: Get with null key throws IllegalArgumentException.
     */
    @org.junit.jupiter.api.Test
    void testGetWithNullKey() {
        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.get(null)
        );
    }

    // exists()

    /**
     * Test: exists() returns true for existing configuration.
     */
    @org.junit.jupiter.api.Test
    void testExistsReturnsTrueForExistingConfiguration() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("exists.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);

        // Act
        boolean result = service.exists(new ConfigurationKey("exists.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result);
    }

    /**
     * Test: exists() returns false for non-existing configuration.
     */
    @org.junit.jupiter.api.Test
    void testExistsReturnsFalseForMissingConfiguration() {
        // Act
        boolean result = service.exists(new ConfigurationKey("nonexistent.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result);
    }

    /**
     * Test: exists() returns false after removal.
     */
    @org.junit.jupiter.api.Test
    void testExistsReturnsFalseAfterRemoval() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("removed.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);
        service.remove(new ConfigurationKey("removed.key"));

        // Act
        boolean result = service.exists(new ConfigurationKey("removed.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result);
    }

    /**
     * Test: exists() with null key throws IllegalArgumentException.
     */
    @org.junit.jupiter.api.Test
    void testExistsWithNullKey() {
        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.exists(null)
        );
    }

    // list()

    /**
     * Test: list() returns all registered configurations.
     */
    @org.junit.jupiter.api.Test
    void testListReturnsAllConfigurations() {
        // Arrange
        ConfigurationEntry entry1 = new ConfigurationEntry(
                new ConfigurationKey("list.key1"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value1",
                "Description 1",
                false,
                Instant.now()
        );

        ConfigurationEntry entry2 = new ConfigurationEntry(
                new ConfigurationKey("list.key2"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value2",
                "Description 2",
                false,
                Instant.now()
        );

        service.register(entry1);
        service.register(entry2);

        // Act
        Collection<ConfigurationEntry> result = service.list();

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(2, result.size());
    }

    /**
     * Test: list() returns empty collection when no configurations registered.
     */
    @org.junit.jupiter.api.Test
    void testListReturnsEmptyWhenNoConfigurations() {
        // Act
        Collection<ConfigurationEntry> result = service.list();

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isEmpty());
    }

    /**
     * Test: list() returns unmodifiable collection.
     */
    @org.junit.jupiter.api.Test
    void testListReturnsUnmodifiableCollection() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("unmodifiable.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);

        // Act
        Collection<ConfigurationEntry> result = service.list();

        // Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> result.clear()
        );
    }

    // remove()

    /**
     * Test: Remove an existing configuration.
     */
    @org.junit.jupiter.api.Test
    void testRemoveExistingConfiguration() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("remove.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        service.register(entry);

        // Act
        boolean result = service.remove(new ConfigurationKey("remove.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result);
        org.junit.jupiter.api.Assertions.assertFalse(service.exists(new ConfigurationKey("remove.key")));
    }

    /**
     * Test: Remove a non-existing configuration returns false.
     */
    @org.junit.jupiter.api.Test
    void testRemoveNonExistingConfiguration() {
        // Act
        boolean result = service.remove(new ConfigurationKey("nonexistent.key"));

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result);
    }

    /**
     * Test: Remove with null key throws IllegalArgumentException.
     */
    @org.junit.jupiter.api.Test
    void testRemoveWithNullKey() {
        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.remove(null)
        );
    }

    /**
     * Test: Remove one configuration does not affect others.
     */
    @org.junit.jupiter.api.Test
    void testRemoveOneDoesNotAffectOthers() {
        // Arrange
        ConfigurationEntry entry1 = new ConfigurationEntry(
                new ConfigurationKey("remove.key1"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value1",
                "Description 1",
                false,
                Instant.now()
        );

        ConfigurationEntry entry2 = new ConfigurationEntry(
                new ConfigurationKey("remove.key2"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value2",
                "Description 2",
                false,
                Instant.now()
        );

        service.register(entry1);
        service.register(entry2);

        // Act
        service.remove(new ConfigurationKey("remove.key1"));

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(service.exists(new ConfigurationKey("remove.key2")));
        org.junit.jupiter.api.Assertions.assertFalse(service.exists(new ConfigurationKey("remove.key1")));
    }
}