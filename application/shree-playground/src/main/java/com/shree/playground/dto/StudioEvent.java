package com.shree.playground.dto;

import java.time.Instant;
import java.util.Map;

public record StudioEvent(
        String type,
        String requestId,
        String stage,
        Instant timestamp,
        Map<String, Object> metadata
) {}