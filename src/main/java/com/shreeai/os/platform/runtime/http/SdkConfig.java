package com.shreeai.os.platform.runtime.http;

import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <b>SdkConfig</b> — Spring configuration for Shree AI SDK beans.
 *
 * <p>Creates a {@link ShreeAI} bean using the local (in-memory) provider
 * so that controllers can delegate chat requests to the platform's runtime
 * pipeline without requiring external LLM infrastructure.
 *
 * @since Sprint 19
 */
@Configuration
public class SdkConfig {

    @Bean
    public ShreeAI shreeAi() {
        return ShreeAI.builder()
                .apiKey("local")
                .build();
    }
}
