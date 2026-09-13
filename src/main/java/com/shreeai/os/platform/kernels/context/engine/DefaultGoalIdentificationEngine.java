package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.GoalComplexity;
import com.shreeai.os.platform.kernels.context.model.GoalEvidence;
import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * <b>DefaultGoalIdentificationEngine</b>
 *
 * <p>Deterministic, rule-based implementation of {@link GoalIdentificationEngine}.</p>
 *
 * <p><b>Identification Strategy:</b></p>
 * <ul>
 *   <li>Identifies primary goal from main action verb + object</li>
 *   <li>Identifies sub-goals from compound sentences</li>
 *   <li>Determines complexity based on goal count and nature</li>
 *   <li>Preserves original wording from user input</li>
 * </ul>
 *
 * <p><b>Determinism Guarantee:</b> Same input always produces identical output.</p>
 *
 * @see GoalIdentificationEngine
 * @see GoalStructure
 */
public final class DefaultGoalIdentificationEngine implements GoalIdentificationEngine {

    private static final String DETECTION_METHOD = "rule-based-deterministic";

    private static final List<String> ACTION_VERBS = List.of(
            "learn", "build", "create", "develop", "make", "write", "design",
            "implement", "understand", "explain", "compare", "analyze", "plan",
            "review", "debug", "fix", "deploy", "setup", "configure", "test",
            "optimize", "migrate", "integrate", "refactor", "document", "study"
    );

    public DefaultGoalIdentificationEngine() {
    }

    @Override
    public GoalStructure identify(String userInput) {
        Objects.requireNonNull(userInput, "userInput must not be null");

        String normalizedInput = userInput.trim();

        if (normalizedInput.isEmpty()) {
            return GoalStructure.none(DETECTION_METHOD);
        }

        List<GoalNode> goals = extractGoals(normalizedInput);

        if (goals.isEmpty()) {
            return GoalStructure.none(DETECTION_METHOD);
        }

        GoalNode primaryGoal = goals.get(0);
        List<GoalNode> subGoals = goals.size() > 1 ? goals.subList(1, goals.size()) : List.of();
        GoalComplexity complexity = assessComplexity(goals);

        return new GoalStructure(primaryGoal, subGoals, complexity, Instant.now(), DETECTION_METHOD);
    }

    private static final String CONJUNCTION_SPLIT = "\\s+and\\s+";

    private List<GoalNode> extractGoals(String input) {
        List<GoalNode> goals = new ArrayList<>();
        String[] sentences = input.split("[.!?]+");

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;

            for (String clause : sentence.split(CONJUNCTION_SPLIT)) {
                clause = clause.trim();
                if (clause.isEmpty()) continue;

                boolean allowFallback = goals.isEmpty();
                GoalNode goal = extractGoalFromClause(clause, allowFallback);
                if (goal != null) {
                    goals.add(goal);
                }
            }
        }

        if (goals.isEmpty()) {
            GoalNode goal = extractGoalFromClause(input, true);
            if (goal != null) {
                goals.add(goal);
            }
        }

        return goals;
    }

    private GoalNode extractGoalFromClause(String clause, boolean allowFallback) {
        String lowerClause = clause.toLowerCase();

        for (String verb : ACTION_VERBS) {
            if (containsWholeWord(lowerClause, verb)) {
                int verbIndex = lowerClause.indexOf(verb);
                String title = extractGoalTitle(clause, verbIndex);
                double confidence = calculateGoalConfidence(clause, verb);
                GoalEvidence evidence = new GoalEvidence(clause.trim(), 0, clause.trim().length());
                return new GoalNode(title, confidence, evidence);
            }
        }

        if (allowFallback && !clause.trim().isEmpty()) {
            GoalEvidence evidence = new GoalEvidence(clause.trim(), 0, clause.trim().length());
            return new GoalNode(clause.trim(), 0.5, evidence);
        }

        return null;
    }

    private String extractGoalTitle(String clause, int verbIndex) {
        String fromVerb = clause.substring(verbIndex).trim();
        int endIndex = fromVerb.length();
        for (String marker : List.of(".", "!", "?", ";", ":")) {
            int markerIndex = fromVerb.indexOf(marker);
            if (markerIndex > 0 && markerIndex < endIndex) {
                endIndex = markerIndex;
            }
        }
        String title = fromVerb.substring(0, Math.min(endIndex, 50)).trim();
        return title.isEmpty() ? fromVerb : title;
    }

    private double calculateGoalConfidence(String clause, String verb) {
        double confidence = 0.6;

        if (clause.length() > verb.length() + 5) {
            confidence += 0.1;
        }

        String lowerClause = clause.toLowerCase();
        List<String> specificTerms = List.of("java", "python", "spring", "react", "database", "api");
        for (String term : specificTerms) {
            if (lowerClause.contains(term)) {
                confidence += 0.1;
                break;
            }
        }

        if (lowerClause.contains("complete") || lowerClause.contains("full") ||
            lowerClause.contains("detailed")) {
            confidence += 0.1;
        }

        return Math.min(1.0, confidence);
    }

    private GoalComplexity assessComplexity(List<GoalNode> goals) {
        if (goals.size() == 1) {
            return GoalComplexity.SIMPLE;
        } else if (goals.size() <= 3) {
            return GoalComplexity.MODERATE;
        } else {
            return GoalComplexity.COMPLEX;
        }
    }

    private boolean containsWholeWord(String input, String keyword) {
        String regex = "\\b" + Pattern.quote(keyword) + "\\b";
        return Pattern.compile(regex).matcher(input).find();
    }
}
