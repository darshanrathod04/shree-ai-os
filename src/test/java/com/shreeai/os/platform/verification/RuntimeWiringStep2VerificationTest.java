package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.llm.router.LlmRouter;
import com.shreeai.os.platform.resolver.CapabilityType;
import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.service.DefaultRuntimeService;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.security.api.PermissionManager;
import com.shreeai.os.platform.security.model.PermissionDecision;
import com.shreeai.os.platform.security.model.PermissionRequest;
import com.shreeai.os.platform.services.ProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Phase 2 Steps 2.1 & 2.2:
 * - Step 2.1: ShreeAI.builder().apiKey(...) wires into ByokSettingsService and DefaultRuntimeService.llmRouter.
 * - Step 2.2: DefaultRuntimeService.graphPermissionManager() fails closed on unmapped high-risk capabilities.
 */
class RuntimeWiringStep2VerificationTest {

    @Test
    @DisplayName("Step 2.1a: ShreeAI.builder().apiKey(sk-...) wires OpenAI into BYOK & LlmRouter")
    void testBuilderApiKeyWiresOpenAiIntoRouter() throws Exception {
        ShreeAI shree = ShreeAI.builder()
                .apiKey("sk-test-openai-key-12345678")
                .build();

        assertTrue(shree.settings().provider(ProviderType.OPENAI).isPresent());
        assertTrue(shree.settings().provider(ProviderType.OPENAI).get().enabled());

        DefaultRuntimeService runtime = (DefaultRuntimeService) shree.client().runtime();
        LlmRouter router = extractLlmRouter(runtime);
        List<String> providers = router.providerNames();
        assertFalse(providers.isEmpty());
        assertEquals("openai", providers.get(0));
        assertTrue(providers.contains("in-memory"));
        runtime.stop();
    }

    @Test
    @DisplayName("Step 2.1b: ShreeAI.builder().apiKey(AIza...) wires Gemini into BYOK & LlmRouter")
    void testBuilderApiKeyWiresGeminiIntoRouter() throws Exception {
        ShreeAI shree = ShreeAI.builder()
                .apiKey("AIzaSyTestGeminiKey12345678")
                .build();

        assertTrue(shree.settings().provider(ProviderType.GEMINI).isPresent());
        assertTrue(shree.settings().provider(ProviderType.GEMINI).get().enabled());

        DefaultRuntimeService runtime = (DefaultRuntimeService) shree.client().runtime();
        LlmRouter router = extractLlmRouter(runtime);
        List<String> providers = router.providerNames();
        assertFalse(providers.isEmpty());
        assertEquals("gemini", providers.get(0));
        assertTrue(providers.contains("in-memory"));
        runtime.stop();
    }

    @Test
    @DisplayName("Step 2.1c: ShreeAI.builder().apiKey(local) leaves BYOK store empty")
    void testLocalApiKeyDoesNotRegisterCloudProvider() {
        ShreeAI shree = ShreeAI.builder()
                .apiKey("local")
                .build();

        assertTrue(shree.settings().providers().isEmpty());
        shree.client().runtime().stop();
    }

    @Test
    @DisplayName("Step 2.2: graphPermissionManager fails closed on unmapped high-risk capabilities")
    void testGraphPermissionManagerFailClosedForHighRiskCapabilities() throws Exception {
        DefaultRuntimeService runtime = new DefaultRuntimeService(
                RuntimeConfiguration.builder().runtimeName("perm-test").build(),
                RuntimeContract.builder().contractVersion("1.0.0").build()
        );

        Method m = DefaultRuntimeService.class.getDeclaredMethod("graphPermissionManager");
        m.setAccessible(true);
        PermissionManager pm = (PermissionManager) m.invoke(runtime);

        // High-risk unmapped capabilities must DENY (fail-closed)
        assertEquals(PermissionDecision.DENY,
                pm.check(new PermissionRequest(CapabilityType.TOOLS.name(), "run", Map.of())));
        assertEquals(PermissionDecision.DENY,
                pm.check(new PermissionRequest(CapabilityType.MODELS.name(), "call", Map.of())));
        assertEquals(PermissionDecision.DENY,
                pm.check(new PermissionRequest(CapabilityType.AGENTS.name(), "spawn", Map.of())));

        // Unknown / null toolIds must DENY (fail-closed)
        assertEquals(PermissionDecision.DENY,
                pm.check(new PermissionRequest("UNKNOWN_TOOL", "exec", Map.of())));
        assertEquals(PermissionDecision.DENY,
                pm.check(new PermissionRequest(null, "exec", Map.of())));

        // Read-only internal orchestration capabilities remain ALLOW
        assertEquals(PermissionDecision.ALLOW,
                pm.check(new PermissionRequest(CapabilityType.IDENTITY.name(), "resolve", Map.of())));
        assertEquals(PermissionDecision.ALLOW,
                pm.check(new PermissionRequest(CapabilityType.CONTEXT.name(), "build", Map.of())));
        assertEquals(PermissionDecision.ALLOW,
                pm.check(new PermissionRequest(CapabilityType.REASONING.name(), "analyze", Map.of())));
    }

    private static LlmRouter extractLlmRouter(DefaultRuntimeService runtime) throws Exception {
        Field f = DefaultRuntimeService.class.getDeclaredField("llmRouter");
        f.setAccessible(true);
        return (LlmRouter) f.get(runtime);
    }
}
