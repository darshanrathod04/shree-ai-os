package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * <b>KnowledgeRankingService</b>
 *
 * <p>Ranks knowledge nodes by relevance to a query.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Ranks knowledge nodes by relevance to search query</li>
 *   <li>Considers relevance, confidence, authority, freshness, relationship strength</li>
 *   <li>Returns top-k most relevant knowledge nodes</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-101</p>
 */
public final class KnowledgeRankingService {

    /**
     * Ranks knowledge nodes by relevance to the query.
     *
     * <p>Ranking factors (in order of importance):</p>
     * <ol>
     *   <li>Text relevance (label/description match)</li>
     *   <li>Confidence (higher confidence ranks higher)</li>
     *   <li>Authority (source authority)</li>
     *   <li>Freshness (newer knowledge ranks higher)</li>
     *   <li>Relationship strength (number of relationships)</li>
     * </ol>
     *
     * @param query the search query
     * @param knowledgeNodes the knowledge nodes to rank
     * @param limit the maximum number of results to return
     * @return ranked list of knowledge nodes (most relevant first)
     */
    public List<KnowledgeNode> rankByRelevance(String query, List<KnowledgeNode> knowledgeNodes, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String queryLower = query.toLowerCase();

        return knowledgeNodes.stream()
                .sorted((a, b) -> {
                    double scoreA = calculateRelevanceScore(queryLower, a);
                    double scoreB = calculateRelevanceScore(queryLower, b);
                    return Double.compare(scoreB, scoreA); // Descending order
                })
                .limit(limit)
                .toList();
    }

    /**
     * Calculates relevance score for a knowledge node against a query.
     *
     * <p>Score components:</p>
     * <ul>
     *   <li>Text relevance: 0-50 points</li>
     *   <li>Confidence: 0-20 points</li>
     *   <li>Authority: 0-15 points</li>
     *   <li>Freshness: 0-10 points</li>
     *   <li>Relationship strength: 0-5 points</li>
     * </ul>
     *
     * @param queryLower the lowercase query
     * @param node the knowledge node to score
     * @return relevance score (0-100)
     */
    private double calculateRelevanceScore(String queryLower, KnowledgeNode node) {
        double score = 0.0;

        // Text relevance (0-50 points)
        String label = node.getLabel().toLowerCase();
        String description = node.getDescription() != null ? node.getDescription().toLowerCase() : "";

        if (label.equals(queryLower)) {
            score += 50.0; // Exact label match
        } else if (label.contains(queryLower)) {
            score += 35.0; // Label contains query
        } else if (description.contains(queryLower)) {
            score += 25.0; // Description contains query
        } else {
            // Check for word overlap
            String[] queryWords = queryLower.split("\\s+");
            String[] labelWords = label.split("\\s+");
            String[] descWords = description.split("\\s+");

            long matches = 0;
            for (String queryWord : queryWords) {
                for (String labelWord : labelWords) {
                    if (labelWord.contains(queryWord)) {
                        matches++;
                        break;
                    }
                }
                if (matches == 0) { // Check description if not in label
                    for (String descWord : descWords) {
                        if (descWord.contains(queryWord)) {
                            matches++;
                            break;
                        }
                    }
                }
            }
            if (queryWords.length > 0) {
                score += (matches * 10.0) / queryWords.length;
            }
        }

        // Confidence (0-20 points)
        Map<String, Object> metadata = node.getMetadata();
        if (metadata.containsKey("confidence")) {
            double confidence = ((Number) metadata.get("confidence")).doubleValue();
            score += confidence * 20.0;
        }

        // Authority (0-15 points)
        if (metadata.containsKey("authority")) {
            double authority = ((Number) metadata.get("authority")).doubleValue();
            score += authority * 15.0;
        }

        // Freshness (0-10 points) - newer knowledge ranks higher
        long hoursSinceUpdate = java.time.Duration.between(
                node.getUpdatedAt(),
                Instant.now()
        ).toHours();
        double freshnessScore = Math.max(0, 10.0 - (hoursSinceUpdate / 24.0)); // Decay over days
        score += freshnessScore;

        // Relationship strength (0-5 points) - based on metadata if available
        if (metadata.containsKey("relationshipCount")) {
            int relationshipCount = ((Number) metadata.get("relationshipCount")).intValue();
            score += Math.min(5.0, relationshipCount * 1.0);
        }

        return score;
    }

    /**
     * Ranks knowledge nodes by similarity to a text.
     *
     * @param text the reference text
     * @param knowledgeNodes the knowledge nodes to rank
     * @param limit the maximum number of results
     * @return ranked knowledge nodes
     */
    public List<KnowledgeNode> rankBySimilarity(String text, List<KnowledgeNode> knowledgeNodes, int limit) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String textLower = text.toLowerCase();

        return knowledgeNodes.stream()
                .sorted((a, b) -> {
                    double similarityA = calculateTextSimilarity(textLower, a.getLabel() + " " + (a.getDescription() != null ? a.getDescription() : ""));
                    double similarityB = calculateTextSimilarity(textLower, b.getLabel() + " " + (b.getDescription() != null ? b.getDescription() : ""));
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