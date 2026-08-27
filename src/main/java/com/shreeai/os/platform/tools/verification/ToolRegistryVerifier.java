package com.shreeai.os.platform.tools.verification;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.engine.DefaultToolExecutor;
import com.shreeai.os.platform.tools.impl.*;
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

        registry.register(new EchoTool());
        registry.register(new FileSystemTool());
        registry.register(new TerminalTool());
        registry.register(new GitTool());
        registry.register(new BrowserTool());

        DefaultToolExecutor executor =
                new DefaultToolExecutor(registry);

        ToolResponse echoResponse =
                executor.execute(
                        new ToolRequest(
                                "echo",
                                Map.of("message", "Shree AI")
                        )
                );

        ToolResponse fileResponse =
                executor.execute(
                        new ToolRequest(
                                "filesystem",
                                Map.of(
                                        "operation", "exists",
                                        "path", "pom.xml"
                                )
                        )
                );

        ToolResponse terminalResponse =
                executor.execute(
                        new ToolRequest(
                                "terminal",
                                Map.of(
                                        "command",
                                        "echo Shree"
                                )
                        )
                );

        ToolResponse gitResponse =
                executor.execute(
                        new ToolRequest(
                                "git",
                                Map.of(
                                        "operation", "status",
                                        "path", "."
                                )
                        )
                );

        ToolResponse browserResponse =
                executor.execute(
                        new ToolRequest(
                                "browser",
                                Map.of(
                                        "url",
                                        "https://example.com"
                                )
                        )
                );

        return echoResponse.success()
                && fileResponse.success()
                && terminalResponse.success()
                && gitResponse.success()
                && browserResponse.success()
                && "Shree AI".equals(
                echoResponse.data().get("echo")
        );
    }
}