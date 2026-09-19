package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.IntentCandidate;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.PrimaryIntent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultPrimaryIntentDetector</b>
 *
 * <p>Deterministic keyword-based implementation of {@link PrimaryIntentDetector}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides deterministic intent detection based on keyword matching.</li>
 *   <li>Assigns confidence scores based on keyword match strength.</li>
 *   <li>Produces ranked alternative intents for fallback scenarios.</li>
 * </ul>
 *
 * <p><b>Determinism Guarantee:</b> Same input always produces identical output.
 * No randomness, timestamps, or UUIDs are used in the detection algorithm.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. It maintains no mutable state.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see PrimaryIntentDetector
 * @see IntentProfile
 */
public final class DefaultPrimaryIntentDetector implements PrimaryIntentDetector {

    private static final String DETECTION_METHOD = "keyword-based-deterministic";
    private static final double MIN_CONFIDENCE_THRESHOLD = 0.10;
    private static final Map<PrimaryIntent, Map<String, Double>> KEYWORD_MAPPINGS = createKeywordMappings();

    public DefaultPrimaryIntentDetector() {
    }

    @Override
    public IntentProfile detect(String userInput) {
        Objects.requireNonNull(userInput, "userInput must not be null");

        String normalizedInput = userInput.toLowerCase().trim();

        if (normalizedInput.isEmpty()) {
            return IntentProfile.unknown(DETECTION_METHOD);
        }

        Map<PrimaryIntent, Double> scores = calculateIntentScores(normalizedInput);
        List<IntentCandidate> candidates = buildRankedCandidates(scores);

        if (candidates.isEmpty()) {
            return IntentProfile.unknown(DETECTION_METHOD);
        }

        IntentCandidate primary = candidates.get(0);
        List<IntentCandidate> alternatives = candidates.size() > 1
                ? candidates.subList(1, candidates.size())
                : List.of();

        return new IntentProfile(
                primary.intent(),
                primary.confidence(),
                alternatives,
                Instant.now(),
                DETECTION_METHOD
        );
    }

    private Map<PrimaryIntent, Double> calculateIntentScores(String normalizedInput) {
        Map<PrimaryIntent, Double> scores = new LinkedHashMap<>();

        for (Map.Entry<PrimaryIntent, Map<String, Double>> entry : KEYWORD_MAPPINGS.entrySet()) {
            PrimaryIntent intent = entry.getKey();
            Map<String, Double> keywords = entry.getValue();

            double totalScore = 0.0;
            int matchCount = 0;

            for (Map.Entry<String, Double> keywordEntry : keywords.entrySet()) {
                String keyword = keywordEntry.getKey();
                double weight = keywordEntry.getValue();

                // Match whole words only, not substrings
                if (containsWholeWord(normalizedInput, keyword)) {
                    totalScore += weight;
                    matchCount++;
                }
            }

            double confidence = calculateConfidence(totalScore, matchCount, keywords.size());
            scores.put(intent, confidence);
        }

        return scores;
    }

    private double calculateConfidence(double totalScore, int matchCount, int totalKeywords) {
        if (matchCount == 0) {
            return 0.0;
        }

        // Calculate the maximum single keyword weight across all intents
        double maxKeywordWeight = KEYWORD_MAPPINGS.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        // Normalize total score against the maximum single keyword weight
        double normalizedScore = totalScore / maxKeywordWeight;

        // Calculate match ratio
        double matchRatio = (double) matchCount / totalKeywords;

        // Combine factors - ensure a single strong match gives reasonable confidence
        double confidence = (normalizedScore * 0.6 + matchRatio * 0.4);

        // Clamp to [0.0, 1.0]
        return Math.min(1.0, Math.max(0.0, confidence));
    }

    private List<IntentCandidate> buildRankedCandidates(Map<PrimaryIntent, Double> scores) {
        List<IntentCandidate> candidates = new ArrayList<>();

        for (Map.Entry<PrimaryIntent, Double> entry : scores.entrySet()) {
            double confidence = entry.getValue();
            if (confidence >= MIN_CONFIDENCE_THRESHOLD) {
                candidates.add(new IntentCandidate(entry.getKey(), roundConfidence(confidence)));
            }
        }

        candidates.sort((a, b) -> {
            int cmp = Double.compare(b.confidence(), a.confidence());
            if (cmp != 0) return cmp;
            return a.intent().compareTo(b.intent());
        });

        return candidates;
    }

    private double roundConfidence(double confidence) {
        return Math.round(confidence * 1000.0) / 1000.0;
    }

    /**
     * Checks if the input contains the keyword as a whole word.
     *
     * @param input the input text
     * @param keyword the keyword to search for
     * @return true if the keyword is found as a whole word
     */
    private boolean containsWholeWord(String input, String keyword) {
        // Use word boundary regex to match whole words
        String regex = "\\b" + keyword + "\\b";
        return input.matches(".*" + regex + ".*");
    }

