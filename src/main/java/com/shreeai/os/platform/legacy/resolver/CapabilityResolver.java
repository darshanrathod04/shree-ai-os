package com.shreeai.os.platform.legacy.resolver;

import com.shreeai.os.platform.legacy.capability.Capability;
import com.shreeai.os.platform.legacy.capability.CapabilityContext;
import com.shreeai.os.platform.legacy.capability.CapabilityRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/**
 * Capability Resolver — SHADOW MODE ONLY.
 * <p>
 * Determines which registered capability SHOULD handle the current request.
 * It does NOT execute anything. It does NOT replace switch(intent).
 * It does NOT change production behavior.
 * <p>
 * The resolver answers one question:
 * "Which registered capability SHOULD handle this request?"
 * <p>
 * Execution still belongs to the existing production system (AgentBrain).
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Receive Intent + CapabilityContext + CapabilityRegistry</li>
 *   <li>Evaluate all registered capabilities via {@link CapabilityScorer}</li>
 *   <li>Select the best matching capability</li>
 *   <li>Return an immutable {@link CapabilityResolution}</li>
 * </ul>
 * <p>
 * Non-responsibilities:
 * <ul>
 *   <li>Does NOT execute capabilities</li>
 *   <li>Does NOT replace production routing</li>
 *   <li>Does NOT call external services or LLMs</li>
 *   <li>Does NOT modify any state</li>
 * </ul>
 * <p>
 * Thread-safe: no mutable static state, constructor injection only.
 * Performance target: < 1ms average resolution.
 *
 * @see CapabilityScorer
 * @see CapabilityResolution
 * @see ResolutionStrategy
 */
@Component
public final class CapabilityResolver {

    private static final Logger log =
            LoggerFactory.getLogger(CapabilityResolver.class);

    private static final double DIRECT_MATCH_THRESHOLD = 0.65;
    private static final double PRIORITY_MATCH_THRESHOLD = 0.50;
    private static final double MINIMUM_VALID_SCORE = 0.30;

    private final CapabilityRegistry registry;

    /** Immutable snapshot of registered capabilities */
    private final List<Capability> cachedCapabilities;

    /** Cached default Chat capability */
    private final Capability defaultChatCapability;

    public CapabilityResolver(CapabilityRegistry registry) {

        this.registry = registry;

        if (registry == null) {
            this.cachedCapabilities = List.of();
            this.defaultChatCapability = null;
            return;
        }

        List<Capability> snapshot = List.copyOf(registry.listAll());
        this.cachedCapabilities = snapshot;

        Capability chat = null;

        for (Capability capability : snapshot) {
            if ("chat".equalsIgnoreCase(capability.getName())) {
                chat = capability;
                break;
            }
        }

        this.defaultChatCapability = chat;
    }

    /**
     * Resolve best capability.
     */
    public CapabilityResolution resolve(
            String intent,
            CapabilityContext context
    ) {

        Objects.requireNonNull(
                registry,
                "CapabilityRegistry must not be null"
        );


        long startTime = System.nanoTime();

        if (intent == null || intent.isBlank()) {
            return createUnknownResolution(
                    null,
                    System.nanoTime() - startTime,
                    ""
            );
        }

        if (cachedCapabilities.isEmpty()) {
            return createUnknownResolution(
                    null,
                    System.nanoTime() - startTime,
                    intent
            );
        }

        List<CapabilityResolution.Candidate> candidates =
                new ArrayList<>(cachedCapabilities.size());

        CapabilityResolution.Candidate best = null;

        for (Capability capability : cachedCapabilities) {

            double score =
                    CapabilityScorer.score(capability, intent, context);

            CapabilityResolution.Candidate candidate =
                    new CapabilityResolution.Candidate(
                            capability,
                            score,
                            null
                    );

            candidates.add(candidate);

            if (best == null || score > best.getScore()) {
                best = candidate;
            }
        }

        candidates.sort(Comparator.naturalOrder());
        best = candidates.get(0);

        CapabilityResolution resolution =
                buildResolution(
                        best,
                        candidates,
                        intent,
                        System.nanoTime() - startTime
                );

        logResolution(resolution);

        return resolution;
    }

