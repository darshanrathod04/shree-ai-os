package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.inference.model.Hypothesis;
import com.shreeai.os.platform.kernels.inference.model.InferenceResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.model.Memory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Advanced evidence-based inference engine for the Inference Kernel.
 *
 * <p>This engine converts evidence and an upstream reasoning result into
 * competing, evidence-bounded hypotheses. It deliberately avoids treating
 * the reasoning conclusion as ground truth.</p>
 *
 * <p>The engine evaluates:</p>
 * <ul>
 *     <li>request/evidence relevance</li>
 *     <li>source quality</li>
 *     <li>source confidence</li>
 *     <li>memory importance</li>
 *     <li>knowledge authority</li>
 *     <li>freshness</li>
 *     <li>supporting evidence</li>
 *     <li>opposing evidence</li>
 *     <li>cross-source agreement</li>
 *     <li>evidence coverage</li>
 *     <li>unknown information</li>
 *     <li>next investigation requirements</li>
 * </ul>
 *
 * <p><b>Architectural Responsibility:</b> Inference Kernel</p>
 * <p><b>Version:</b> 2.0</p>
 *
 * <p><b>Legacy dependency:</b> None.</p>
 */
public final class DefaultInferenceEngine {

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

    private static final Set<String> NEGATION_TERMS = Set.of(
            "not", "never", "no", "without", "cannot", "can't",
            "isn't", "wasn't", "doesn't", "don't", "won't",
            "failed", "fails", "false", "wrong", "incorrect"
    );

    /**
     * Performs evidence-based inference.
     *
     * @param request the original user request
     * @param reasoningResult the upstream reasoning result
     * @param memories recalled memories
     * @param knowledgeNodes retrieved knowledge nodes
     * @param context execution context
     * @return inference result
     */
    public InferenceResult infer(
            String request,
            ReasoningResult reasoningResult,
            List<Memory> memories,
            List<KnowledgeNode> knowledgeNodes,
            String context) {

        String inferenceId = "inf-" + UUID.randomUUID().toString().substring(0, 8);

        String normalizedRequest = normalize(request);
        String normalizedContext = normalize(context);

        List<InferenceEvidence> evidence = collectEvidence(
                normalizedRequest,
                reasoningResult,
                memories,
                knowledgeNodes
        );

        evidence.sort(
                Comparator.comparingDouble(InferenceEvidence::overallScore)
                        .reversed()
                        .thenComparing(InferenceEvidence::stableId)
        );

        List<String> supportingEvidence = new ArrayList<>();
        List<String> contradictingEvidence = new ArrayList<>();
        List<String> unknownInformation = new ArrayList<>();

        collectEvidenceSignals(
                evidence,
                reasoningResult,
                supportingEvidence,
                contradictingEvidence
        );

        if (memories == null || memories.isEmpty()) {
            unknownInformation.add("No relevant memories available");
        }

        if (knowledgeNodes == null || knowledgeNodes.isEmpty()) {
            unknownInformation.add("No relevant knowledge available");
        }

        Set<String> requestTerms = extractTerms(normalizedRequest);

        double coverage = calculateCoverage(requestTerms, evidence);
        double agreement = calculateAgreement(evidence);
        double diversity = calculateDiversity(evidence);

        unknownInformation.addAll(
                identifyUnknownInformation(
                        requestTerms,
                        evidence,
                        reasoningResult,
                        coverage,
                        agreement
                )
        );

        List<Hypothesis> hypotheses = generateHypotheses(
                normalizedRequest,
                reasoningResult,
                evidence,
                requestTerms,
                coverage,
                agreement,
                diversity
        );

        hypotheses = rankHypotheses(hypotheses);

        Hypothesis bestHypothesis;

        if (hypotheses.isEmpty()) {
            bestHypothesis = new Hypothesis(
                    "h0",
                    "Insufficient evidence to form a reliable hypothesis",
                    0.10,
                    List.of(),
                    List.of("No sufficiently relevant evidence was available"),
                    "UNLIKELY",
                    0
            );

            hypotheses = List.of(bestHypothesis);
        } else {
            bestHypothesis = hypotheses.get(0);
        }

        if (bestHypothesis.confidence() < 0.60) {
            unknownInformation.add(
                    "The leading hypothesis remains below the high-confidence threshold"
            );
        }

        String nextInvestigation = recommendNextInvestigation(
                normalizedRequest,
                evidence,
                bestHypothesis,
                unknownInformation,
                coverage,
                agreement
        );

        double overallConfidence = calculateOverallConfidence(
                bestHypothesis,
                reasoningResult,
                coverage,
                agreement,
                diversity
        );

        String investigationContext = normalizedContext.isBlank()
                ? "inference"
                : normalizedContext;

        return new InferenceResult(
                inferenceId,
                hypotheses,
                bestHypothesis,
                overallConfidence,
                deduplicate(supportingEvidence),
                deduplicate(contradictingEvidence),
                deduplicate(unknownInformation),
                nextInvestigation,
                investigationContext,
                Instant.now()
        );
    }

