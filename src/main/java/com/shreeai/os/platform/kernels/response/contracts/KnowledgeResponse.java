package com.shreeai.os.platform.kernels.response.contracts;

import java.util.List;

public record KnowledgeResponse(
        String title,
        List<String> evidence,
        List<String> relatedTopics,
        double confidence
) implements KernelResponse {}