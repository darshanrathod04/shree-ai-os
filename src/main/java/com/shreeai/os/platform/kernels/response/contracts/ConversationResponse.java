package com.shreeai.os.platform.kernels.response.contracts;

public record ConversationResponse(
        String title,
        String answer,
        double confidence
) implements KernelResponse {}