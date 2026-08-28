package com.shreeai.os.platform.kernels.response.contracts;

import java.util.List;

public record PlanningResponse(
        String title,
        String goal,
        List<String> subtasks,
        List<String> recommendations,
        double confidence
) implements KernelResponse {}