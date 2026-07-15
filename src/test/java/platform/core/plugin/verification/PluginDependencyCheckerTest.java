package platform.core.plugin.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import platform.core.plugin.model.Plugin;
import platform.core.plugin.model.PluginDescriptor;
import platform.core.plugin.model.PluginId;
import platform.core.plugin.model.PluginState;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PluginDependencyCheckerTest {

    private PluginDependencyChecker checker;
    private PluginDescriptor descriptor;

    @BeforeEach
    void setUp() {
        checker = new PluginDependencyChecker(Set.of("memory", "llm", "scheduler"));
        Plugin plugin = new Plugin(new PluginId("test-plugin"), "Test Plugin", "1.0.0");
        descriptor = new PluginDescriptor(plugin, PluginState.LOADED, Instant.now(), "TestProvider");
    }

    @Test
    void shouldPassWithAllDependenciesAvailable() {
        VerificationResult result = checker.checkDependencies(descriptor, List.of("memory", "llm"));
        assertTrue(result.isValid());
        assertEquals(0, result.errorCount());
    }

    @Test
    void shouldFailWithMissingDependency() {
        VerificationResult result = checker.checkDependencies(descriptor, List.of("memory", "unknown-dep"));
        assertFalse(result.isValid());
        assertEquals(1, result.errorCount());
        assertTrue(result.issues().get(0).message().contains("unknown-dep"));
    }

    @Test
    void shouldFailWithMultipleMissingDependencies() {
        VerificationResult result = checker.checkDependencies(descriptor, List.of("missing-a", "missing-b"));
        assertFalse(result.isValid());
        assertEquals(2, result.errorCount());
    }

    @Test
    void shouldPassWithEmptyDependencyList() {
        VerificationResult result = checker.checkDependencies(descriptor, List.of());
        assertTrue(result.isValid());
    }

    @Test
    void shouldPassWithNullDependencyList() {
        VerificationResult result = checker.checkDependencies(descriptor, null);
        assertTrue(result.isValid());
    }

    @Test
    void shouldRejectNullDescriptor() {
        assertThrows(NullPointerException.class, () ->
                checker.checkDependencies(null, List.of("memory")));
    }

    @Test
    void shouldRejectNullAvailableDependencies() {
        assertThrows(NullPointerException.class, () ->
                new PluginDependencyChecker(null));
    }

    @Test
    void shouldReturnUnmodifiableAvailableDependencies() {
        Set<String> deps = checker.availableDependencies();
        assertThrows(UnsupportedOperationException.class, () -> deps.add("new-dep"));
    }
}