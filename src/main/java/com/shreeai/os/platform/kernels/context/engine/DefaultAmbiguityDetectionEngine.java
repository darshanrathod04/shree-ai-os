package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.AmbiguityProfile;
import com.shreeai.os.platform.kernels.context.model.AmbiguityReason;
import com.shreeai.os.platform.kernels.context.model.AmbiguityType;
import com.shreeai.os.platform.kernels.context.model.DomainCandidate;
import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.PrimaryDomain;
import com.shreeai.os.platform.kernels.context.model.PrimaryIntent;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <b>DefaultAmbiguityDetectionEngine</b>
 *
 * <p>Stateless, deterministic, rule-based implementation of
 * {@link AmbiguityDetectionEngine}.</p>
 *
 * <p><b>Diagnosis Pipeline (executed in this exact order):</b></p>
 * <ol>
 *   <li>Structural completeness - only existing cognitive artifacts are read.</li>
 *   <li>MISSING_DOMAIN - BUILD / DEBUG / PLAN intents with no domain.</li>
 *   <li>MISSING_PLATFORM - a single clear MOBILE domain with no platform.</li>
 *   <li>MISSING_GOAL - no concrete goal could be identified.</li>
 *   <li>MULTIPLE_DOMAINS - competing domains with no dominant confidence.</li>
 *   <li>GENERIC_REQUEST - a bare generic action with no specific subject.</li>
 *   <li>Ambiguity score - sum of severities clamped to 0.0 - 1.0.</li>
 * </ol>
 *
 * <p><b>Determinism Guarantee:</b> No randomness, no timestamps, no LLM and no
 * caches. The same input artifacts always produce an identical
 * {@link AmbiguityProfile}.</p>
 *
 * <p><b>Architectural Boundary:</b> Diagnoses only. This engine never asks
 * clarification questions, infers missing values, or decides runtime behaviour.</p>
 *
 * <p><b>Ownership:</b> Context Kernel - P1 Context Intelligence</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see AmbiguityDetectionEngine
 */
public final class DefaultAmbiguityDetectionEngine implements AmbiguityDetectionEngine {

    /** Title produced by the goal engine when no goal was identified. */
    private static final String MISSING_GOAL_TITLE = "no goal identified";

    /** Minimum gap required for one domain to dominate the runners-up. */
    private static final double DOMINANCE_THRESHOLD = 0.15;

    /** Intents that genuinely require a target domain to proceed. */
    private static final Set<PrimaryIntent> DOMAIN_REQUIRING_INTENTS = Set.of(
            PrimaryIntent.BUILD, PrimaryIntent.DEBUG, PrimaryIntent.PLAN);

    /** Goal titles that represent a bare generic action. */
    private static final Set<String> GENERIC_TITLES = Set.of(
            "build", "explain", "create", "make", "do",
            "create project", "build project", "make project",
            "create something", "build something", "make something");

    /** Goal titles that carry no concrete objective. */
    private static final Set<String> GOAL_MISSING_MARKERS = Set.of(
            "help", "help me", "i need help", "assist", "assist me",
            "please help", "can you help me", "need help");

    /**
     * Creates the deterministic ambiguity diagnosis engine. Stateless.
     */
    public DefaultAmbiguityDetectionEngine() {
    }

