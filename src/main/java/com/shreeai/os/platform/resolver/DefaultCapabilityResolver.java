package com.shreeai.os.platform.resolver;

import com.shreeai.os.platform.gateway.GatewayRequest;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.IntentType;
import com.shreeai.os.platform.sdk.SDKRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>DefaultCapabilityResolver</b>
 *
 * <p>Reference implementation of {@link CapabilityResolver}.</p>
 *
 * <p>Intent detection is deterministic, CPU-only rule matching over the
 * request message, context, and metadata. It never calls the runtime, never
 * executes kernels, and never invokes an LLM.</p>
 *
 * <p>Every plan starts with the cross-cutting baseline capabilities
 * (CONTEXT, VALIDATION, SAFETY, OBSERVABILITY) and adds the capabilities
 * required by the detected intent.</p>
 *
 * <p><b>Ownership:</b> Platform Resolver</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultCapabilityResolver implements CapabilityResolver {

    /**
     * Cross-cutting capabilities required for every request.
     */
    private static final List<CapabilityType> BASELINE_CAPABILITIES = List.of(
            CapabilityType.CONTEXT,
            CapabilityType.VALIDATION,
            CapabilityType.SAFETY,
            CapabilityType.OBSERVABILITY
    );

    /**
     * Intent-specific capabilities, keyed by intent.
     */
    private static final Map<IntentType, List<CapabilityType>> INTENT_CAPABILITIES =
            Map.ofEntries(
                    Map.entry(IntentType.MEMORY_STORE,
                            List.of(CapabilityType.IDENTITY, CapabilityType.MEMORY)),
                    Map.entry(IntentType.MEMORY_RECALL,
                            List.of(CapabilityType.IDENTITY, CapabilityType.MEMORY,
                                    CapabilityType.REASONING)),
                    Map.entry(IntentType.PLANNING,
                            List.of(CapabilityType.IDENTITY, CapabilityType.PLANNING,
                                    CapabilityType.REASONING, CapabilityType.EXECUTION,
                                    CapabilityType.ORCHESTRATION)),
                    Map.entry(IntentType.KNOWLEDGE_QUERY,
                            List.of(CapabilityType.IDENTITY, CapabilityType.KNOWLEDGE,
                                    CapabilityType.REASONING)),
                    Map.entry(IntentType.KNOWLEDGE_SEARCH,
                            List.of(CapabilityType.IDENTITY, CapabilityType.KNOWLEDGE)),
                    Map.entry(IntentType.EXECUTION,
                            List.of(CapabilityType.IDENTITY, CapabilityType.PLANNING,
                                    CapabilityType.REASONING, CapabilityType.EXECUTION,
                                    CapabilityType.TOOLS, CapabilityType.ORCHESTRATION)),
                    Map.entry(IntentType.REFLECTION,
                            List.of(CapabilityType.IDENTITY, CapabilityType.MEMORY,
                                    CapabilityType.REASONING)),
                    Map.entry(IntentType.DEVELOPER,
                            List.of(CapabilityType.IDENTITY, CapabilityType.KNOWLEDGE,
                                    CapabilityType.PLANNING, CapabilityType.REASONING,
                                    CapabilityType.EXECUTION, CapabilityType.TOOLS,
                                    CapabilityType.MODELS, CapabilityType.AGENTS,
                                    CapabilityType.ORCHESTRATION)),
                    Map.entry(IntentType.PROJECT_INTELLIGENCE,
                            List.of(CapabilityType.IDENTITY, CapabilityType.KNOWLEDGE,
                                    CapabilityType.REASONING, CapabilityType.MODELS)),
                    Map.entry(IntentType.CHAT,
                            List.of(CapabilityType.IDENTITY, CapabilityType.MEMORY,
                                    CapabilityType.KNOWLEDGE, CapabilityType.MODELS,
                                    CapabilityType.REASONING))
            );

    /**
     * Keyword rules evaluated in priority (most specific first) order.
     */
    private static final List<Rule> RULES = List.of(
            new Rule(IntentType.DEVELOPER,
                    "implement", "fix the bug", "fix bug", "refactor", "add feature",
                    "write a class", "write a method", "unit test", "sprint",
                    "change the code", "code review"),
            new Rule(IntentType.PROJECT_INTELLIGENCE,
                    "analyze project", "project intelligence", "architecture of",
                    "impact analysis", "class discovery", "codebase"),
            new Rule(IntentType.MEMORY_STORE,
                    "remember", "memorize", "store this", "keep in mind", "save that"),
            new Rule(IntentType.MEMORY_RECALL,
                    "do you remember", "what did i", "recall", "earlier",
                    "previous conversation"),
            new Rule(IntentType.PLANNING,
                    "plan", "goal", "strategy", "roadmap", "steps to", "organize",
                    "break down", "objectives", "schedule"),
            new Rule(IntentType.KNOWLEDGE_QUERY,
                    "what is", "explain", "tell me about", "how does", "define",
                    "why is", "what are"),
            new Rule(IntentType.KNOWLEDGE_SEARCH,
                    "search", "look up", "find articles", "find documents",
                    "find documentation"),
            new Rule(IntentType.EXECUTION,
                    "execute", "run the", "perform", "create file", "write the code",
                    "build the", "take action", "do it now"),
            new Rule(IntentType.REFLECTION,
                    "reflect", "review", "evaluate", "lesson learned",
                    "what went wrong", "improve")
    );

    private static final double OVERRIDE_CONFIDENCE = 0.98;
    private static final double FALLBACK_CONFIDENCE = 0.60;

    @Override
    public CapabilityPlan resolve(SDKRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Detection detection = detect(request);
        List<CapabilityRequirement> requirements = buildRequirements(detection.intent());
        String requestId = deriveRequestId(request);
        Map<String, Object> metadata = buildMetadata(detection, requirements.size());
        return CapabilityPlan.builder()
                .requestId(requestId)
                .detectedIntent(detection.intent())
                .requirements(requirements)
                .confidence(detection.confidence())
                .metadata(metadata)
                .build();
    }

    @Override
    public CapabilityPlan resolve(GatewayRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return resolve(request.sdkRequest());
    }

    // ========================================================================
    // Intent detection
    // ========================================================================

    private Detection detect(SDKRequest request) {
        String text = normalize(request.message());

        // 1. Explicit intent override carried in request metadata.
        Object intentValue = request.metadata().get("intent");
        if (intentValue instanceof String name) {
            try {
                IntentType override = IntentType.valueOf(
                        name.trim().toUpperCase(Locale.ROOT));
                return new Detection(override, OVERRIDE_CONFIDENCE, true);
            } catch (IllegalArgumentException ignored) {
                // fall through to keyword detection
            }
        }

        // 2. Keyword rules in priority order.
        for (Rule rule : RULES) {
            int matches = rule.matches(text);
            if (matches > 0) {
                return new Detection(
                        rule.intent(),
                        keywordConfidence(matches),
                        false);
            }
        }

        // 3. Fallback: conversational intent.
        return new Detection(IntentType.CHAT, FALLBACK_CONFIDENCE, false);
    }

    private static double keywordConfidence(int matches) {
        // 0.75 base, +0.05 per additional keyword, capped at 0.95.
        return Math.min(0.95, 0.75 + 0.05 * Math.min(matches, 4));
    }

    // ========================================================================
    // Capability assembly
    // ========================================================================

    private static List<CapabilityRequirement> buildRequirements(IntentType intent) {
        List<CapabilityRequirement> requirements =
                new ArrayList<>(BASELINE_CAPABILITIES.size() + 8);
        for (CapabilityType capability : BASELINE_CAPABILITIES) {
            requirements.add(new CapabilityRequirement(
                    capability,
                    CapabilityRequirement.Priority.REQUIRED,
                    "cross-cutting baseline capability"));
        }
        for (CapabilityType capability
                : INTENT_CAPABILITIES.getOrDefault(intent, List.of())) {
            requirements.add(new CapabilityRequirement(
                    capability,
                    capability == CapabilityType.ORCHESTRATION
                            ? CapabilityRequirement.Priority.RECOMMENDED
                            : CapabilityRequirement.Priority.REQUIRED,
                    "required for " + intent + " intent"));
        }
        return List.copyOf(requirements);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private static String deriveRequestId(SDKRequest request) {
        if (request.sessionId() != null && !request.sessionId().isBlank()) {
            return request.sessionId();
        }
        return "req-" + UUID.randomUUID();
    }

    private static Map<String, Object> buildMetadata(
            Detection detection, int capabilityCount) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "CapabilityResolver");
        metadata.put("resolver", "DefaultCapabilityResolver");
        metadata.put("intentSource", detection.overridden() ? "metadata" : "keyword");
        metadata.put("capabilityCount", capabilityCount);
        return metadata;
    }

    private static String normalize(String message) {
        return message == null
                ? ""
                : message.toLowerCase(Locale.ROOT);
    }

    // ========================================================================
    // Detection result
    // ========================================================================

    private record Detection(IntentType intent, double confidence, boolean overridden) {
    }

    /**
     * A keyword rule: a detected intent and the keywords that trigger it.
     */
    private record Rule(IntentType intent, List<String> keywords) {

        private Rule(IntentType intent, String... keywords) {
            this(intent, List.of(keywords));
        }

        /**
         * Returns the number of distinct keywords present in the text.
         */
        private int matches(String text) {
            int count = 0;
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    count++;
                }
            }
            return count;
        }
    }
}