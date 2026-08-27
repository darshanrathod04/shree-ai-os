package com.shreeai.os.platform.tools.impl;

import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Constitutional File System Tool.
 *
 * Supported operations:
 * - read
 * - write
 * - exists
 */
public final class FileSystemTool implements Tool {

    @Override
    public String id() {
        return "filesystem";
    }

    @Override
    public String name() {
        return "File System Tool";
    }

    @Override
    public String description() {
        return "Safe file read/write operations.";
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        String operation = String.valueOf(
                request.arguments().getOrDefault("operation", "")
        );

        String file = String.valueOf(
                request.arguments().getOrDefault("path", "")
        );

        try {

            Path path = Path.of(file);

            return switch (operation) {

                case "exists" -> ToolResponse.success(
                        "Exists check completed",
                        Map.of("exists", Files.exists(path))
                );

                case "read" -> ToolResponse.success(
                        "File read successfully",
                        Map.of("content", Files.readString(path))
                );

                case "write" -> {

                    String content = String.valueOf(
                            request.arguments().getOrDefault("content", "")
                    );

                    if (path.getParent() != null) {
                        Files.createDirectories(path.getParent());
                    }

                    Files.writeString(path, content);

                    yield ToolResponse.success(
                            "File written successfully",
                            Map.of("path", file)
                    );
                }

                default -> ToolResponse.failure(
                        "Unsupported operation: " + operation
                );
            };

        } catch (IOException e) {

            return ToolResponse.failure(e.getMessage());

        }
    }
}