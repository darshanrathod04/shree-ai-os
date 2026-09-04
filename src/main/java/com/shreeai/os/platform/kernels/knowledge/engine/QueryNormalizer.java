package com.shreeai.os.platform.kernels.knowledge.engine;

/**
 * Lightweight query normalization layer applied BEFORE retrieval.
 * <p>
 * Rules (in order):
 *   1. Lowercase the entire query.
 *   2. Trim leading/trailing whitespace and punctuation.
 *   3. Remove leading interrogative prefixes only: "who is", "what is",
 *      "tell me about", "explain".
 *   4. Preserve meaningful multi-word entities (e.g., "spring boot").
 * <p>
 * This is NOT an AI heuristic – it's a deterministic prefix-stripping layer
 * that ensures consistent query handling across Knowledge Search, Knowledge Query,
 * and Knowledge Ranking.
 */
public final class QueryNormalizer {

    private static final String[] INTERROGATIVE_PREFIXES = {
            "who is",
            "what is",
            "tell me about",
            "explain"
    };

    private QueryNormalizer() {
        // utility class – no instantiation
    }

    /**
     * Normalizes a raw user query for use in knowledge retrieval.
     * <p>
     * Example transformations:
     * <ul>
     *   <li>"who is darshan"      → "darshan"</li>
     *   <li>"what is java"        → "java"</li>
     *   <li>"explain html"        → "html"</li>
     *   <li>"tell me about spring boot" → "spring boot"</li>
     *   <li>"  Who is Darshan  " → "darshan"</li>
     * </ul>
     *
     * @param query the raw user query (may be null)
     * @return the normalized query, or an empty string if the input was null/blank
     */
    public static String normalize(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        String normalized = query.toLowerCase(java.util.Locale.ROOT);
        normalized = normalized.trim();

        // Remove leading interrogative prefixes
        for (String prefix : INTERROGATIVE_PREFIXES) {
            // Build pattern: prefix followed by optional whitespace
            // (so "who is" and "who is darshan" both strip correctly)
            String pattern = prefix + "\\s*";
            normalized = normalized.replaceFirst(pattern, "");
        }

        // Final trim after prefix removal
        normalized = normalized.trim();

        return normalized;
    }
}