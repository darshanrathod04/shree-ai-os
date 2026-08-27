package com.shreeai.os.platform.tools.verification;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.engine.DefaultToolExecutor;
import com.shreeai.os.platform.tools.impl.FileSystemTool;
import com.shreeai.os.platform.tools.impl.GitTool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.registry.DefaultToolRegistry;
import com.shreeai.os.platform.tools.impl.EchoTool;
import com.shreeai.os.platform.tools.impl.TerminalTool;

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

        return echoResponse.success()
                && fileResponse.success()
                && terminalResponse.success()
                && gitResponse.success()
                && "Shree AI".equals(
                echoResponse.data().get("echo")
        );
    }
}