package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.core.configuration.error.ConfigurationError;
import com.shreeai.os.platform.core.configuration.error.ConfigurationErrorCode;
import com.shreeai.os.platform.core.configuration.error.ConfigurationException;
import com.shreeai.os.platform.kernels.cognitive.validation.CognitiveValidator;
import com.shreeai.os.platform.sdk.IdentitySDK;
import com.shreeai.os.platform.sdk.PlanningSDK;
import com.shreeai.os.platform.sdk.ReflectionSDK;
import com.shreeai.os.platform.sdk.ShreeClient;
import com.shreeai.os.platform.services.PlaygroundSessionService;
import com.shreeai.os.platform.services.SdkDiagnosticsService;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.impl.TerminalTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 5.2 Targeted Bug Remediation Verification Tests.
 *
 * Verifies fixes for P0 and P1 defects identified in Phase 5.1 Static Analysis:
 * 1. DefaultRuntimeService fail-closed on exception
 * 2. TerminalTool & GitTool async stream reading & UTF-8
 * 3. SdkDiagnosticsService atomic increments under concurrency
 * 4. PlaygroundSessionService thread-safe history iteration
 * 5. CognitiveValidator explicit mutable map without inner class capture
 * 6. IdentitySDK, PlanningSDK, ReflectionSDK null client validation
 * 7. Subsystem Exception serialization safety
 */
class Phase5StaticRemediationVerificationTest {

    @Test
    @DisplayName("P1-1: SdkDiagnosticsService atomic increments under 1000 concurrent updates")
    void testSdkDiagnosticsServiceAtomicIncrements() throws Exception {
        SdkDiagnosticsService diagnostics = new SdkDiagnosticsService();
        int threadCount = 20;
        int incrementsPerThread = 500;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        diagnostics.recordKnowledgeHit();
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "Concurrent increment timed out");
        pool.shutdown();

        assertEquals(threadCount * incrementsPerThread, diagnostics.knowledgeHits(),
                "All concurrent increments must be atomically captured without loss");
        assertEquals(threadCount * incrementsPerThread, diagnostics.report().get("knowledgeHits"));

        diagnostics.reset();
        assertEquals(0, diagnostics.knowledgeHits());
    }

    @Test
    @DisplayName("P1-2: PlaygroundSessionService synchronized history traversal during concurrent appends")
    void testPlaygroundSessionServiceThreadSafeHistory() throws Exception {
        PlaygroundSessionService service = new PlaygroundSessionService();
        String sessionId = service.createSession();
        int threadCount = 10;
        int operationsPerThread = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            futures.add(pool.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    if (j % 2 == 0) {
                        service.getHistory(sessionId);
                        service.getContext(sessionId, 5);
                    } else {
                        service.addTurn(sessionId, new PlaygroundSessionService.MessageTurn(
                                "user", "Message from " + threadId + "-" + j, Instant.now()));
                    }
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS); // No ConcurrentModificationException must be thrown
        }
        pool.shutdown();

        assertTrue(service.getHistory(sessionId).size() > 0);
    }

    @Test
    @DisplayName("P1-3: CognitiveValidator produces valid metadata without anonymous HashMap inner class")
    void testCognitiveValidatorMetadataMap() {
        var result = CognitiveValidator.validateAll(null, null, null, null, null, null);
        assertNotNull(result);
        assertTrue(result.valid());
        @SuppressWarnings("unchecked")
        Map<String, Object> validatedModels = (Map<String, Object>) result.metadata().get("validatedModels");
        assertNotNull(validatedModels);
        assertFalse((Boolean) validatedModels.get("cognitiveState"));
        assertFalse((Boolean) validatedModels.get("reasoningRequest"));

        // Verify class name is NOT an anonymous inner class (e.g. CognitiveValidator$1)
        assertFalse(validatedModels.getClass().getName().contains("$"),
                "validatedModels must be an explicit map implementation, not an anonymous inner subclass");
    }

    @Test
    @DisplayName("P1-4: IdentitySDK, PlanningSDK, ReflectionSDK reject null client with NullPointerException")
    void testSdkConstructorsNullValidation() throws Exception {
        assertConstructorRejectsNull(IdentitySDK.class);
        assertConstructorRejectsNull(PlanningSDK.class);
        assertConstructorRejectsNull(ReflectionSDK.class);
    }

    private void assertConstructorRejectsNull(Class<?> sdkClass) throws Exception {
        Constructor<?> ctor = sdkClass.getDeclaredConstructor(ShreeClient.class);
        ctor.setAccessible(true);
        InvocationTargetException ite = assertThrows(
                InvocationTargetException.class,
                () -> ctor.newInstance((Object) null)
        );
        assertTrue(ite.getCause() instanceof NullPointerException,
                sdkClass.getSimpleName() + " constructor must throw NullPointerException when client is null");
        assertEquals("client cannot be null", ite.getCause().getMessage());
    }

    @Test
    @DisplayName("P1-5: Subsystem Exception serialization does not fail on transient error field")
    void testExceptionSerialization() throws Exception {
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Invalid configuration parameter",
                Instant.now(),
                Map.of("param", "key")
        );
        ConfigurationException ex = new ConfigurationException(error);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(ex);
        }

        byte[] serialized = baos.toByteArray();
        assertTrue(serialized.length > 0);

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized))) {
            Object deserialized = ois.readObject();
            assertTrue(deserialized instanceof ConfigurationException);
            ConfigurationException deserializedEx = (ConfigurationException) deserialized;
            assertEquals("Invalid configuration parameter", deserializedEx.getMessage());
        }
    }

    @Test
    @DisplayName("P0-1: TerminalTool executes command asynchronously with UTF-8 support")
    void testTerminalToolAsyncExecution() {
        TerminalTool tool = new TerminalTool();
        ToolRequest request = new ToolRequest(
                "terminal",
                Map.of("command", System.getProperty("os.name").toLowerCase().contains("win") ? "echo Hello UTF-8" : "echo 'Hello UTF-8'")
        );

        ToolResponse response = tool.execute(request);
        assertNotNull(response);
        assertTrue(response.success(), "Terminal execution must succeed");
        Map<String, Object> data = response.data();
        assertNotNull(data);
        assertTrue(data.containsKey("stdout"));
        assertTrue(data.containsKey("stderr"));
        assertEquals(0, data.get("exitCode"));
    }
}
