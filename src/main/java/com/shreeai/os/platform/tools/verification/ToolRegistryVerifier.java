package com.shreeai.os.platform.tools.verification;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;

import java.util.Map;

/**
 * Simple architectural verifier for Tool Registry.
 */
public final class ToolRegistryVerifier {

    public boolean verify() {

        DefaultToolRegistry registry = new DefaultToolRegistry();

        registry.register(new Tool() {

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
                return "Verification tool";
            }

            @Override
            public ToolResponse execute(ToolRequest request) {
                return ToolResponse.success(
                        "OK",
                        Map.of("verified", true)
                );
            }
        });

        return registry.contains("echo")
                && registry.find("echo").isPresent()
                && registry.getAll().size() == 1;
    }
}