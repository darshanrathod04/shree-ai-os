package com.shreeai.os.platform.tools.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.tools.api.Tool;
import com.shreeai.os.platform.tools.model.ToolRequest;
import com.shreeai.os.platform.tools.model.ToolResponse;
import com.shreeai.os.platform.tools.registry.ToolRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <b>ToolCallingAgent</b>
 *
 * <p>Bounded ReAct-style tool-calling loop that connects any {@link LlmProvider}
 * (including {@code LlmRouter}) with {@link ToolRegistry} and {@link ToolExecutor}.
 * Every tool invocation passes through {@link ToolExecutor}, preserving
 * fail-closed {@code PermissionManager} and {@code ApprovalService} gates.</p>
 */
public final class ToolCallingAgent {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int DEFAULT_MAX_STEPS = 5;

    private final LlmProvider llmProvider;
    private final ToolExecutor toolExecutor;
    private final ToolRegistry toolRegistry;
    private final int maxSteps;

    public ToolCallingAgent(
            LlmProvider llmProvider,
            ToolExecutor toolExecutor,
            ToolRegistry toolRegistry) {
        this(llmProvider, toolExecutor, toolRegistry, DEFAULT_MAX_STEPS);
    }

    public ToolCallingAgent(
            LlmProvider llmProvider,
            ToolExecutor toolExecutor,
            ToolRegistry toolRegistry,
            int maxSteps) {
        this.llmProvider = Objects.requireNonNull(llmProvider, "llmProvider must not be null");
        this.toolExecutor = Objects.requireNonNull(toolExecutor, "toolExecutor must not be null");
        this.toolRegistry = Objects.requireNonNull(toolRegistry, "toolRegistry must not be null");
        if (maxSteps < 1 || maxSteps > 20) {
            throw new IllegalArgumentException("maxSteps must be between 1 and 20");
        }
        this.maxSteps = maxSteps;
    }

    public Result run(String model, String userGoal) {
        Objects.requireNonNull(model, "model must not be null");
        if (userGoal == null || userGoal.isBlank()) {
            throw new IllegalArgumentException("userGoal must not be null or blank");
        }

        StringBuilder transcript = new StringBuilder();
        transcript.append(buildSystemPrompt()).append("\n\nUser Goal: ").append(userGoal).append("\n");

        List<ToolStep> steps = new ArrayList<>();
        ToolCall previousCall = null;
        for (int step = 1; step <= maxSteps; step++) {
            LlmRequest request = LlmRequest.builder()
                    .model(model)
                    .prompt(transcript.toString())
                    .stream(false)
                    .build();

            String rawOutput;
            try (Stream<String> tokens = llmProvider.stream(request)) {
                rawOutput = tokens.collect(Collectors.joining()).trim();
            }

            Optional<ToolCall> parsedCall = tryParseToolCall(rawOutput);
            if (parsedCall.isEmpty() && looksLikeMalformedToolJson(rawOutput)) {
                String repairPrompt = transcript + "\nAssistant output had malformed JSON: " + rawOutput
                        + "\nRespond ONLY with valid JSON: {\"tool\": \"<toolId>\", \"arguments\": {...}} or {\"final\": \"<answer>\"}.";
                try (Stream<String> retryTokens = llmProvider.stream(LlmRequest.builder()
                        .model(model)
                        .prompt(repairPrompt)
                        .stream(false)
                        .build())) {
                    rawOutput = retryTokens.collect(Collectors.joining()).trim();
                }
                parsedCall = tryParseToolCall(rawOutput);
            }
            if (parsedCall.isEmpty()) {
                String finalAnswer = extractFinalAnswer(rawOutput);
                return new Result(finalAnswer, List.copyOf(steps), true, step);
            }

            ToolCall call = parsedCall.get();
            if (call.equals(previousCall)) {
                return new Result(
                        "Aborted repeated identical tool call to '" + call.toolId() + "' to prevent infinite loop.",
                        List.copyOf(steps),
                        false,
                        step
                );
            }
            previousCall = call;
            ToolResponse response = toolExecutor.execute(new ToolRequest(call.toolId(), call.arguments()));
            steps.add(new ToolStep(step, call.toolId(), call.arguments(), response));

            transcript.append("\nStep ").append(step)
                    .append(" Tool Call: ").append(call.toolId())
                    .append(" args=").append(call.arguments())
                    .append("\nObservation (success=").append(response.success())
                    .append("): ").append(response.message())
                    .append(" data=").append(response.data())
                    .append("\nNext:");
        }

        return new Result(
                "Max tool-calling steps (" + maxSteps + ") reached without final answer.",
                List.copyOf(steps),
                false,
                maxSteps
        );
    }

    private String buildSystemPrompt() {
        String tools = toolRegistry.getAll().stream()
                .map(Tool::id)
                .sorted()
                .collect(Collectors.joining(", "));
        return "You are a Shree AI OS tool-calling agent.\n"
                + "Available tools: [" + tools + "]\n"
                + "To call a tool, respond ONLY with JSON: {\"tool\": \"<toolId>\", \"arguments\": {...}}\n"
                + "When you have the final answer, respond with JSON: {\"final\": \"<answer>\"} or plain text.";
    }

    private static Optional<ToolCall> tryParseToolCall(String rawOutput) {
        String json = extractJsonObject(stripCodeFences(rawOutput));
        if (!json.startsWith("{")) {
            return Optional.empty();
        }
        try {
            JsonNode node = MAPPER.readTree(json);
            if (node.hasNonNull("tool")) {
                String toolId = node.get("tool").asText();
                Map<String, Object> args = node.has("arguments") && node.get("arguments").isObject()
                        ? MAPPER.convertValue(node.get("arguments"), new TypeReference<>() {})
                        : Map.of();
                return Optional.of(new ToolCall(toolId, args));
            }
        } catch (Exception ignored) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static boolean looksLikeMalformedToolJson(String rawOutput) {
        String s = stripCodeFences(rawOutput);
        return (s.contains("\"tool\"") || s.contains("'tool'")) && s.contains("{");
    }

    private static String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1).trim();
        }
        return text;
    }

    private static String extractFinalAnswer(String rawOutput) {
        String json = stripCodeFences(rawOutput);
        if (json.startsWith("{")) {
            try {
                JsonNode node = MAPPER.readTree(json);
                if (node.hasNonNull("final")) {
                    return node.get("final").asText();
                }
            } catch (Exception ignored) {
                // Fall through to rawOutput
            }
        }
        return rawOutput;
    }

    private static String stripCodeFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private record ToolCall(String toolId, Map<String, Object> arguments) {}

    public record ToolStep(int step, String toolId, Map<String, Object> arguments, ToolResponse response) {}

    public record Result(String finalAnswer, List<ToolStep> steps, boolean completed, int iterationsUsed) {}
}
