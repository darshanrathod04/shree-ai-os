package com.shreeai.os.platform.runtime.orchestration;

import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.IntentType;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.KernelType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <b>IntentAnalyzer</b>
 *
 * <p>Deterministic, LLM-free multi-intent analyzer that extracts primary intent,
 * secondary intents, required kernels, confidence, and entities from a user
 * request string.</p>
 *
 * <p>Detection is purely keyword/pattern-based. The analyzer does not call any
 * LLM or external service. It runs offline and deterministically in all
 * environments.</p>
 *
 * <p><b>Supported patterns:</b></p>
 * <ul>
 *   <li>MEMORY_STORE — "remember this", "store", "note", "save this"</li>
 *   <li>MEMORY_RECALL — "what do you remember", "recall", "what did i say"</li>
 *   <li>PLANNING — "roadmap", "plan", "create a", "build a", "architecture"</li>
 *   <li>KNOWLEDGE — "what is", "explain", "how does", "tell me about"</li>
 *   <li>EXECUTION — "execute", "run", "deploy", "build it"</li>
 * </ul>
 *
 * @since Sprint-12
 */
public final class IntentAnalyzer {

    // ─────────────────────────────────────────────────────────────────────────
    // Keyword patterns
    // ─────────────────────────────────────────────────────────────────────────

    private static final List<Pattern> MEMORY_STORE_PATTERNS = List.of(
            Pattern.compile("(?i)\\bremember\\b.*"),
            Pattern.compile("(?i)\\bstor(e|ing)\\b"),
            Pattern.compile("(?i)\\bkeep in mind\\b"),
            Pattern.compile("(?i)\\bnote that\\b"),
            Pattern.compile("(?i)\\bsave this\\b"),
            Pattern.compile("(?i)\\bdon'?t forget\\b"),
            Pattern.compile("(?i)\\btrack (that|this)\\b")
    );

    private static final List<Pattern> MEMORY_RECALL_PATTERNS = List.of(
            Pattern.compile("(?i)\\brecall\\b"),
            Pattern.compile("(?i)\\bwhat do you remember\\b"),
            Pattern.compile("(?i)\\bwhat did i (say|tell|mention)\\b"),
            Pattern.compile("(?i)\\bdo you remember\\b"),
            Pattern.compile("(?i)\\bpast (information|data|context)\\b")
    );

    private static final List<Pattern> PLANNING_PATTERNS = List.of(
            Pattern.compile("(?i)\\broadmap\\b"),
            Pattern.compile("(?i)\\bplan\\b"),
            Pattern.compile("(?i)\\bcreate a.*roadmap\\b"),
            Pattern.compile("(?i)\\bbuild a.*(plan|roadmap|project)\\b"),
            Pattern.compile("(?i)\\bgenerate a.*(plan|roadmap)\\b"),
            Pattern.compile("(?i)\\barchitect(ure|ing)?\\b"),
            Pattern.compile("(?i)\\bphases?\\b"),
            Pattern.compile("(?i)\\bimplementation\\s+steps?\\b"),
            Pattern.compile("(?i)\\bexecution\\s+plan\\b"),
            Pattern.compile("(?i)\\bmilestones?\\b"),
            // Catches "build/design/create [something] for [domain]" — common planning phrasing
            Pattern.compile("(?i)\\bbuild (an?|the)?\\s+(ai|app|application|system|tool|assistant|service|platform|solution|chatbot|model|api)\\b"),
            Pattern.compile("(?i)\\bdesign (an?|the)?\\s+(ai|app|application|system|tool|assistant|service|platform|solution)\\b"),
            Pattern.compile("(?i)\\bcreate (an?|the)?\\s+(ai|app|application|system|tool|assistant|service|platform|solution)\\b")
    );

    private static final List<Pattern> KNOWLEDGE_PATTERNS = List.of(
            Pattern.compile("(?i)\\bwhat is\\b"),
            Pattern.compile("(?i)\\bwhat are\\b"),
            Pattern.compile("(?i)\\bhow does\\b"),
            Pattern.compile("(?i)\\bhow do\\b"),
            Pattern.compile("(?i)\\bexplain\\b"),
            Pattern.compile("(?i)\\bdescribe\\b"),
            Pattern.compile("(?i)\\btell me about\\b"),
            Pattern.compile("(?i)\\bdefine\\b"),
            Pattern.compile("(?i)\\bclarify\\b"),
            Pattern.compile("(?i)\\bwhat'?s a\\b")
    );