    /**
     * Builds a common evidence representation from reasoning, memory and
     * knowledge sources.
     */
    private List<InferenceEvidence> collectEvidence(
            String request,
            ReasoningResult reasoningResult,
            List<Memory> memories,
            List<KnowledgeNode> knowledgeNodes) {

        List<InferenceEvidence> evidence = new ArrayList<>();
        Set<String> fingerprints = new HashSet<>();

        Set<String> requestTerms = extractTerms(request);

        // -------------------------------------------------------------
        // Reasoning conclusion
        // -------------------------------------------------------------

        if (reasoningResult != null
                && reasoningResult.conclusion() != null
                && !reasoningResult.conclusion().isBlank()) {

            String conclusion = normalize(reasoningResult.conclusion());
            Set<String> terms = extractTerms(conclusion);

            double relevance = lexicalRelevance(requestTerms, terms);
            double quality = clamp(
                    reasoningResult.confidence(),
                    0.0,
                    1.0
            );

            double overall =
                    (relevance * 0.45)
                            + (quality * 0.55);

            String fingerprint = fingerprint(conclusion);

            if (fingerprints.add(fingerprint)) {
                evidence.add(
                        new InferenceEvidence(
                                "REASONING",
                                "reasoning-conclusion",
                                conclusion,
                                terms,
                                relevance,
                                quality,
                                overall,
                                reasoningResult.confidence(),
                                0.0,
                                1.0,
                                false
                        )
                );
            }
        }

        // -------------------------------------------------------------
        // Memory evidence
        // -------------------------------------------------------------

        if (memories != null) {
            for (Memory memory : memories) {

                if (memory == null
                        || memory.content() == null
                        || memory.content().text() == null) {
                    continue;
                }

                String text = normalize(memory.content().text());

                if (text.isBlank()) {
                    continue;
                }

                Set<String> terms = extractTerms(text);

                double relevance = lexicalRelevance(requestTerms, terms);

                double confidence = clamp(
                        memory.metadata().confidence(),
                        0.0,
                        1.0
                );

                double importance = clamp(
                        memory.metadata().importance(),
                        0.0,
                        1.0
                );

                double freshness = freshness(memory.updatedAt());

                double quality =
                        (confidence * 0.45)
                                + (importance * 0.30)
                                + (freshness * 0.25);

                double overall =
                        (relevance * 0.50)
                                + (quality * 0.35)
                                + (importance * 0.15);

                String fingerprint = fingerprint(text);

                if (!fingerprints.add(fingerprint)) {
                    continue;
                }

                evidence.add(
                        new InferenceEvidence(
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
                                containsNegation(terms)
                        )
                );
            }
        }

        // -------------------------------------------------------------
        // Knowledge evidence
        // -------------------------------------------------------------

        if (knowledgeNodes != null) {
            for (KnowledgeNode node : knowledgeNodes) {

                if (node == null) {
                    continue;
                }

                String label = normalize(node.getLabel());
                String description = normalize(node.getDescription());

                String text;

                if (description.isBlank()) {
                    text = label;
                } else {
                    text = label + " - " + description;
                }

                if (text.isBlank()) {
                    continue;
                }

                Set<String> terms = extractTerms(text);

                double relevance = lexicalRelevance(requestTerms, terms);

                double confidence = metadataScore(
                        node.getMetadata(),
                        "confidence",
                        0.50
                );

                double authority = metadataScore(
                        node.getMetadata(),
                        "authority",
                        0.50
                );

                double freshness = freshness(node.getUpdatedAt());

                double quality =
                        (confidence * 0.45)
                                + (authority * 0.35)
                                + (freshness * 0.20);

                double overall =
                        (relevance * 0.50)
                                + (quality * 0.40)
                                + (authority * 0.10);

                String fingerprint = fingerprint(text);

                if (!fingerprints.add(fingerprint)) {
                    continue;
                }

                evidence.add(
                        new InferenceEvidence(
                                "KNOWLEDGE",
                                node.getId().toString(),
                                text,
                                terms,
                                relevance,
                                quality,
                                overall,
                                confidence,
                                authority,
                                freshness,
                                containsNegation(terms)
                        )
                );
            }
        }

        return evidence;
    }

