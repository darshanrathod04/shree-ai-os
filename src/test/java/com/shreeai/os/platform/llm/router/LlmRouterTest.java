package com.shreeai.os.platform.llm.router;

import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.llm.LlmResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link LlmRouter} fail-over and chain resolution. */
class LlmRouterTest {

    private static LlmProvider provider(String name, boolean healthy, String token) {
        return new LlmProvider() {
            @Override
            public String providerName() {
                return name;
            }

            @Override
            public Stream<String> stream(LlmRequest request) {
                if (!healthy) {
                    throw new IllegalStateException(name + " unavailable");
                }
                return Stream.of(token);
            }
        };
    }

    @Test
    void routesToFirstHealthyProvider() {
        LlmRouter router = new LlmRouter(List.of(
                provider("openai", false, "x"),
                provider("ollama", true, "local-token")));

        LlmRequest request = LlmRequest.builder().prompt("p").build();

        assertEquals(List.of("openai", "ollama"), router.providerNames());
        assertEquals("local-token", router.complete(request).content());
        assertTrue(router.route(request).map(LlmProvider::providerName).orElse("").equals("ollama"));
    }

    @Test
    void preferredProviderWinsWhenHealthy() {
        LlmRouter router = new LlmRouter(List.of(
                provider("openai", true, "cloud-token"),
                provider("ollama", true, "local-token")));

        assertEquals("cloud-token", router.complete(LlmRequest.builder().prompt("p").build()).content());
    }

    @Test
    void throwsWhenEveryProviderFails() {
        LlmRouter router = new LlmRouter(List.of(
                provider("openai", false, "x"),
                provider("gemini", false, "y")));

        LlmRequest request = LlmRequest.builder().prompt("p").build();

        assertThrows(IllegalStateException.class, () -> router.stream(request));
        assertTrue(router.route(request).isEmpty());
    }

    @Test
    void routerIsItselfALlmProvider() {
        LlmRouter router = new LlmRouter(List.of(provider("ollama", true, "t")));

        assertEquals(LlmRouter.ROUTER_NAME, router.providerName());

        LlmResponse response = router.complete(LlmRequest.builder().prompt("p").build());
        assertEquals("t", response.content());
        assertEquals(Boolean.TRUE, response.done());
    }

    @Test
    void fromChainResolvesRegistryByProviderName() {
        LlmProvider openai = provider("openai", true, "a");
        LlmProvider ollama = provider("ollama", true, "b");

        LlmRouter router = LlmRouter.fromChain(
                "openai,ollama", Map.of("openai", openai, "ollama", ollama));

        assertEquals(List.of("openai", "ollama"), router.providerNames());
    }

    @Test
    void fromChainRejectsEmptyResolution() {
        assertThrows(IllegalArgumentException.class,
                () -> LlmRouter.fromChain("gemini", Map.of("openai", provider("openai", true, "a"))));
    }
}