package com.shreeai.os.platform.kernels.response.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable professional response produced by the Response Synthesizer.
 */
public record SynthesizedResponse(

        String answer,
        List<ResponseSection> sections,
        double confidence,
        ResponseStyle style,
        Instant generatedAt,
        Map<String, Object> structuredData

) {

    public SynthesizedResponse {

        Objects.requireNonNull(answer);
        Objects.requireNonNull(sections);
        Objects.requireNonNull(style);

        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "Confidence must be between 0.0 and 1.0"
            );
        }

        generatedAt = generatedAt == null
                ? Instant.now()
                : generatedAt;

        sections = List.copyOf(sections);

        structuredData = structuredData == null
                ? Map.of()
                : Map.copyOf(structuredData);
    }

    /**
     * Backward-compatible constructor for responses without
     * additional structured data.
     */
    public SynthesizedResponse(
            String answer,
            List<ResponseSection> sections,
            double confidence,
            ResponseStyle style,
            Instant generatedAt
    ) {
        this(answer, sections, confidence, style, generatedAt, Map.of());
    }

    public static SynthesizedResponse simple(
            String answer,
            double confidence
    ) {

        return new SynthesizedResponse(
                answer,
                List.of(),
                confidence,
                ResponseStyle.PROFESSIONAL,
                Instant.now()
        );
    }

}