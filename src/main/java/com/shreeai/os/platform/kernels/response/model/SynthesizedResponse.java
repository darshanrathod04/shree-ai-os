package com.shreeai.os.platform.kernels.response.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * SynthesizedResponse
 *
 * Final professional response produced by the Response Synthesizer.
 *
 * This is the ONLY response model exposed from the Response Kernel.
 * It contains structured sections, evidence and confidence while
 * hiding internal reasoning.
 */
public record SynthesizedResponse(

        String answer,

        String summary,

        ResponseStyle style,

        List<ResponseSection> sections,

        List<String> evidence,

        double confidence,

        Instant generatedAt

) {

    public SynthesizedResponse {

        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Answer cannot be null or blank");
        }

        if (summary == null) {
            summary = "";
        }

        if (style == null) {
            style = ResponseStyle.CHAT;
        }

        sections = sections == null ? List.of() : List.copyOf(sections);
        evidence = evidence == null ? List.of() : List.copyOf(evidence);

        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
        }

        generatedAt = Objects.requireNonNullElseGet(
                generatedAt,
                Instant::now
        );
    }

    /**
     * Convenience factory for conversational responses.
     */
    public static SynthesizedResponse chat(String answer) {
        return new SynthesizedResponse(
                answer,
                answer,
                ResponseStyle.CHAT,
                List.of(),
                List.of(),
                1.0,
                Instant.now()
        );
    }
}