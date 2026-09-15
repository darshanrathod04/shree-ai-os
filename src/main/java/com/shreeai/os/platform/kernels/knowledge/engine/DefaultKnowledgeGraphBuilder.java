package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ChunkMetadata;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptType;
import com.shreeai.os.platform.kernels.knowledge.model.GraphConcept;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocumentChunk;
import com.shreeai.os.platform.kernels.knowledge.model.RelationshipType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <b>DefaultKnowledgeGraphBuilder</b>
 *
 * <p>The default {@link KnowledgeGraphBuilder}: a stateless, thread-safe,
 * deterministic graph construction engine over the built-in technology
 * dictionary. It performs no embeddings, no LLM calls and no NLP-library
 * parsing - every discovery is a longest-match dictionary scan or an explicit
 * dictionary rule.</p>
 *
 * <p><b>Pipeline (locked):</b></p>
 * <ol>
 *   <li><b>Concept extraction:</b> longest-match, word-bounded, case-insensitive
 *       dictionary scan over the document title, every section heading and
 *       every content paragraph.</li>
 *   <li><b>Canonicalization:</b> every alias collapses into its single
 *       dictionary canonical name (e.g. {@code springboot}/{@code spring boot}
 *       → {@code Spring Boot}, {@code JAVA} → {@code Java}).</li>
 *   <li><b>Relationship detection:</b> explicit dictionary rules emit
 *       {@code PREREQUISITE}/{@code PART_OF}/{@code DEPENDS_ON} edges when
 *       both endpoints are present; co-occurrence tiers emit
 *       {@code RELATED_TO} edges (title &gt; heading &gt; repeated section
 *       &gt; single paragraph). Explicit rules suppress co-occurrence edges
 *       between the same pair.</li>
 *   <li><b>Deduplication:</b> one concept per canonical name, one edge per
 *       {@code (from, type, to)}; canonical ordering by name.</li>
 * </ol>
 *
 * <p><b>Deterministic ids:</b> {@code conceptId = SHA-256(type +
 * canonicalName)} and {@code relationshipId = SHA-256(fromConcept + type +
 * toConcept)} (pipe-delimited seeds) - identical input always yields
 * identical ids and an identical graph.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K3 Knowledge Graph Builder</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultKnowledgeGraphBuilder implements KnowledgeGraphBuilder {

    /** Locked confidence tier: both concepts co-occur in the document title. */
    public static final double TITLE_CONFIDENCE = 1.00;

    /** Locked confidence tier: both concepts co-occur in section headings. */
    public static final double HEADING_CONFIDENCE = 0.95;

    /** Locked confidence tier: explicit dictionary prerequisite rule. */
    public static final double PREREQUISITE_CONFIDENCE = 0.90;

    /** Locked confidence tier: repeated co-occurrence across sections. */
    public static final double SECTION_CO_OCCURRENCE_CONFIDENCE = 0.80;

    /** Locked confidence tier: single-section (paragraph level) co-occurrence. */
    public static final double PARAGRAPH_RELATION_CONFIDENCE = 0.70;

    private static final Pattern PARAGRAPH_SEPARATOR = Pattern.compile("\\n[ \\t]*\\n");

    /** Where a concept was discovered; drives the co-occurrence confidence tier. */
    private enum Provenance {
        TITLE,
        HEADING,
        BODY
    }

    private record DictionaryEntry(String canonicalName, ConceptType type, List<String> aliases) {
    }

    private record DirectedRule(String from, String to) {
    }

    private record AliasBinding(String alias, String canonicalName, ConceptType type) {
    }

    private record Match(String canonicalName, ConceptType type, int start, int end) {
    }

    private static final List<DictionaryEntry> DICTIONARY = List.of(
            new DictionaryEntry("Java", ConceptType.LANGUAGE, List.of("java", "jdk")),
            new DictionaryEntry("Kotlin", ConceptType.LANGUAGE, List.of("kotlin")),
            new DictionaryEntry("Spring Boot", ConceptType.FRAMEWORK, List.of("spring boot", "springboot")),
            new DictionaryEntry("Spring", ConceptType.FRAMEWORK, List.of("spring")),
            new DictionaryEntry("Spring Data", ConceptType.LIBRARY, List.of("spring data")),
            new DictionaryEntry("Collections", ConceptType.LIBRARY, List.of("collection framework", "collections")),
            new DictionaryEntry("Streams", ConceptType.LIBRARY, List.of("stream api", "streams")),
            new DictionaryEntry("List", ConceptType.TOPIC, List.of("list")),
            new DictionaryEntry("PostgreSQL", ConceptType.DATABASE, List.of("postgresql", "postgres")),
            new DictionaryEntry("Maven", ConceptType.TOOL, List.of("maven")),
            new DictionaryEntry("Gradle", ConceptType.TOOL, List.of("gradle")),
            new DictionaryEntry("Hibernate", ConceptType.LIBRARY, List.of("hibernate")),
            new DictionaryEntry("JUnit", ConceptType.TOOL, List.of("junit")),
            new DictionaryEntry("Docker", ConceptType.TOOL, List.of("docker")),
            new DictionaryEntry("Kubernetes", ConceptType.TOOL, List.of("kubernetes")));

    private static final List<DirectedRule> PREREQUISITE_RULES = List.of(
            new DirectedRule("Collections", "Streams"));

    private static final List<DirectedRule> PART_OF_RULES = List.of(
            new DirectedRule("List", "Collections"),
            new DirectedRule("Streams", "Collections"));

    private static final List<DirectedRule> DEPENDS_ON_RULES = List.of(
            new DirectedRule("Spring Boot", "Java"));

    private static final List<AliasBinding> ALIAS_BINDINGS = buildAliasBindings();
    private static final Map<String, DictionaryEntry> ENTRY_BY_NAME = buildEntryIndex();

    private static List<AliasBinding> buildAliasBindings() {
        List<AliasBinding> bindings = new ArrayList<>();
        for (DictionaryEntry entry : DICTIONARY) {
            for (String alias : entry.aliases()) {
                bindings.add(new AliasBinding(
                        alias.toLowerCase(Locale.ROOT), entry.canonicalName(), entry.type()));
            }
        }
        bindings.sort(Comparator.comparingInt((AliasBinding binding) -> binding.alias().length()).reversed()
                .thenComparing(AliasBinding::alias)
                .thenComparing(binding -> binding.canonicalName()));
        return List.copyOf(bindings);
    }

    private static Map<String, DictionaryEntry> buildEntryIndex() {
        Map<String, DictionaryEntry> index = new HashMap<>();
        for (DictionaryEntry entry : DICTIONARY) {
            index.put(entry.canonicalName().toLowerCase(Locale.ROOT), entry);
        }
        return Map.copyOf(index);
    }

    @Override
    public ConceptGraph build(KnowledgeDocument document) {
        Objects.requireNonNull(document, "document must not be null");
        Map<String, ConceptOccurrence> occurrences = new HashMap<>();
        recordOccurrences(document.title(), Provenance.TITLE, -1, occurrences);
        for (KnowledgeDocumentChunk chunk : document.chunks()) {
            ChunkMetadata metadata = chunk.metadata();
            if (metadata.section() != null && !metadata.section().isBlank()) {
                recordOccurrences(metadata.section(), Provenance.HEADING, metadata.chunkIndex(), occurrences);
            }
            String content = chunk.content();
            if (content == null || content.isBlank()) {
                continue;
            }
            String[] paragraphs = PARAGRAPH_SEPARATOR.split(content);
            for (int paragraphIndex = 0; paragraphIndex < paragraphs.length; paragraphIndex++) {
                if (!paragraphs[paragraphIndex].isBlank()) {
                    recordOccurrences(paragraphs[paragraphIndex], Provenance.BODY,
                            metadata.chunkIndex(), occurrences);
                }
            }
        }

        List<GraphConcept> concepts = new ArrayList<>(occurrences.size());
        Map<String, GraphConcept> conceptById = new HashMap<>();
        for (ConceptOccurrence occurrence : occurrences.values()) {
            GraphConcept concept = new GraphConcept(
                    conceptIdFor(occurrence.type(), occurrence.canonicalName()),
                    occurrence.canonicalName(),
                    occurrence.type());
            concepts.add(concept);
            conceptById.put(occurrence.key(), concept);
        }

        Map<String, ConceptRelationship> edges = new LinkedHashMap<>();
        Set<String> explicitPairs = new HashSet<>();
        addExplicitRules(edges, explicitPairs, conceptById,
                PREREQUISITE_RULES, RelationshipType.PREREQUISITE, PREREQUISITE_CONFIDENCE);
        addExplicitRules(edges, explicitPairs, conceptById,
                PART_OF_RULES, RelationshipType.PART_OF, SECTION_CO_OCCURRENCE_CONFIDENCE);
        addExplicitRules(edges, explicitPairs, conceptById,
                DEPENDS_ON_RULES, RelationshipType.DEPENDS_ON, SECTION_CO_OCCURRENCE_CONFIDENCE);
        addRelatedToEdges(edges, explicitPairs, occurrences, conceptById);

        List<ConceptRelationship> relationships = new ArrayList<>(edges.values());
        return new ConceptGraph(concepts, relationships);
    }

    /** Longest-match dictionary scan of one region, recorded under a provenance. */
    private static void recordOccurrences(String text,
                                          Provenance provenance,
                                          int chunkIndex,
                                          Map<String, ConceptOccurrence> occurrences) {
        for (Match match : scanMatches(text)) {
            String key = match.canonicalName().toLowerCase(Locale.ROOT);
            occurrences
                    .computeIfAbsent(key, ignored ->
                            new ConceptOccurrence(key, match.canonicalName(), match.type()))
                    .record(provenance, chunkIndex);
        }
    }

    /** Longest-match, word-bounded, case-insensitive dictionary scan. */
    private static List<Match> scanMatches(String text) {
        Objects.requireNonNull(text, "text must not be null");
        List<Match> matches = new ArrayList<>();
        String lower = text.toLowerCase(Locale.ROOT);
        int position = 0;
        int length = lower.length();
        while (position < length) {
            if (position > 0 && isWordChar(lower.charAt(position - 1))) {
                position++;
                continue;
            }
            Match match = matchAt(lower, position);
            if (match != null) {
                matches.add(match);
                position = match.end();
            } else {
                position++;
            }
        }
        return List.copyOf(matches);
    }

    /** Tries every alias, longest first; both ends must be word boundaries. */
    private static Match matchAt(String lower, int start) {
        for (AliasBinding binding : ALIAS_BINDINGS) {
            int end = start + binding.alias().length();
            if (end <= lower.length()
                    && lower.startsWith(binding.alias(), start)
                    && (end == lower.length() || !isWordChar(lower.charAt(end)))) {
                return new Match(binding.canonicalName(), binding.type(), start, end);
            }
        }
        return null;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c);
    }

    private static void addExplicitRules(Map<String, ConceptRelationship> edges,
                                         Set<String> explicitPairs,
                                         Map<String, GraphConcept> conceptById,
                                         List<DirectedRule> rules,
                                         RelationshipType type,
                                         double confidence) {
        for (DirectedRule rule : rules) {
            GraphConcept from = conceptById.get(rule.from().toLowerCase(Locale.ROOT));
            GraphConcept to = conceptById.get(rule.to().toLowerCase(Locale.ROOT));
            if (from == null || to == null || from.conceptId().equals(to.conceptId())) {
                continue;
            }
            String relationshipId = relationshipIdFor(from.conceptId(), type, to.conceptId());
            edges.putIfAbsent(relationshipId, new ConceptRelationship(
                    relationshipId, from.conceptId(), to.conceptId(), type, confidence));
            explicitPairs.add(pairKey(from.conceptId(), to.conceptId()));
        }
    }

    /** Emits RELATED_TO edges at the highest provenance tier available. */
    private static void addRelatedToEdges(Map<String, ConceptRelationship> edges,
                                          Set<String> explicitPairs,
                                          Map<String, ConceptOccurrence> occurrences,
                                          Map<String, GraphConcept> conceptById) {
        List<ConceptOccurrence> ordered = occurrences.values().stream()
                .sorted(Comparator.comparing(ConceptOccurrence::canonicalName))
                .toList();
        for (int i = 0; i < ordered.size(); i++) {
            for (int j = i + 1; j < ordered.size(); j++) {
                ConceptOccurrence first = ordered.get(i);
                ConceptOccurrence second = ordered.get(j);
                GraphConcept from = conceptById.get(first.key());
                GraphConcept to = conceptById.get(second.key());
                if (explicitPairs.contains(pairKey(from.conceptId(), to.conceptId()))) {
                    continue;
                }
                double confidence = relatedToConfidence(first, second);
                if (confidence < 0.0) {
                    continue;
                }
                String sourceId = from.conceptId().compareTo(to.conceptId()) < 0
                        ? from.conceptId() : to.conceptId();
                String targetId = sourceId.equals(from.conceptId()) ? to.conceptId() : from.conceptId();
                String relationshipId = relationshipIdFor(
                        sourceId, RelationshipType.RELATED_TO, targetId);
                edges.putIfAbsent(relationshipId, new ConceptRelationship(
                        relationshipId, sourceId, targetId, RelationshipType.RELATED_TO, confidence));
            }
        }
    }

    /** Locked tier cascade; negative means no textual co-occurrence. */
    private static double relatedToConfidence(ConceptOccurrence first, ConceptOccurrence second) {
        if (first.inTitle() && second.inTitle()) {
            return TITLE_CONFIDENCE;
        }
        if (first.inHeading() && second.inHeading()) {
            return HEADING_CONFIDENCE;
        }
        long sharedSections = first.bodyChunks().stream()
                .filter(second.bodyChunks()::contains)
                .count();
        if (sharedSections >= 2) {
            return SECTION_CO_OCCURRENCE_CONFIDENCE;
        }
        if (sharedSections >= 1) {
            return PARAGRAPH_RELATION_CONFIDENCE;
        }
        return -1.0;
    }

    private static String pairKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }

    /** Mutable per-document accumulation of one concept's provenance. */
    private static final class ConceptOccurrence {

        private final String key;
        private final String canonicalName;
        private final ConceptType type;
        private boolean inTitle;
        private boolean inHeading;
        private final Set<Integer> bodyChunks = new HashSet<>();

        private ConceptOccurrence(String key, String canonicalName, ConceptType type) {
            this.key = key;
            this.canonicalName = canonicalName;
            this.type = type;
        }

        private void record(Provenance provenance, int chunkIndex) {
            switch (provenance) {
                case TITLE -> inTitle = true;
                case HEADING -> inHeading = true;
                case BODY -> bodyChunks.add(chunkIndex);
                default -> throw new IllegalStateException("unknown provenance: " + provenance);
            }
        }

        private String key() {
            return key;
        }

        private String canonicalName() {
            return canonicalName;
        }

        private ConceptType type() {
            return type;
        }

        private boolean inTitle() {
            return inTitle;
        }

        private boolean inHeading() {
            return inHeading;
        }

        private Set<Integer> bodyChunks() {
            return bodyChunks;
        }
    }

    /**
     * Computes the deterministic concept id: {@code SHA-256(type +
     * canonicalName)} (pipe-delimited seed).
     *
     * @param type          the concept family (must not be null)
     * @param canonicalName the canonical concept name (must not be null)
     * @return the lowercase hex SHA-256 concept id (never null)
     */
    public static String conceptIdFor(ConceptType type, String canonicalName) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(canonicalName, "canonicalName must not be null");
        return sha256Hex(type.name() + "|" + canonicalName);
    }

    /**
     * Computes the deterministic relationship id: {@code SHA-256(fromConcept +
     * type + toConcept)} (pipe-delimited seed).
     *
     * @param fromConceptId the source concept id (must not be null)
     * @param type          the relationship kind (must not be null)
     * @param toConceptId   the target concept id (must not be null)
     * @return the lowercase hex SHA-256 relationship id (never null)
     */
    public static String relationshipIdFor(String fromConceptId,
                                           RelationshipType type,
                                           String toConceptId) {
        Objects.requireNonNull(fromConceptId, "fromConceptId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(toConceptId, "toConceptId must not be null");
        return sha256Hex(fromConceptId + "|" + type.name() + "|" + toConceptId);
    }

    /** Lowercase hex SHA-256 of the UTF-8 encoded seed. */
    private static String sha256Hex(String seed) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is unavailable", e);
        }
        byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16));
            hex.append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }
}