    /**
     * Generates competing hypotheses from independent evidence roles.
     */
    private List<Hypothesis> generateHypotheses(
            String request,
            ReasoningResult reasoningResult,
            List<InferenceEvidence> evidence,
            Set<String> requestTerms,
            double coverage,
            double agreement,
            double diversity) {

        List<Hypothesis> hypotheses = new ArrayList<>();

        int index = 0;

        InferenceEvidence reasoningEvidence = findFirst(
                evidence,
                "REASONING"
        );

        List<InferenceEvidence> memoryEvidence = filterByType(
                evidence,
                "MEMORY"
        );

        List<InferenceEvidence> knowledgeEvidence = filterByType(
                evidence,
                "KNOWLEDGE"
        );

        // -------------------------------------------------------------
        // Hypothesis 1: evidence synthesis
        // -------------------------------------------------------------

        if (reasoningEvidence != null) {

            List<String> supporting = buildSupportingEvidence(
                    reasoningEvidence,
                    evidence,
                    5
            );

            List<String> opposing = buildOpposingEvidence(
                    reasoningEvidence,
                    evidence,
                    5
            );

            double evidenceStrength = weightedEvidenceStrength(
                    evidence,
                    requestTerms
            );

            double confidence =
                    reasoningEvidence.sourceConfidence() * 0.35
                            + evidenceStrength * 0.30
                            + coverage * 0.15
                            + agreement * 0.10
                            + diversity * 0.10;

            confidence -= contradictionPenalty(
                    supporting,
                    opposing
            );

            confidence = clamp(confidence, 0.10, 0.95);

            String status = statusFor(confidence);

            hypotheses.add(
                    new Hypothesis(
                            "h" + index++,
                            "Primary evidence synthesis: "
                                    + truncate(
                                    reasoningEvidence.content(),
                                    180
                            ),
                            confidence,
                            supporting,
                            opposing,
                            status,
                            1
                    )
            );
        }

        // -------------------------------------------------------------
        // Hypothesis 2: memory-supported explanation
        // -------------------------------------------------------------

        if (!memoryEvidence.isEmpty()) {

            InferenceEvidence topMemory = memoryEvidence.get(0);

            List<String> supporting = new ArrayList<>();

            supporting.add(
                    "Memory evidence: "
                            + topMemory.content()
            );

            for (InferenceEvidence item : memoryEvidence) {
                if (item != topMemory
                        && item.relevanceScore() >= 0.20) {

                    supporting.add(
                            "Additional memory evidence: "
                                    + item.content()
                    );
                }
            }

            List<String> opposing = findOpposingEvidence(
                    topMemory,
                    evidence,
                    4
            );

            double confidence =
                    topMemory.overallScore() * 0.55
                            + memoryAgreement(memoryEvidence) * 0.20
                            + coverage * 0.15
                            + reasoningConfidence(reasoningResult) * 0.10;

            if (topMemory.negated()) {
                confidence -= 0.20;
            }

            confidence = clamp(confidence, 0.10, 0.90);

            hypotheses.add(
                    new Hypothesis(
                            "h" + index++,
                            "Memory-supported hypothesis: "
                                    + topMemory.content(),
                            confidence,
                            limit(supporting, 5),
                            limit(opposing, 4),
                            statusFor(confidence),
                            2
                    )
            );
        }

        // -------------------------------------------------------------
        // Hypothesis 3: knowledge-supported explanation
        // -------------------------------------------------------------

        if (!knowledgeEvidence.isEmpty()) {

            InferenceEvidence topKnowledge = knowledgeEvidence.get(0);

            List<String> supporting = new ArrayList<>();

            supporting.add(
                    "Knowledge evidence: "
                            + topKnowledge.content()
            );

            for (InferenceEvidence item : knowledgeEvidence) {
                if (item != topKnowledge
                        && item.relevanceScore() >= 0.20) {

                    supporting.add(
                            "Additional knowledge evidence: "
                                    + item.content()
                    );
                }
            }

            List<String> opposing = findOpposingEvidence(
                    topKnowledge,
                    evidence,
                    4
            );

            double confidence =
                    topKnowledge.overallScore() * 0.60
                            + knowledgeAgreement(knowledgeEvidence) * 0.20
                            + coverage * 0.10
                            + reasoningConfidence(reasoningResult) * 0.10;

            if (topKnowledge.negated()) {
                confidence -= 0.20;
            }

            confidence = clamp(confidence, 0.10, 0.92);

            hypotheses.add(
                    new Hypothesis(
                            "h" + index++,
                            "Knowledge-supported hypothesis: "
                                    + topKnowledge.content(),
                            confidence,
                            limit(supporting, 5),
                            limit(opposing, 4),
                            statusFor(confidence),
                            3
                    )
            );
        }

        // -------------------------------------------------------------
        // Hypothesis 4: competing interpretation
        // -------------------------------------------------------------

        if (evidence.size() >= 2) {

            InferenceEvidence strongest = evidence.get(0);
            InferenceEvidence second = evidence.get(1);

            double divergence =
                    1.0 - jaccard(
                            strongest.terms(),
                            second.terms()
                    );

            if (divergence >= 0.35) {

                double confidence =
                        Math.min(
                                strongest.overallScore(),
                                second.overallScore()
                        ) * 0.65
                                + (1.0 - agreement) * 0.20
                                + coverage * 0.15;

                confidence = clamp(confidence, 0.10, 0.80);

                List<String> supporting = List.of(
                        "Competing evidence source A: "
                                + strongest.content(),
                        "Competing evidence source B: "
                                + second.content()
                );

                List<String> opposing = List.of(
                        "Evidence sources have limited semantic overlap"
                );

                hypotheses.add(
                        new Hypothesis(
                                "h" + index++,
                                "Competing interpretation: available evidence supports more than one plausible explanation.",
                                confidence,
                                supporting,
                                opposing,
                                statusFor(confidence),
                                4
                        )
                );
            }
        }

        // -------------------------------------------------------------
        // No evidence fallback
        // -------------------------------------------------------------

        if (hypotheses.isEmpty()) {
            hypotheses.add(
                    new Hypothesis(
                            "h" + index,
                            "Insufficient evidence to form a reliable hypothesis for: "
                                    + (request.isBlank()
                                    ? "the current request"
                                    : request),
                            0.10,
                            List.of(),
                            List.of("No relevant evidence available"),
                            "UNLIKELY",
                            0
                    )
            );
        }

        return hypotheses;
    }

