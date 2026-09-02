package com.shree.playground.dto;

import java.util.Map;

public record IdentityUpdateRequest(
        String identityId,
        Map<String, Object> updates
) {}