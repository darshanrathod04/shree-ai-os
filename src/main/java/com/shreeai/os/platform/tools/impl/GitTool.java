package com.shreeai.os.platform.tools.impl;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

public final class GitTool implements Tool {

    @Override
    public String id() {
        return "git";
    }

    @Override
    public String name() {
        return "Git Tool";
    }

    @Override
    public String description() {
        return "Safe Git operations.";
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        String operation = String.valueOf(
                request.arguments().getOrDefault("operation", "")
        );

        String workingDir = String.valueOf(
                request.arguments().getOrDefault("path", ".")
        );

        try {

            return switch (operation) {

                case "status" -> run(
                        workingDir,
                        "git", "status", "--short"
                );

                case "branch" -> run(
                        workingDir,
                        "git", "branch"
                );

                case "init" -> run(
                        workingDir,
                        "git", "init"
                );

                default -> ToolResponse.failure(
                        "Unsupported git operation: " + operation
                );
            };

        } catch (Exception e) {

            return ToolResponse.failure(e.getMessage());

        }
    }

    private ToolResponse run(
            String dir,
            String... command
    ) throws Exception {

        Process process = new ProcessBuilder(command)
                .directory(new java.io.File(dir))
                .start();

        String output = read(process.getInputStream());
        String error = read(process.getErrorStream());

        int exit = process.waitFor();

        return ToolResponse.success(
                "Git command completed",
                Map.of(
                        "stdout", output,
                        "stderr", error,
                        "exitCode", exit
                )
        );
    }

    private String read(java.io.InputStream stream)
            throws Exception {

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(stream)
                );

        StringBuilder builder = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }

        return builder.toString().trim();
    }
}