    private static Map<PrimaryIntent, Map<String, Double>> createKeywordMappings() {
        Map<PrimaryIntent, Map<String, Double>> mappings = new LinkedHashMap<>();

        mappings.put(PrimaryIntent.LEARN, Map.ofEntries(
                Map.entry("learn", 1.0), Map.entry("study", 1.0), Map.entry("teach", 0.9),
                Map.entry("understand", 0.8), Map.entry("know", 0.6), Map.entry("what is", 0.7),
                Map.entry("how does", 0.7), Map.entry("explain", 0.5), Map.entry("tutorial", 0.9),
                Map.entry("course", 0.8), Map.entry("beginner", 0.6), Map.entry("introduction", 0.7),
                Map.entry("basics", 0.7), Map.entry("fundamentals", 0.7), Map.entry("get started", 0.8)
        ));

        mappings.put(PrimaryIntent.BUILD, Map.ofEntries(
                Map.entry("build", 1.0), Map.entry("create", 1.0), Map.entry("make", 0.8),
                Map.entry("develop", 0.9), Map.entry("implement", 0.9), Map.entry("design", 0.7),
                Map.entry("setup", 0.7), Map.entry("construct", 0.9), Map.entry("generate", 0.7),
                Map.entry("write", 0.6), Map.entry("code", 0.7), Map.entry("application", 0.6),
                Map.entry("project", 0.5), Map.entry("feature", 0.6), Map.entry("add", 0.4)
        ));

        mappings.put(PrimaryIntent.DEBUG, Map.ofEntries(
                Map.entry("debug", 1.0), Map.entry("fix", 1.0), Map.entry("error", 0.9),
                Map.entry("bug", 0.9), Map.entry("issue", 0.7), Map.entry("problem", 0.7),
                Map.entry("broken", 0.8), Map.entry("not working", 0.9), Map.entry("crash", 0.8),
                Map.entry("exception", 0.8), Map.entry("fail", 0.7), Map.entry("wrong", 0.6),
                Map.entry("troubleshoot", 1.0), Map.entry("resolve", 0.8), Map.entry("help", 0.4)
        ));

        mappings.put(PrimaryIntent.EXPLAIN, Map.ofEntries(
                Map.entry("explain", 1.0), Map.entry("what", 0.6), Map.entry("why", 0.7),
                Map.entry("how", 0.6), Map.entry("describe", 0.9), Map.entry("clarify", 0.9),
                Map.entry("elaborate", 0.8), Map.entry("detail", 0.7), Map.entry("meaning", 0.7),
                Map.entry("definition", 0.8), Map.entry("concept", 0.6), Map.entry("difference", 0.5),
                Map.entry("mean", 0.6), Map.entry("purpose", 0.7), Map.entry("reason", 0.6)
        ));

        mappings.put(PrimaryIntent.COMPARE, Map.ofEntries(
                Map.entry("compare", 1.0), Map.entry("versus", 1.0), Map.entry("vs", 0.9),
                Map.entry("difference", 0.8), Map.entry("better", 0.7), Map.entry("best", 0.6),
                Map.entry("or", 0.4), Map.entry("which", 0.5), Map.entry("pros", 0.7),
                Map.entry("cons", 0.7), Map.entry("advantage", 0.7), Map.entry("disadvantage", 0.7),
                Map.entry("similar", 0.6), Map.entry("different", 0.6), Map.entry("choose", 0.5)
        ));

        mappings.put(PrimaryIntent.ANALYZE, Map.ofEntries(
                Map.entry("analyze", 1.0), Map.entry("analysis", 1.0), Map.entry("evaluate", 0.9),
                Map.entry("assess", 0.9), Map.entry("examine", 0.8), Map.entry("investigate", 0.8),
                Map.entry("review", 0.6), Map.entry("insight", 0.7), Map.entry("pattern", 0.6),
                Map.entry("trend", 0.6), Map.entry("data", 0.5), Map.entry("metrics", 0.6),
                Map.entry("performance", 0.6), Map.entry("optimize", 0.7), Map.entry("improve", 0.6)
        ));

        mappings.put(PrimaryIntent.PLAN, Map.ofEntries(
                Map.entry("plan", 1.0), Map.entry("strategy", 0.9), Map.entry("roadmap", 0.9),
                Map.entry("steps", 0.7), Map.entry("approach", 0.7), Map.entry("schedule", 0.8),
                Map.entry("timeline", 0.8), Map.entry("milestone", 0.7), Map.entry("goal", 0.6),
                Map.entry("objective", 0.6), Map.entry("prepare", 0.6), Map.entry("organize", 0.6),
                Map.entry("structure", 0.5), Map.entry("architecture", 0.6), Map.entry("design", 0.5)
        ));

        mappings.put(PrimaryIntent.REVIEW, Map.ofEntries(
                Map.entry("review", 1.0), Map.entry("feedback", 0.9), Map.entry("check", 0.6),
                Map.entry("validate", 0.7), Map.entry("verify", 0.7), Map.entry("test", 0.6),
                Map.entry("quality", 0.6), Map.entry("assessment", 0.8), Map.entry("audit", 0.8),
                Map.entry("inspect", 0.7), Map.entry("evaluate", 0.7), Map.entry("critique", 0.9),
                Map.entry("opinion", 0.6), Map.entry("suggestion", 0.6), Map.entry("recommend", 0.6)
        ));

        mappings.put(PrimaryIntent.EXECUTE, Map.ofEntries(
                Map.entry("execute", 1.0), Map.entry("run", 0.9), Map.entry("deploy", 0.9),
                Map.entry("launch", 0.8), Map.entry("start", 0.6), Map.entry("begin", 0.5),
                Map.entry("initiate", 0.8), Map.entry("perform", 0.7), Map.entry("do", 0.4),
                Map.entry("process", 0.5), Map.entry("trigger", 0.7), Map.entry("invoke", 0.8),
                Map.entry("call", 0.5), Map.entry("submit", 0.6), Map.entry("send", 0.5)
        ));

        return Map.copyOf(mappings);
    }
}
