package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.DiscoveryReason;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeTopic;
import com.shreeai.os.platform.kernels.acquisition.model.RequirementPriority;
import com.shreeai.os.platform.kernels.context.model.ContextIntelligence;
import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.ExperienceLevel;
import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.PrimaryDomain;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <b>DefaultSourceDiscoveryEngine</b>
 *
 * <p>The K0.6.1 implementation of {@link SourceDiscoveryEngine}. Performs a
 * five-stage, fully deterministic discovery pipeline:</p>
 *
 * <ol>
 *   <li><b>Domain Expansion</b> - the detected {@link PrimaryDomain} is
 *       expanded into required knowledge topics via a locked dictionary
 *       (priority {@link RequirementPriority#CRITICAL}).</li>
 *   <li><b>Goal Expansion</b> - goal titles are matched against a locked
 *       keyword dictionary using whole-token matching (priority
 *       {@link RequirementPriority#HIGH}). No NLP, no regex, no ML.</li>
 *   <li><b>Constraint Expansion</b> - only <em>explicit</em> user constraints
 *       are expanded via a locked dictionary (priority
 *       {@link RequirementPriority#MEDIUM}). Hidden constraints are never
 *       inferred.</li>
 *   <li><b>Priority Assignment</b> - locked rules: domain&rarr;CRITICAL,
 *       goal&rarr;HIGH, constraint&rarr;MEDIUM, optional enrichment&rarr;LOW.
 *       No scoring model.</li>
 *   <li><b>Canonicalization</b> - duplicates removed (first/highest-priority
 *       occurrence wins), stable ordering applied (priority descending, then
 *       topic name ascending), deterministic SHA-256 topic ids assigned and
 *       discovery reasons preserved.</li>
 * </ol>
 *
 * <p><b>Locked confidence values:</b></p>
 * <ul>
 *   <li>Domain topics: the {@link DomainProfile#confidence()} when positive,
 *       otherwise the locked fallback {@code 0.75}.</li>
 *   <li>Goal topics: the originating {@link GoalNode#confidence()} when
 *       positive, otherwise the locked fallback {@code 0.75}.</li>
 *   <li>Constraint topics: {@code 1.0} - the constraint was explicitly stated
 *       by the user.</li>
 * </ul>
 *
 * <p>The engine is stateless, thread-safe and deterministic - identical inputs
 * always produce structurally equal requirement sets. No LLM, no embeddings,
 * no probability, no external calls.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.1 Source Discovery</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultSourceDiscoveryEngine implements SourceDiscoveryEngine {

    /** Locked confidence fallback when a detection records zero confidence. */
    public static final double FALLBACK_CONFIDENCE = 0.75;

    /** Locked confidence for topics derived from explicit user constraints. */
    public static final double CONSTRAINT_CONFIDENCE = 1.0;

    /** Locked topic produced for an explicit duration constraint. */
    public static final String TOPIC_ROADMAP = "Roadmap";

    /** Locked topic produced for an explicit budget constraint. */
    public static final String TOPIC_FREE_RESOURCES = "Free Resources";

    /** Locked topic produced for an explicit platform constraint. */
    public static final String TOPIC_PLATFORM_SETUP = "Platform Setup";

    /** Locked optional-enrichment topic for the GENERAL domain. */
    public static final String TOPIC_GENERAL_RESEARCH = "General Research";

    /**
     * Locked domain expansion dictionary: primary domain -&gt; knowledge topics.
     * Built with a {@link LinkedHashMap} so iteration order is stable.
     */
    private static final Map<PrimaryDomain, List<String>> DOMAIN_EXPANSION;

    static {
        Map<PrimaryDomain, List<String>> dict = new LinkedHashMap<>();
        dict.put(PrimaryDomain.JAVA, List.of("Java", "OOP", "Collections"));
        dict.put(PrimaryDomain.SPRING, List.of("Spring", "Spring Boot"));
        dict.put(PrimaryDomain.DATABASE, List.of("SQL", "JDBC"));
        dict.put(PrimaryDomain.AI, List.of("LLM", "RAG"));
        dict.put(PrimaryDomain.WEB, List.of("HTML", "CSS", "JavaScript"));
        dict.put(PrimaryDomain.MOBILE, List.of("Android", "iOS", "Mobile UI"));
        dict.put(PrimaryDomain.DEVOPS, List.of("CI/CD", "Docker", "Kubernetes"));
        dict.put(PrimaryDomain.CLOUD, List.of("Cloud Computing", "AWS", "Azure"));
        dict.put(PrimaryDomain.SECURITY,
                List.of("Authentication", "Encryption", "Secure Coding"));
        dict.put(PrimaryDomain.FINANCE,
                List.of("Accounting", "Financial Analysis", "Banking"));
        dict.put(PrimaryDomain.MEDICAL,
                List.of("Healthcare", "Medical Terminology", "Diagnostics"));
        dict.put(PrimaryDomain.EDUCATION,
                List.of("Curriculum Design", "Learning Theory", "Assessment"));
        DOMAIN_EXPANSION = Map.copyOf(dict);
    }

    /**
     * Locked goal keyword dictionary: keyword -&gt; knowledge topics. Matching is
     * whole-token, case-insensitive and deterministic; multi-word keywords must
     * appear as contiguous token sequences. Built with a {@link LinkedHashMap}
     * so matching (and therefore reason) order is stable.
     */
    private static final Map<String, List<String>> GOAL_KEYWORD_EXPANSION;

    static {
        Map<String, List<String>> dict = new LinkedHashMap<>();
        dict.put("java", List.of("Streams", "JDBC", "Spring"));
        dict.put("spring", List.of("Spring Boot", "REST API"));
        dict.put("developer", List.of("Interview Preparation"));
        dict.put("interview", List.of("Interview Preparation"));
        dict.put("web", List.of("HTML", "CSS", "JavaScript"));
        dict.put("database", List.of("SQL", "JDBC"));
        dict.put("sql", List.of("SQL", "JDBC"));
        dict.put("android", List.of("Android Development", "Mobile UI"));
        dict.put("mobile", List.of("Android Development", "Mobile UI"));
        dict.put("machine learning", List.of("Model Training", "Prompt Engineering"));
        dict.put("llm", List.of("Prompt Engineering", "RAG"));
        dict.put("ai", List.of("Prompt Engineering", "RAG"));
        dict.put("cloud", List.of("Cloud Deployment"));
        dict.put("devops", List.of("CI/CD", "Docker"));
        GOAL_KEYWORD_EXPANSION = Map.copyOf(dict);
    }

    @Override
    public KnowledgeRequirementSet discover(ContextIntelligence contextIntelligence) {
        Objects.requireNonNull(contextIntelligence,
                "contextIntelligence must not be null");

        // Ordered accumulator; first discovery wins on duplicates because the
        // stages run in priority order: domain -> goal -> constraint.
        Map<String, KnowledgeTopic> byName = new LinkedHashMap<>();
        List<DiscoveryReason> reasons = new ArrayList<>();
        Set<String> seenReasons = new LinkedHashSet<>();

        expandDomain(contextIntelligence.domainProfile(), byName, reasons, seenReasons);
        expandGoals(contextIntelligence.goals(), byName, reasons, seenReasons);
        expandConstraints(contextIntelligence.constraints(), byName, reasons, seenReasons);

        return canonicalize(byName, reasons);
    }

    // ---- Stage 1: domain expansion ------------------------------------------

    /**
     * Expands the detected primary domain into CRITICAL knowledge topics via
     * the locked dictionary. The GENERAL domain yields a single optional
     * enrichment topic at LOW priority; UNKNOWN yields nothing (no domain was
     * confidently detected, so no domain knowledge is required).
     */
    private static void expandDomain(DomainProfile domainProfile,
                                     Map<String, KnowledgeTopic> byName,
                                     List<DiscoveryReason> reasons,
                                     Set<String> seenReasons) {
        if (domainProfile == null) {
            return;
        }
        PrimaryDomain domain = domainProfile.primaryDomain();
        if (domain == null) {
            return;
        }
        double confidence = domainProfile.confidence() > 0.0
                ? domainProfile.confidence()
                : FALLBACK_CONFIDENCE;

        if (domain == PrimaryDomain.UNKNOWN) {
            return;
        }
        if (domain == PrimaryDomain.GENERAL) {
            // Locked optional-enrichment rule: GENERAL domain knowledge is
            // nice-to-have, never critical.
            addTopic(TOPIC_GENERAL_RESEARCH, RequirementPriority.LOW,
                    confidence, domain.name(), DiscoveryReason.SOURCE_DOMAIN_PROFILE,
                    byName, reasons, seenReasons);
            return;
        }
        List<String> topics = DOMAIN_EXPANSION.getOrDefault(domain, List.of());
        for (String topicName : topics) {
            addTopic(topicName, RequirementPriority.CRITICAL,
                    confidence, domain.name(), DiscoveryReason.SOURCE_DOMAIN_PROFILE,
                    byName, reasons, seenReasons);
        }
    }

    // ---- Stage 2: goal expansion ----------------------------------------------

    /**
     * Expands goal titles (primary goal first, then sub-goals in order) into
     * HIGH knowledge topics using the locked whole-token keyword dictionary.
     */
    private static void expandGoals(GoalStructure goals,
                                    Map<String, KnowledgeTopic> byName,
                                    List<DiscoveryReason> reasons,
                                    Set<String> seenReasons) {
        if (goals == null) {
            return;
        }
        if (goals.primaryGoal() != null) {
            expandGoalNode(goals.primaryGoal(), byName, reasons, seenReasons);
        }
        for (GoalNode subGoal : goals.subGoals()) {
            if (subGoal != null) {
                expandGoalNode(subGoal, byName, reasons, seenReasons);
            }
        }
    }

    /**
     * Expands a single goal node: matches its title against the locked keyword
     * dictionary and records the goal title as the discovery evidence.
     */
    private static void expandGoalNode(GoalNode goalNode,
                                       Map<String, KnowledgeTopic> byName,
                                       List<DiscoveryReason> reasons,
                                       Set<String> seenReasons) {
        String title = goalNode.title();
        if (title == null || title.isBlank()) {
            return;
        }
        double confidence = goalNode.confidence() > 0.0
                ? goalNode.confidence()
                : FALLBACK_CONFIDENCE;
        List<String> tokens = tokenize(title);
        for (Map.Entry<String, List<String>> entry : GOAL_KEYWORD_EXPANSION.entrySet()) {
            if (containsTokenSequence(tokens, entry.getKey())) {
                for (String topicName : entry.getValue()) {
                    addTopic(topicName, RequirementPriority.HIGH,
                            confidence, title, DiscoveryReason.SOURCE_GOAL_STRUCTURE,
                            byName, reasons, seenReasons);
                }
            }
        }
    }

    /**
     * Deterministic whole-token containment: {@code true} when the keyword
     * (lowercased, itself tokenized) appears as a contiguous token sequence in
     * the token list. Pure list comparison - no regex, no NLP.
     */
    private static boolean containsTokenSequence(List<String> tokens, String keyword) {
        List<String> keywordTokens = tokenize(keyword);
        if (keywordTokens.isEmpty() || keywordTokens.size() > tokens.size()) {
            return false;
        }
        for (int start = 0; start <= tokens.size() - keywordTokens.size(); start++) {
            boolean matched = true;
            for (int offset = 0; offset < keywordTokens.size(); offset++) {
                if (!tokens.get(start + offset).equals(keywordTokens.get(offset))) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lowercases and splits text into alphanumeric tokens. Letters and digits
     * are token characters; every other character is a separator (so
     * {@code "CI/CD"} in a title tokenizes as {@code ci}, {@code cd}).
     */
    private static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean tokenChar = Character.isLetterOrDigit(c);
            if (tokenChar) {
                current.append(Character.toLowerCase(c));
            } else if (current.length() > 0) {
                tokens.add(current.toString());
                current.setLength(0);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    // ---- Stage 3: constraint expansion ---------------------------------------

    /**
     * Expands only <em>explicit</em> user constraints into MEDIUM knowledge
     * topics via the locked dictionary. Missing constraints are never invented
     * and hidden constraints are never inferred. Language and output-format
     * preferences shape <em>delivery</em>, not required knowledge, and are
     * therefore deliberately not expanded.
     */
    private static void expandConstraints(UserConstraints constraints,
                                          Map<String, KnowledgeTopic> byName,
                                          List<DiscoveryReason> reasons,
                                          Set<String> seenReasons) {
        if (constraints == null) {
            return;
        }
        if (constraints.duration() != null && !constraints.duration().isBlank()) {
            addTopic(TOPIC_ROADMAP, RequirementPriority.MEDIUM,
                    CONSTRAINT_CONFIDENCE, constraints.duration(),
                    DiscoveryReason.SOURCE_USER_CONSTRAINTS, byName, reasons, seenReasons);
        }
        if (constraints.experience() != null) {
            String topic = switch (constraints.experience()) {
                case BEGINNER -> "Fundamentals";
                case INTERMEDIATE -> "Best Practices";
                case ADVANCED -> "Advanced Concepts";
                case EXPERT -> "Expert Techniques";
            };
            addTopic(topic, RequirementPriority.MEDIUM,
                    CONSTRAINT_CONFIDENCE, constraints.experience().name(),
                    DiscoveryReason.SOURCE_USER_CONSTRAINTS, byName, reasons, seenReasons);
        }
        if (constraints.budget() != null && !constraints.budget().isBlank()) {
            addTopic(TOPIC_FREE_RESOURCES, RequirementPriority.MEDIUM,
                    CONSTRAINT_CONFIDENCE, constraints.budget(),
                    DiscoveryReason.SOURCE_USER_CONSTRAINTS, byName, reasons, seenReasons);
        }
        if (constraints.platform() != null) {
            addTopic(TOPIC_PLATFORM_SETUP, RequirementPriority.MEDIUM,
                    CONSTRAINT_CONFIDENCE, constraints.platform().name(),
                    DiscoveryReason.SOURCE_USER_CONSTRAINTS, byName, reasons, seenReasons);
        }
    }

    // ---- Stage 5: canonicalization -------------------------------------------

    /**
     * Builds the canonical {@link KnowledgeRequirementSet}: stable ordering
     * (priority descending CRITICAL&rarr;LOW, then topic name ascending),
     * deterministic SHA-256 topic ids, and discovery reasons preserved in
     * discovery order (exact duplicates removed).
     */
    private static KnowledgeRequirementSet canonicalize(Map<String, KnowledgeTopic> byName,
                                                        List<DiscoveryReason> reasons) {
        List<KnowledgeTopic> ordered = new ArrayList<>(byName.values());
        ordered.sort((a, b) -> {
            int byPriority = Integer.compare(priorityRank(a.priority()),
                    priorityRank(b.priority()));
            if (byPriority != 0) {
                return byPriority;
            }
            return a.name().compareTo(b.name());
        });
        List<KnowledgeTopic> canonical = new ArrayList<>(ordered.size());
        for (KnowledgeTopic topic : ordered) {
            canonical.add(new KnowledgeTopic(
                    KnowledgeTopic.deterministicTopicId(topic.name()),
                    topic.name(), topic.priority(), topic.confidence()));
        }
        return new KnowledgeRequirementSet(canonical, List.copyOf(reasons));
    }

    /** Locked priority ranking: CRITICAL sorts before HIGH before MEDIUM before LOW. */
    private static int priorityRank(RequirementPriority priority) {
        return switch (priority) {
            case CRITICAL -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        };
    }

    // ---- shared helpers ------------------------------------------------------

    /**
     * Registers one discovered topic and its discovery reason. The first
     * occurrence of a topic name wins (stages run in priority order, so the
     * highest-priority source naturally dominates); exact duplicate reasons are
     * dropped.
     */
    private static void addTopic(String topicName,
                                 RequirementPriority priority,
                                 double confidence,
                                 String evidence,
                                 String sourceArtifact,
                                 Map<String, KnowledgeTopic> byName,
                                 List<DiscoveryReason> reasons,
                                 Set<String> seenReasons) {
        String key = topicName.toLowerCase(java.util.Locale.ROOT);
        byName.putIfAbsent(key, new KnowledgeTopic(
                KnowledgeTopic.deterministicTopicId(topicName),
                topicName, priority, confidence));
        DiscoveryReason reason = new DiscoveryReason(topicName, evidence, sourceArtifact);
        if (seenReasons.add(reason.topicName() + "|" + reason.evidence()
                + "|" + reason.sourceArtifact())) {
            reasons.add(reason);
        }
    }
}
