package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecision;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionTarget;
import com.shreeai.os.platform.kernels.acquisition.model.FreshnessReason;
import com.shreeai.os.platform.kernels.acquisition.model.SelectedSource;
import com.shreeai.os.platform.kernels.acquisition.model.SourceSelectionPlan;
import com.shreeai.os.platform.kernels.context.model.ConstraintEvidence;
import com.shreeai.os.platform.kernels.context.model.ContextIntelligence;
import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.model.FreshnessLevel;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>DefaultFreshnessPolicyEngine</b>
 *
 * <p>The K0.6.4 implementation of {@link FreshnessPolicyEngine}. Performs a
 * five-stage, fully deterministic cache decision pipeline over the K0.6.3
 * {@link SourceSelectionPlan} and the K1 {@link KnowledgeSourceRegistry}:</p>
 *
 * <ol>
 *   <li><b>Registry Lookup</b> - the selected source id is resolved against the
 *       registry. When no such source exists the decision is
 *       {@link AcquisitionDecision#ACQUIRE}.</li>
 *   <li><b>Status Check</b> - only {@link KnowledgeSourceStatus#ACTIVE} sources
 *       may serve cache decisions. {@code REGISTERED} and {@code DISABLED}
 *       sources yield {@link AcquisitionDecision#ACQUIRE}.</li>
 *   <li><b>Age Evaluation</b> - the age of the knowledge is measured against the
 *       injected clock and bucketed with the locked K5 freshness policy, then
 *       mapped to {@code USE_CACHE} or {@code REFRESH}.</li>
 *   <li><b>Latest Override</b> - when the context artifacts carry an explicit
 *       recency request, the decision is raised to
 *       {@link AcquisitionDecision#REFRESH}.</li>
 *   <li><b>Build Decision Plan</b> - one target and one reason per selection, in
 *       the canonical order of the selection plan. No downloads, no network
 *       calls, no ingestion.</li>
 * </ol>
 *
 * <p><b>Locked time policy:</b> cache validity is not re-invented here - it is
 * the locked K5 freshness vocabulary ({@link FreshnessLevel}) evaluated through
 * {@link AcquisitionDecision#forFreshnessLevel(FreshnessLevel)}: 0-30 days and
 * 31-180 days ({@code LATEST}, {@code CURRENT}) reuse the cache; 181-365 days
 * and beyond ({@code RECENT}, {@code OUTDATED}, {@code ARCHIVED}) refresh
 * it.</p>
 *
 * <p><b>Timestamp contract (read-only):</b> the K1 {@link KnowledgeSource} model
 * carries no mutable "last updated" field and is never modified by this engine.
 * The knowledge timestamp is therefore resolved deterministically from the
 * immutable source, in this locked order: the {@value #UPDATED_AT_METADATA_KEY}
 * declaration, then the {@value #INGESTED_AT_METADATA_KEY} alias (K5
 * vocabulary), then {@link KnowledgeSource#registeredAt()}. An unparseable
 * declaration is ignored in favour of the next source of truth - the engine
 * never fails and never invents a timestamp.</p>
 *
 * <p><b>Locked recency keywords:</b> {@code latest}, {@code newest},
 * {@code current}, {@code today} - matched whole-word and case-insensitively
 * against the extracted context artifacts (goal titles, goal evidence and
 * constraint evidence). The raw prompt is never re-parsed.</p>
 *
 * <p><b>Never fails:</b> an empty selection plan, an empty registry and a null
 * context all produce a well-formed plan. The engine is stateless, thread-safe
 * and deterministic. No LLM, no embeddings, no network calls, no external
 * dependencies.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.4 Freshness &amp; Cache Policy</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see FreshnessPolicyEngine
 */
public final class DefaultFreshnessPolicyEngine implements FreshnessPolicyEngine {

    /** Metadata key declaring when the cached knowledge was last updated. */
    public static final String UPDATED_AT_METADATA_KEY = "updatedAt";

    /** Metadata key alias declaring when the knowledge was ingested (K5 vocabulary). */
    public static final String INGESTED_AT_METADATA_KEY = "ingestedAt";

    /**
     * Locked recency keywords that raise a cache decision to
     * {@link AcquisitionDecision#REFRESH}. Matched whole-word and
     * case-insensitively - never fuzzy, never stemmed.
     */
    public static final List<String> LATEST_REQUEST_KEYWORDS =
            List.of("latest", "newest", "current", "today");

    /**
     * Locked cache validity limit in days: knowledge older than this must be
     * refreshed. Exactly the K5 {@link FreshnessLevel#MAX_CURRENT_DAYS} boundary.
     */
    public static final long MAX_CACHE_AGE_DAYS = FreshnessLevel.MAX_CURRENT_DAYS;

    private final Clock clock;

    /**
     * Creates an engine reading time from the system UTC clock.
     */
    public DefaultFreshnessPolicyEngine() {
        this(Clock.systemUTC());
    }

    /**
     * Creates an engine reading time from an explicit clock, keeping the cache
     * policy fully deterministic for tests.
     *
     * @param clock the decision clock (must not be null)
     */
    public DefaultFreshnessPolicyEngine(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public AcquisitionDecisionPlan decide(SourceSelectionPlan selectionPlan,
                                          KnowledgeSourceRegistry registry,
                                          ContextIntelligence context) {
        Objects.requireNonNull(selectionPlan, "selectionPlan must not be null");
        Objects.requireNonNull(registry, "registry must not be null");

        Instant now = clock.instant();
        boolean latestRequested = detectsLatestRequest(context);

        List<AcquisitionDecisionTarget> targets = new ArrayList<>();
        List<FreshnessReason> reasons = new ArrayList<>();
        for (SelectedSource selection : selectionPlan.selections()) {
            KnowledgeSource source = registry.findById(selection.sourceId()).orElse(null);
            FreshnessReason reason = decideFor(selection.sourceId(), source,
                    latestRequested, now);
            targets.add(new AcquisitionDecisionTarget(selection.topicId(),
                    selection.topicName(), selection.sourceId(), reason.decision()));
            reasons.add(reason);
        }
        return new AcquisitionDecisionPlan(List.copyOf(targets), List.copyOf(reasons));
    }

    /**
     * Resolves the single locked decision for one selected source, applying the
     * locked pipeline order: registry lookup, status check, age evaluation, then
     * the explicit latest-request override.
     *
     * @param sourceId        the selected source id (must not be null)
     * @param source          the registered source, or null when missing
     * @param latestRequested whether the context artifacts request the latest knowledge
     * @param now             the decision instant
     * @return the immutable reason carrying the decision and its justification
     */
    private static FreshnessReason decideFor(String sourceId,
                                            KnowledgeSource source,
                                            boolean latestRequested,
                                            Instant now) {
        if (source == null) {
            return new FreshnessReason(sourceId, AcquisitionDecision.ACQUIRE,
                    "Source missing: no registered source carries this id - "
                            + "first-time acquisition required");
        }
        if (source.status() != KnowledgeSourceStatus.ACTIVE) {
            return new FreshnessReason(sourceId, AcquisitionDecision.ACQUIRE,
                    "Source unusable: status=" + source.status() + " (not ACTIVE) - "
                            + "first-time acquisition required");
        }

        Instant lastUpdated = lastUpdatedAtOf(source);
        FreshnessLevel freshness = FreshnessLevel.ofAge(
                Duration.between(lastUpdated, now));
        long ageDays = ageInDays(lastUpdated, now);

        if (latestRequested) {
            return new FreshnessReason(sourceId, AcquisitionDecision.REFRESH,
                    "User requested latest: explicit freshness request found in the "
                            + "context artifacts - cache bypassed, refresh required");
        }
        if (AcquisitionDecision.forFreshnessLevel(freshness)
                == AcquisitionDecision.USE_CACHE) {
            return new FreshnessReason(sourceId, AcquisitionDecision.USE_CACHE,
                    "Cache valid: age=" + ageDays + " days, freshness=" + freshness
                            + ", cache limit=" + MAX_CACHE_AGE_DAYS + " days");
        }
        return new FreshnessReason(sourceId, AcquisitionDecision.REFRESH,
                "Cache expired: age=" + ageDays + " days, freshness=" + freshness
                        + ", cache limit=" + MAX_CACHE_AGE_DAYS + " days");
    }

    /**
     * Resolves the immutable timestamp of the cached knowledge for a registered
     * source: the {@value #UPDATED_AT_METADATA_KEY} declaration first, then the
     * {@value #INGESTED_AT_METADATA_KEY} alias, then
     * {@link KnowledgeSource#registeredAt()}.
     *
     * @param source the registered source (must not be null)
     * @return the resolved timestamp (never null)
     * @throws NullPointerException if source is null
     */
    public static Instant lastUpdatedAtOf(KnowledgeSource source) {
        Objects.requireNonNull(source, "source must not be null");
        return parseInstant(source.metadata().get(UPDATED_AT_METADATA_KEY))
                .or(() -> parseInstant(source.metadata().get(INGESTED_AT_METADATA_KEY)))
                .orElseGet(source::registeredAt);
    }

    /**
     * Parses a declared timestamp declaration. Accepted forms are the ISO-8601
     * instant ({@code 2026-01-01T00:00:00Z}), an ISO-8601 offset date-time and a
     * plain ISO-8601 date ({@code 2026-01-01}, treated as midnight UTC). Anything
     * else - including null and blank - is ignored, never guessed.
     *
     * @param declaration the raw metadata declaration (may be null)
     * @return the parsed instant, or empty when it is absent or unrecognized
     */
    public static Optional<Instant> parseInstant(String declaration) {
        if (declaration == null || declaration.isBlank()) {
            return Optional.empty();
        }
        String value = declaration.trim();
        try {
            return Optional.of(Instant.parse(value));
        } catch (RuntimeException ignored) {
            // Fall through to the next accepted ISO-8601 form.
        }
        try {
            return Optional.of(OffsetDateTime.parse(value).toInstant());
        } catch (RuntimeException ignored) {
            // Fall through to the date-only form.
        }
        try {
            return Optional.of(LocalDate.parse(value)
                    .atStartOfDay(ZoneOffset.UTC).toInstant());
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Returns true when the context artifacts carry an explicit recency request.
     * The raw prompt is never re-parsed - only the artifacts the Context Kernel
     * already extracted are inspected.
     *
     * @param context the context intelligence aggregate (may be null)
     * @return true when an explicit latest request is present
     */
    public static boolean detectsLatestRequest(ContextIntelligence context) {
        if (context == null) {
            return false;
        }
        for (String text : recencyTextsOf(context)) {
            if (containsRecencyKeyword(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true when the given text carries one of the locked recency
     * keywords as a whole word. Matching is trimmed, lowercased and exact - no
     * fuzzy matching, no stemming, no substring matching.
     *
     * @param text the text to inspect (may be null)
     * @return true when a locked recency keyword is present
     */
    public static boolean containsRecencyKeyword(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        for (String token : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (LATEST_REQUEST_KEYWORDS.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /** Collects the deterministic, declaration-ordered recency texts of the context. */
    private static List<String> recencyTextsOf(ContextIntelligence context) {
        List<String> texts = new ArrayList<>();

        GoalStructure goals = context.goals();
        if (goals != null) {
            addGoalTexts(goals.primaryGoal(), texts);
            if (goals.subGoals() != null) {
                for (GoalNode subGoal : goals.subGoals()) {
                    addGoalTexts(subGoal, texts);
                }
            }
        }

        UserConstraints constraints = context.constraints();
        if (constraints != null && constraints.evidence() != null) {
            for (ConstraintEvidence evidence : constraints.evidence()) {
                if (evidence != null) {
                    texts.add(evidence.matchedText());
                }
            }
        }
        return List.copyOf(texts);
    }

    /** Adds a goal title and its evidence text, when present. */
    private static void addGoalTexts(GoalNode goal, List<String> texts) {
        if (goal == null) {
            return;
        }
        texts.add(goal.title());
        if (goal.evidence() != null) {
            texts.add(goal.evidence().matchedText());
        }
    }

    /** Whole, non-negative age in days (future timestamps are treated as zero). */
    private static long ageInDays(Instant from, Instant now) {
        return Math.max(0L, Duration.between(from, now).toDays());
    }
}