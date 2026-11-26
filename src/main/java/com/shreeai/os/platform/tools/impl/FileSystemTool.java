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

    private static final long MAX_READ_BYTES = 2L * 1024 * 1024;
    private final Path sandboxRoot;

    public FileSystemTool() {
        String rootProp = System.getProperty("shree.tools.fs.root", System.getenv("SHREE_TOOLS_FS_ROOT"));
        this.sandboxRoot = (rootProp != null && !rootProp.isBlank())
                ? Path.of(rootProp).toAbsolutePath().normalize()
                : null;
    }

    public FileSystemTool(Path sandboxRoot) {
        this.sandboxRoot = sandboxRoot == null ? null : sandboxRoot.toAbsolutePath().normalize();
    }

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

        if (file.isBlank() || file.indexOf('\0') >= 0) {
            return ToolResponse.failure("Invalid or empty file path");
        }

        try {

            Path path = resolveSafePath(file);

            return switch (operation) {

                case "exists" -> ToolResponse.success(
                        "Exists check completed",
                        Map.of("exists", Files.exists(path))
                );

                case "read" -> {
                    if (Files.exists(path) && Files.size(path) > MAX_READ_BYTES) {
                        yield ToolResponse.failure("File exceeds maximum read size (2 MB)");
                    }
                    yield ToolResponse.success(
                            "File read successfully",
                            Map.of("content", Files.readString(path))
                    );
                }

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

        } catch (SecurityException e) {

            return ToolResponse.failure("Security violation: " + e.getMessage());

        } catch (IOException e) {

            return ToolResponse.failure(e.getMessage());

        }
    }

    private Path resolveSafePath(String rawPath) {
        Path raw = Path.of(rawPath);
        if (sandboxRoot != null) {
            Path resolved = (raw.isAbsolute() ? raw : sandboxRoot.resolve(raw)).toAbsolutePath().normalize();
            if (!resolved.startsWith(sandboxRoot)) {
                throw new SecurityException("Path escapes sandbox root: " + rawPath);
            }
            return resolved;
        }
        return raw.normalize();
    }
}