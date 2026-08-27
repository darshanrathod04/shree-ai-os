package com.shreeai.os.platform.kernels.knowledge.engine.search;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeGraph;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class DefaultKnowledgeSearchEngine implements KnowledgeSearchEngine {

    @Override
    public List<KnowledgeNode> semanticSearch(
            KnowledgeGraph graph,
            String query
    ) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(query);

        String q = normalize(query);

        if (q.isBlank()) {
            return List.of();
        }

        return graph.getNodes().stream()
                .filter(Objects::nonNull)
                .filter(node -> score(node, q) > 0)
                .sorted(Comparator.comparingDouble(
                        (KnowledgeNode node) -> score(node, q)
                ).reversed())
                .toList();
    }

    @Override
    public List<KnowledgeNode> keywordSearch(
            KnowledgeGraph graph,
            String keyword
    ) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(keyword);

        String q = normalize(keyword);

        return graph.getNodes().stream()
                .filter(node ->
                        normalize(node.getLabel()).contains(q) ||
                                normalize(node.getDescription()).contains(q)
                )
                .toList();
    }

    @Override
    public List<KnowledgeNode> topicSearch(
            KnowledgeGraph graph,
            String topic
    ) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(topic);

        String q = normalize(topic);

        return graph.getNodes().stream()
                .filter(node -> {
                    Object value = node.getMetadata().get("topic");
                    return value != null &&
                            normalize(value.toString()).equals(q);
                })
                .toList();
    }

    @Override
    public List<KnowledgeNode> tagSearch(
            KnowledgeGraph graph,
            Iterable<String> tags
    ) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(tags);

        Set<String> normalizedTags = toSet(tags);

        return graph.getNodes().stream()
                .filter(node -> {
                    Object value = node.getMetadata().get("tags");

                    if (!(value instanceof Iterable<?> iterable)) {
                        return false;
                    }

                    for (Object tag : iterable) {
                        if (tag != null &&
                                normalizedTags.contains(normalize(tag.toString()))) {
                            return true;
                        }
                    }

                    return false;
                })
                .toList();
    }

    private double score(KnowledgeNode node, String query) {

        double score = 0;

        if (normalize(node.getLabel()).contains(query)) {
            score += 5;
        }

        if (normalize(node.getDescription()).contains(query)) {
            score += 3;
        }

        for (Object value : node.getMetadata().values()) {

            if (value != null &&
                    normalize(value.toString()).contains(query)) {

                score += 1;
            }
        }

        return score;
    }

    private String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).trim();
    }

    private Set<String> toSet(Iterable<String> tags) {

        List<String> values = new ArrayList<>();

        for (String tag : tags) {
            if (tag != null) {
                values.add(normalize(tag));
            }
        }

        return Set.copyOf(values);
    }
}