    private static final List<Pattern> EXECUTION_PATTERNS = List.of(
            Pattern.compile("(?i)\\bexecute\\b"),
            Pattern.compile("(?i)\\brun (it|the|this)\\b"),
            Pattern.compile("(?i)\\bdeploy\\b"),
            Pattern.compile("(?i)\\bbuild it\\b"),
            Pattern.compile("(?i)\\bimplement it\\b"),
            Pattern.compile("(?i)\\bstart (the )?(project|task|implementation)\\b")
    );

    private static final List<Pattern> REFLECTION_PATTERNS = List.of(
            Pattern.compile("(?i)\\bwhat went well\\b"),
            Pattern.compile("(?i)\\bwhat could be better\\b"),
            Pattern.compile("(?i)\\breflect(ion)?\\b"),
            Pattern.compile("(?i)\\blearnings?\\b")
    );

    // Sprint-14: Developer Agent patterns
    private static final List<Pattern> DEVELOPER_PATTERNS = List.of(
            // Feature additions
            Pattern.compile("(?i)\\badd (jwt|feature|user|product)\\b"),
            Pattern.compile("(?i)\\bimplement\\s+"),
            // Refactoring
            Pattern.compile("(?i)\\brefactor(ing|ed)?\\b"),
            Pattern.compile("(?i)\\brename\\s+"),
            Pattern.compile("(?i)\\bextract\\s+(service|layer)\\b"),
            // Bug fixes
            Pattern.compile("(?i)\\bfix(ed|ing)?\\b"),
            Pattern.compile("(?i)\\bpatch\\b"),
            Pattern.compile("(?i)\\bhotfix\\b"),
            // Optimization
            Pattern.compile("(?i)\\boptimi[sz](e[sd]?|ing)?\\b"),
            Pattern.compile("(?i)\\bperformance\\b"),
            Pattern.compile("(?i)\\bcach(e[sd]?|ing)\\b"),
            // API creation
            Pattern.compile("(?i)\\bcreate\\s+(a\\s+)?(rest|api|endpoint)\\b"),
            Pattern.compile("(?i)\\badd\\s+(a\\s+)?(rest|api|endpoint)\\b"),
            // Entity creation
            Pattern.compile("(?i)\\badd\\s+(a\\s+)?entity\\b"),
            Pattern.compile("(?i)\\bnew\\s+model\\b"),
            // Security
            Pattern.compile("(?i)\\bjwt\\b"),
            Pattern.compile("(?i)\\boauth\\b"),
            Pattern.compile("(?i)\\bauthentication\\b"),
            Pattern.compile("(?i)\\bauthorization\\b"),
            Pattern.compile("(?i)\\bsecure\\b"),
            // Database
            Pattern.compile("(?i)\\bmigration\\b"),
            Pattern.compile("(?i)\\brepository\\b"),
            // Direct developer keywords
            Pattern.compile("(?i)\\bclass\\b"),
            Pattern.compile("(?i)\\binterface\\b"),
            Pattern.compile("(?i)\\bmethod\\b"),
            Pattern.compile("(?i)\\brefactor(ing)?\\b"),
            Pattern.compile("(?i)\\bendpoint\\b"),
            Pattern.compile("(?i)\\bapi\\b"),
            Pattern.compile("(?i)\\bservice\\b"),
            Pattern.compile("(?i)\\brepository\\b")
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Domain entity extractors
    // ─────────────────────────────────────────────────────────────────────────

    private static final Pattern DOMAIN_JAVA = Pattern.compile("(?i)\\bjava\\b");
    private static final Pattern DOMAIN_AI = Pattern.compile("(?i)\\b(ai|ml|machine learning|llm|gpt|neural|nlp)\\b");
    private static final Pattern DOMAIN_SAAS = Pattern.compile("(?i)\\b(saas|subscription|multi.?tenant)\\b");
    private static final Pattern DOMAIN_FITNESS = Pattern.compile("(?i)\\b(fitness|workout|gym|exercise|training)\\b");
    private static final Pattern DOMAIN_EDUCATION = Pattern.compile("(?i)\\b(education|learning|course|student|school)\\b");
    private static final Pattern DOMAIN_HEALTHCARE = Pattern.compile("(?i)\\b(health|medical|doctor|hipaa|patient)\\b");
    private static final Pattern DOMAIN_ECOMMERCE = Pattern.compile("(?i)\\b(e.?commerce|shop|order|cart|payment)\\b");
    private static final Pattern DOMAIN_FINANCE = Pattern.compile("(?i)\\b(finance|banking|trading|investment|stock)\\b");

    // ─────────────────────────────────────────────────────────────────────────
    // Analysis
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Analyzes the given input string and returns intent analysis result.
     *
     * @param input the raw user input (must not be null)
     * @return the intent analysis result (never null)
     */
    public IntentAnalysisResult analyze(String input) {
        if (input == null || input.isBlank()) {
            return IntentAnalysisResult.builder()
                    .primaryIntent(IntentType.CHAT)
                    .confidence(0.0)
                    .originalInput("")
                    .build();
        }

        String normalized = input.trim();
        double highestConfidence = 0.0;
        IntentType primaryIntent = IntentType.CHAT;

        // Score each intent category
        double storeScore = score(normalized, MEMORY_STORE_PATTERNS);
        double recallScore = score(normalized, MEMORY_RECALL_PATTERNS);
        double planningScore = score(normalized, PLANNING_PATTERNS);
        double knowledgeScore = score(normalized, KNOWLEDGE_PATTERNS);
        double executionScore = score(normalized, EXECUTION_PATTERNS);
        double developerScore = score(normalized, DEVELOPER_PATTERNS); // Sprint-14

        // Determine primary intent (highest score wins).
        // If no intent is detected (all scores = 0), fall back to CHAT.
        if (storeScore == 0.0 && recallScore == 0.0 && planningScore == 0.0
                && knowledgeScore == 0.0 && executionScore == 0.0 && developerScore == 0.0) {
            primaryIntent = IntentType.CHAT;
            highestConfidence = 0.0;
        } else {
            // When KNOWLEDGE is present alongside PLANNING, prefer KNOWLEDGE
            // as the primary intent — the user is asking a foundational question
            // and the planning happens around it.
            if (knowledgeScore > 0.0 && planningScore > 0.0
                    && knowledgeScore >= planningScore * 0.8) {
                primaryIntent = IntentType.KNOWLEDGE_QUERY;
                highestConfidence = knowledgeScore;
            } else {
                highestConfidence = storeScore;
                primaryIntent = IntentType.MEMORY_STORE;

                if (recallScore > highestConfidence) {
                    highestConfidence = recallScore;
                    primaryIntent = IntentType.MEMORY_RECALL;
                }
                if (planningScore > highestConfidence) {
                    highestConfidence = planningScore;
                    primaryIntent = IntentType.PLANNING;
                }
                if (knowledgeScore > highestConfidence) {
                    highestConfidence = knowledgeScore;
                    primaryIntent = IntentType.KNOWLEDGE_QUERY;
                }
                if (executionScore > highestConfidence) {
                    highestConfidence = executionScore;
                    primaryIntent = IntentType.EXECUTION;
                }
                // Sprint-14: Developer Agent — lower priority than other intents
                if (developerScore > highestConfidence) {
                    highestConfidence = developerScore;
                    primaryIntent = IntentType.DEVELOPER;
                }
            }
        }

        // Build secondary intents
        List<IntentType> secondaryIntents = buildSecondaryIntents(
                normalized, primaryIntent, storeScore, recallScore,
                planningScore, knowledgeScore, executionScore, developerScore
        );

        // Build required kernels
        List<KernelType> requiredKernels = buildRequiredKernels(
                primaryIntent, secondaryIntents
        );

        // Extract entities
        Map<String, String> entities = extractEntities(normalized);

        return IntentAnalysisResult.builder()
                .primaryIntent(primaryIntent)
                .secondaryIntents(secondaryIntents)
                .requiredKernels(requiredKernels)
                .confidence(Math.round(highestConfidence * 100.0) / 100.0)
                .entities(entities)
                .originalInput(input)
                .build();
    }

    /**
     * Scores an input against a list of patterns.
     *
     * @param input    the normalized input
     * @param patterns the patterns to match
     * @return confidence score in [0.0, 1.0]
     */
    private double score(String input, List<Pattern> patterns) {
        int matchCount = 0;
        for (Pattern pattern : patterns) {
            if (pattern.matcher(input).find()) {
                matchCount++;
            }
        }
        if (matchCount == 0) {
            return 0.0;
        }
        // First strong match = 0.9, each additional = +0.05, cap at 1.0
        return Math.min(0.9 + (matchCount - 1) * 0.05, 1.0);
    }

    /**
     * Builds the list of secondary intents.
     */
    private List<IntentType> buildSecondaryIntents(
            String input,
            IntentType primary,
            double storeScore,
            double recallScore,
            double planningScore,
            double knowledgeScore,
            double executionScore,
            double developerScore
    ) {
        List<IntentType> secondaries = new ArrayList<>();
        double threshold = 0.3;

        // If primary is MEMORY_STORE and PLANNING keywords present → secondary PLANNING
        if (primary == IntentType.MEMORY_STORE && planningScore >= threshold) {
            secondaries.add(IntentType.PLANNING);
        }

        // If primary is PLANNING and MEMORY_STORE keywords present → secondary MEMORY_STORE
        if (primary == IntentType.PLANNING && storeScore >= threshold) {
            secondaries.add(IntentType.MEMORY_STORE);
        }

        // If primary is PLANNING and KNOWLEDGE keywords present → secondary KNOWLEDGE
        if (primary == IntentType.PLANNING && knowledgeScore >= threshold) {
            secondaries.add(IntentType.KNOWLEDGE_QUERY);
        }

        // If primary is KNOWLEDGE and PLANNING keywords present → secondary PLANNING
        if ((primary == IntentType.KNOWLEDGE_QUERY || primary == IntentType.KNOWLEDGE_SEARCH)
                && planningScore >= threshold) {
            secondaries.add(IntentType.PLANNING);
        }

        // If primary is EXECUTION → always secondary REFLECTION
        if (primary == IntentType.EXECUTION) {
            secondaries.add(IntentType.REFLECTION);
        }

        // Generic knowledge + planning cross-detection
        if (storeScore >= threshold && knowledgeScore >= threshold
                && primary != IntentType.MEMORY_STORE && primary != IntentType.KNOWLEDGE_QUERY) {
            if (!secondaries.contains(IntentType.MEMORY_STORE)) {
                secondaries.add(IntentType.MEMORY_STORE);
            }
            if (!secondaries.contains(IntentType.KNOWLEDGE_QUERY)) {
                secondaries.add(IntentType.KNOWLEDGE_QUERY);
            }
        }

        return secondaries;
    }

    /**
     * Maps intent types to their owning kernel types.
     */
    private List<KernelType> buildRequiredKernels(
            IntentType primary,
            List<IntentType> secondaries
    ) {
        List<KernelType> kernels = new ArrayList<>();
        kernels.add(kernelFor(primary));
        for (IntentType secondary : secondaries) {
            KernelType kernel = kernelFor(secondary);
            if (!kernels.contains(kernel)) {
                kernels.add(kernel);
            }
        }
        return kernels;
    }

    private static KernelType kernelFor(IntentType intent) {
        return switch (intent) {
            case MEMORY_STORE, MEMORY_RECALL -> KernelType.MEMORY;
            case PLANNING -> KernelType.PLANNING;
            case KNOWLEDGE_QUERY, KNOWLEDGE_SEARCH -> KernelType.KNOWLEDGE;
            case EXECUTION -> KernelType.EXECUTION;
            case REFLECTION -> KernelType.REFLECTION;
            case DEVELOPER -> KernelType.DEVELOPER; // Sprint-14
            case CHAT -> KernelType.CHIEF;
        };
    }

    /**
     * Extracts domain entities from the input.
     */
    private Map<String, String> extractEntities(String input) {
        Map<String, String> entities = new LinkedHashMap<>();

        if (DOMAIN_JAVA.matcher(input).find()) {
            entities.put("domain", "JAVA");
        } else if (DOMAIN_AI.matcher(input).find()) {
            entities.put("domain", "AI");
        } else if (DOMAIN_SAAS.matcher(input).find()) {
            entities.put("domain", "SAAS");
        } else if (DOMAIN_FITNESS.matcher(input).find()) {
            entities.put("domain", "FITNESS");
        } else if (DOMAIN_EDUCATION.matcher(input).find()) {
            entities.put("domain", "EDUCATION");
        } else if (DOMAIN_HEALTHCARE.matcher(input).find()) {
            entities.put("domain", "HEALTHCARE");
        } else if (DOMAIN_ECOMMERCE.matcher(input).find()) {
            entities.put("domain", "ECOMMERCE");
        } else if (DOMAIN_FINANCE.matcher(input).find()) {
            entities.put("domain", "FINANCE");
        }

        return entities;
    }
}
