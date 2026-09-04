package com.shreeai.os.platform.kernels.developer.patch;

import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult.CompileReport;
import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult.CompileReport.CompileStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>CompileValidationService</b>
 *
 * <p>Executes a Maven {@code compile} against the project root and captures
 * the diagnostics (errors, warnings, files compiled). Never throws raw
 * exceptions — every failure path is converted into a structured
 * {@link CompileReport}.</p>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-17)</p>
 *
 * @since Sprint-17
 */
public final class CompileValidationService {

    private static final Pattern ERROR_PATTERN = Pattern.compile("\\[ERROR\\]\\s+(.+)");
    private static final Pattern WARNING_PATTERN = Pattern.compile("\\[WARNING\\]\\s+(.+)");
    private static final Pattern FILES_COMPILED_PATTERN = Pattern.compile(
            "Building jar|Compiling\\s+\\d+|Files\\s*compiled:\\s*(\\d+)", Pattern.CASE_INSENSITIVE
    );

    private static final int DEFAULT_TIMEOUT_SECONDS = 60;

    /**
     * Runs a Maven compile in the project directory. If the project has a
     * Maven wrapper, that is preferred over a global mvn.
     *
     * @param projectPath the project root directory
     * @return the compile report (never null)
     */
    public CompileReport compile(String projectPath) {
        return compile(projectPath, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Runs a Maven compile in the project directory with a custom timeout.
     *
     * @param projectPath the project root directory
     * @param timeoutSeconds the maximum execution time
     * @return the compile report (never null)
     */
    public CompileReport compile(String projectPath, int timeoutSeconds) {
        if (projectPath == null || projectPath.isBlank()) {
            return CompileReport.skipped("No project path provided");
        }
        Path dir = Path.of(projectPath);
        if (!java.nio.file.Files.isDirectory(dir)) {
            return CompileReport.skipped("Not a directory: " + dir);
        }

        ProcessBuilder pb = buildMavenProcess(dir);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return CompileReport.failure(1, 0,
                        List.of("Maven compile timed out after " + timeoutSeconds + "s"));
            }
            int exitCode = process.exitValue();
            String output = readAllOutput(process);
            return parseOutput(output, exitCode);
        } catch (IOException e) {
            return CompileReport.failure(1, 0,
                    List.of("Maven execution failed: " + e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompileReport.failure(1, 0,
                    List.of("Maven execution interrupted: " + e.getMessage()));
        } catch (Exception e) {
            return CompileReport.failure(1, 0,
                    List.of("Unexpected error: " + e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    /**
     * Parses the output of a Maven compile.
     */
    public CompileReport parseOutput(String output, int exitCode) {
        Objects.requireNonNull(output, "output");
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int filesCompiled = 0;

        String[] lines = output.split("\\r?\\n");
        for (String line : lines) {
            Matcher errM = ERROR_PATTERN.matcher(line);
            if (errM.find()) {
                errors.add(errM.group(1).trim());
                continue;
            }
            Matcher warnM = WARNING_PATTERN.matcher(line);
            if (warnM.find()) {
                warnings.add(warnM.group(1).trim());
                continue;
            }
            Matcher fcM = FILES_COMPILED_PATTERN.matcher(line);
            if (fcM.find()) {
                try {
                    filesCompiled = Math.max(filesCompiled, Integer.parseInt(fcM.group(1)));
                } catch (NumberFormatException ignored) {
                    // not a numeric line — that's fine
                }
            }
        }

        // Diagnostics include both errors and warnings
        List<String> diagnostics = new ArrayList<>();
        diagnostics.addAll(errors);
        diagnostics.addAll(warnings);

        if (exitCode == 0) {
            return CompileReport.success(filesCompiled, warnings.size(), diagnostics);
        } else {
            return CompileReport.failure(errors.size(), warnings.size(), diagnostics);
        }
    }

    /**
     * Builds a quick structural compile report by checking the modified sources
     * without invoking Maven. Useful for fast feedback and tests.
     */
    public CompileReport staticCheck(List<String> sourceFiles) {
        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return CompileReport.success(0, 0, List.of("No source files to check"));
        }
        List<String> diagnostics = new ArrayList<>();
        int errors = 0;
        int warnings = 0;
        for (String file : sourceFiles) {
            if (file == null) continue;
            // Check for basic Java file issues
            if (file.trim().isEmpty()) {
                warnings++;
                diagnostics.add("Empty file detected");
                continue;
            }
            int openBraces = (int) file.chars().filter(c -> c == '{').count();
            int closeBraces = (int) file.chars().filter(c -> c == '}').count();
            if (openBraces != closeBraces) {
                errors++;
                diagnostics.add("Unbalanced braces in file: " + openBraces + " open, " + closeBraces + " close");
            }
        }
        return new CompileReport(
                errors == 0 ? CompileStatus.SUCCESS : CompileStatus.FAILURE,
                sourceFiles.size(), errors, warnings,
                Collections.unmodifiableList(diagnostics));
    }

    private ProcessBuilder buildMavenProcess(Path dir) {
        // Use mvnw (Maven wrapper) if available, otherwise mvn
        boolean hasWrapper = java.nio.file.Files.exists(dir.resolve("mvnw"))
                || java.nio.file.Files.exists(dir.resolve("mvnw.cmd"));
        String[] command = hasWrapper
                ? new String[]{"./mvnw", "compile", "-q", "-o"}
                : new String[]{"mvn", "compile", "-q", "-o"};
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(dir.toFile());
        return pb;
    }

    private String readAllOutput(Process process) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
