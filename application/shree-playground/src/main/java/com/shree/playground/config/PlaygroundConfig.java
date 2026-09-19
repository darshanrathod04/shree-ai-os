package com.shree.playground.config;

import com.shreeai.os.platform.sdk.ShreeAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaygroundConfig {

    @Value("${shree.llm.gemini.api-key:${gemini.api.key:${gemini.api-key:${shree.ai.api-key:${GEMINI_API_KEY:}}}}}")
    private String geminiApiKey;

    @Value("${shree.llm.chain:${SHREE_LLM_CHAIN:${shree.ai.chain:gemini,in-memory}}}")
    private String llmChain;

    @Value("${shree.llm.gemini.model:${gemini.model:gemini-3.6-flash}}")
    private String geminiModel;

    @Bean
    public ShreeAI shreeAI() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            System.setProperty("shree.llm.gemini.api-key", geminiApiKey.trim());
            System.setProperty("gemini.api.key", geminiApiKey.trim());
            System.setProperty("gemini.api-key", geminiApiKey.trim());
            System.setProperty("shree.ai.api-key", geminiApiKey.trim());
            System.setProperty("GEMINI_API_KEY", geminiApiKey.trim());
            System.out.println(">>> [PLAYGROUND] Injected Gemini API Key into system properties (masked: "
                    + (geminiApiKey.length() > 6 ? geminiApiKey.substring(0, 4) + "..." : "SET") + ")");
        }

        if (geminiModel != null && !geminiModel.isBlank()) {
            System.setProperty("shree.llm.gemini.model", geminiModel.trim());
            System.setProperty("gemini.model", geminiModel.trim());
            System.out.println(">>> [PLAYGROUND] Injected Gemini model into system properties: " + geminiModel.trim());
        }

        if (llmChain != null && !llmChain.isBlank()) {
            System.setProperty("shree.llm.chain", llmChain.trim());
            System.setProperty("SHREE_LLM_CHAIN", llmChain.trim());
            System.out.println(">>> [PLAYGROUND] Injected LLM Chain into system properties: " + llmChain.trim());
        }

        ShreeAI ai = ShreeAI.builder()
                .apiKey("local")
                .build();

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                ai.settings().configureApiKey(
                        com.shreeai.os.platform.services.ProviderType.GEMINI,
                        geminiApiKey.trim()
                );
                System.out.println(">>> [PLAYGROUND] Successfully activated BYOK Gemini provider in runtime.");
            } catch (Exception e) {
                System.err.println(">>> [PLAYGROUND] Note: BYOK Gemini auto-config: " + e.getMessage());
            }
        }

        return ai;
    }

}