    /**
     * Ranks hypotheses using confidence, evidence quality, and priority.
     */
    private List<Hypothesis> rankHypotheses(
            List<Hypothesis> hypotheses) {

        return hypotheses.stream()
                .sorted(
                        Comparator
                                .comparingDouble(Hypothesis::confidence)
                                .reversed()
                                .thenComparingInt(Hypothesis::priority)
                                .thenComparing(Hypothesis::id)
                )
                .toList();
    }

    /**
     * Calculates overall inference confidence separately from the best
     * hypothesis confidence.
     */
    private double calculateOverallConfidence(
            Hypothesis best,
            ReasoningResult reasoningResult,
            double coverage,
            double agreement,
            double diversity) {

        double reasoningConfidence =
                reasoningConfidence(reasoningResult);

        double result =
                best.confidence() * 0.50
                        + reasoningConfidence * 0.15
                        + coverage * 0.15
                        + agreement * 0.10
                        + diversity * 0.10;

        if ("UNCERTAIN".equals(best.status())) {
            result -= 0.05;
        }

        if ("UNLIKELY".equals(best.status())) {
            result -= 0.10;
        }

        return clamp(result, 0.10, 0.95);
    }

    /**
     * Determines missing information and uncertainty.
     */
    private List<String> identifyUnknownInformation(
            Set<String> requestTerms,
            List<InferenceEvidence> evidence,
            ReasoningResult reasoningResult,
            double coverage,
            double agreement) {

        List<String> unknowns = new ArrayList<>();

        if (requestTerms.isEmpty()) {
            unknowns.add(
                    "The request does not contain enough meaningful terms for reliable inference"
            );
        }

        if (coverage < 0.40) {
            unknowns.add(
                    "Evidence covers only a limited portion of the request"
            );
        }

        if (coverage >= 0.40 && coverage < 0.70) {
            unknowns.add(
                    "Some request concepts are not directly supported by evidence"
            );
        }

        if (agreement < 0.40 && evidence.size() >= 2) {
            unknowns.add(
                    "Independent evidence sources do not strongly agree"
            );
        }

        if (reasoningResult != null
                && reasoningResult.confidence() < 0.50) {

            unknowns.add(
                    "Upstream reasoning confidence is low"
            );
        }

        if (evidence.size() == 1) {
            unknowns.add(
                    "Only one substantive evidence source is available"
            );
        }

        return unknowns;
    }

