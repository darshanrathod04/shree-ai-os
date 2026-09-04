package com.shree.playground.dto;

public record PlanCreateRequest(
        String objectiveId,
        String objective,
        String scope
) {}