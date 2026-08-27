package com.shreeai.os.platform.tools.impl;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Constitutional Terminal Tool.
 *
 * Security Rules:
 * - Blocks destructive commands
 * - Captures stdout/stderr
 * - Returns immutable ToolResponse
 */
public final class TerminalTool implements Tool {

    private static final List<String> BLOCKED = List.of(
            "rm",
            "rmdir",
            "del",
            "format",
            "shutdown",
            "reboot",
            "mkfs"
    );

    @Override
    public String id() {
        return "terminal";
    }

    @Override
    public String name() {
        return "Terminal Tool";
    }

    @Override
    public String description() {
        return "Secure terminal execution.";
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        String command = String.valueOf(
                request.arguments().getOrDefault("command", "")
        );

        if (command.isBlank()) {
            return ToolResponse.failure("Command cannot be empty");
        }

        for (String blocked : BLOCKED) {
            if (command.startsWith(blocked)) {
                return ToolResponse.failure(
                        "Blocked command: " + blocked
                );
            }
        }

        try {

            Process process;

            if (System.getProperty("os.name")
                    .toLowerCase()
                    .contains("win")) {

                process = new ProcessBuilder(
                        "cmd", "/c", command
                ).start();

            } else {

                process = new ProcessBuilder(
                        "bash", "-c", command
                ).start();
            }

            String stdout = read(process.getInputStream());
            String stderr = read(process.getErrorStream());

            int exit = process.waitFor();

            return ToolResponse.success(
                    "Terminal execution completed",
                    Map.of(
                            "stdout", stdout,
                            "stderr", stderr,
                            "exitCode", exit
                    )
            );

        } catch (Exception e) {

            return ToolResponse.failure(e.getMessage());
        }
    }

    private String read(java.io.InputStream stream)
            throws Exception {

        StringBuilder builder = new StringBuilder();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(stream)
                );

        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }

        return builder.toString().trim();
    }
}