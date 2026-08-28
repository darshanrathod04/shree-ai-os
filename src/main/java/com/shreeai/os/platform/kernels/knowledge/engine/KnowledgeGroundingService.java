package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeCitation;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgePayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * <b>KnowledgeGroundingService</b>
 *
 * <p>Turns ranked knowledge nodes into a structured, citation-backed
 * {@link KnowledgePayload} and scores how strongly the payload is grounded
 * in the knowledge graph.</p>
 *
 * <p><b>Grounding score model (0.0-1.0):</b></p>
 * <ul>
 *   <li>Term coverage (50%): fraction of query terms found in the labels or
 *       descriptions of the cited nodes.</li>
 *   <li>Evidence quality (50%): average of node confidence (60%) and
 *       authority (40%); nodes without metadata contribute a neutral 0.5.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-101</p>
 */
public final class KnowledgeGroundingService {

    /** Maximum number of nodes cited in a single payload. */
    private static final int MAX_CITATIONS = 10;

    /** Minimum query term length considered meaningful for coverage. */
    private static final int MIN_TERM_LENGTH = 3;

    /** Default evidence contribution for nodes without confidence metadata. */
    private static final double NEUTRAL_EVIDENCE = 0.5;

    /**
     * Grounds a query on ranked knowledge nodes, producing a citation-backed payload.
     *
     * @param query        the originating query (must not be null)
     * @param rankedNodes  the ranked knowledge nodes (must not be null; may be empty)
     * @param fallbackTitle title to use when no nodes are available (may be null)
     * @return a grounded payload (never null; empty citations when no nodes match)
     */
    public KnowledgePayload ground(String query, List<KnowledgeNode> rankedNodes, String fallbackTitle) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(rankedNodes, "rankedNodes must not be null");

        if (rankedNodes.isEmpty()) {
            return KnowledgePayload.of(
                    query,
                    fallbackTitle != null ? fallbackTitle : query,
                    "",
                    List.of(),
                    0.0,
                    0);
        }

        List<KnowledgeNode> citedNodes = rankedNodes.stream()
                .limit(MAX_CITATIONS)
                .toList();

        List<KnowledgeCitation> citations = new ArrayList<>();
        for (int i = 0; i < citedNodes.size(); i++) {
            citations.add(KnowledgeCitation.fromNode(i + 1, citedNodes.get(i)));
        }

        KnowledgeNode top = citedNodes.getFirst();

        return KnowledgePayload.of(
                query,
                top.getLabel() != null && !top.getLabel().isBlank()
                        ? top.getLabel()
                        : (fallbackTitle != null ? fallbackTitle : query),
                top.getDescription() != null ? top.getDescription() : "",
                citations,
                groundingScore(query, citedNodes),
                citedNodes.size());
    }

    /**
     * Computes the grounding score for a query against cited nodes.
     *
     * @param query      the query (must not be null)
     * @param citedNodes the cited nodes (must not be null or empty)
     * @return the grounding score (0.0-1.0)
     */
    public double groundingScore(String query, List<KnowledgeNode> citedNodes) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(citedNodes, "citedNodes must not be null");

        if (citedNodes.isEmpty()) {
            return 0.0;
        }

        List<String> terms = significantTerms(query);

        if (terms.isEmpty()) {
            return NEUTRAL_EVIDENCE;
        }

        long covered = terms.stream()
                .filter(term -> citedNodes.stream().anyMatch(node -> nodeMentions(node, term)))
                .count();

        double coverage = (double) covered / terms.size();
        double evidence = citedNodes.stream()
                .mapToDouble(this::evidenceQuality)
                .average()
                .orElse(NEUTRAL_EVIDENCE);

        return Math.max(0.0, Math.min(1.0, 0.5 * coverage + 0.5 * evidence));
    }

    private boolean nodeMentions(KnowledgeNode node, String term) {
        String label = lower(node.getLabel());
        String description = lower(node.getDescription());
        return label.contains(term) || description.contains(term);
    }

    private double evidenceQuality(KnowledgeNode node) {
        Object confidence = node.getMetadata().get("confidence");
        Object authority = node.getMetadata().get("authority");

        double c = confidence instanceof Number number ? number.doubleValue() : NEUTRAL_EVIDENCE;
        double a = authority instanceof Number number ? number.doubleValue() : NEUTRAL_EVIDENCE;

        return Math.max(0.0, Math.min(1.0, 0.6 * c + 0.4 * a));
    }

    private List<String> significantTerms(String query) {
        String[] words = lower(query).split("[^a-z0-9]+");
        List<String> terms = new ArrayList<>();
        for (String word : words) {
            if (word.length() >= MIN_TERM_LENGTH && !terms.contains(word)) {
                terms.add(word);
            }
        }
        return terms;
    }

    private String lower(String text) {
        return text != null ? text.toLowerCase(Locale.ROOT).trim() : "";
    }
}