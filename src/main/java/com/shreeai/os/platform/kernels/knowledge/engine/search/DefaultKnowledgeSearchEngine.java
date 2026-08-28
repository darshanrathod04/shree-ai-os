package com.shreeai.os.platform.kernels.knowledge.engine.search;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeGraph;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;

import java.util.*;
import java.util.stream.Collectors;

public final class DefaultKnowledgeSearchEngine implements KnowledgeSearchEngine {

    private static final double LABEL_WEIGHT = 5.0;
    private static final double DESCRIPTION_WEIGHT = 3.0;
    private static final double METADATA_WEIGHT = 1.0;

    @Override
    public List<KnowledgeNode> semanticSearch(
            KnowledgeGraph graph,
            String query
    ) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(query, "query must not be null");

        List<String> queryTokens = tokenize(query);

        if (queryTokens.isEmpty()) {
            return List.of();
        }

        return graph.getNodes().stream()
                .filter(Objects::nonNull)
                .filter(node -> semanticScore(node, queryTokens) > 0)
                .sorted(Comparator.comparingDouble(
                        (KnowledgeNode node) -> semanticScore(node, queryTokens)
                ).reversed())
                .toList();
    }

    @Override
    public List<KnowledgeNode> keywordSearch(
            KnowledgeGraph graph,
            String keyword
    ) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(keyword, "keyword must not be null");

        String normalized = normalize(keyword);

        if (normalized.isBlank()) {
            return List.of();
        }

        return graph.getNodes().stream()
                .filter(Objects::nonNull)
                .filter(node -> containsKeyword(node, normalized))
                .sorted(Comparator.comparingDouble(
                        (KnowledgeNode node) -> keywordScore(node, normalized)
                ).reversed())
                .toList();
    }

    @Override
    public List<KnowledgeNode> topicSearch(
            KnowledgeGraph graph,
            String topic
    ) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(topic, "topic must not be null");

        String normalized = normalize(topic);

        return graph.getNodes().stream()
                .filter(Objects::nonNull)
                .filter(node -> {
                    Object value = node.getMetadata().get("topic");
                    return value != null &&
                            normalize(value.toString()).equals(normalized);
                })
                .toList();
    }

    @Override
    public List<KnowledgeNode> tagSearch(
            KnowledgeGraph graph,
            Iterable<String> tags
    ) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(tags, "tags must not be null");

        Set<String> normalizedTags = toSet(tags);

        if (normalizedTags.isEmpty()) {
            return List.of();
        }

        return graph.getNodes().stream()
                .filter(Objects::nonNull)
                .filter(node -> hasMatchingTag(node, normalizedTags))
                .sorted(Comparator.comparingDouble(
                        (KnowledgeNode node) -> tagScore(node, normalizedTags)
                ).reversed())
                .toList();
    }

    // ----------------------------------------------------------
    // Enterprise Scoring
    // ----------------------------------------------------------

    private double semanticScore(
            KnowledgeNode node,
            List<String> queryTokens
    ) {
        double score = 0;

        List<String> labelTokens = tokenize(node.getLabel());
        List<String> descriptionTokens = tokenize(node.getDescription());

        score += overlap(labelTokens, queryTokens) * LABEL_WEIGHT;
        score += overlap(descriptionTokens, queryTokens) * DESCRIPTION_WEIGHT;

        for (Object value : node.getMetadata().values()) {

            if (value instanceof Iterable<?> iterable) {

                List<String> tokens = new ArrayList<>();

                for (Object item : iterable) {
                    if (item != null) {
                        tokens.addAll(tokenize(item.toString()));
                    }
                }

                score += overlap(tokens, queryTokens) * METADATA_WEIGHT;

            } else if (value != null) {

                score += overlap(
                        tokenize(value.toString()),
                        queryTokens
                ) * METADATA_WEIGHT;
            }
        }

        return score;
    }

    private double keywordScore(
            KnowledgeNode node,
            String keyword
    ) {
        double score = 0;

        if (normalize(node.getLabel()).contains(keyword)) {
            score += LABEL_WEIGHT;
        }

        if (normalize(node.getDescription()).contains(keyword)) {
            score += DESCRIPTION_WEIGHT;
        }

        for (Object value : node.getMetadata().values()) {

            if (value instanceof Iterable<?> iterable) {

                for (Object item : iterable) {
                    if (item != null &&
                            normalize(item.toString()).contains(keyword)) {
                        score += METADATA_WEIGHT;
                    }
                }

            } else if (value != null &&
                    normalize(value.toString()).contains(keyword)) {

                score += METADATA_WEIGHT;
            }
        }

        return score;
    }

    // ----------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------

    private boolean containsKeyword(
            KnowledgeNode node,
            String keyword
    ) {
        return keywordScore(node, keyword) > 0;
    }

    private boolean hasMatchingTag(
            KnowledgeNode node,
            Set<String> tags
    ) {

        Object value = node.getMetadata().get("tags");

        if (!(value instanceof Iterable<?> iterable)) {
            return false;
        }

        for (Object tag : iterable) {
            if (tag != null &&
                    tags.contains(normalize(tag.toString()))) {
                return true;
            }
        }

        return false;
    }

    private double tagScore(
            KnowledgeNode node,
            Set<String> tags
    ) {

        Object value = node.getMetadata().get("tags");

        if (!(value instanceof Iterable<?> iterable)) {
            return 0;
        }

        double score = 0;

        for (Object tag : iterable) {
            if (tag != null &&
                    tags.contains(normalize(tag.toString()))) {
                score++;
            }
        }

        return score;
    }

    private double overlap(
            List<String> source,
            List<String> query
    ) {

        if (source.isEmpty() || query.isEmpty()) {
            return 0;
        }

        Set<String> sourceSet = new HashSet<>(source);

        long matches = query.stream()
                .filter(sourceSet::contains)
                .count();

        return (double) matches / query.size();
    }

    private List<String> tokenize(String text) {

        String normalized = normalize(text);

        if (normalized.isBlank()) {
            return List.of();
        }

        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toList());
    }

    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Set<String> toSet(Iterable<String> tags) {

        Set<String> values = new HashSet<>();

        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                values.add(normalize(tag));
            }
        }

        return Set.copyOf(values);
    }
}