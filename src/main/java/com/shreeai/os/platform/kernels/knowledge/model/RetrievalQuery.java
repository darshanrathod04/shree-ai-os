package com.shreeai.os.platform.kernels.knowledge.model;

import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * <b>RetrievalQuery</b>
 *
 * <p>The canonical cognitive search request of the K4 retrieval pipeline. It
 * couples the Context Intelligence artifacts (intent, domain, goals,
 * constraints) with the deterministic keyword set derived from them - the
 * engine never parses a raw prompt again.</p>
 *
 * <p><b>Keyword sources (locked):</b> the primary intent, the primary domain,
 * the goal title and every sub-goal title. Tokens are lowercase alphanumeric
 * runs of at least three characters; noise markers ({@code UNKNOWN},
 * {@code GENERAL}) are skipped; duplicates collapse.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K4 Retrieval and Ranking</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param intent      the canonical intent profile (never null)
 * @param domain      the canonical domain profile (never null)
 * @param goals       the canonical goal structure (never null)
 * @param constraints the canonical user constraints (never null)
 * @param keywords    the deterministic, normalized keyword list (never null;
 *                    lowercase, deduplicated, order-stable)
 */
public record RetrievalQuery(
        IntentProfile intent,
        DomainProfile domain,
        GoalStructure goals,
        UserConstraints constraints,
        List<String> keywords) {

    private static final int MIN_KEYWORD_LENGTH = 3;

    /**
     * Compact constructor that validates the context artifacts and
     * normalizes the keyword list (lowercase, blank-free, deduplicated,
     * defensively copied).
     *
     * @throws NullPointerException if any context artifact is null
     */
    public RetrievalQuery {
        Objects.requireNonNull(intent, "intent must not be null");
        Objects.requireNonNull(domain, "domain must not be null");
        Objects.requireNonNull(goals, "goals must not be null");
        Objects.requireNonNull(constraints, "constraints must not be null");
        keywords = normalizeKeywords(keywords);
    }

    /**
     * Builds the canonical retrieval query from Context Intelligence
     * artifacts. Keywords are derived deterministically from the primary
     * intent, the primary domain, the goal title and every sub-goal title.
     *
     * @param intent      the detected intent profile (must not be null)
     * @param domain      the detected domain profile (must not be null)
     * @param goals       the identified goal structure (must not be null)
     * @param constraints the extracted user constraints (must not be null)
     * @return a new immutable RetrievalQuery (never null)
     */
    public static RetrievalQuery fromContext(IntentProfile intent,
                                             DomainProfile domain,
                                             GoalStructure goals,
                                             UserConstraints constraints) {
        List<String> derived = new ArrayList<>();
        addTokenWords(intent.primaryIntent().name(), derived);
        addTokenWords(domain.primaryDomain().name(), derived);
        addTokenWords(goals.primaryGoal().title(), derived);
        for (GoalNode subGoal : goals.subGoals()) {
            addTokenWords(subGoal.title(), derived);
        }
        return new RetrievalQuery(intent, domain, goals, constraints, derived);
    }

    /**
     * Returns the number of normalized keywords.
     *
     * @return the keyword count (never negative)
     */
    public int keywordCount() {
        return keywords.size();
    }

    private static List<String> normalizeKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String keyword : keywords) {
            if (keyword != null) {
                String normalized = keyword.trim().toLowerCase(Locale.ROOT);
                if (!normalized.isEmpty()) {
                    unique.add(normalized);
                }
            }
        }
        return List.copyOf(unique);
    }

    private static void addTokenWords(String raw, List<String> target) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        for (String token : raw.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (token.length() >= MIN_KEYWORD_LENGTH && !isNoiseToken(token)) {
                target.add(token);
            }
        }
    }

    private static boolean isNoiseToken(String token) {
        return token.equals("unknown") || token.equals("general") || token.equals("none");
    }

    @Override
    public String toString() {
        return String.format("RetrievalQuery{intent=%s, domain=%s, keywords=%s}",
                intent.primaryIntent(), domain.primaryDomain(), keywords);
    }
}
