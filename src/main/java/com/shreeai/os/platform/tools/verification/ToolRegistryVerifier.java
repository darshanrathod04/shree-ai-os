package com.shreeai.os.platform.tools.verification;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;
import com.shreeai.os.platform.tools.impl.EchoTool;

import java.util.Map;

/**
 * Simple architectural verifier for Tool Registry.
 */
public final class ToolRegistryVerifier {

    public boolean verify() {

        DefaultToolRegistry registry = new DefaultToolRegistry();

        registry.register(new EchoTool());

        ToolResponse response =
                registry.find("echo")
                        .orElseThrow()
                        .execute(
                                new ToolRequest(
                                        "echo",
                                        Map.of("message", "Hello")
                                )
                        );

        return response.success()
                && "Hello".equals(response.data().get("echo"))
                && registry.getAll().size() == 1;
    }
}