    /**
     * Recommends the next investigation based on the actual uncertainty.
     */
    private String recommendNextInvestigation(
            String request,
            List<InferenceEvidence> evidence,
            Hypothesis bestHypothesis,
            List<String> unknownInformation,
            double coverage,
            double agreement) {

        if (evidence.isEmpty()) {
            return "Retrieve at least one authoritative source directly addressing: "
                    + (request.isBlank() ? "the request" : request);
        }

        if (coverage < 0.40) {
            return "Retrieve evidence specifically covering the missing concepts in the request before relying on the leading hypothesis.";
        }

        if (agreement < 0.40 && evidence.size() >= 2) {
            return "Independently verify the conflicting evidence sources and determine which source has stronger authority and recency.";
        }

        if (bestHypothesis.confidence() < 0.60) {
            return "Collect an independent evidence source that can directly confirm or challenge the leading hypothesis.";
        }

        if (!unknownInformation.isEmpty()) {
            return "Resolve the remaining unknown information before making a high-impact decision.";
        }

        return "No immediate investigation is required; continue with verification if the decision is consequential.";
    }

    /**
     * Collects human-readable evidence classifications.
     */
    private void collectEvidenceSignals(
            List<InferenceEvidence> evidence,
            ReasoningResult reasoningResult,
            List<String> supporting,
            List<String> contradicting) {

        if (reasoningResult != null) {

            if (reasoningResult.confidence() >= 0.50) {
                supporting.add(
                        "Reasoning conclusion: "
                                + reasoningResult.conclusion()
                );
            } else {
                contradicting.add(
                        "Low reasoning confidence: "
                                + formatScore(reasoningResult.confidence())
                );
            }

            if (reasoningResult.risks() != null) {
                for (String risk : reasoningResult.risks()) {
                    if (risk != null && !risk.isBlank()) {
                        contradicting.add(
                                "Reasoning risk: " + risk
                        );
                    }
                }
            }
        }

        for (InferenceEvidence item : evidence) {

            if (item.relevanceScore() < 0.15) {
                continue;
            }

            if (item.negated()) {
                contradicting.add(
                        item.sourceType()
                                + " evidence indicates a negative or conflicting signal: "
                                + item.content()
                );
            } else if (item.overallScore() >= 0.45) {
                supporting.add(
                        item.sourceType()
                                + " evidence: "
                                + item.content()
                );
            } else {
                contradicting.add(
                        "Low-strength "
                                + item.sourceType().toLowerCase(Locale.ROOT)
                                + " evidence: "
                                + item.content()
                );
            }
        }
    }

