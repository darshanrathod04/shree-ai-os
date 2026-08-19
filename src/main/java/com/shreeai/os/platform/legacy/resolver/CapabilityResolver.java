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
public class CapabilityResolver {

    private static final Logger log = LoggerFactory.getLogger(CapabilityResolver.class);
    // Intent match contributes 0.40, so scored capabilities supporting the intent get >= 0.72
    // Non-supporting capabilities get ~0.22 (priority + health + availability)
    private static final double DIRECT_MATCH_THRESHOLD = 0.65;
    private static final double PRIORITY_MATCH_THRESHOLD = 0.50;
    // Below this threshold, no capability truly supports the intent
    private static final double MINIMUM_VALID_SCORE = 0.30;

    private final CapabilityRegistry registry;

    /**
     * Constructor injection only — no field injection, no mutable static state.
     */
    public CapabilityResolver(CapabilityRegistry registry) {
        this.registry = registry;
    }

    /**
     * Resolve which capability should handle the given intent.
     * <p>
     * This is the main entry point. It evaluates all capabilities,
     * scores them, and returns the best match as an immutable result.
     * <p>
     * Shadow mode only — never affects execution.
     *
     * @param intent  the detected intent from IntentEngine
     * @param context the capability context (may be null)
     * @return immutable CapabilityResolution with selected capability and metadata
     */
    public CapabilityResolution resolve(String intent, CapabilityContext context) {
        long startTime = System.nanoTime();

        // Validate input
        if (intent == null || intent.isBlank()) {
            long elapsed = System.nanoTime() - startTime;
            log.warn("[RESOLVER] Cannot resolve null/blank intent");
            return createUnknownResolution(null, elapsed, "");
        }

        // Get all registered capabilities
        List<Capability> allCapabilities = registry.listAll();
        if (allCapabilities.isEmpty()) {
            long elapsed = System.nanoTime() - startTime;
            log.warn("[RESOLVER] No capabilities registered for intent: {}", intent);
            return createUnknownResolution(null, elapsed, intent);
        }

        // Score all capabilities
        List<CapabilityResolution.Candidate> scoredCandidates = new ArrayList<>();
        for (Capability cap : allCapabilities) {
            double score = CapabilityScorer.score(cap, intent, context);
            String candidateReason = String.format(
                    "score=%.2f, priority=%d, health=%s, enabled=%s",
                    score, cap.getPriority(), cap.getHealthStatus(), cap.isEnabled()
            );
            scoredCandidates.add(new CapabilityResolution.Candidate(cap, score, candidateReason));
        }

        // Sort by score descending
        scoredCandidates.sort(Comparator.naturalOrder());

        // Select best candidate
        CapabilityResolution.Candidate best = scoredCandidates.get(0);

        // Determine resolution strategy and build result
        long elapsed = System.nanoTime() - startTime;
        CapabilityResolution resolution = buildResolution(best, scoredCandidates, intent, elapsed, context);

        // Log resolution (observer only)
        logResolution(resolution, scoredCandidates);

        return resolution;
    }

    /**
     * Resolve with only intent (no context).
     * Convenience method for simpler use cases.
     */
    public CapabilityResolution resolve(String intent) {
        return resolve(intent, null);
    }

    /**
     * Compare resolver result with actual production handler.
     * Shadow mode — logs comparison but never changes behavior.
     */
    public void compareWithProduction(String intent, CapabilityContext context, String actualHandlerName) {
        CapabilityResolution resolution = resolve(intent, context);
        String predictedHandler = resolution.isResolved()
                ? resolution.getSelectedCapability().getName()
                : "null";

        boolean matches = predictedHandler.equalsIgnoreCase(actualHandlerName);

        if (!matches) {
            log.info("[RESOLVER COMPARE] MISMATCH | intent={} | predicted={} | actual={} | conf={} | strategy={}",
                    intent, predictedHandler, actualHandlerName,
                    String.format("%.0f%%", resolution.getConfidence() * 100),
                    resolution.getStrategy());
        }
    }

