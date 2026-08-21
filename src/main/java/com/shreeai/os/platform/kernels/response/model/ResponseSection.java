package com.shreeai.os.platform.kernels.response.model;

import java.util.List;
import java.util.Objects;

/**
 * Immutable section of a synthesized response.
 *
 * Example:
 *  Title: "Strengths"
 *  Content: ["Clean architecture", "DTO pattern"]
 */
public record ResponseSection(
        String title,
        List<String> content
) {

    public ResponseSection {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Section title cannot be null or blank");
        }

        content = content == null ? List.of() : List.copyOf(content);

        Objects.requireNonNull(content, "Content cannot be null");
    }
}