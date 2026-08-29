package com.shreeai.os.platform.runtime.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link KernelRegistry}.
 */
@DisplayName("KernelRegistry Tests")
class KernelRegistryTest {

    private KernelRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new KernelRegistry();
    }

    @Test
    @DisplayName("New registry is empty")
    void newRegistryIsEmpty() {
        assertEquals(0, registry.size());
        assertTrue(registry.registeredCapabilities().isEmpty());
    }

    @Test
    @DisplayName("Register and resolve a handler")
    void registerAndResolve() {
        KernelHandler handler = (capability, input, context) ->
                RichExecutionResult.success(capability, "output", 0.9);

        registry.register(ExecutionCapability.KNOWLEDGE_SEARCH, handler);

        Optional<KernelHandler> resolved = registry.resolve(ExecutionCapability.KNOWLEDGE_SEARCH);
        assertTrue(resolved.isPresent());
        assertEquals(1, registry.size());
        assertTrue(registry.isRegistered(ExecutionCapability.KNOWLEDGE_SEARCH));
    }

    @Test
    @DisplayName("Resolve unregistered capability returns empty")
    void resolveUnregisteredReturnsEmpty() {
        Optional<KernelHandler> resolved = registry.resolve(ExecutionCapability.TASK_EXECUTION);
        assertTrue(resolved.isEmpty());
    }

    @Test
    @DisplayName("Register replaces previous handler for same capability")
    void registerReplacesHandler() {
        KernelHandler first = (capability, input, context) ->
                RichExecutionResult.success(capability, "first", 0.5);
        KernelHandler second = (capability, input, context) ->
                RichExecutionResult.success(capability, "second", 0.8);

        registry.register(ExecutionCapability.MEMORY_RECALL, first);
        registry.register(ExecutionCapability.MEMORY_RECALL, second);

        assertEquals(1, registry.size());
        RichExecutionResult result = registry.resolve(ExecutionCapability.MEMORY_RECALL)
                .orElseThrow()
                .handle(ExecutionCapability.MEMORY_RECALL, "input", Map.of());
        assertEquals("second", result.output());
    }

    @Test
    @DisplayName("Register null capability throws IllegalArgumentException")
    void registerNullCapabilityThrows() {
        KernelHandler handler = (capability, input, context) ->
                RichExecutionResult.success(capability, "output", 0.9);
        assertThrows(IllegalArgumentException.class,
                () -> registry.register(null, handler));
    }

    @Test
    @DisplayName("Register null handler throws IllegalArgumentException")
    void registerNullHandlerThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.register(ExecutionCapability.KNOWLEDGE_SEARCH, null));
    }

    @Test
    @DisplayName("Resolve null capability returns empty")
    void resolveNullCapabilityReturnsEmpty() {
        Optional<KernelHandler> resolved = registry.resolve(null);
        assertTrue(resolved.isEmpty());
    }

    @Test
    @DisplayName("isRegistered returns false for null capability")
    void isRegisteredNullReturnsFalse() {
        assertFalse(registry.isRegistered(null));
    }

    @Test
    @DisplayName("registeredCapabilities returns unmodifiable set")
    void registeredCapabilitiesIsUnmodifiable() {
        KernelHandler handler = (capability, input, context) ->
                RichExecutionResult.success(capability, "output", 0.9);
        registry.register(ExecutionCapability.KNOWLEDGE_SEARCH, handler);

        Set<ExecutionCapability> capabilities = registry.registeredCapabilities();
        assertThrows(UnsupportedOperationException.class,
                () -> capabilities.add(ExecutionCapability.TASK_EXECUTION));
    }

    @Test
    @DisplayName("All five capabilities can be registered")
    void allCapabilitiesCanBeRegistered() {
        KernelHandler handler = (capability, input, context) ->
                RichExecutionResult.success(capability, "output", 0.9);

        for (ExecutionCapability capability : ExecutionCapability.values()) {
            registry.register(capability, handler);
        }

        assertEquals(5, registry.size());
        for (ExecutionCapability capability : ExecutionCapability.values()) {
            assertTrue(registry.isRegistered(capability));
        }
    }

    @Test
    @DisplayName("Concurrent registration is thread-safe")
    void concurrentRegistrationIsThreadSafe() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        KernelHandler handler = (capability, input, context) ->
                RichExecutionResult.success(capability, "output", 0.9);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    registry.register(ExecutionCapability.KNOWLEDGE_SEARCH, handler);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, registry.size());
        assertTrue(registry.isRegistered(ExecutionCapability.KNOWLEDGE_SEARCH));
    }
}