    private List<String> buildSupportingEvidence(
            InferenceEvidence primary,
            List<InferenceEvidence> evidence,
            int limit) {

        List<String> result = new ArrayList<>();

        result.add(
                primary.sourceType()
                        + " evidence: "
                        + primary.content()
        );

        for (InferenceEvidence item : evidence) {

            if (item == primary) {
                continue;
            }

            if (item.relevanceScore() >= 0.20
                    && item.overallScore() >= 0.35
                    && !item.negated()) {

                result.add(
                        item.sourceType()
                                + " evidence: "
                                + item.content()
                );
            }

            if (result.size() >= limit) {
                break;
            }
        }

        return result;
    }

    private List<String> buildOpposingEvidence(
            InferenceEvidence primary,
            List<InferenceEvidence> evidence,
            int limit) {

        return findOpposingEvidence(primary, evidence, limit);
    }

    private List<String> findOpposingEvidence(
            InferenceEvidence primary,
            List<InferenceEvidence> evidence,
            int limit) {

        List<String> result = new ArrayList<>();

        for (InferenceEvidence item : evidence) {

            if (item == primary) {
                continue;
            }

            boolean conflictingSource =
                    item.negated() != primary.negated();

            boolean lowQuality =
                    item.overallScore() < 0.30;

            boolean lowRelevance =
                    item.relevanceScore() < 0.15;

            if (conflictingSource || lowQuality || lowRelevance) {

                result.add(
                        item.sourceType()
                                + " counter-signal: "
                                + item.content()
                );
            }

            if (result.size() >= limit) {
                break;
            }
        }

        return result;
    }

    private double weightedEvidenceStrength(
            List<InferenceEvidence> evidence,
            Set<String> requestTerms) {

        if (evidence.isEmpty()) {
            return 0.0;
        }

        double weightedScore = 0.0;
        double totalWeight = 0.0;

        for (InferenceEvidence item : evidence) {

            double relevanceWeight =
                    Math.max(0.05, item.relevanceScore());

            double sourceWeight =
                    switch (item.sourceType()) {
                        case "KNOWLEDGE" -> 1.00;
                        case "REASONING" -> 0.95;
                        case "MEMORY" -> 0.80;
                        default -> 0.60;
                    };

            double score =
                    item.qualityScore() * 0.50
                            + item.relevanceScore() * 0.30
                            + item.freshness() * 0.20;

            weightedScore += score * relevanceWeight * sourceWeight;
            totalWeight += relevanceWeight * sourceWeight;
        }

        return totalWeight == 0.0
                ? 0.0
                : clamp(weightedScore / totalWeight, 0.0, 1.0);
    }

