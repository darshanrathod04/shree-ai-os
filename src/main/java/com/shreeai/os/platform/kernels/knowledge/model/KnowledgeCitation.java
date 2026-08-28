package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeCitation</b>
 *
 * <p>A verifiable, numbered citation referring back to a specific
 * {@link KnowledgeNode} that grounded an answer or payload.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Constitutional Authority:</b> EIO-KNW-101, EIO-KNW-102</p>
 */
public final class KnowledgeCitation {

    /** Maximum number of characters kept in a citation snippet. */
    private static final int SNIPPET_MAX_LENGTH = 160;

    private final int index;
    private final String knowledgeId;
    private final String label;
    private final String snippet;
    private final String source;
    private final double confidence;
    private final double authority;

    private KnowledgeCitation(
            int index,
            String knowledgeId,
            String label,
            String snippet,
            String source,
            double confidence,
            double authority) {
        this.index = index;
        this.knowledgeId = knowledgeId;
        this.label = label;
        this.snippet = snippet;
        this.source = source;
        this.confidence = confidence;
        this.authority = authority;
    }

    /**
     * Builds a citation from a knowledge node at the given (1-based) position.
     *
     * <p>Provenance is read from node metadata keys {@code source},
     * {@code confidence} and {@code authority}; absent values degrade
     * gracefully (empty source, 0.0 scores).</p>
     *
     * @param index the 1-based citation index (must be &gt;= 1)
     * @param node  the knowledge node being cited (must not be null)
     * @return a new KnowledgeCitation instance
     */
    public static KnowledgeCitation fromNode(int index, KnowledgeNode node) {
        if (index < 1) {
            throw new IllegalArgumentException("citation index must be >= 1");
        }
        Objects.requireNonNull(node, "node must not be null");

        Map<String, Object> metadata = node.getMetadata();

        return new KnowledgeCitation(
                index,
                node.getId() != null ? node.getId().value() : "",
                node.getLabel() != null ? node.getLabel() : "",
                snippetOf(node.getDescription()),
                stringOf(metadata, "source"),
                doubleOf(metadata, "confidence"),
                doubleOf(metadata, "authority"));
    }

    /** @return the 1-based citation index used for inline markers such as {@code [1]}. */
    public int getIndex() {
        return index;
    }

    /** @return the identifier of the cited knowledge node (never null). */
    public String getKnowledgeId() {
        return knowledgeId;
    }

    /** @return the label of the cited knowledge node (never null). */
    public String getLabel() {
        return label;
    }

    /** @return a short excerpt of the cited node's description (never null). */
    public String getSnippet() {
        return snippet;
    }

    /** @return the provenance source recorded on the node (never null, may be empty). */
    public String getSource() {
        return source;
    }

    /** @return the confidence of the cited node (0.0-1.0; 0.0 when unknown). */
    public double getConfidence() {
        return confidence;
    }

    /** @return the authority of the cited node (0.0-1.0; 0.0 when unknown). */
    public double getAuthority() {
        return authority;
    }

    /**
     * Renders this citation as a markdown reference line, e.g.
     * {@code [1] **Label** — source}.
     *
     * @return a markdown citation line (never null)
     */
    public String toMarkdownLine() {
        StringBuilder line = new StringBuilder();
        line.append('[').append(index).append("] **")
                .append(label.isEmpty() ? knowledgeId : label)
                .append("**");

        if (!source.isBlank()) {
            line.append(" — ").append(source);
        }

        if (!snippet.isBlank()) {
            line.append(": ").append(snippet);
        }

        return line.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeCitation that = (KnowledgeCitation) o;
        return index == that.index
                && Double.compare(that.confidence, confidence) == 0
                && Double.compare(that.authority, authority) == 0
                && knowledgeId.equals(that.knowledgeId)
                && label.equals(that.label)
                && snippet.equals(that.snippet)
                && source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, knowledgeId, label, snippet, source, confidence, authority);
    }

    @Override
    public String toString() {
        return "KnowledgeCitation{"
                + "index=" + index
                + ", knowledgeId='" + knowledgeId + '\''
                + ", label='" + label + '\''
                + ", source='" + source + '\''
                + '}';
    }

    private static String snippetOf(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }
        String trimmed = description.trim();
        if (trimmed.length() <= SNIPPET_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, SNIPPET_MAX_LENGTH - 3) + "...";
    }

    private static String stringOf(Map<String, Object> metadata, String key) {
        if (metadata == null) {
            return "";
        }
        Object value = metadata.get(key);
        return value != null ? value.toString() : "";
    }

    private static double doubleOf(Map<String, Object> metadata, String key) {
        if (metadata == null) {
            return 0.0;
        }
        Object value = metadata.get(key);
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }
}