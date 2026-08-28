package com.shreeai.os.platform.kernels.cognitive.engine;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Advanced deterministic reasoning engine for the Cognitive Kernel.
 *
 * <p>
 * This engine performs structured evidence-based reasoning over:
 * </p>
 *
 * <ul>
 *     <li>User supplied request evidence</li>
 *     <li>Memory evidence</li>
 *     <li>Knowledge evidence</li>
 *     <li>Evidence relevance</li>
 *     <li>Evidence quality</li>
 *     <li>Source confidence</li>
 *     <li>Memory importance</li>
 *     <li>Knowledge authority</li>
 *     <li>Evidence freshness</li>
 *     <li>Cross-source agreement</li>
 *     <li>Potential contradiction detection</li>
 *     <li>Evidence coverage</li>
 *     <li>Uncertainty</li>
 *     <li>Alternative interpretations</li>
 * </ul>
 *
 * <p>
 * The engine is provider-independent and does not require an LLM.
 * A future model-backed reasoning provider can be integrated above or
 * alongside this deterministic reasoning foundation without changing
 * the core ReasoningResult contract.
 * </p>
 *
 * <p>
 * Important architectural rule:
 * <b>the user request itself is a valid evidence source.</b>
 * A request containing project structure, facts, constraints, observations,
 * requirements or other supplied information must not collapse into
 * "insufficient evidence" merely because Memory or Knowledge are empty.
 * </p>
 *
 * <p><b>Architectural Responsibility:</b> Cognitive Kernel - Reasoning</p>
 * <p><b>Version:</b> 2.1</p>
 * <p><b>Legacy dependency:</b> None</p>
 */
