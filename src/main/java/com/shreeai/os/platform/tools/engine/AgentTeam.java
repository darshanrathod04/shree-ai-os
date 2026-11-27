package com.shreeai.os.platform.tools.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <b>AgentTeam</b>
 *
 * <p>OpenAI Agents SDK / CrewAI style multi-agent handoff orchestrator.
 * Specialist {@link SpecialistAgent}s can dynamically hand off control
 * ({@code {"handoff": "<agentName>", "task": "..."}}) to another specialist
 * with bounded handoff depth and full hop audit trail.</p>
 */
public final class AgentTeam {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int DEFAULT_MAX_HANDOFFS = 5;

    private final LlmProvider llmProvider;
    private final Map<String, SpecialistAgent> specialists = new LinkedHashMap<>();
    private final int maxHandoffs;

    public AgentTeam(LlmProvider llmProvider) {
        this(llmProvider, DEFAULT_MAX_HANDOFFS);
    }

    public AgentTeam(LlmProvider llmProvider, int maxHandoffs) {
        this.llmProvider = Objects.requireNonNull(llmProvider, "llmProvider must not be null");
        if (maxHandoffs < 1 || maxHandoffs > 15) {
            throw new IllegalArgumentException("maxHandoffs must be in [1, 15]");
        }
        this.maxHandoffs = maxHandoffs;
    }

    public AgentTeam register(SpecialistAgent agent) {
        Objects.requireNonNull(agent, "agent must not be null");
        specialists.put(agent.name(), agent);
        return this;
    }

    public TeamResult run(String entryAgentName, String model, String goal) {
        Objects.requireNonNull(entryAgentName, "entryAgentName must not be null");
        if (goal == null || goal.isBlank()) {
            throw new IllegalArgumentException("goal must not be null or blank");
        }
        if (!specialists.containsKey(entryAgentName)) {
            throw new IllegalArgumentException("Unknown entry agent: " + entryAgentName);
        }

        String currentAgent = entryAgentName;
        String currentTask = goal;
        List<HandoffHop> hops = new ArrayList<>();

        for (int i = 1; i <= maxHandoffs; i++) {
            SpecialistAgent agent = specialists.get(currentAgent);
            if (agent == null) {
                return new TeamResult("Unknown handoff target: " + currentAgent, List.copyOf(hops), false);
            }

            String prompt = buildAgentPrompt(agent, currentTask, hops);
            String rawOutput;
            try (Stream<String> tokens = llmProvider.stream(LlmRequest.builder()
                    .model(model == null || model.isBlank() ? "default" : model)
                    .prompt(prompt)
                    .stream(false)
                    .build())) {
                rawOutput = tokens.collect(Collectors.joining()).trim();
            }

            HandoffInstruction handoff = tryParseHandoff(rawOutput);
            if (handoff == null) {
                hops.add(new HandoffHop(i, currentAgent, null, currentTask, rawOutput));
                return new TeamResult(rawOutput, List.copyOf(hops), true);
            }

            hops.add(new HandoffHop(i, currentAgent, handoff.targetAgent(), handoff.task(), rawOutput));
            currentAgent = handoff.targetAgent();
            currentTask = handoff.task().isBlank() ? currentTask : handoff.task();
        }

        return new TeamResult(
                "Max handoffs (" + maxHandoffs + ") reached without final response.",
                List.copyOf(hops),
                false
        );
    }

    private String buildAgentPrompt(SpecialistAgent agent, String task, List<HandoffHop> history) {
        String peers = specialists.values().stream()
                .filter(s -> !s.name().equals(agent.name()))
                .map(s -> s.name() + " (" + s.role() + ")")
                .collect(Collectors.joining(", "));
        return "Agent: " + agent.name() + " | Role: " + agent.role() + "\n"
                + "Instructions: " + agent.instructions() + "\n"
                + "Available handoff targets: [" + peers + "]\n"
                + "To hand off, respond ONLY with JSON: {\"handoff\": \"<agentName>\", \"task\": \"<subtask>\"}\n"
                + "Previous hops: " + history.size() + "\n"
                + "Task: " + task;
    }

    private static HandoffInstruction tryParseHandoff(String raw) {
        String trimmed = raw.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(trimmed.substring(start, end + 1));
            if (node.hasNonNull("handoff")) {
                return new HandoffInstruction(
                        node.get("handoff").asText(),
                        node.path("task").asText("")
                );
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    public record SpecialistAgent(String name, String role, String instructions) {
        public SpecialistAgent {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(role, "role must not be null");
            instructions = instructions == null ? "" : instructions;
        }
    }

    private record HandoffInstruction(String targetAgent, String task) {}

    public record HandoffHop(int hop, String fromAgent, String toAgent, String task, String output) {}

    public record TeamResult(String finalAnswer, List<HandoffHop> hops, boolean completed) {}
}
