package com.shree.playground.config;

import com.shreeai.os.platform.sdk.ShreeAI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaygroundConfig {

    @Bean
    public ShreeAI shreeAI() {

        return ShreeAI.builder()
                .apiKey("local")
                .build();
    }

}