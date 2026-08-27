package com.shreeai.os.platform.tools.model;

import java.util.Map;
import java.util.Objects;

public record ToolRequest(

        String toolId,
        Map<String, Object> arguments

) {

    public ToolRequest {
        Objects.requireNonNull(toolId);
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }

}