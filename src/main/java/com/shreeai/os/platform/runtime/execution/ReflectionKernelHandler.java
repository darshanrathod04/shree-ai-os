package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionInput;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link KernelHandler} that bridges the Cognitive Kernel's
 * {@link DefaultReflectionEngine} to the Runtime execution dispatch layer.
 * When dispatched, it evaluates a prior execution's quality, assigns a
 * verdict, and returns the reflection analysis embedded in the
 * {@link RichExecutionResult} metadata.
 *
 * @since 2.1
 */
public final class ReflectionKernelHandler implements KernelHandler {

    private final DefaultReflectionEngine reflectionEngine;

    public ReflectionKernelHandler() {
        this.reflectionEngine = new DefaultReflectionEngine();
    }

    public ReflectionKernelHandler(DefaultReflectionEngine reflectionEngine) {
        this.reflectionEngine = Objects.requireNonNull(
                reflectionEngine, "reflectionEngine must not be null");
    }

    @Override
    public RichExecutionResult handle(
            ExecutionCapability capability,
            String input,
            Map<String, Object> context) {

        Objects.requireNonNull(capability, "capability must not be null");
        Objects.requireNonNull(context, "context must not be null");

        try {
            ReflectionInput reflectionInput = buildReflectionInput(input, context);
            ReflectionAnalysis analysis = reflectionEngine.reflect(reflectionInput);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("reflectionVerdict", analysis.verdict().name());
            metadata.put("reflectionScore", analysis.score());
            metadata.put("reflectionLessons", analysis.lessons());
            metadata.put("reflectionSummary", analysis.summary());
            metadata.put("retryAdvised", analysis.retryAdvised());
            metadata.put("memoryWorthy", analysis.memoryWorthy());
            metadata.put("evaluatedAt", analysis.evaluatedAt().toString());

            String output = buildReflectionOutput(analysis);
            double confidence = clamp(analysis.score());

            return RichExecutionResult.builder()
                    .capability(capability)
                    .status(ExecutionStatus.SUCCESS)
                    .output(output)
                    .confidence(confidence)
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            return RichExecutionResult.failure(
                    capability,
                    "Reflection kernel failed: " + e.getMessage());
        }
    }

    private ReflectionInput buildReflectionInput(String input, Map<String, Object> context) {
        String requestId = (String) context.getOrDefault(
                "requestId", "reflection-" + System.nanoTime());
        String requestText = input != null && !input.isBlank()
                ? input
                : (String) context.getOrDefault("requestText", "");
        int planStepCount = toInt(context.get("planStepCount"), 0);
        String actionStatus = (String) context.getOrDefault("actionStatus", "SUCCESS");
        boolean executionSuccess = toBoolean(context.get("executionSuccess"), true);
        String responseSummary = (String) context.getOrDefault(
                "responseSummary", requestText);
        double confidence = toDouble(context.get("confidence"), 0.5);

        return new ReflectionInput(
                requestId,
                requestText,
                planStepCount,
                actionStatus,
                executionSuccess,
                responseSummary,
                confidence);
    }

    private String buildReflectionOutput(ReflectionAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("Reflection Verdict: ").append(analysis.verdict().name());
        sb.append(" (Score: ").append(String.format("%.2f", analysis.score())).append(")");
        if (!analysis.lessons().isEmpty()) {
            sb.append(" | Lessons: ").append(String.join("; ", analysis.lessons()));
        }
        return sb.toString();
    }

    private static int toInt(Object value, int fallback) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        return fallback;
    }

    private static boolean toBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean b) {
            return b;
        }
        return fallback;
    }

    private static double toDouble(Object value, double fallback) {
        if (value instanceof Number n) {
            return clamp(n.doubleValue());
        }
        return fallback;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
