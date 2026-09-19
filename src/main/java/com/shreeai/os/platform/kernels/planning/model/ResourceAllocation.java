package com.shreeai.os.platform.kernels.planning.model;

import java.util.Objects;

/** Immutable allocation of positive whole hours to a primary resource. */
public record ResourceAllocation(ResourceType type, int hours) {
    public ResourceAllocation {
        Objects.requireNonNull(type, "type must not be null");
        if (hours < 1) {
            throw new IllegalArgumentException("hours must be >= 1");
        }
    }
}
