 package com.shreeai.os.platform.runtime.unit;

import org.junit.jupiter.api.Test;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.api.RuntimeBuilder;
import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.lifecycle.RuntimeState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RuntimeBuilder}.
 *
 * <p>Verifies that the RuntimeBuilder correctly constructs Runtime instances
 * and validates required parameters.</p>
 */
class RuntimeBuilderTest {

    @Test
    void shouldBuildRuntimeWithValidConfiguration() {
        RuntimeConfiguration config = RuntimeConfiguration.builder()
                .runtimeName("test-runtime")
                .build();

        RuntimeContract contract = RuntimeContract.builder()
                .contractVersion("1.0.0")
                .build();

        Runtime runtime = RuntimeBuilder.newInstance()
                .configuration(config)
                .contract(contract)
                .build();

        assertNotNull(runtime);
        assertEquals(config, runtime.configuration());
        assertEquals(contract, runtime.contract());
    }

    @Test
    void shouldThrowExceptionWhenConfigurationMissing() {
        RuntimeContract contract = RuntimeContract.builder().build();

        assertThrows(IllegalStateException.class, () ->
                RuntimeBuilder.newInstance()
                        .contract(contract)
                        .build()
        );
    }

    @Test
    void shouldThrowExceptionWhenContractMissing() {
        RuntimeConfiguration config = RuntimeConfiguration.builder().build();

        assertThrows(IllegalStateException.class, () ->
                RuntimeBuilder.newInstance()
                        .configuration(config)
                        .build()
        );
    }

    @Test
    void shouldStartInInitializingState() {
        RuntimeConfiguration config = RuntimeConfiguration.builder().build();
        RuntimeContract contract = RuntimeContract.builder().build();

        Runtime runtime = RuntimeBuilder.newInstance()
                .configuration(config)
                .contract(contract)
                .build();

        assertEquals(
                RuntimeState.INITIALIZING,
                runtime.lifecycle().currentState()
        );
    }

    @Test
    void shouldTransitionToReadyOnStart() {
        RuntimeConfiguration config = RuntimeConfiguration.builder().build();
        RuntimeContract contract = RuntimeContract.builder().build();

        Runtime runtime = RuntimeBuilder.newInstance()
                .configuration(config)
                .contract(contract)
                .build();

        runtime.start();

        assertEquals(
                RuntimeState.READY,
                runtime.lifecycle().currentState()
        );
    }
}