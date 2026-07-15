package platform.core.plugin.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import platform.core.plugin.model.Plugin;
import platform.core.plugin.model.PluginDescriptor;
import platform.core.plugin.model.PluginId;
import platform.core.plugin.model.PluginState;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PluginVerifierTest {

    private PluginDependencyChecker depChecker;
    private PluginCompatibilityChecker compatChecker;
    private PluginVerifier verifier;
    private PluginDescriptor validDescriptor;
    private Plugin validPlugin;

    @BeforeEach
    void setUp() {
        depChecker = new PluginDependencyChecker(Set.of("memory", "llm", "scheduler"));
        compatChecker = new PluginCompatibilityChecker("21", "1.0.0", "1.0.0");
        verifier = new PluginVerifier(depChecker, compatChecker, Set.of());

        validPlugin = new Plugin(new PluginId("my-plugin"), "My Plugin", "1.0.0");
        validDescriptor = new PluginDescriptor(validPlugin, PluginState.LOADED, Instant.now(), "SCC");
    }

    @Test
    void shouldPassValidPluginDescriptor() {
        VerificationResult result = verifier.verify(validDescriptor);
        assertTrue(result.isValid());
    }

    @Test
    void shouldDetectDuplicatePluginId() {
        PluginId existingId = new PluginId("my-plugin");
        verifier = new PluginVerifier(depChecker, compatChecker, Set.of(existingId));
        VerificationResult result = verifier.verify(validDescriptor);
        assertFalse(result.isValid());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.message().contains("Duplicate plugin ID")));
    }

    @Test
    void shouldRejectNullDescriptor() {
        assertThrows(NullPointerException.class, () -> verifier.verify(null));
    }

    @Test
    void shouldRejectInvalidIdAndVersion() {
        Plugin plugin = new Plugin(new PluginId("123-invalid"), "My Plugin", "bad-version");
        PluginDescriptor descriptor = new PluginDescriptor(
                plugin, PluginState.LOADED, Instant.now(), "ValidProvider");
        VerificationResult result = verifier.verify(descriptor);
        assertFalse(result.isValid());
        assertTrue(result.errorCount() >= 1);
    }

    @Test
    void shouldIncludeVersionInfoOnValidDescriptor() {
        VerificationResult result = verifier.verify(validDescriptor);
        assertTrue(result.isValid());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.message().contains("follows semantic versioning")));
    }

    @Test
    void shouldRejectPluginIdStartingWithDigit() {
        Plugin plugin = new Plugin(new PluginId("123-invalid"), "My Plugin", "1.0.0");
        PluginDescriptor descriptor = new PluginDescriptor(
                plugin, PluginState.LOADED, Instant.now(), "Provider");
        VerificationResult result = verifier.verify(descriptor);
        assertFalse(result.isValid());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.message().contains("Plugin ID must start")));
    }

    @Test
    void shouldRejectPluginIdWithSpaces() {
        Plugin plugin = new Plugin(new PluginId("123 starts with digit"), "Name", "1.0.0");
        PluginDescriptor descriptor = new PluginDescriptor(
                plugin, PluginState.LOADED, Instant.now(), "Provider");
        VerificationResult result = verifier.verify(descriptor);
        assertFalse(result.isValid());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.message().contains("Plugin ID must start")));
    }

    @Test
    void shouldRejectPluginNameTooLong() {
        String longName = "a".repeat(129);
        Plugin plugin = new Plugin(new PluginId("my-plugin"), longName, "1.0.0");
        PluginDescriptor descriptor = new PluginDescriptor(
                plugin, PluginState.LOADED, Instant.now(), "Provider");
        VerificationResult result = verifier.verify(descriptor);
        assertFalse(result.isValid());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.message().contains("between 1 and 128")));
    }

    @Test
    void shouldAcceptOneDotZeroStyleVersions() {
        Plugin plugin = new Plugin(new PluginId("my-plugin"), "My Plugin", "0.0.1");
        PluginDescriptor descriptor = new PluginDescriptor(
                plugin, PluginState.LOADED, Instant.now(), "Provider");
        VerificationResult result = verifier.verify(descriptor);
        assertTrue(result.isValid());
    }

    @Test
    void shouldRejectSingleDigitVersion() {
        Plugin plugin = new Plugin(new PluginId("my-plugin"), "My Plugin", "1");
        PluginDescriptor descriptor = new PluginDescriptor(
                plugin, PluginState.LOADED, Instant.now(), "Provider");
        VerificationResult result = verifier.verify(descriptor);
        assertFalse(result.isValid());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.message().contains("semantic versioning")));
    }

    @Test
    void shouldRejectInvalidSemanticVersion() {
        Plugin plugin = new Plugin(new PluginId("my-plugin"), "My Plugin", "abc");
        PluginDescriptor descriptor = new PluginDescriptor(
                plugin, PluginState.LOADED, Instant.now(), "TestProvider");
        VerificationResult result = verifier.verify(descriptor);
        assertFalse(result.isValid());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.message().contains("semantic versioning")));
    }
}