    private double memoryAgreement(
            List<InferenceEvidence> memories) {

        return agreementWithin(memories);
    }

    private double knowledgeAgreement(
            List<InferenceEvidence> knowledge) {

        return agreementWithin(knowledge);
    }

    private double agreementWithin(
            List<InferenceEvidence> evidence) {

        if (evidence.size() < 2) {
            return evidence.isEmpty() ? 0.0 : 0.50;
        }

        double total = 0.0;
        int count = 0;

        for (int i = 0; i < evidence.size(); i++) {

            for (int j = i + 1; j < evidence.size(); j++) {

                total += jaccard(
                        evidence.get(i).terms(),
                        evidence.get(j).terms()
                );

                count++;
            }
        }

        return count == 0
                ? 0.50
                : clamp(total / count, 0.0, 1.0);
    }

    private double calculateAgreement(
            List<InferenceEvidence> evidence) {

        if (evidence.size() < 2) {
            return evidence.isEmpty() ? 0.0 : 0.50;
        }

        double weightedAgreement = 0.0;
        double totalWeight = 0.0;

        for (int i = 0; i < evidence.size(); i++) {

            for (int j = i + 1; j < evidence.size(); j++) {

                InferenceEvidence left = evidence.get(i);
                InferenceEvidence right = evidence.get(j);

                if (left.sourceType().equals(right.sourceType())) {
                    continue;
                }

                double overlap = jaccard(
                        left.terms(),
                        right.terms()
                );

                double weight =
                        Math.max(0.05, left.overallScore())
                                * Math.max(0.05, right.overallScore());

                weightedAgreement += overlap * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight == 0.0) {
            return 0.35;
        }

        return clamp(
                weightedAgreement / totalWeight,
                0.0,
                1.0
        );
    }

    private double calculateDiversity(
            List<InferenceEvidence> evidence) {

        Set<String> sourceTypes = new HashSet<>();

        for (InferenceEvidence item : evidence) {
            sourceTypes.add(item.sourceType());
        }

        if (sourceTypes.size() >= 3) {
            return 1.0;
        }

        if (sourceTypes.size() == 2) {
            return 0.75;
        }

        if (sourceTypes.size() == 1) {
            return evidence.size() >= 2 ? 0.40 : 0.20;
        }

        return 0.0;
    }

    private double lexicalRelevance(
            Set<String> requestTerms,
            Set<String> evidenceTerms) {

        if (requestTerms.isEmpty() || evidenceTerms.isEmpty()) {
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
                (double) overlap / requestTerms.size();

        double precision =
                (double) overlap / evidenceTerms.size();

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

    private double calculateCoverage(
            Set<String> requestTerms,
            List<InferenceEvidence> evidence) {

        if (requestTerms.isEmpty() || evidence.isEmpty()) {
            return 0.0;
        }

        Set<String> coveredTerms = new HashSet<>();

        for (InferenceEvidence item : evidence) {
            coveredTerms.addAll(item.terms());
        }

        int covered = 0;

        for (String term : requestTerms) {
            if (coveredTerms.contains(term)) {
                covered++;
            }
        }

        return clamp(
                (double) covered / requestTerms.size(),
                0.0,
                1.0
        );
    }

    private double contradictionPenalty(
            List<String> supporting,
            List<String> opposing) {

        if (opposing.isEmpty()) {
            return 0.0;
        }

        double penalty =
                Math.min(
                        0.25,
                        opposing.size() * 0.04
                );

        if (supporting.isEmpty()) {
            penalty += 0.10;
        }

        return penalty;
    }

    private double reasoningConfidence(
            ReasoningResult reasoningResult) {

        if (reasoningResult == null) {
            return 0.0;
        }

        return clamp(
                reasoningResult.confidence(),
                0.0,
                1.0
        );
    }

    private String statusFor(double confidence) {

        if (confidence >= 0.65) {
            return "LIKELY";
        }

        if (confidence >= 0.40) {
            return "POSSIBLE";
        }

        if (confidence >= 0.20) {
            return "UNCERTAIN";
        }

        return "UNLIKELY";
    }

    private InferenceEvidence findFirst(
            List<InferenceEvidence> evidence,
            String sourceType) {

        for (InferenceEvidence item : evidence) {
            if (sourceType.equals(item.sourceType())) {
                return item;
            }
        }

        return null;
    }

    private List<InferenceEvidence> filterByType(
            List<InferenceEvidence> evidence,
            String sourceType) {

        List<InferenceEvidence> result = new ArrayList<>();

        for (InferenceEvidence item : evidence) {
            if (sourceType.equals(item.sourceType())) {
                result.add(item);
            }
        }

        result.sort(
                Comparator.comparingDouble(InferenceEvidence::overallScore)
                        .reversed()
                        .thenComparing(InferenceEvidence::stableId)
        );

        return result;
    }

    private Set<String> extractTerms(String text) {

        if (text == null || text.isBlank()) {
            return Set.of();
        }

        Set<String> terms = new LinkedHashSet<>();

        var matcher =
                TOKEN_PATTERN.matcher(
                        text.toLowerCase(Locale.ROOT)
                );

        while (matcher.find()) {

            String token = matcher.group();

            if (token.length() < 2) {
                continue;
            }

            if (STOP_WORDS.contains(token)) {
                continue;
            }

            terms.add(token);
        }

        return terms;
    }

    private boolean containsNegation(Set<String> terms) {

        for (String term : terms) {
            if (NEGATION_TERMS.contains(term)) {
                return true;
            }
        }

        return false;
    }

    private String fingerprint(String text) {

        return String.join(
                " ",
                extractTerms(text)
                        .stream()
                        .sorted()
                        .limit(50)
                        .toList()
        );
    }

    private double freshness(Instant timestamp) {

        if (timestamp == null) {
            return 0.50;
        }

        long days;

        try {
            days = Math.max(
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
            return 1.00;
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
            java.util.Map<String, Object> metadata,
            String key,
            double fallback) {

        if (metadata == null) {
            return fallback;
        }

        Object value = metadata.get(key);

        if (value instanceof Number number) {
            return clamp(
                    number.doubleValue(),
                    0.0,
                    1.0
            );
        }

        if (value instanceof String text) {
            try {
                return clamp(
                        Double.parseDouble(text),
                        0.0,
                        1.0
                );
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }

        return fallback;
    }

    private double jaccard(
            Set<String> left,
            Set<String> right) {

        if (left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection =
                new HashSet<>(left);

        intersection.retainAll(right);

        Set<String> union =
                new HashSet<>(left);

        union.addAll(right);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size()
                / union.size();
    }

    private List<String> deduplicate(
            List<String> values) {

        return new ArrayList<>(
                new LinkedHashSet<>(values)
        );
    }

    private List<String> limit(
            List<String> values,
            int maximum) {

        if (values.size() <= maximum) {
            return List.copyOf(values);
        }

        return List.copyOf(
                values.subList(0, maximum)
        );
    }

    private String truncate(
            String text,
            int maximum) {

        if (text == null) {
            return "";
        }

        if (text.length() <= maximum) {
            return text;
        }

        return text.substring(
                0,
                Math.max(0, maximum - 3)
        ) + "...";
    }

    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double clamp(
            double value,
            double minimum,
            double maximum) {

        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }

    private String formatScore(double value) {

        return String.format(
                Locale.ROOT,
                "%.2f",
                value
        );
    }

    /**
     * Internal immutable representation of inference evidence.
     */
    private record InferenceEvidence(
            String sourceType,
            String stableId,
            String content,
            Set<String> terms,
            double relevanceScore,
            double qualityScore,
            double overallScore,
            double sourceConfidence,
            double importanceOrAuthority,
            double freshness,
            boolean negated) {
    }
}