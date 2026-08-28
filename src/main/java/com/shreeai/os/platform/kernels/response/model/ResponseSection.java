package com.shreeai.os.platform.kernels.response.model;

import java.util.Objects;

/**
 * Immutable section of a synthesized response.
 */
public record ResponseSection(

        String title,
        String content

) {

    public ResponseSection {
        Objects.requireNonNull(title);
        Objects.requireNonNull(content);
    }

}