    @Override
    public AmbiguityProfile diagnose(IntentProfile intentProfile,
                                     DomainProfile domainProfile,
                                     UserConstraints constraints,
                                     GoalStructure goals) {
        Objects.requireNonNull(intentProfile, "intentProfile must not be null");
        Objects.requireNonNull(domainProfile, "domainProfile must not be null");
        Objects.requireNonNull(constraints, "constraints must not be null");
        Objects.requireNonNull(goals, "goals must not be null");

        List<AmbiguityReason> reasons = new ArrayList<>();

        String goalTitle = normalize(goals.primaryGoal().title());
        double goalConfidence = goals.primaryGoal().confidence();
        PrimaryIntent intent = intentProfile.primaryIntent();
        PrimaryDomain primaryDomain = domainProfile.primaryDomain();
        List<DomainCandidate> detectedDomains = domainProfile.detectedDomains();

        // Stage A: generic request (evaluated first so a bare verb is not
        // mis-reported as a missing domain).
        boolean genericRequest = GENERIC_TITLES.contains(goalTitle);
        if (genericRequest) {
            reasons.add(new AmbiguityReason(AmbiguityType.GENERIC_REQUEST,
                    "The request is a generic action without a specific subject, target, "
                            + "or domain and needs more concrete input.",
                    AmbiguityReason.SEVERITY_HIGH));
        }

        // Stage B: missing goal.
        if (isMissingGoal(goalTitle, goalConfidence)) {
            reasons.add(new AmbiguityReason(AmbiguityType.MISSING_GOAL,
                    "No concrete goal could be identified from the request - the user's "
                            + "objective is unknown.",
                    AmbiguityReason.SEVERITY_CRITICAL));
        }

        // Stage C: missing domain, only for intents that truly need one.
        if (!genericRequest && DOMAIN_REQUIRING_INTENTS.contains(intent)
                && (primaryDomain == PrimaryDomain.GENERAL
                    || primaryDomain == PrimaryDomain.UNKNOWN)) {
            reasons.add(new AmbiguityReason(AmbiguityType.MISSING_DOMAIN,
                    "A " + intent.name() + " request requires a target domain, but none "
                            + "was specified.",
                    AmbiguityReason.SEVERITY_HIGH));
        }

        // Stage D: multiple domain competition - never choose one here.
        if (detectedDomains.size() >= 2) {
            double top = detectedDomains.get(0).confidence();
            double second = detectedDomains.get(1).confidence();
            if (top - second < DOMINANCE_THRESHOLD) {
                reasons.add(new AmbiguityReason(AmbiguityType.MULTIPLE_DOMAINS,
                        "Multiple domains compete with no dominant confidence: "
                                + describeDomains(detectedDomains)
                                + ". No single domain should be chosen.",
                        AmbiguityReason.SEVERITY_MEDIUM));
            }
        }

        // Stage E: missing platform for a single clear mobile target.
        if (detectedDomains.size() == 1 && primaryDomain == PrimaryDomain.MOBILE
                && constraints.platform() == null) {
            reasons.add(new AmbiguityReason(AmbiguityType.MISSING_PLATFORM,
                    "A mobile request requires a target platform (Android or iOS), but "
                            + "none was specified.",
                    AmbiguityReason.SEVERITY_MEDIUM));
        }

        // Stage F: structural completeness extras.
        if (intent == PrimaryIntent.PLAN && constraints.duration() == null) {
            reasons.add(new AmbiguityReason(AmbiguityType.MISSING_DURATION,
                    "A planning request should specify an explicit duration, but none "
                            + "was provided.",
                    AmbiguityReason.SEVERITY_LOW));
        }
        if (primaryDomain == PrimaryDomain.EDUCATION
                && intent == PrimaryIntent.LEARN && constraints.language() == null) {
            reasons.add(new AmbiguityReason(AmbiguityType.MISSING_LANGUAGE,
                    "A learning request should specify the target language, but none "
                            + "was provided.",
                    AmbiguityReason.SEVERITY_LOW));
        }

        // Stage G: no identifiable context at all.
        if (intent == PrimaryIntent.UNKNOWN && primaryDomain == PrimaryDomain.UNKNOWN) {
            reasons.add(new AmbiguityReason(AmbiguityType.INSUFFICIENT_CONTEXT,
                    "The request contains no identifiable context, so execution cannot "
                            + "proceed meaningfully.",
                    AmbiguityReason.SEVERITY_CRITICAL));
        }

        // Deterministic ordering: highest severity first, then type name.
        reasons.sort(Comparator
                .comparingDouble((AmbiguityReason r) -> r.severity()).reversed()
                .thenComparing(r -> r.type().name()));

        double score = clamp(reasons.stream()
                .mapToDouble(AmbiguityReason::severity)
                .sum(), 0.0, 1.0);

        return new AmbiguityProfile(!reasons.isEmpty(), reasons, score,
                buildExplanation(!reasons.isEmpty(), reasons));
    }

    private boolean isMissingGoal(String goalTitle, double goalConfidence) {
        return goalTitle.isEmpty()
                || goalTitle.equals(MISSING_GOAL_TITLE)
                || goalConfidence == 0.0
                || GOAL_MISSING_MARKERS.contains(goalTitle);
    }

    private String normalize(String title) {
        return title == null ? "" : title.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private String describeDomains(List<DomainCandidate> domains) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < domains.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            DomainCandidate candidate = domains.get(i);
            sb.append(candidate.domain().name())
                    .append('=')
                    .append(String.format("%.3f", candidate.confidence()));
        }
        return sb.append(']').toString();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String buildExplanation(boolean detected, List<AmbiguityReason> reasons) {
        if (!detected) {
            return "No ambiguity detected - the request is structurally complete.";
        }
        StringBuilder sb = new StringBuilder("Ambiguity detected (")
                .append(reasons.size())
                .append(reasons.size() == 1 ? " risk): " : " risks): ");
        for (int i = 0; i < reasons.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            AmbiguityReason reason = reasons.get(i);
            sb.append(reason.type().name())
                    .append('(')
                    .append(String.format("%.2f", reason.severity()))
                    .append(')');
        }
        return sb.toString();
    }
}
