package com.shreeai.os.platform.kernels.knowledge.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lightweight query normalization layer applied BEFORE retrieval.
 * <p>
 * Rules (in order):
 *   1. Lowercase the entire query.
 *   2. Trim leading/trailing whitespace and punctuation.
 *   3. Remove leading interrogative prefixes: "who is", "what is", "how to become",
 *      "tell me about", "explain", etc.
 *   4. Strip isolated and trailing punctuation (e.g. trailing dots '.').
 *   5. Preserve meaningful multi-word entities (e.g., "spring boot").
 * <p>
 * This is NOT an AI heuristic – it's a deterministic prefix-stripping layer
 * that ensures consistent query handling across Knowledge Search, Knowledge Query,
 * and Knowledge Ranking.
 */
public final class QueryNormalizer {

    private static final String[] INTERROGATIVE_PREFIXES = {
            "can you tell me about",
            "can you tell me",
            "can you explain",
            "can you",
            "tell me about",
            "tell me",
            "please explain",
            "how to become",
            "how to build",
            "how to create",
            "how to make",
            "how to use",
            "how to",
            "how do i",
            "how can i",
            "how does",
            "how do",
            "how is",
            "how are",
            "whats is",
            "what's is",
            "what's",
            "what is",
            "what are",
            "what was",
            "who is",
            "who was",
            "who are",
            "explain"
    };

    /**
     * Standard English stop-words filtered during relevance computation.
     */
    public static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
            "any", "are", "as", "at", "be", "because", "been", "before", "being", "below",
            "between", "both", "but", "by", "can", "could", "did", "do", "does", "doing",
            "down", "during", "each", "few", "for", "from", "further", "had", "has",
            "have", "having", "he", "her", "here", "hers", "herself", "him", "himself",
            "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "just",
            "me", "more", "most", "my", "myself", "no", "nor", "not", "now", "of", "off",
            "on", "once", "only", "or", "other", "our", "ours", "ourselves", "out", "over",
            "own", "s", "same", "she", "should", "so", "some", "such", "t", "than",
            "that", "the", "their", "theirs", "them", "themselves", "then", "there",
            "these", "they", "this", "those", "through", "to", "too", "under", "until",
            "up", "very", "was", "we", "were", "what", "when", "where", "which", "while",
            "who", "whom", "why", "will", "with", "would", "you", "your", "yours",
            "yourself", "yourselves",
            "become", "becomes", "becoming", "build", "building", "create", "creating",
            "make", "making", "days", "day", "want", "need", "please"
    );

    private QueryNormalizer() {
        // utility class – no instantiation
    }

    /**
     * Normalizes a raw user query for use in knowledge retrieval.
     *
     * @param query the raw user query (may be null)
     * @return the normalized query, or an empty string if the input was null/blank
     */
    public static String normalize(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        String normalized = query.toLowerCase(Locale.ROOT).trim();

        // Remove leading interrogative prefixes
        for (String prefix : INTERROGATIVE_PREFIXES) {
            String pattern = "^" + java.util.regex.Pattern.quote(prefix) + "\\s*";
            normalized = normalized.replaceFirst(pattern, "");
        }

        // Strip isolated punctuation tokens (e.g. " .", " ?", " !")
        normalized = normalized.replaceAll("(?<=\\s)[\\p{Punct}]+(?=\\s|$)", " ");

        // Final trim after prefix removal, stripping leading/trailing punctuation
        normalized = normalized.replaceAll("^[\\p{Punct}\\s]+|[\\p{Punct}\\s]+$", "").trim();

        return normalized;
    }

    /**
     * Strips stop-words and punctuation from a text string, retaining only content words.
     *
     * @param text the text to filter
     * @return cleaned string with stop-words removed
     */
    public static String stripStopWords(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String clean = text.replaceAll("[\\p{Punct}]+", " ").trim();
        String[] tokens = clean.split("\\s+");
        List<String> remaining = new ArrayList<>();
        for (String token : tokens) {
            String w = token.trim().toLowerCase(Locale.ROOT);
            if (!w.isBlank() && !STOP_WORDS.contains(w)) {
                remaining.add(w);
            }
        }
        return remaining.isEmpty() ? clean : String.join(" ", remaining);
    }
}