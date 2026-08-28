package com.shreeai.os.platform.tools.impl;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;

import java.util.Map;

/**
 * Constitutional reference tool.
 *
 * Future tools (Git, Terminal, Browser, FileSystem)
 * must follow this exact execution pattern.
 */
public final class EchoTool implements Tool {

    @Override
    public String id() {
        return "echo";
    }

    @Override
    public String name() {
        return "Echo Tool";
    }

    @Override
    public String description() {
        return "Returns the provided message.";
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        Object value = request.arguments().get("message");

        String message = value == null
                ? ""
                : value.toString();

        return ToolResponse.success(
                "Echo completed",
                Map.of(
                        "echo", message
                )
        );
    }
}