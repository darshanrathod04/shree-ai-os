package com.shreeai.os.developer.infrastructure;

import com.shreeai.os.platform.sdk.ProjectSDK;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <b>ShreeAiOsConfig</b>
 *
 * <p>Wires the Shree AI OS SDK as Spring beans. This is the only place
 * the application reaches into the platform. Everything else uses
 * {@code ShreeAI}, {@code ProjectSDK}, {@code KnowledgeSDK}, etc.
 * injected via constructor.</p>
 *
 * <p>Configuration values come from {@code application.properties} with
 * the {@code shree.sdk.*} prefix.</p>
 *
 * <p><b>Bean Graph:</b></p>
 * <pre>
 * ShreeAiOsConfig
 *   ├─ @Bean ShreeAI        →  shree.ai (singleton, built via ShreeBuilder)
 *   └─ @Bean ProjectSDK      →  projectSDK (singleton, package-private constructor)
 *
 * WorkspaceService          →  injects ProjectSDK
 * DeveloperWorkflowService  →  injects ProjectSDK
 * AiChatService             →  injects ShreeAI + ProjectSDK  (Sprint-17.3)
 * </pre>
 *
 * <p><b>Application Layer Rule:</b> Never instantiate SDK classes
 * outside this class. Always inject via constructor.</p>
 */
@Configuration
public class ShreeAiOsConfig {

    @Value("${shree.sdk.api-key:local}")
    private String apiKey;

    @Value("${shree.sdk.provider:in-memory}")
    private String provider;

    /**
     * Creates the main ShreeAI SDK instance.
     * Used by AiChatService for knowledge/memory operations.
     */
    @Bean
    public ShreeAI shreeAi() {
        return ShreeAI.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * Creates the ProjectSDK instance.
     * Used by WorkspaceService and DeveloperWorkflowService for project
     * analysis, class discovery, impact analysis, and autonomous workflow.
     *
     * <p>Note: {@code ProjectSDK} has a package-private constructor, so it
     * must be instantiated here rather than via {@code new} at call-sites.
     * The {@code @Bean} factory ensures a single shared instance.</p>
     */
    @Bean
    public ProjectSDK projectSdk() {
        return new ProjectSDK();
    }

    /**
     * Returns the configured provider (in-memory, openai, etc.).
     * Exposed for the diagnostics endpoint.
     */
    public String getProvider() {
        return provider;
    }
}
