package com.shreeai.os.platform.runtime.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FeatureFlags Tests")
class FeatureFlagsTest {

    private FeatureFlags flags;

    @BeforeEach
    void setUp() {
        flags = new FeatureFlags();
        // Clear any inherited environment properties that might interfere
        for (FeatureFlag flag : FeatureFlag.values()) {
            System.clearProperty("shree.feature." + flag.name());
        }
    }

    @AfterEach
    void tearDown() {
        for (FeatureFlag flag : FeatureFlag.values()) {
            System.clearProperty("shree.feature." + flag.name());
        }
    }

    @Test
    @DisplayName("defaults match FeatureFlag definitions")
    void defaultsMatchDefinitions() {
        assertEquals(FeatureFlag.AUTONOMOUS_DISPATCH.defaultEnabled(),
                flags.isEnabled(FeatureFlag.AUTONOMOUS_DISPATCH));
        assertEquals(FeatureFlag.STRUCTURED_LOGGING.defaultEnabled(),
                flags.isEnabled(FeatureFlag.STRUCTURED_LOGGING));
        assertEquals(FeatureFlag.OPEN_TELEMETRY.defaultEnabled(),
                flags.isEnabled(FeatureFlag.OPEN_TELEMETRY));
    }

    @Test
    @DisplayName("programmatic override takes precedence")
    void programmaticOverrideTakesPrecedence() {
        flags.set(FeatureFlag.OPEN_TELEMETRY, true);
        assertTrue(flags.isEnabled(FeatureFlag.OPEN_TELEMETRY));

        flags.set(FeatureFlag.OPEN_TELEMETRY, false);
        assertFalse(flags.isEnabled(FeatureFlag.OPEN_TELEMETRY));
    }

    @Test
    @DisplayName("clear removes programmatic override")
    void clearRemovesOverride() {
        flags.set(FeatureFlag.OPEN_TELEMETRY, true);
        assertTrue(flags.isEnabled(FeatureFlag.OPEN_TELEMETRY));

        flags.clear(FeatureFlag.OPEN_TELEMETRY);
        assertEquals(FeatureFlag.OPEN_TELEMETRY.defaultEnabled(),
                flags.isEnabled(FeatureFlag.OPEN_TELEMETRY));
    }

    @Test
    @DisplayName("system property overrides default")
    void systemPropertyOverridesDefault() {
        System.setProperty("shree.feature.OPEN_TELEMETRY", "true");
        assertTrue(flags.isEnabled(FeatureFlag.OPEN_TELEMETRY));
    }

    @Test
    @DisplayName("system property false value disables")
    void systemPropertyFalseDisables() {
        System.setProperty("shree.feature.AUTONOMOUS_DISPATCH", "false");
        assertFalse(flags.isEnabled(FeatureFlag.AUTONOMOUS_DISPATCH));
    }

    @Test
    @DisplayName("system property accepts 1/0 and on/off")
    void systemPropertyAcceptsAlternateBooleans() {
        System.setProperty("shree.feature.OPEN_TELEMETRY", "1");
        assertTrue(flags.isEnabled(FeatureFlag.OPEN_TELEMETRY));
        System.setProperty("shree.feature.OPEN_TELEMETRY", "off");
        assertFalse(flags.isEnabled(FeatureFlag.OPEN_TELEMETRY));
    }

    @Test
    @DisplayName("invalid system property value falls through to default")
    void invalidSystemPropertyFallsThrough() {
        System.setProperty("shree.feature.OPEN_TELEMETRY", "maybe");
        assertEquals(FeatureFlag.OPEN_TELEMETRY.defaultEnabled(),
                flags.isEnabled(FeatureFlag.OPEN_TELEMETRY));
    }

    @Test
    @DisplayName("delegate fallback is consulted last")
    void delegateFallback() {
        FeatureFlags parent = new FeatureFlags();
        parent.set(FeatureFlag.WORKFLOW_ENGINE, true);

        FeatureFlags child = new FeatureFlags(parent);
        assertTrue(child.isEnabled(FeatureFlag.WORKFLOW_ENGINE));
    }

    @Test
    @DisplayName("child override beats delegate fallback")
    void childOverrideBeatsDelegate() {
        FeatureFlags parent = new FeatureFlags();
        parent.set(FeatureFlag.WORKFLOW_ENGINE, true);

        FeatureFlags child = new FeatureFlags(parent);
        child.set(FeatureFlag.WORKFLOW_ENGINE, false);
        assertFalse(child.isEnabled(FeatureFlag.WORKFLOW_ENGINE));
    }

    @Test
    @DisplayName("snapshot contains all flags with resolved state")
    void snapshotContainsAllFlags() {
        Map<FeatureFlag, Boolean> snapshot = flags.snapshot();
        assertEquals(FeatureFlag.values().length, snapshot.size());
        for (FeatureFlag flag : FeatureFlag.values()) {
            assertTrue(snapshot.containsKey(flag));
            assertEquals(flags.isEnabled(flag), snapshot.get(flag));
        }
    }

    @Test
    @DisplayName("null flag throws")
    void nullFlagThrows() {
        assertThrows(NullPointerException.class, () -> flags.isEnabled(null));
        assertThrows(NullPointerException.class, () -> flags.set(null, true));
        assertThrows(NullPointerException.class, () -> flags.clear(null));
    }

    @Test
    @DisplayName("fromName resolves case-insensitively")
    void fromNameResolvesCaseInsensitively() {
        Optional<FeatureFlag> resolved = FeatureFlag.fromName("open_telemetry");
        assertEquals(Optional.of(FeatureFlag.OPEN_TELEMETRY), resolved);
    }

    @Test
    @DisplayName("fromName returns empty for unknown or blank")
    void fromNameEmptyForUnknown() {
        assertEquals(Optional.empty(), FeatureFlag.fromName("NOT_A_FLAG"));
        assertEquals(Optional.empty(), FeatureFlag.fromName(""));
        assertEquals(Optional.empty(), FeatureFlag.fromName(null));
    }
}