    /**
     * Convenience overload.
     */
    public CapabilityResolution resolve(String intent) {
        return resolve(intent, null);
    }

    /**
     * Shadow comparison with production handler.
     */
    public void compareWithProduction(
            String intent,
            CapabilityContext context,
            String actualHandlerName
    ) {

        CapabilityResolution resolution = resolve(intent, context);

        String predicted =
                resolution.isResolved()
                        ? resolution.getSelectedCapability().getName()
                        : "null";

        if (!predicted.equalsIgnoreCase(actualHandlerName)) {

            log.info(
                    "[RESOLVER COMPARE] intent={} predicted={} actual={} strategy={}",
                    intent,
                    predicted,
                    actualHandlerName,
                    resolution.getStrategy()
            );
        }
    }

    // ---------------------------------------------------------------------
    // Internal Resolution
    // ---------------------------------------------------------------------

    private CapabilityResolution buildResolution(
            CapabilityResolution.Candidate best,
            List<CapabilityResolution.Candidate> candidates,
            String intent,
            long processingTime
    ) {

        Capability capability = best.getCapability();
        double score = best.getScore();

        boolean directMatch = false;

        for (String supported : capability.getSupportedIntents()) {
            if (supported.equalsIgnoreCase(intent)) {
                directMatch = true;
                break;
            }
        }

        ResolutionStrategy strategy;
        String reason;
        double confidence;

        if (directMatch && score >= DIRECT_MATCH_THRESHOLD) {

            strategy = ResolutionStrategy.DIRECT_MATCH;
            confidence = Math.min(score, 1.0);

            reason =
                    "Direct capability match: "
                            + capability.getName()
                            + " -> "
                            + intent;

        } else if (score >= PRIORITY_MATCH_THRESHOLD) {

            strategy =
                    candidates.size() > 1
                            ? ResolutionStrategy.PRIORITY_SELECTION
                            : ResolutionStrategy.DIRECT_MATCH;

            confidence = score;

            reason =
                    "Highest priority capability selected: "
                            + capability.getName();

        } else if (score >= MINIMUM_VALID_SCORE) {

            strategy = ResolutionStrategy.FALLBACK;
            confidence = score;

            reason =
                    "Fallback capability: "
                            + capability.getName();

        } else {

            if (defaultChatCapability == null) {
                return createUnknownResolution(
                        candidates,
                        processingTime,
                        intent
                );
            }

            capability = defaultChatCapability;
            strategy = ResolutionStrategy.DEFAULT;
            confidence = 0.30;
            reason = "Default Chat capability";
        }

        return new CapabilityResolution(
                capability,
                confidence,
                reason,
                strategy,
                candidates,
                processingTime,
                intent,
                deriveCategory(capability, intent)
        );
    }

    private CapabilityResolution createUnknownResolution(
            List<CapabilityResolution.Candidate> candidates,
            long processingTime,
            String intent
    ) {

        return new CapabilityResolution(
                null,
                0.0,
                "No capability found for intent: " + intent,
                ResolutionStrategy.UNKNOWN,
                candidates == null ? List.of() : candidates,
                processingTime,
                intent,
                "UNKNOWN"
        );
    }

    private String deriveCategory(
            Capability capability,
            String intent
    ) {

        if (capability == null) {
            return "UNKNOWN";
        }

        return switch (capability.getName().toLowerCase()) {

            case "learning" -> "LEARNING";

            case "quiz" -> "QUIZ";

            case "roadmap" -> "PLANNING";

            case "chat" -> "CHAT";

            default -> intent;
        };
    }

    private void logResolution(CapabilityResolution resolution) {

        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug(
                "[RESOLVER] intent={} selected={} strategy={} time={}us",
                resolution.getMatchedIntent(),
                resolution.isResolved()
                        ? resolution.getSelectedCapability().getName()
                        : "null",
                resolution.getStrategy(),
                resolution.getProcessingTimeNanos() / 1000
        );
    }
}