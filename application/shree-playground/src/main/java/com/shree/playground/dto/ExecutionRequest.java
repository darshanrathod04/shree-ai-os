package com.shree.playground.dto;

public record ExecutionRequest(
        String capability,
        String input
) {}