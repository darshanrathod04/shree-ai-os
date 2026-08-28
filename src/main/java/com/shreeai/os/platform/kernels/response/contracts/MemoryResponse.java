package com.shreeai.os.platform.kernels.response.contracts;

import java.util.List;

public record MemoryResponse(
        String title,
        List<String> memories,
        double confidence
) implements KernelResponse {}