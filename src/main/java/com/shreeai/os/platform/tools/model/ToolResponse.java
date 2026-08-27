package com.shreeai.os.platform.tools.model;

import java.util.Map;

public record ToolResponse(

        boolean success,
        String message,
        Map<String, Object> data

) {

    public static ToolResponse success(
            String message,
            Map<String, Object> data
    ) {
        return new ToolResponse(
                true,
                message,
                data == null ? Map.of() : Map.copyOf(data)
        );
    }

    public static ToolResponse failure(String message) {
        return new ToolResponse(
                false,
                message,
                Map.of()
        );
    }
}