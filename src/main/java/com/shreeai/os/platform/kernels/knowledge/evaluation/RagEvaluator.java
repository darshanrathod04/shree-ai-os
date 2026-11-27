package com.shreeai.os.platform.kernels.knowledge.evaluation;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <b>RagEvaluator</b>
 *
 * <p>Automated RAG evaluation triad (LlamaIndex / RAGAS style) scoring:</p>
 * <ul>
 *   <li><b>Faithfulness:</b> proportion of answer sentences grounded in retrieved context</li>
 *   <li><b>Context Precision:</b> proportion of retrieved chunks relevant to the query</li>
 *   <li><b>Answer Relevance:</b> query-term coverage in the generated answer</li>
 * </ul>
 */
public final class RagEvaluator {

    private static final double DEFAULT_PASS_THRESHOLD = 0.60;
    private static final Set<String> STOPWORDS = Set.of(
            "the", "and", "for", "with", "that", "this", "from", "into", "using",
            "are", "was", "were", "have", "has", "does", "did", "can", "could",
            "would", "should", "what", "how", "why", "when", "where", "which"
    );

    public RagEvaluationReport evaluate(String query, List<String> retrievedContexts, String generatedAnswer) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(retrievedContexts, "retrievedContexts must not be null");
        Objects.requireNonNull(generatedAnswer, "generatedAnswer must not be null");

        double faithfulness = scoreFaithfulness(retrievedContexts, generatedAnswer);
        double contextPrecision = scoreContextPrecision(query, retrievedContexts);
        double answerRelevance = scoreAnswerRelevance(query, generatedAnswer);
        double overall = (faithfulness * 0.45) + (contextPrecision * 0.25) + (answerRelevance * 0.30);
        boolean passing = overall >= DEFAULT_PASS_THRESHOLD && faithfulness >= 0.50;

        return new RagEvaluationReport(faithfulness, contextPrecision, answerRelevance, overall, passing);
    }

    private static double scoreFaithfulness(List<String> contexts, String answer) {
        if (answer.isBlank() || contexts.isEmpty()) {
            return 0.0;
        }
        Set<String> contextTokens = contexts.stream()
                .flatMap(c -> tokenize(c).stream())
                .collect(Collectors.toSet());
        if (contextTokens.isEmpty()) {
            return 0.0;
        }

        List<String> sentences = Arrays.stream(answer.split("[.!?\\r\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (sentences.isEmpty()) {
            return 0.0;
        }

        int grounded = 0;
        for (String sentence : sentences) {
            Set<String> sTokens = tokenize(sentence);
            if (sTokens.isEmpty()) {
                grounded++;
                continue;
            }
            long overlap = sTokens.stream().filter(contextTokens::contains).count();
            if (((double) overlap / sTokens.size()) >= 0.35) {
                grounded++;
            }
        }
        return (double) grounded / sentences.size();
    }

    private static double scoreContextPrecision(String query, List<String> contexts) {
        if (contexts.isEmpty()) {
            return 0.0;
        }
        Set<String> queryTokens = tokenize(query);
        if (queryTokens.isEmpty()) {
            return 1.0;
        }
        int relevant = 0;
        for (String ctx : contexts) {
            Set<String> ctxTokens = tokenize(ctx);
            long overlap = queryTokens.stream().filter(ctxTokens::contains).count();
            if (overlap > 0) {
                relevant++;
            }
        }
        return (double) relevant / contexts.size();
    }

    private static double scoreAnswerRelevance(String query, String answer) {
        if (answer.isBlank()) {
            return 0.0;
        }
        Set<String> queryTokens = tokenize(query);
        if (queryTokens.isEmpty()) {
            return 1.0;
        }
        Set<String> answerTokens = tokenize(answer);
        long hits = queryTokens.stream()
                .filter(q -> answerTokens.stream().anyMatch(a -> tokenMatches(q, a)))
                .count();
        return (double) hits / queryTokens.size();
    }

    private static boolean tokenMatches(String a, String b) {
        if (a.equals(b)) return true;
        return a.length() >= 4 && b.length() >= 4 && (a.startsWith(b) || b.startsWith(a));
    }

    private static Set<String> tokenize(String text) {
        Set<String> tokens = new LinkedHashSet<>();
        for (String raw : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (raw.length() >= 3 && !STOPWORDS.contains(raw)) {
                tokens.add(raw);
            }
        }
        return tokens;
    }

    public record RagEvaluationReport(
            double faithfulness,
            double contextPrecision,
            double answerRelevance,
            double overallScore,
            boolean passing
    ) {}
}
