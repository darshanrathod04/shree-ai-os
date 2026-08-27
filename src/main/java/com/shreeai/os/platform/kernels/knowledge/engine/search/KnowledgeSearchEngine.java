package com.shreeai.os.platform.kernels.knowledge.engine.search;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeGraph;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;

import java.util.List;

public interface KnowledgeSearchEngine {

    List<KnowledgeNode> semanticSearch(
            KnowledgeGraph graph,
            String query
    );

    List<KnowledgeNode> keywordSearch(
            KnowledgeGraph graph,
            String keyword
    );

    List<KnowledgeNode> topicSearch(
            KnowledgeGraph graph,
            String topic
    );

    List<KnowledgeNode> tagSearch(
            KnowledgeGraph graph,
            Iterable<String> tags
    );
}