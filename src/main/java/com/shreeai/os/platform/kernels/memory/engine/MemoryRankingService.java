package com.shreeai.os.platform.kernels.memory.engine;

import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryMetadata;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * <b>MemoryRankingService</b>
 *
 * <p>Ranks memories by relevance to a query.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Ranks memories by relevance to search query</li>
 *   <li>Considers recency, importance, confidence, and frequency</li>
 *   <li>Returns top-k most relevant memories</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 */
public final class MemoryRankingService {

    /**
     * Ranks memories by relevance to the query.
     *
     * <p>Ranking factors (in order of importance):</p>
     * <ol>
     *   <li>Text similarity (exact match, contains)</li>
     *   <li>Recency (newer memories rank higher)</li>
     *   <li>Importance (higher importance ranks higher)</li>
     *   <li>Confidence (higher confidence ranks higher)</li>
     *   <li>Access count (frequently accessed ranks higher)</li>
     * </ol>
     *
     * @param query the search query
     * @param memories the memories to rank
     * @param limit the maximum number of results to return
     * @return ranked list of memories (most relevant first)
     */
    public List<Memory> rankByRelevance(String query, List<Memory> memories, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String queryLower = query.toLowerCase();

        return memories.stream()
                .sorted((a, b) -> {
                    double scoreA = calculateRelevanceScore(queryLower, a);
                    double scoreB = calculateRelevanceScore(queryLower, b);
                    return Double.compare(scoreB, scoreA); // Descending order
                })
                .limit(limit)
                .toList();
    }

    /**
     * Calculates relevance score for a memory against a query.
     *
     * <p>Score components:</p>
     * <ul>
     *   <li>Text match: 0-50 points</li>
     *   <li>Recency: 0-20 points</li>
     *   <li>Importance: 0-15 points</li>
     *   <li>Confidence: 0-10 points</li>
     *   <li>Access count: 0-5 points</li>
     * </ul>
     *
     * @param queryLower the lowercase query
     * @param memory the memory to score
     * @return relevance score (0-100)
     */
    private double calculateRelevanceScore(String queryLower, Memory memory) {
        double score = 0.0;

        // Text similarity (0-50 points)
        String text = memory.content().text().toLowerCase();
        if (text.equals(queryLower)) {
            score += 50.0; // Exact match
        } else if (text.contains(queryLower)) {
            score += 30.0; // Contains query
        } else {
            // Check for word overlap
            String[] queryWords = queryLower.split("\\s+");
            String[] textWords = text.split("\\s+");
            long matches = 0;
            for (String queryWord : queryWords) {
                for (String textWord : textWords) {
                    if (textWord.contains(queryWord)) {
                        matches++;
                        break;
                    }
                }
            }
            if (queryWords.length > 0) {
                score += (matches * 10.0) / queryWords.length;
            }
        }

        // Recency (0-20 points) - newer memories score higher
        long hoursSinceCreation = java.time.Duration.between(
                memory.createdAt(),
                Instant.now()
        ).toHours();
        double recencyScore = Math.max(0, 20.0 - (hoursSinceCreation / 24.0)); // Decay over days
        score += recencyScore;

        // Importance (0-15 points)
        score += memory.metadata().importance() * 15.0;

        // Confidence (0-10 points)
        score += memory.metadata().confidence() * 10.0;

        // Access count (0-5 points) - logarithmic scale
        long accessCount = memory.metadata().accessCount();
        score += Math.min(5.0, Math.log10(accessCount + 1) * 2.5);

        return score;
    }

    /**
     * Ranks memories by similarity to a text.
     *
     * @param text the reference text
     * @param memories the memories to rank
     * @param limit the maximum number of results
     * @return ranked memories
     */
    public List<Memory> rankBySimilarity(String text, List<Memory> memories, int limit) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String textLower = text.toLowerCase();

        return memories.stream()
                .sorted((a, b) -> {
                    double similarityA = calculateTextSimilarity(textLower, a.content().text());
                    double similarityB = calculateTextSimilarity(textLower, b.content().text());
                    return Double.compare(similarityB, similarityA);
                })
                .limit(limit)
                .toList();
    }

    /**
     * Calculates text similarity score (0-100).
     *
     * @param text1 first text
     * @param text2 second text
     * @return similarity score
     */
    private double calculateTextSimilarity(String text1, String text2) {
        String text2Lower = text2.toLowerCase();

        if (text1.equals(text2Lower)) {
            return 100.0;
        }

        if (text2Lower.contains(text1)) {
            return 80.0;
        }

        // Word overlap
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2Lower.split("\\s+");

        if (words1.length == 0 || words2.length == 0) {
            return 0.0;
        }

        long matches = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word2.contains(word1)) {
                    matches++;
                    break;
                }
            }
        }

        return (matches * 100.0) / words1.length;
    }
}