public final class DefaultReasoningEngine {

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N}_'-]*");

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "can", "could",
            "did", "do", "does", "for", "from", "how", "i", "in", "is", "it",
            "its", "may", "might", "of", "on", "or", "should", "that", "the",
            "their", "there", "this", "to", "was", "we", "what", "when",
            "where", "which", "who", "why", "will", "with", "would", "you",
            "your"
    );

    private static final Set<String> CONTRADICTION_MARKERS = Set.of(
            "not", "never", "no", "without", "cannot", "can't",
            "false", "incorrect", "wrong", "untrue", "fails", "failed",
            "doesn't", "isn't", "wasn't", "won't"
    );

    /**
     * Performs advanced evidence-based reasoning.
     *
     * @param request the original request text
     * @param memories recalled memories
     * @param knowledgeNodes retrieved knowledge nodes
     * @return immutable reasoning result
     */
    public ReasoningResult reason(
            String request,
            List<Memory> memories,
            List<KnowledgeNode> knowledgeNodes) {

        String reasoningId = "rsn-" + UUID.randomUUID();

        String normalizedRequest = normalize(request);

        Set<String> requestTerms =
                extractMeaningfulTerms(normalizedRequest);

        List<ReasoningEvidence> evidence =
                collectEvidence(
                        normalizedRequest,
                        requestTerms,
                        memories,
                        knowledgeNodes
                );

        evidence.sort(
                Comparator.comparingDouble(
                                ReasoningEvidence::overallScore
                        )
                        .reversed()
                        .thenComparing(
                                ReasoningEvidence::sourcePriority
                        )
                        .thenComparing(
                                ReasoningEvidence::stableKey
                        )
        );

        List<String> findings = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        List<String> alternatives = new ArrayList<>();

        int reasoningSteps = 0;

        // ================================================================
        // STEP 1 - REQUEST ANALYSIS
        // ================================================================

        reasoningSteps++;

        String requestSummary =
                normalizedRequest.isBlank()
                        ? "No request text provided"
                        : normalizedRequest;

        findings.add(
                "Request analyzed: " + requestSummary
        );

        if (requestTerms.isEmpty()) {
            risks.add(
                    "Request contains insufficient semantic terms for reliable relevance analysis"
            );
        }

        // ================================================================
        // STEP 2 - EVIDENCE INVENTORY
        // ================================================================

        reasoningSteps++;

        int requestEvidenceCount = 0;
        int memoryEvidenceCount = 0;
        int knowledgeEvidenceCount = 0;

        for (ReasoningEvidence item : evidence) {

            switch (item.sourceType()) {

                case "REQUEST" ->
                        requestEvidenceCount++;

                case "MEMORY" ->
                        memoryEvidenceCount++;

                case "KNOWLEDGE" ->
                        knowledgeEvidenceCount++;

                default -> {
                    // Intentionally ignored.
                }
            }
        }

        if (evidence.isEmpty()) {

            findings.add(
                    "No evidence was available"
            );

            risks.add(
                    "Reasoning is evidence-limited"
            );

        } else {

            findings.add(
                    "Collected "
                            + evidence.size()
                            + " evidence item(s): "
                            + requestEvidenceCount
                            + " request, "
                            + memoryEvidenceCount
                            + " memory, "
                            + knowledgeEvidenceCount
                            + " knowledge"
            );
        }

        // ================================================================
        // STEP 3 - EVIDENCE QUALITY
        // ================================================================

        reasoningSteps++;

        double averageQuality =
                evidence.stream()
                        .mapToDouble(
                                ReasoningEvidence::qualityScore
                        )
                        .average()
                        .orElse(0.0);

        double averageRelevance =
                evidence.stream()
                        .mapToDouble(
                                ReasoningEvidence::relevanceScore
                        )
                        .average()
                        .orElse(0.0);

        if (!evidence.isEmpty()) {

            findings.add(
                    "Evidence quality assessed at "
                            + formatScore(averageQuality)
                            + " with relevance at "
                            + formatScore(averageRelevance)
            );
        }

        if (averageQuality < 0.40
                && !evidence.isEmpty()) {

            risks.add(
                    "Available evidence has low aggregate source quality"
            );
        }

        // ================================================================
        // STEP 4 - CROSS SOURCE AGREEMENT
        // ================================================================

        reasoningSteps++;

        AgreementAnalysis agreement =
                analyzeAgreement(evidence);

        if (agreement.agreementScore() >= 0.70
                && agreement.comparisons() > 0) {

            findings.add(
                    "Independent evidence shows strong agreement ("
                            + formatScore(
                            agreement.agreementScore()
                    )
                            + ")"
            );

        } else if (agreement.agreementScore() >= 0.45
                && agreement.comparisons() > 0) {

            findings.add(
                    "Independent evidence shows partial agreement ("
                            + formatScore(
                            agreement.agreementScore()
                    )
                            + ")"
            );

        } else if (agreement.comparisons() > 0) {

            findings.add(
                    "Independent evidence sources provide limited agreement"
            );

            risks.add(
                    "Evidence agreement is insufficient for a high-confidence conclusion"
            );
        }

        // ================================================================
        // STEP 5 - CONTRADICTION ANALYSIS
        // ================================================================

        reasoningSteps++;

        List<String> contradictionFindings =
                detectContradictions(evidence);

        if (!contradictionFindings.isEmpty()) {

            findings.addAll(
                    contradictionFindings
            );

            risks.add(
                    "Conflicting evidence was detected and confidence was reduced"
            );
        }

        // ================================================================
        // STEP 6 - EVIDENCE COVERAGE
        // ================================================================

        reasoningSteps++;

        double coverage =
                calculateCoverage(
                        requestTerms,
                        evidence
                );

        if (coverage >= 0.70) {

            findings.add(
                    "Evidence provides strong request coverage ("
                            + formatScore(coverage)
                            + ")"
            );

        } else if (coverage >= 0.40) {

            findings.add(
                    "Evidence provides partial request coverage ("
                            + formatScore(coverage)
                            + ")"
            );

            risks.add(
                    "Some important request concepts are not directly supported"
            );

        } else {

            findings.add(
                    "Evidence provides limited request coverage ("
                            + formatScore(coverage)
                            + ")"
            );

            risks.add(
                    "Most request concepts lack direct supporting evidence"
            );
        }

        // ================================================================
        // STEP 7 - CONCLUSION SYNTHESIS
        // ================================================================

        reasoningSteps++;

        String conclusion =
                synthesizeConclusion(
                        normalizedRequest,
                        evidence,
                        agreement,
                        contradictionFindings
                );

        findings.add(
                "Derived an evidence-grounded conclusion from ranked sources"
        );

        // ================================================================
        // STEP 8 - ALTERNATIVE INTERPRETATIONS
        // ================================================================

        reasoningSteps++;

        alternatives.addAll(
                generateEvidenceBackedAlternatives(
                        normalizedRequest,
                        evidence,
                        agreement
                )
        );

        if (alternatives.isEmpty()) {

            alternatives.add(
                    "Alternative: available evidence is insufficient to establish a competing conclusion"
            );
        }

        findings.add(
                "Generated "
                        + alternatives.size()
                        + " evidence-bounded alternative perspective(s)"
        );

        // ================================================================
        // STEP 9 - CONFIDENCE AND UNCERTAINTY
        // ================================================================

        reasoningSteps++;

        double confidence =
                calculateConfidence(
                        evidence,
                        averageQuality,
                        averageRelevance,
                        agreement.agreementScore(),
                        coverage,
                        contradictionFindings.size()
                );

        if (confidence < 0.50) {

            risks.add(
                    "Conclusion remains uncertain because evidence strength is below the high-confidence threshold"
            );
        }

        if (independentEvidenceCount(evidence) < 2) {

            risks.add(
                    "Independent evidence diversity is limited"
            );
        }

        // ================================================================
        // STEP 10 - SCOPE AND REASONING TYPE
        // ================================================================

        reasoningSteps++;

        String scope =
                deriveScope(
                        normalizedRequest,
                        evidence
                );

        /*
         * IMPORTANT:
         *
         * This is an established Shree AI OS public contract.
         *
         * Advanced reasoning subtype information is stored in metadata,
         * while the ReasoningResult reasoningType remains:
         *
         * EVIDENCE_BASED_REASONING
         *
         * This prevents downstream inference and pipeline components from
         * losing information because the reasoning implementation became
         * more sophisticated.
         */
        String reasoningType =
                determineReasoningType(
                        normalizedRequest,
                        evidence,
                        agreement,
                        contradictionFindings
                );

        // ================================================================
        // EVIDENCE PRESENTATION
        // ================================================================

        List<String> evidenceDescriptions =
                evidence.stream()
                        .limit(12)
                        .map(
                                ReasoningEvidence::description
                        )
                        .toList();

        Map<String, Object> metadata =
                buildMetadata(
                        normalizedRequest,
                        requestTerms,
                        evidence,
                        averageQuality,
                        averageRelevance,
                        agreement,
                        coverage,
                        contradictionFindings,
                        confidence
                );

        String summary =
                buildSummary(
                        evidence,
                        confidence,
                        agreement.agreementScore(),
                        coverage
                );

        return new ReasoningResult(
                reasoningId,
                summary,
                List.copyOf(findings),
                List.copyOf(evidenceDescriptions),
                conclusion,
                confidence,
                List.copyOf(
                        deduplicate(risks)
                ),
                List.copyOf(
                        deduplicate(alternatives)
                ),
                scope,
                reasoningType,
                reasoningSteps,
                Map.copyOf(metadata),
                Instant.now()
        );
    }

    /**
     * Converts all available sources into a common evidence model.
     *
     * <p>
     * IMPORTANT:
     * The request is ALWAYS retained as evidence when non-empty.
     * Memory and Knowledge do not replace request evidence.
     * </p>
     */
    private List<ReasoningEvidence> collectEvidence(
            String request,
            Set<String> requestTerms,
            List<Memory> memories,
            List<KnowledgeNode> knowledgeNodes) {

        List<ReasoningEvidence> evidence =
                new ArrayList<>();

        Set<String> fingerprints =
                new HashSet<>();

        // ================================================================
        // REQUEST EVIDENCE
        // ================================================================

        if (!request.isBlank()) {

            Set<String> requestEvidenceTerms =
                    extractMeaningfulTerms(request);

            String requestFingerprint =
                    fingerprint(request);

            if (fingerprints.add(
                    "REQUEST:" + requestFingerprint
            )) {

                evidence.add(
                        new ReasoningEvidence(
                                "REQUEST",
                                "request-evidence",
                                request,
                                requestEvidenceTerms,
                                1.0,
                                0.90,
                                0.92,
                                1.0,
                                1.0,
                                1.0,
                                null,
                                "USER_REQUEST",
                                0
                        )
                );
            }
        }

        // ================================================================
        // MEMORY EVIDENCE
        // ================================================================

        if (memories != null) {

            for (Memory memory : memories) {

                if (memory == null
                        || memory.content() == null) {
                    continue;
                }

                String text =
                        normalize(
                                memory.content().text()
                        );

                if (text.isBlank()) {
                    continue;
                }

                Set<String> terms =
                        extractMeaningfulTerms(text);

                double relevance =
                        lexicalRelevance(
                                requestTerms,
                                terms
                        );

                double confidence =
                        clamp(
                                memory.metadata().confidence(),
                                0.0,
                                1.0
                        );

                double importance =
                        clamp(
                                memory.metadata().importance(),
                                0.0,
                                1.0
                        );

                double freshness =
                        calculateFreshness(
                                memory.updatedAt()
                        );

                double quality =
                        (confidence * 0.50)
                                + (importance * 0.25)
                                + (freshness * 0.25);

                double overall =
                        (relevance * 0.45)
                                + (quality * 0.40)
                                + (importance * 0.15);

                String fingerprint =
                        fingerprint(text);

                if (!fingerprints.add(
                        "MEMORY:" + fingerprint
                )) {
                    continue;
                }

                evidence.add(
                        new ReasoningEvidence(
                                "MEMORY",
                                memory.id().toString(),
                                text,
                                terms,
                                relevance,
                                quality,
                                overall,
                                confidence,
                                importance,
                                freshness,
                                null,
                                memory.metadata().source(),
                                1
                        )
                );
            }
        }

        // ================================================================
        // KNOWLEDGE EVIDENCE
        // ================================================================

        if (knowledgeNodes != null) {

            for (KnowledgeNode node : knowledgeNodes) {

                if (node == null) {
                    continue;
                }

                String label =
                        normalize(
                                node.getLabel()
                        );

                String description =
                        normalize(
                                node.getDescription()
                        );

                String text =
                        joinNonBlank(
                                label,
                                description
                        );

                if (text.isBlank()) {
                    continue;
                }

                Set<String> terms =
                        extractMeaningfulTerms(text);

                double relevance =
                        lexicalRelevance(
                                requestTerms,
                                terms
                        );

                double sourceConfidence =
                        metadataScore(
                                node.getMetadata(),
                                "confidence",
                                0.50
                        );

                double authority =
                        metadataScore(
                                node.getMetadata(),
                                "authority",
                                0.50
                        );

                double freshness =
                        calculateFreshness(
                                node.getUpdatedAt()
                        );

                double quality =
                        (sourceConfidence * 0.45)
                                + (authority * 0.35)
                                + (freshness * 0.20);

                double overall =
                        (relevance * 0.50)
                                + (quality * 0.40)
                                + (authority * 0.10);

                String fingerprint =
                        fingerprint(text);

                if (!fingerprints.add(
                        "KNOWLEDGE:" + fingerprint
                )) {
                    continue;
                }

                evidence.add(
                        new ReasoningEvidence(
                                "KNOWLEDGE",
                                node.getId().toString(),
                                text,
                                terms,
                                relevance,
                                quality,
                                overall,
                                sourceConfidence,
                                authority,
                                freshness,
                                authority,
                                node.getType() != null
                                        ? node.getType().name()
                                        : null,
                                2
                        )
                );
            }
        }

        return evidence;
    }

    /**
     * Calculates lexical relevance without pretending that lexical overlap
     * is equivalent to semantic understanding.
     */
    private double lexicalRelevance(
            Set<String> requestTerms,
            Set<String> evidenceTerms) {

        if (requestTerms.isEmpty()
                || evidenceTerms.isEmpty()) {

            return 0.0;
        }

        int overlap = 0;

        for (String term : requestTerms) {

            if (evidenceTerms.contains(term)) {
                overlap++;
            }
        }

        if (overlap == 0) {
            return 0.0;
        }

        double recall =
                (double) overlap
                        / requestTerms.size();

        double precision =
                (double) overlap
                        / evidenceTerms.size();

        if (recall + precision == 0.0) {
            return 0.0;
        }

        return clamp(
                2.0 * recall * precision
                        / (recall + precision),
                0.0,
                1.0
        );
    }

    /**
     * Calculates request coverage from the entire evidence set.
     */
    private double calculateCoverage(
            Set<String> requestTerms,
            List<ReasoningEvidence> evidence) {

        if (requestTerms.isEmpty()
                || evidence.isEmpty()) {

            return 0.0;
        }

        Set<String> covered =
                new HashSet<>();

        for (ReasoningEvidence item : evidence) {

            covered.addAll(
                    item.terms()
            );
        }

        int coveredTerms = 0;

        for (String term : requestTerms) {

            if (covered.contains(term)) {
                coveredTerms++;
            }
        }

        return clamp(
                (double) coveredTerms
                        / requestTerms.size(),
                0.0,
                1.0
        );
    }

    /**
     * Evaluates agreement only between independent external evidence sources.
     *
     * <p>
     * REQUEST evidence is intentionally excluded from agreement calculations
     * because it is the observation being reasoned about, not an independent
     * external confirmation of itself.
     * </p>
     */
    private AgreementAnalysis analyzeAgreement(
            List<ReasoningEvidence> evidence) {

        List<ReasoningEvidence> independent =
                evidence.stream()
                        .filter(
                                item ->
                                        !"REQUEST".equals(
                                                item.sourceType()
                                        )
                        )
                        .toList();

        if (independent.size() < 2) {

            return new AgreementAnalysis(
                    independent.isEmpty()
                            ? 0.0
                            : 0.50,
                    independent.isEmpty()
                            ? 1.0
                            : 0.50,
                    0
            );
        }

        double weightedAgreement = 0.0;
        double totalWeight = 0.0;
        int comparisons = 0;

        for (int i = 0;
             i < independent.size();
             i++) {

            ReasoningEvidence left =
                    independent.get(i);

            for (int j = i + 1;
                 j < independent.size();
                 j++) {

                ReasoningEvidence right =
                        independent.get(j);

                if (left.sourceType()
                        .equals(
                                right.sourceType()
                        )) {

                    continue;
                }

                double overlap =
                        jaccard(
                                left.terms(),
                                right.terms()
                        );

                double pairWeight =
                        Math.max(
                                0.05,
                                left.overallScore()
                        )
                                * Math.max(
                                0.05,
                                right.overallScore()
                        );

                weightedAgreement +=
                        overlap * pairWeight;

                totalWeight += pairWeight;

                comparisons++;
            }
        }

        if (comparisons == 0
                || totalWeight == 0.0) {

            return new AgreementAnalysis(
                    0.35,
                    0.65,
                    comparisons
            );
        }

        double agreement =
                weightedAgreement
                        / totalWeight;

        agreement =
                clamp(
                        agreement,
                        0.0,
                        1.0
                );

        return new AgreementAnalysis(
                agreement,
                1.0 - agreement,
                comparisons
        );
    }

    /**
     * Detects conservative contradiction signals.
     *
     * <p>
     * This method intentionally reports "potential contradiction" rather than
     * claiming formal logical contradiction from lexical markers alone.
     * </p>
     */
    private List<String> detectContradictions(
            List<ReasoningEvidence> evidence) {

        List<String> findings =
                new ArrayList<>();

        List<ReasoningEvidence> independent =
                evidence.stream()
                        .filter(
                                item ->
                                        !"REQUEST".equals(
                                                item.sourceType()
                                        )
                        )
                        .toList();

        for (int i = 0;
             i < independent.size();
             i++) {

            ReasoningEvidence left =
                    independent.get(i);

            for (int j = i + 1;
                 j < independent.size();
                 j++) {

                ReasoningEvidence right =
                        independent.get(j);

                if (left.sourceType()
                        .equals(
                                right.sourceType()
                        )) {

                    continue;
                }

                Set<String> sharedTerms =
                        new HashSet<>(
                                left.terms()
                        );

                sharedTerms.retainAll(
                        right.terms()
                );

                if (sharedTerms.size() < 2) {
                    continue;
                }

                boolean leftNegative =
                        containsContradictionMarker(
                                left.terms()
                        );

                boolean rightNegative =
                        containsContradictionMarker(
                                right.terms()
                        );

                if (leftNegative != rightNegative) {

                    findings.add(
                            "Potential contradiction detected between "
                                    + left.sourceType()
                                    + " evidence "
                                    + left.stableKey()
                                    + " and "
                                    + right.sourceType()
                                    + " evidence "
                                    + right.stableKey()
                                    + " around shared concepts: "
                                    + formatTerms(
                                    sharedTerms
                            )
                    );
                }
            }
        }

        return findings.stream()
                .distinct()
                .limit(5)
                .toList();
    }

    /**
     * Produces an evidence-grounded conclusion.
     *
     * <p>
     * Request evidence receives special treatment because it represents
     * information explicitly supplied by the user/application. It must be
     * preserved even when no external evidence exists.
     * </p>
     */
    private String synthesizeConclusion(
            String request,
            List<ReasoningEvidence> evidence,
            AgreementAnalysis agreement,
            List<String> contradictions) {

        if (evidence.isEmpty()) {

            return "Insufficient evidence to form a reliable conclusion for the request: "
                    + (
                    request.isBlank()
                            ? "no request provided"
                            : request
            );
        }

        ReasoningEvidence requestEvidence =
                evidence.stream()
                        .filter(
                                item ->
                                        "REQUEST".equals(
                                                item.sourceType()
                                        )
                        )
                        .findFirst()
                        .orElse(null);

        if (requestEvidence != null) {

            StringBuilder conclusion =
                    new StringBuilder();

            conclusion.append(
                    "Evidence-grounded assessment: "
            );

            conclusion.append(
                    requestEvidence.content()
            );

            // Detect architectural concepts (controller/service/repository/entity layers)
            // present in the supplied evidence and reflect them as normalized
            // architectural terminology so the conclusion stays evidence-grounded
            // without inventing layers that were not mentioned.
            String architecturalAssessment =
                    buildArchitecturalAssessment(evidence);

            if (!architecturalAssessment.isBlank()) {
                conclusion.append(" ");
                conclusion.append(
                        architecturalAssessment
                );
            }

            List<ReasoningEvidence> externalSupporting =
                    evidence.stream()
                            .filter(
                                    item ->
                                            !"REQUEST".equals(
                                                    item.sourceType()
                                            )
                            )
                            .filter(
                                    item ->
                                            item.relevanceScore()
                                                    >= 0.15
                            )
                            .limit(4)
                            .toList();

            if (!externalSupporting.isEmpty()) {

                conclusion.append(
                        " External supporting evidence: "
                );

                for (int i = 0;
                     i < externalSupporting.size();
                     i++) {

                    if (i > 0) {
                        conclusion.append(" | ");
                    }

                    conclusion.append(
                            externalSupporting
                                    .get(i)
                                    .description()
                    );
                }
            }

            if (!contradictions.isEmpty()) {

                conclusion.append(
                        " However, potential conflicting signals were detected, "
                                + "so this assessment should be treated as provisional."
                );

            } else if (agreement.comparisons() > 0
                    && agreement.agreementScore() >= 0.70) {

                conclusion.append(
                        " Independent evidence is broadly consistent "
                                + "with the supplied information."
                );

            } else if (!externalSupporting.isEmpty()) {

                conclusion.append(
                        " The available external evidence provides additional "
                                + "support, but independent confirmation is not complete."
                );

            } else {

                conclusion.append(
                        " The assessment is grounded in information explicitly "
                                + "supplied by the requester; external verification "
                                + "is limited."
                );
            }

            return conclusion.toString();
        }

        ReasoningEvidence primary =
                evidence.get(0);

        StringBuilder conclusion =
                new StringBuilder();

        conclusion.append(
                "Primary evidence: "
        );

        conclusion.append(
                primary.description()
        );

        List<ReasoningEvidence> supporting =
                evidence.stream()
                        .filter(
                                item ->
                                        item != primary
                        )
                        .filter(
                                item ->
                                        item.relevanceScore()
                                                >= 0.15
                        )
                        .limit(3)
                        .toList();

        if (!supporting.isEmpty()) {

            conclusion.append(
                    " Supporting evidence: "
            );

            for (int i = 0;
                 i < supporting.size();
                 i++) {

                if (i > 0) {
                    conclusion.append(" | ");
                }

                conclusion.append(
                        supporting
                                .get(i)
                                .description()
                );
            }
        }

        if (!contradictions.isEmpty()) {

            conclusion.append(
                    " However, conflicting signals were detected, "
                            + "so this conclusion should be treated as provisional."
            );

        } else if (agreement.comparisons() > 0
                && agreement.agreementScore() >= 0.70) {

            conclusion.append(
                    " Independent evidence is broadly consistent "
                            + "with this conclusion."
            );

        } else {

            conclusion.append(
                    " The available evidence supports this interpretation, "
                            + "but independent confirmation is limited."
            );
        }

        return conclusion.toString();
    }

    /**
     * Builds an architectural assessment from evidence signals.
     *
     * <p>This method inspects the supplied evidence for architectural layer
     * terminology (controller, service, repository, entity, DTO) and maps the
     * detected terms to normalized architectural layer references. It never
     * invents layers — it only reflects signals already present in the
     * evidence. When the evidence describes source structure but not actual
     * defect analysis, a source-level limitation statement is appended so the
     * conclusion remains honest about its scope.</p>
     *
     * @param evidence the collected reasoning evidence
     * @return the architectural assessment fragment, or empty string if the
     *         evidence contains no architectural signals
     */
    private String buildArchitecturalAssessment(
            List<ReasoningEvidence> evidence) {

        StringBuilder rawSignals = new StringBuilder();

        for (ReasoningEvidence item : evidence) {
            rawSignals.append(item.content()).append(" ");
        }

        String content = rawSignals.toString().toLowerCase(Locale.ROOT);

        List<String> layers = new ArrayList<>();

        if (content.contains("controller")
                || content.matches(".*\\bcontrollers?\\b.*")) {
            layers.add("Controller");
        }
        if (content.contains("service")
                || content.matches(".*\\bservices?\\b.*")) {
            layers.add("Service");
        }
        if (content.contains("repositor")
                || content.matches(".*\\brepositor(?:y|ies)\\b.*")) {
            layers.add("Repository");
        }
        if (content.contains("entit")
                || content.matches(".*\\bentit(?:y|ies)\\b.*")) {
            layers.add("Entity");
        }
        if (content.contains("dto")) {
            layers.add("DTO");
        }

        if (layers.isEmpty()) {
            return "";
        }

        StringBuilder assessment = new StringBuilder();

        assessment.append("The supplied evidence describes a structured layered "
                + "application including ");

        for (int i = 0; i < layers.size(); i++) {
            if (i > 0) {
                if (i == layers.size() - 1) {
                    assessment.append(" and ");
                } else {
                    assessment.append(", ");
                }
            }
            assessment.append(layers.get(i)).append(" layer");
        }

        assessment.append(". "
                + "While the structure and technologies are evidence-supported, "
                + "source-level defects cannot be determined from the supplied "
                + "evidence alone.");

        return assessment.toString();
    }

    /**
     * Generates alternatives only from available evidence.
     */
    private List<String> generateEvidenceBackedAlternatives(
            String request,
            List<ReasoningEvidence> evidence,
            AgreementAnalysis agreement) {

        List<String> alternatives =
                new ArrayList<>();

        List<ReasoningEvidence> externalEvidence =
                evidence.stream()
                        .filter(
                                item ->
                                        !"REQUEST".equals(
                                                item.sourceType()
                                        )
                        )
                        .toList();

        if (!externalEvidence.isEmpty()) {

            ReasoningEvidence secondary =
                    externalEvidence.get(0);

            alternatives.add(
                    "Alternative evidence perspective: "
                            + secondary.description()
            );
        }

        if (externalEvidence.size() >= 2) {

            ReasoningEvidence tertiary =
                    externalEvidence.get(1);

            alternatives.add(
                    "Secondary interpretation: "
                            + tertiary.description()
                            + " should also be considered before making "
                            + "a high-impact decision."
            );
        }

        if (agreement.agreementScore() < 0.50
                && agreement.comparisons() > 0) {

            alternatives.add(
                    "Competing interpretation remains plausible because "
                            + "independent evidence sources do not strongly agree."
            );
        }

        if (request.contains("?")
                && externalEvidence.isEmpty()) {

            alternatives.add(
                    "Verification path: obtain at least one independent "
                            + "source before treating the current assessment "
                            + "as definitive."
            );
        }

        if (alternatives.isEmpty()) {

            alternatives.add(
                    "Verification path: obtain independent evidence "
                            + "before making a high-impact decision."
            );
        }

        return alternatives.stream()
                .distinct()
                .limit(4)
                .toList();
    }

    /**
     * Calculates confidence from independent dimensions.
     */
    private double calculateConfidence(
            List<ReasoningEvidence> evidence,
            double averageQuality,
            double averageRelevance,
            double agreement,
            double coverage,
            int contradictionCount) {

        if (evidence.isEmpty()) {
            return 0.10;
        }

        double evidenceStrength =
                averageQuality * 0.30
                        + averageRelevance * 0.25
                        + agreement * 0.20
                        + coverage * 0.20
                        + evidenceDiversity(evidence) * 0.05;

        double contradictionPenalty =
                Math.min(
                        0.35,
                        contradictionCount * 0.10
                );

        double independentEvidenceBonus =
                Math.min(
                        0.10,
                        Math.max(
                                0,
                                independentEvidenceCount(
                                        evidence
                                ) - 1
                        ) * 0.025
                );

        double confidence =
                evidenceStrength
                        + independentEvidenceBonus
                        - contradictionPenalty;

        /*
         * Request evidence is authoritative only as user-supplied evidence,
         * not as independently verified evidence.
         *
         * A request-only assessment is useful for traceability but must not
         * be promoted to "verified" epistemic confidence. When no external
         * evidence (memory/knowledge) is available, the conclusion remains
         * highly uncertain and confidence must stay minimal.
         */
        if (independentEvidenceCount(evidence) == 0) {

            confidence =
                    Math.min(
                            confidence,
                            0.45
                    );
        }

        return clamp(
                confidence,
                0.10,
                0.95
        );
    }

    /**
     * Rewards independent evidence diversity.
     */
    private double evidenceDiversity(
            List<ReasoningEvidence> evidence) {

        Set<String> sources =
                new HashSet<>();

        for (ReasoningEvidence item : evidence) {

            if (!"REQUEST".equals(
                    item.sourceType()
            )) {

                sources.add(
                        item.sourceType()
                );
            }
        }

        if (sources.size() >= 2) {
            return 1.0;
        }

        if (sources.size() == 1) {
            return 0.50;
        }

        return 0.25;
    }

    /**
     * Preserves the existing Shree AI OS reasoning type contract.
     *
     * <p>
     * Advanced subtype information is exposed through metadata rather than
     * breaking the public reasoning type expected by downstream pipeline
     * components.
     * </p>
     */
    private String determineReasoningType(
            String request,
            List<ReasoningEvidence> evidence,
            AgreementAnalysis agreement,
            List<String> contradictions) {

        return "EVIDENCE_BASED_REASONING";
    }

    /**
     * Derives a compact reasoning scope.
     */
    private String deriveScope(
            String request,
            List<ReasoningEvidence> evidence) {

        Set<String> terms =
                extractMeaningfulTerms(request);

        if (!terms.isEmpty()) {

            return String.join(
                    ", ",
                    terms.stream()
                            .limit(8)
                            .toList()
            );
        }

        if (!evidence.isEmpty()) {

            return evidence.get(0)
                    .sourceType()
                    .toLowerCase(
                            Locale.ROOT
                    );
        }

        return "unknown";
    }

    /**
     * Builds structured metadata without leaking mutable internal state.
     */
    private Map<String, Object> buildMetadata(
            String request,
            Set<String> requestTerms,
            List<ReasoningEvidence> evidence,
            double averageQuality,
            double averageRelevance,
            AgreementAnalysis agreement,
            double coverage,
            List<String> contradictions,
            double confidence) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "reasoningVersion",
                "2.1"
        );

        metadata.put(
                "providerIndependent",
                true
        );

        metadata.put(
                "evidenceGrounded",
                true
        );

        metadata.put(
                "requestIsEvidence",
                !request.isBlank()
        );

        metadata.put(
                "requestLength",
                request.length()
        );

        metadata.put(
                "requestTermCount",
                requestTerms.size()
        );

        metadata.put(
                "evidenceCount",
                evidence.size()
        );

        metadata.put(
                "requestEvidenceCount",
                evidence.stream()
                        .filter(
                                item ->
                                        "REQUEST".equals(
                                                item.sourceType()
                                        )
                        )
                        .count()
        );

        metadata.put(
                "memoryEvidenceCount",
                evidence.stream()
                        .filter(
                                item ->
                                        "MEMORY".equals(
                                                item.sourceType()
                                        )
                        )
                        .count()
        );

        metadata.put(
                "knowledgeEvidenceCount",
                evidence.stream()
                        .filter(
                                item ->
                                        "KNOWLEDGE".equals(
                                                item.sourceType()
                                        )
                        )
                        .count()
        );

        metadata.put(
                "independentEvidenceCount",
                independentEvidenceCount(
                        evidence
                )
        );

        metadata.put(
                "averageEvidenceQuality",
                round(averageQuality)
        );

        metadata.put(
                "averageEvidenceRelevance",
                round(averageRelevance)
        );

        metadata.put(
                "evidenceAgreement",
                round(
                        agreement.agreementScore()
                )
        );

        metadata.put(
                "evidenceDisagreement",
                round(
                        agreement.disagreementScore()
                )
        );

        metadata.put(
                "agreementComparisons",
                agreement.comparisons()
        );

        metadata.put(
                "evidenceCoverage",
                round(coverage)
        );

        metadata.put(
                "contradictionCount",
                contradictions.size()
        );

        metadata.put(
                "confidence",
                round(confidence)
        );

        metadata.put(
                "confidenceBand",
                confidenceBand(
                        confidence
                )
        );

        metadata.put(
                "reasoningSubtype",
                deriveReasoningSubtype(
                        evidence,
                        agreement,
                        contradictions
                )
        );

        List<Map<String, Object>> evidenceTrace =
                evidence.stream()
                        .limit(12)
                        .map(
                                item -> {

                                    Map<String, Object> trace =
                                            new LinkedHashMap<>();

                                    trace.put(
                                            "sourceType",
                                            item.sourceType()
                                    );

                                    trace.put(
                                            "sourceId",
                                            item.stableKey()
                                    );

                                    trace.put(
                                            "relevance",
                                            round(
                                                    item.relevanceScore()
                                            )
                                    );

                                    trace.put(
                                            "quality",
                                            round(
                                                    item.qualityScore()
                                            )
                                    );

                                    trace.put(
                                            "overallScore",
                                            round(
                                                    item.overallScore()
                                            )
                                    );

                                    trace.put(
                                            "confidence",
                                            round(
                                                    item.sourceConfidence()
                                            )
                                    );

                                    trace.put(
                                            "importanceOrAuthority",
                                            round(
                                                    item.importanceOrAuthority()
                                            )
                                    );

                                    trace.put(
                                            "freshness",
                                            round(
                                                    item.freshness()
                                            )
                                    );

                                    trace.put(
                                            "sourceDescriptor",
                                            item.sourceDescriptor()
                                    );

                                    return Map.copyOf(
                                            trace
                                    );
                                }
                        )
                        .toList();

        metadata.put(
                "evidenceTrace",
                evidenceTrace
        );

        metadata.put(
                "verificationRequired",
                confidence < 0.70
                        || !contradictions.isEmpty()
        );

        metadata.put(
                "reasoningQuality",
                qualityBand(
                        confidence,
                        contradictions.size(),
                        coverage
                )
        );

        return metadata;
    }

    private String deriveReasoningSubtype(
            List<ReasoningEvidence> evidence,
            AgreementAnalysis agreement,
            List<String> contradictions) {

        boolean hasRequest =
                evidence.stream()
                        .anyMatch(
                                item ->
                                        "REQUEST".equals(
                                                item.sourceType()
                                        )
                        );

        boolean hasMemory =
                evidence.stream()
                        .anyMatch(
                                item ->
                                        "MEMORY".equals(
                                                item.sourceType()
                                        )
                        );

        boolean hasKnowledge =
                evidence.stream()
                        .anyMatch(
                                item ->
                                        "KNOWLEDGE".equals(
                                                item.sourceType()
                                        )
                        );

        if (!contradictions.isEmpty()) {

            return "CONTRADICTION_AWARE_EVIDENCE_REASONING";
        }

        if (hasMemory && hasKnowledge) {

            if (agreement.agreementScore() >= 0.70) {

                return "CROSS_SOURCE_EVIDENCE_SYNTHESIS";
            }

            return "MULTI_SOURCE_EVIDENCE_REASONING";
        }

        if (hasRequest
                && (hasMemory || hasKnowledge)) {

            return "REQUEST_GROUNDED_EVIDENCE_REASONING";
        }

        if (hasRequest) {

            return "REQUEST_EVIDENCE_REASONING";
        }

        if (hasMemory || hasKnowledge) {

            return "EXTERNAL_EVIDENCE_REASONING";
        }

        return "EVIDENCE_LIMITED_REASONING";
    }

    private String buildSummary(
            List<ReasoningEvidence> evidence,
            double confidence,
            double agreement,
            double coverage) {

        return "Advanced evidence-based reasoning completed using "
                + evidence.size()
                + " evidence item(s); confidence="
                + formatScore(confidence)
                + ", agreement="
                + formatScore(agreement)
                + ", coverage="
                + formatScore(coverage)
                + ".";
    }

    private String qualityBand(
            double confidence,
            int contradictionCount,
            double coverage) {

        if (contradictionCount > 0) {
            return "CONDITIONAL";
        }

        if (confidence >= 0.80
                && coverage >= 0.70) {

            return "HIGH";
        }

        if (confidence >= 0.60
                && coverage >= 0.50) {

            return "MODERATE";
        }

        if (confidence >= 0.40) {
            return "LIMITED";
        }

        return "LOW";
    }

    private String confidenceBand(
            double confidence) {

        if (confidence >= 0.80) {
            return "HIGH";
        }

        if (confidence >= 0.60) {
            return "MODERATE";
        }

        if (confidence >= 0.40) {
            return "LOW";
        }

        return "VERY_LOW";
    }

    private boolean containsContradictionMarker(
            Set<String> terms) {

        for (String term : terms) {

            if (CONTRADICTION_MARKERS.contains(
                    term
            )) {

                return true;
            }
        }

        return false;
    }

    private double calculateFreshness(
            Instant timestamp) {

        if (timestamp == null) {
            return 0.50;
        }

        long days;

        try {

            days =
                    Math.max(
                            0,
                            Duration.between(
                                    timestamp,
                                    Instant.now()
                            ).toDays()
                    );

        } catch (Exception e) {

            return 0.50;
        }

        if (days <= 1) {
            return 1.0;
        }

        if (days <= 7) {
            return 0.95;
        }

        if (days <= 30) {
            return 0.85;
        }

        if (days <= 90) {
            return 0.70;
        }

        if (days <= 365) {
            return 0.50;
        }

        return 0.30;
    }

    private double metadataScore(
            Map<String, Object> metadata,
            String key,
            double fallback) {

        if (metadata == null
                || metadata.isEmpty()) {

            return fallback;
        }

        Object value =
                metadata.get(key);

        if (value instanceof Number number) {

            return clamp(
                    number.doubleValue(),
                    0.0,
                    1.0
            );
        }

        if (value instanceof String string) {

            try {

                return clamp(
                        Double.parseDouble(string),
                        0.0,
                        1.0
                );

            } catch (NumberFormatException ignored) {

                return fallback;
            }
        }

        return fallback;
    }

    private Set<String> extractMeaningfulTerms(
            String text) {

        if (text == null
                || text.isBlank()) {

            return Set.of();
        }

        Set<String> terms =
                new LinkedHashSet<>();

        var matcher =
                TOKEN_PATTERN.matcher(
                        text.toLowerCase(
                                Locale.ROOT
                        )
                );

        while (matcher.find()) {

            String token =
                    matcher.group();

            if (token.length() < 2) {
                continue;
            }

            if (STOP_WORDS.contains(
                    token
            )) {

                continue;
            }

            terms.add(token);
        }

        return terms;
    }

    private double jaccard(
            Set<String> left,
            Set<String> right) {

        if (left.isEmpty()
                || right.isEmpty()) {

            return 0.0;
        }

        Set<String> intersection =
                new HashSet<>(
                        left
                );

        intersection.retainAll(
                right
        );

        Set<String> union =
                new HashSet<>(
                        left
                );

        union.addAll(
                right
        );

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size()
                / union.size();
    }

    private String fingerprint(
            String text) {

        return String.join(
                " ",
                extractMeaningfulTerms(
                        text
                )
                        .stream()
                        .sorted()
                        .limit(40)
                        .toList()
        );
    }

    private String formatTerms(
            Set<String> terms) {

        return terms.stream()
                .sorted()
                .limit(8)
                .reduce(
                        (a, b) ->
                                a + ", " + b
                )
                .orElse(
                        "shared concepts"
                );
    }

    private String joinNonBlank(
            String left,
            String right) {

        if (left == null
                || left.isBlank()) {

            return right == null
                    ? ""
                    : right;
        }

        if (right == null
                || right.isBlank()) {

            return left;
        }

        return left + " - " + right;
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private String truncate(
            String value,
            int maxLength) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                Math.max(
                        0,
                        maxLength - 3
                )
        ) + "...";
    }

    private List<String> deduplicate(
            List<String> values) {

        return new ArrayList<>(
                new LinkedHashSet<>(
                        values
                )
        );
    }

    private int independentEvidenceCount(
            List<ReasoningEvidence> evidence) {

        int count = 0;

        for (ReasoningEvidence item :
                evidence) {

            if (!"REQUEST".equals(
                    item.sourceType()
            )) {

                count++;
            }
        }

        return count;
    }

    private double clamp(
            double value,
            double min,
            double max) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }

    private double round(
            double value) {

        return Math.round(
                value * 10000.0
        ) / 10000.0;
    }

    private String formatScore(
            double value) {

        return String.format(
                Locale.ROOT,
                "%.2f",
                value
        );
    }

    /**
     * Internal immutable representation of reasoning evidence.
     */
    private record ReasoningEvidence(
            String sourceType,
            String stableKey,
            String content,
            Set<String> terms,
            double relevanceScore,
            double qualityScore,
            double overallScore,
            double sourceConfidence,
            double importanceOrAuthority,
            double freshness,
            Double authority,
            String sourceDescriptor,
            int sourcePriority) {

        String description() {

            String safeContent =
                    content == null
                            ? ""
                            : content;

            return sourceType
                    + " ["
                    + stableKey
                    + "] "
                    + safeContent;
        }
    }

    /**
     * Internal immutable agreement analysis result.
     */
    private record AgreementAnalysis(
            double agreementScore,
            double disagreementScore,
            int comparisons) {
    }
}