    // ── Private Helpers ──

    private CapabilityResolution buildResolution(
            CapabilityResolution.Candidate best,
            List<CapabilityResolution.Candidate> candidates,
            String intent,
            long processingTimeNanos,
            CapabilityContext context) {

        Capability bestCap = best.getCapability();
        double bestScore = best.getScore();
        ResolutionStrategy strategy;
        String reason;
        double confidence;
        String resolvedCategory;

        // Determine if this is a direct match (high score, intent supported)
        boolean directIntentMatch = bestCap.getSupportedIntents().stream()
                .anyMatch(si -> si.equalsIgnoreCase(intent));

        if (directIntentMatch && bestScore >= DIRECT_MATCH_THRESHOLD) {
            strategy = ResolutionStrategy.DIRECT_MATCH;
            confidence = Math.min(1.0, bestScore);
            reason = String.format(
                    "Capability '%s' directly supports intent '%s' with score %.0f%%",
                    bestCap.getName(), intent, confidence * 100);
        } else if (bestScore >= PRIORITY_MATCH_THRESHOLD) {
            strategy = candidates.size() > 1
                    ? ResolutionStrategy.PRIORITY_SELECTION
                    : ResolutionStrategy.DIRECT_MATCH;
            confidence = bestScore;
            reason = String.format(
                    "Capability '%s' selected with score %.0f%% over %d candidates",
                    bestCap.getName(), confidence * 100, candidates.size());
        } else if (bestScore >= MINIMUM_VALID_SCORE) {
            strategy = ResolutionStrategy.FALLBACK;
            confidence = bestScore;
            reason = String.format(
                    "Fallback to capability '%s' with score %.0f%% (no strong match)",
                    bestCap.getName(), bestScore * 100);
        } else {
            // Very low scores across all capabilities — use default (Chat if available, or unknown)
            Optional<Capability> defaultCap = registry.getByName("chat");
            if (defaultCap.isPresent()) {
                strategy = ResolutionStrategy.DEFAULT;
                confidence = 0.3;
                reason = "No matching capability; defaulting to Chat";
                bestCap = defaultCap.get();
            } else {
                return createUnknownResolution(candidates, processingTimeNanos, intent);
            }
        }

        resolvedCategory = deriveCategory(bestCap, intent);

        return new CapabilityResolution(
                bestCap,
                confidence,
                reason,
                strategy,
                candidates,
                processingTimeNanos,
                intent,
                resolvedCategory
        );
    }

    private CapabilityResolution createUnknownResolution(
            List<CapabilityResolution.Candidate> candidates,
            long processingTimeNanos,
            String intent) {

        return new CapabilityResolution(
                null,
                0.0,
                "No capability found for intent: " + intent,
                ResolutionStrategy.UNKNOWN,
                candidates != null ? candidates : List.of(),
                processingTimeNanos,
                intent,
                "UNKNOWN"
        );
    }

    /**
     * Derive a human-readable category from the capability name.
     * Future-proof: can be extended with a mapping registry.
     */
    private String deriveCategory(Capability capability, String intent) {
        if (capability == null) return "UNKNOWN";
        return switch (capability.getName().toLowerCase()) {
            case "learning" -> "LEARNING";
            case "quiz" -> "QUIZ";
            case "roadmap" -> "PLANNING";
            case "chat" -> "CHAT";
            default -> intent;
        };
    }

    /**
     * Log resolver output in the standardized format.
     */
    private void logResolution(CapabilityResolution resolution, List<CapabilityResolution.Candidate> candidates) {
        if (!log.isDebugEnabled()) return;

        log.debug("[RESOLVER] Intent: {} | Selected: {} | Strategy: {} | Confidence: {}% | Time: {}μs",
                resolution.getMatchedIntent(),
                resolution.isResolved() ? resolution.getSelectedCapability().getName() : "null",
                resolution.getStrategy(),
                String.format("%.0f", resolution.getConfidence() * 100),
                resolution.getProcessingTimeNanos() / 1000);
    }
}