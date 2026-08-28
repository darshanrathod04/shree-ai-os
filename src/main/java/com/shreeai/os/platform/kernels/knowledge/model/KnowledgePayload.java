package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgePayload</b>
 *
 * <p>The structured, citation-backed result of a grounded knowledge search.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 *
 * @see KnowledgeCitation
 * @see KnowledgeNode
 */
public final class KnowledgePayload {

    private final String query;
    private final String title;
    private final String summary;
    private final List<KnowledgeCitation> citations;
    private final double groundingScore;
    private final int matchedNodeCount;

    private KnowledgePayload(
            String query,
            String title,
            String summary,
            List<KnowledgeCitation> citations,
            double groundingScore,
            int matchedNodeCount) {
        this.query = query;
        this.title = title;
        this.summary = summary;
        this.citations = citations;
        this.groundingScore = groundingScore;
        this.matchedNodeCount = matchedNodeCount;
    }

    /**
     * Creates a new KnowledgePayload with validation and defensive copying.
     *
     * @param query            the originating query (must not be null)
     * @param title            the grounded title (must not be null)
     * @param summary          the grounded summary (must not be null)
     * @param citations        the ordered citations (must not be null)
     * @param groundingScore   the grounding score, clamped to 0.0-1.0
     * @param matchedNodeCount the number of knowledge nodes behind this payload (must be &gt;= 0)
     * @return a new KnowledgePayload instance
     */
    public static KnowledgePayload of(
            String query,
            String title,
            String summary,
            List<KnowledgeCitation> citations,
            double groundingScore,
            int matchedNodeCount) {

        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(citations, "citations must not be null");
        if (matchedNodeCount < 0) {
            throw new IllegalArgumentException("matchedNodeCount must be >= 0");
        }

        double clamped = Math.max(0.0, Math.min(1.0, groundingScore));

        return new KnowledgePayload(
                query,
                title,
                summary,
                List.copyOf(citations),
                clamped,
                matchedNodeCount);
    }

    /** @return the originating query (never null). */
    public String getQuery() {
        return query;
    }

    /** @return the grounded title, usually the top node's label (never null). */
    public String getTitle() {
        return title;
    }

    /** @return the grounded summary, usually the top node's description (never null). */
    public String getSummary() {
        return summary;
    }

    /** @return the ordered, unmodifiable citation list (never null, may be empty). */
    public List<KnowledgeCitation> getCitations() {
        return citations;
    }

    /** @return the grounding score expressing knowledge-graph support (0.0-1.0). */
    public double getGroundingScore() {
        return groundingScore;
    }

    /** @return the number of knowledge nodes this payload was grounded on (&gt;= 0). */
    public int getMatchedNodeCount() {
        return matchedNodeCount;
    }

    /**
     * Returns a serialisable view of this payload suitable for pipeline
     * metadata and SDK structured responses.
     *
     * @return an unmodifiable map view (never null)
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("query", query);
        map.put("title", title);
        map.put("summary", summary);
        map.put("groundingScore", groundingScore);
        map.put("matchedNodeCount", matchedNodeCount);

        List<Map<String, Object>> citationMaps = new ArrayList<>();
        for (KnowledgeCitation citation : citations) {
            Map<String, Object> citationMap = new LinkedHashMap<>();
            citationMap.put("index", citation.getIndex());
            citationMap.put("knowledgeId", citation.getKnowledgeId());
            citationMap.put("label", citation.getLabel());
            citationMap.put("snippet", citation.getSnippet());
            citationMap.put("source", citation.getSource());
            citationMap.put("confidence", citation.getConfidence());
            citationMap.put("authority", citation.getAuthority());
            citationMaps.add(citationMap);
        }
        map.put("citations", Collections.unmodifiableList(citationMaps));

        return Collections.unmodifiableMap(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgePayload that = (KnowledgePayload) o;
        return Double.compare(that.groundingScore, groundingScore) == 0
                && matchedNodeCount == that.matchedNodeCount
                && query.equals(that.query)
                && title.equals(that.title)
                && summary.equals(that.summary)
                && citations.equals(that.citations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, title, summary, citations, groundingScore, matchedNodeCount);
    }

    @Override
    public String toString() {
        return "KnowledgePayload{"
                + "query='" + query + '\''
                + ", title='" + title + '\''
                + ", citations=" + citations.size()
                + ", groundingScore=" + groundingScore
                + '}';
    }
}