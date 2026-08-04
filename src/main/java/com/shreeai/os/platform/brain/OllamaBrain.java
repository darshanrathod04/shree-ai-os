package com.shreeai.os.platform.brain;

import com.shreeai.os.platform.llm.OllamaClient;
import org.springframework.stereotype.Component;

@Component
public class OllamaBrain implements BrainInterface {

    private final OllamaClient ollama;

    public OllamaBrain(OllamaClient ollama) {
        this.ollama = ollama;
    }

    @Override
    public String think(String input) {
        // Use generateDirect to avoid duplicate memory recall
        // The caller (PromptBuilder) has already built a complete prompt
        return ollama.generateDirect(input);
    }
}