package com.shree.playground.dto;

import java.util.Map;

public record IdentityCreateRequest(
        String identityId,
        String identityType,
        Map<String, Object> profile
) {}