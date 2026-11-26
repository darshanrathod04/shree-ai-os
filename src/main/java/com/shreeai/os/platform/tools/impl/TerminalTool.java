package com.shreeai.os.platform.tools.impl;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Constitutional Terminal Tool.
 *
 * Security Rules:
 * - Blocks destructive commands anywhere in command chain
 * - Enforces execution timeout (30s)
 * - Captures stdout/stderr
 * - Returns immutable ToolResponse
 */
public final class TerminalTool implements Tool {

    private static final long TIMEOUT_SECONDS = 30L;
    private static final Pattern BLOCKED_PATTERN = Pattern.compile(
            "(?i)(?:^|[\\s;&|`(])(rm|rmdir|del|format|shutdown|reboot|mkfs|remove-item|format-volume|dd)(?=$|[\\s;&|`)])");

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

        var blockedMatcher = BLOCKED_PATTERN.matcher(command);
        if (blockedMatcher.find()) {
            return ToolResponse.failure(
                    "Blocked command: " + blockedMatcher.group(1).toLowerCase()
            );
        }
        if (command.contains(":(){")) {
            return ToolResponse.failure("Blocked command: fork-bomb pattern");
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

            java.util.concurrent.CompletableFuture<String> stdoutFuture =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> read(process.getInputStream()));
            java.util.concurrent.CompletableFuture<String> stderrFuture =
                    java.util.concurrent.CompletableFuture.supplyAsync(() -> read(process.getErrorStream()));

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return ToolResponse.failure("Command timed out after " + TIMEOUT_SECONDS + " seconds");
            }

            String stdout = stdoutFuture.join();
            String stderr = stderrFuture.join();

            int exit = process.exitValue();

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

    private String read(java.io.InputStream stream) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }
}