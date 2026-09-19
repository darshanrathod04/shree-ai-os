package com.shreeai.os.platform.kernels.inference.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ExplanationSection</b>
 *
 * <p>One structured, display-ready section of the canonical
 * {@link ExplainableDecision} artifact: a locked section type, a locked
 * title and an ordered list of deterministic bullet points.</p>
 *
 * <p>The section carries pure structured data - no markdown, no HTML and no
 * natural-language generation. Any UI, SDK or LLM may later render the
 * bullets into any presentation format.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the bullet list
 * is defensively copied and unmodifiable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I5 Explainable Decision</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param type         the locked section type (never null)
 * @param title        the locked section title (never null or blank)
 * @param bulletPoints the ordered deterministic bullets (never null, may be
 *                     empty)
 */
public record ExplanationSection(
        ExplanationSectionType type,
        String title,
        List<String> bulletPoints
) {

    /**
     * Creates a validated, deeply-immutable explanation section.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if the title is blank
     */
    public ExplanationSection {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(bulletPoints, "bulletPoints must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        bulletPoints = List.copyOf(bulletPoints);
    }

    @Override
    public String toString() {
        return "ExplanationSection{type=" + type
                + ", title='" + title + "'"
                + ", bullets=" + bulletPoints.size() + "}";
    }
}