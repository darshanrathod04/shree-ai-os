package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ChunkMetadata;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.EvidenceItem;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocumentChunk;
import com.shreeai.os.platform.kernels.knowledge.model.RetrievalQuery;
import com.shreeai.os.platform.kernels.knowledge.model.RetrievalResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <b>DefaultKnowledgeRetrievalEngine</b>
 *
 * <p>The default {@link KnowledgeRetrievalEngine}: a deterministic retrieval
 * and ranking engine over an immutable corpus of {@link KnowledgeDocument}s
 * and an optional {@link ConceptGraph}. No embeddings, no vector database, no
 * NLP libraries, no LLM - every signal is an exact, whole-word,
 * case-insensitive match and every score is a locked weighted sum.</p>
 *
 * <p><b>Locked ranking formula:</b></p>
 * <pre>{@code
 * score = (keywordRatio * 0.35) + (conceptRatio * 0.30)
 *       + (sectionRatio * 0.20) + (titleRatio * 0.15)
 * }</pre>
 * where each ratio is the fraction of the query's signals found in the
 * respective deterministic field. Scores are rounded to four decimals for
 * stable comparison.
 *
 * <p><b>Graph expansion:</b> query keywords that name graph concepts select
 * those concepts; their one-hop graph neighbours join the relevant-concept
 * set. Expansion is never recursive. A chunk matches concepts when any
 * relevant concept name occurs (whole-word) in its content, section or
 * document title.</p>
 *
 * <p><b>Stable ordering:</b> relevance score descending, then document title,
 * then section title, then chunk index - identical input always produces
 * identical ordering.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K4 Retrieval and Ranking</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultKnowledgeRetrievalEngine implements KnowledgeRetrievalEngine {

    /** Locked weight of the keyword (chunk content) signal. */
    public static final double KEYWORD_WEIGHT = 0.35;

    /** Locked weight of the graph concept signal. */
    public static final double CONCEPT_WEIGHT = 0.30;

    /** Locked weight of the section title signal. */
    public static final double SECTION_WEIGHT = 0.20;

    /** Locked weight of the document title signal. */
    public static final double TITLE_WEIGHT = 0.15;

    private final List<KnowledgeDocument> corpus;
    private final ConceptGraph graph;
    private final Map<String, GraphConcept> conceptById;

    /**
     * Creates a retrieval engine over the corpus with graph expansion.
     *
     * @param corpus the immutable document corpus (must not be null)
     * @param graph  the concept graph used for one-hop expansion (must not be
     *               null; pass an empty graph to disable expansion)
     */
    public DefaultKnowledgeRetrievalEngine(List<KnowledgeDocument> corpus, ConceptGraph graph) {
        Objects.requireNonNull(corpus, "corpus must not be null");
        Objects.requireNonNull(graph, "graph must not be null");
        this.corpus = List.copyOf(corpus);
        this.graph = graph;
        Map<String, GraphConcept> index = new HashMap<>();
        for (GraphConcept concept : graph.concepts()) {
            index.put(concept.conceptId(), concept);
        }
        this.conceptById = Map.copyOf(index);
    }

    /**
     * Creates a retrieval engine over the corpus without graph expansion.
     *
     * @param corpus the immutable document corpus (must not be null)
     */
    public DefaultKnowledgeRetrievalEngine(List<KnowledgeDocument> corpus) {
        this(corpus, new ConceptGraph(List.of(), List.of()));
    }

    @Override
    public RetrievalResult retrieve(RetrievalQuery query) {
        Objects.requireNonNull(query, "query must not be null");
        List<String> keywords = query.keywords();
        Set<GraphConcept> relevantConcepts = relevantConcepts(keywords);

        List<Candidate> candidates = new ArrayList<>();
        for (KnowledgeDocument document : corpus) {
            for (KnowledgeDocumentChunk chunk : document.chunks()) {
                Candidate candidate = score(document, chunk, keywords, relevantConcepts);
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(Candidate::score).reversed()
                .thenComparing(Candidate::documentTitle)
                .thenComparing(Candidate::section)
                .thenComparingInt(Candidate::chunkIndex));

        List<EvidenceItem> evidence = new ArrayList<>(candidates.size());
        for (Candidate candidate : candidates) {
            evidence.add(new EvidenceItem(candidate.chunkId(), candidate.documentId(),
                    candidate.content(), candidate.score(), candidate.matchedConcepts()));
        }
        return new RetrievalResult(evidence, corpus.size(), evidence.size());
    }

    /**
     * Selects the query-named concepts and expands them exactly one hop:
     * every direct graph neighbour of a selected concept joins the relevant
     * set. Expansion is never recursive.
     */
    private Set<GraphConcept> relevantConcepts(List<String> keywords) {
        Set<GraphConcept> relevant = new LinkedHashSet<>();
        Set<String> selectedIds = new HashSet<>();
        for (GraphConcept concept : graph.concepts()) {
            if (keywords.contains(concept.name().toLowerCase(Locale.ROOT))) {
                relevant.add(concept);
                selectedIds.add(concept.conceptId());
            }
        }
        if (selectedIds.isEmpty()) {
            return Set.copyOf(relevant);
        }
        for (ConceptRelationship relationship : graph.relationships()) {
            if (selectedIds.contains(relationship.fromConcept())) {
                GraphConcept neighbour = conceptById.get(relationship.toConcept());
                if (neighbour != null) {
                    relevant.add(neighbour);
                }
            }
            if (selectedIds.contains(relationship.toConcept())) {
                GraphConcept neighbour = conceptById.get(relationship.fromConcept());
                if (neighbour != null) {
                    relevant.add(neighbour);
                }
            }
        }
        return Set.copyOf(relevant);
    }

    /** Scores one chunk; returns null when no deterministic signal matches. */
    private static Candidate score(KnowledgeDocument document,
                                   KnowledgeDocumentChunk chunk,
                                   List<String> keywords,
                                   Set<GraphConcept> relevantConcepts) {
        ChunkMetadata metadata = chunk.metadata();
        String contentLower = chunk.content().toLowerCase(Locale.ROOT);
        String sectionLower = metadata.section() == null
                ? "" : metadata.section().toLowerCase(Locale.ROOT);
        String titleLower = document.title() == null
                ? "" : document.title().toLowerCase(Locale.ROOT);

        int keywordMatches = 0;
        int sectionMatches = 0;
        int titleMatches = 0;
        for (String keyword : keywords) {
            if (containsWord(contentLower, keyword)) {
                keywordMatches++;
            }
            if (containsWord(sectionLower, keyword)) {
                sectionMatches++;
            }
            if (containsWord(titleLower, keyword)) {
                titleMatches++;
            }
        }

        Set<String> matchedConcepts = new LinkedHashSet<>();
        for (GraphConcept concept : relevantConcepts) {
            String nameLower = concept.name().toLowerCase(Locale.ROOT);
            if (containsWord(contentLower, nameLower)
                    || containsWord(sectionLower, nameLower)
                    || containsWord(titleLower, nameLower)) {
                matchedConcepts.add(concept.name());
            }
        }

        double keywordRatio = keywords.isEmpty() ? 0.0 : (double) keywordMatches / keywords.size();
        double conceptRatio = relevantConcepts.isEmpty()
                ? 0.0 : (double) matchedConcepts.size() / relevantConcepts.size();
        double sectionRatio = keywords.isEmpty() ? 0.0 : (double) sectionMatches / keywords.size();
        double titleRatio = keywords.isEmpty() ? 0.0 : (double) titleMatches / keywords.size();

        double score = round4(KEYWORD_WEIGHT * keywordRatio
                + CONCEPT_WEIGHT * conceptRatio
                + SECTION_WEIGHT * sectionRatio
                + TITLE_WEIGHT * titleRatio);
        if (score <= 0.0) {
            return null;
        }
        return new Candidate(chunk.chunkId(), document.documentId(), chunk.content(),
                score, List.copyOf(matchedConcepts), document.title(),
                metadata.section(), metadata.chunkIndex());
    }

    /** Whole-word, case-insensitive containment over pre-lowered text. */
    private static boolean containsWord(String lowerText, String lowerTerm) {
        if (lowerTerm.isEmpty()) {
            return false;
        }
        int from = 0;
        int found;
        while ((found = lowerText.indexOf(lowerTerm, from)) >= 0) {
            boolean startOk = found == 0
                    || !Character.isLetterOrDigit(lowerText.charAt(found - 1));
            int end = found + lowerTerm.length();
            boolean endOk = end == lowerText.length()
                    || !Character.isLetterOrDigit(lowerText.charAt(end));
            if (startOk && endOk) {
                return true;
            }
            from = found + 1;
        }
        return false;
    }

    /** Rounds to four decimals for stable, comparable scores. */
    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    /** Mutable internal scoring record, ordered before becoming evidence. */
    private record Candidate(
            String chunkId,
            String documentId,
            String content,
            double score,
            List<String> matchedConcepts,
            String documentTitle,
            String section,
            int chunkIndex) {
    }
}
