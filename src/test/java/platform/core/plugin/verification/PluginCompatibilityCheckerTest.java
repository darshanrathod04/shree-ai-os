package platform.core.plugin.verification;

import com.shreeai.os.platform.core.plugin.verification.PluginCompatibilityChecker;
import com.shreeai.os.platform.core.plugin.verification.VerificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.shreeai.os.platform.core.plugin.model.Plugin;
import com.shreeai.os.platform.core.plugin.model.PluginDescriptor;
import com.shreeai.os.platform.core.plugin.model.PluginId;
import com.shreeai.os.platform.core.plugin.model.PluginState;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PluginCompatibilityCheckerTest {

    private PluginCompatibilityChecker checker;
    private PluginDescriptor descriptor;

    @BeforeEach
    void setUp() {
        checker = new PluginCompatibilityChecker("21", "1.0.0", "1.0.0");
        Plugin plugin = new Plugin(new PluginId("test-plugin"), "Test Plugin", "1.0.0");
        descriptor = new PluginDescriptor(plugin, PluginState.LOADED, Instant.now(), "TestProvider");
    }

    @Test
    void shouldPassWithNoConstraints() {
        VerificationResult result = checker.checkCompatibility(descriptor, null, null, null, null);
        assertTrue(result.isValid());
    }

    @Test
    void shouldPassWithMatchingJavaVersion() {
        VerificationResult result = checker.checkCompatibility(descriptor, "21", null, null, null);
        assertTrue(result.isValid());
    }

    @Test
    void shouldFailWithHigherJavaVersion() {
        VerificationResult result = checker.checkCompatibility(descriptor, "22", null, null, null);
        assertFalse(result.isValid());
        assertTrue(result.issues().get(0).message().contains("Java"));
    }

    @Test
    void shouldPassWithLowerJavaVersion() {
        VerificationResult result = checker.checkCompatibility(descriptor, "17", null, null, null);
        assertTrue(result.isValid());
    }

    @Test
    void shouldFailWithInvalidJavaVersionFormat() {
        VerificationResult result = checker.checkCompatibility(descriptor, "abc", null, null, null);
        assertFalse(result.isValid());
        assertTrue(result.issues().get(0).message().contains("Invalid minimum Java version"));
    }

    @Test
    void shouldFailWithHigherMinPlatformVersion() {
        VerificationResult result = checker.checkCompatibility(descriptor, null, "2.0.0", null, null);
        assertFalse(result.isValid());
        assertTrue(result.issues().get(0).message().contains("minimum platform version"));
    }

    @Test
    void shouldFailWithLowerMaxPlatformVersion() {
        VerificationResult result = checker.checkCompatibility(descriptor, null, null, "0.5.0", null);
        assertFalse(result.isValid());
        assertTrue(result.issues().get(0).message().contains("maximum platform version"));
    }

    @Test
    void shouldPassWithValidPlatformRange() {
        VerificationResult result = checker.checkCompatibility(descriptor, null, "0.5.0", "2.0.0", null);
        assertTrue(result.isValid());
    }

    @Test
    void shouldWarnOnMismatchedPluginApiVersion() {
        VerificationResult result = checker.checkCompatibility(descriptor, null, null, null, "0.9.0");
        assertTrue(result.isValid()); // warning doesn't invalidate
        assertEquals(1, result.warningCount());
        assertTrue(result.issues().get(0).message().contains("Plugin compiled for plugin API"));
    }

    @Test
    void shouldPassWithMatchingPluginApiVersion() {
        VerificationResult result = checker.checkCompatibility(descriptor, null, null, null, "1.0.0");
        assertTrue(result.isValid());
        assertEquals(0, result.warningCount());
    }

    @Test
    void shouldFailWithInvalidSemver() {
        VerificationResult result = checker.checkCompatibility(descriptor, null, "abc", "def", "ghi");
        assertFalse(result.isValid());
        assertEquals(3, result.errorCount());
    }

    @Test
    void shouldRejectNullDescriptor() {
        assertThrows(NullPointerException.class, () ->
                checker.checkCompatibility(null, null, null, null, null));
    }

    @Test
    void shouldRejectNullConstructorArgs() {
        assertThrows(NullPointerException.class, () ->
                new PluginCompatibilityChecker(null, "1.0.0", "1.0.0"));
        assertThrows(NullPointerException.class, () ->
                new PluginCompatibilityChecker("21", null, "1.0.0"));
        assertThrows(NullPointerException.class, () ->
                new PluginCompatibilityChecker("21", "1.0.0", null));
    }

    @Test
    void shouldExposeCurrentVersions() {
        assertEquals("21", checker.currentJavaVersion());
        assertEquals("1.0.0", checker.currentPlatformVersion());
        assertEquals("1.0.0", checker.currentPluginApiVersion());
    }
}