 package platform.cognition.uqc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal Query Classifier (UQC) — SHADOW MODE.
 *
 * Responsibilities:
 * - Normalize input
 * - Extract simple entities (deterministic, no LLM)
 * - Predict query category
 * - Predict intent
 * - Calculate confidence
 * - Return immutable ClassificationResult
 *
 * CRITICAL:
 * - This is SHADOW MODE only — it NEVER changes production routing
 * - It NEVER calls Ollama or any LLM
 * - It NEVER modifies session state
 * - It ONLY observes, classifies, compares, and logs
 *
 * Performance target: < 5ms per classification
 */
@Component
public class UniversalQueryClassifier {

    private static final Logger log = LoggerFactory.getLogger(UniversalQueryClassifier.class);

    // Known courses for entity extraction
    private static final String[] KNOWN_COURSES = {
            "java", "spring boot", "springboot", "spring", "dsa", "python",
            "javascript", "react", "angular", "docker", "kubernetes", "aws",
            "sql", "git", "microservices", "rest api", "rest", "design patterns",
            "system design", "node.js", "nodejs", "typescript", "go", "golang",
            "rust", "c++", "cpp", "csharp", "php", "ruby", "swift", "kotlin"
    };

    // Known technologies for entity extraction
    private static final String[] KNOWN_TECHNOLOGIES = {
            "jvm", "jdk", "gradle", "maven", "tomcat", "nginx", "postgresql",
            "mysql", "mongodb", "redis", "kafka", "rabbitmq", "graphql",
            "rest api", "soap", "xml", "json", "yaml", "docker", "kubernetes",
            "jenkins", "git", "github", "gitlab", "aws", "azure", "gcp"
    };

    // Known topics/concepts
    private static final String[] KNOWN_TOPICS = {
            "oop", "object oriented programming", "polymorphism", "inheritance",
            "encapsulation", "abstraction", "recursion", "binary tree",
            "linked list", "array", "sorting", "searching", "graph", "dp",
            "dynamic programming", "greedy", "divide and conquer", "hashing",
            "stack", "queue", "heap", "tree", "avl", "red black", "b tree",
            "spring", "spring boot", "hibernate", "jpa", "jdbc", "servlet",
            "jsp", "mvc", "rest", "soap", "microservices", "monolith",
            "docker", "kubernetes", "ci/cd", "devops", "agile", "scrum"
    };

    public UniversalQueryClassifier() {
        log.info("[UQC] UniversalQueryClassifier initialized (SHADOW MODE)");
    }

    /**
     * Classify user input in SHADOW MODE.
     * This method NEVER changes production behavior.
     * It only observes and logs.
     *
     * @param input the raw user input
     * @return ClassificationResult with predicted intent, category, entities, confidence
     */
    public ClassificationResult classify(String input) {
        long startTime = System.nanoTime();

        if (input == null || input.isBlank()) {
            String safeInput = input != null ? input : "";
            return new ClassificationResult(
                    safeInput, "", "DEFAULT", QueryCategory.UNKNOWN,
                    List.of(), 0.0, List.of(),
                    "Empty input", System.nanoTime() - startTime
            );
        }

        // 1. Normalize
        String normalized = normalize(input);

        // 2. Extract entities
        List<DetectedEntity> entities = extractEntities(normalized);

        // 3. Predict category
        QueryCategory category = predictCategory(normalized, entities);

        // 4. Predict intent
        IntentConfidence intentConf = predictIntent(normalized, category, entities);

        // 5. Calculate confidence
        double confidence = calculateConfidence(normalized, category, intentConf, entities);

        // 6. Build matched rules list
        List<String> matchedRules = new ArrayList<>();
        matchedRules.add("category:" + category);
        matchedRules.add("intent:" + intentConf.getMatchedRule());
        if (!entities.isEmpty()) {
            matchedRules.add("entities:" + entities.size());
        }

        long processingTime = System.nanoTime() - startTime;

        // 7. Build reason
        String reason = buildReason(category, intentConf, entities);

        return new ClassificationResult(
                input, normalized, intentConf.getIntent(), category,
                entities, confidence, matchedRules, reason, processingTime
        );
    }

    /**
     * Normalize input: lowercase, trim, collapse spaces.
     * No LLM, no external calls.
     */
    private String normalize(String input) {
        return input.toLowerCase()
                .trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Extract entities using deterministic rules only.
     * NO LLM.
     */
    private List<DetectedEntity> extractEntities(String normalized) {
        List<DetectedEntity> entities = new ArrayList<>();

        // Check for courses
        for (String course : KNOWN_COURSES) {
            if (normalized.contains(course)) {
                entities.add(new DetectedEntity(
                        DetectedEntity.EntityType.COURSE,
                        course,
                        course,
                        0.95
                ));
                break; // One course per query
            }
        }

        // Check for technologies
        for (String tech : KNOWN_TECHNOLOGIES) {
            if (normalized.contains(tech)) {
                entities.add(new DetectedEntity(
                        DetectedEntity.EntityType.TECHNOLOGY,
                        tech,
                        tech,
                        0.90
                ));
            }
        }

        // Check for topics
        for (String topic : KNOWN_TOPICS) {
            if (normalized.contains(topic)) {
                entities.add(new DetectedEntity(
                        DetectedEntity.EntityType.TOPIC,
                        topic,
                        topic,
                        0.85
                ));
                break; // One topic per query
            }
        }

        // Check for commands
        String[] commands = {"continue", "next", "repeat", "back", "previous", "exit", "stop", "finish"};
        for (String cmd : commands) {
            if (normalized.equals(cmd) || normalized.startsWith(cmd + " ")) {
                entities.add(new DetectedEntity(
                        DetectedEntity.EntityType.COMMAND,
                        cmd,
                        cmd,
                        0.95
                ));
                break;
            }
        }

        // Check for actions
        String[] actions = {"teach", "learn", "quiz", "plan", "roadmap", "explain", "debug", "write", "create"};
        for (String action : actions) {
            if (normalized.startsWith(action + " ") || normalized.equals(action)) {
                entities.add(new DetectedEntity(
                        DetectedEntity.EntityType.ACTION,
                        action,
                        action,
                        0.90
                ));
                break;
            }
        }

        return entities;
    }

    /**
     * Predict query category based on keywords and entities.
     */
    private QueryCategory predictCategory(String normalized, List<DetectedEntity> entities) {
        // Check for multi-action
        if (normalized.contains(" and then ") || normalized.contains(" and ") ||
            normalized.contains(" then ") || normalized.contains(" after that ")) {
            return QueryCategory.MULTI_ACTION;
        }

        // Check for greetings
        if (normalized.matches("^(hello|hi|hey|hii|heyy|good morning|good evening|good afternoon)$")) {
            return QueryCategory.GREETING;
        }

        // Check for acknowledgments
        if (normalized.matches("^(ok|okay|k|kk|thanks|thank you|ty|yes|no|got it|understood|good|nice|great|awesome|cool)$")) {
            return QueryCategory.ACKNOWLEDGMENT;
        }

        // Check for exit
        if (normalized.matches("^(exit|quit|stop|bye|goodbye|cya|leave)$")) {
            return QueryCategory.EXIT;
        }

        // Check for identity
        if (normalized.contains("who am i") || normalized.contains("what is my name") ||
            normalized.contains("mera naam")) {
            return QueryCategory.IDENTITY;
        }

        // Check for memory
        if (normalized.contains("remember") || normalized.contains("what did i say") ||
            normalized.contains("recall")) {
            return QueryCategory.MEMORY;
        }

        // Check for system
        if (normalized.contains("what time") || normalized.contains("weather") ||
            normalized.contains("what can you do") || normalized.contains("help")) {
            return QueryCategory.SYSTEM;
        }

        // Check for roadmap/planning
        if (normalized.contains("roadmap") || normalized.contains("plan") ||
            normalized.contains("career path") || normalized.contains("become a")) {
            return QueryCategory.ROADMAP;
        }

        // Check for quiz
        if (normalized.contains("quiz") || normalized.contains("test me") ||
            normalized.contains("exam")) {
            return QueryCategory.QUIZ;
        }

        // Check for debugging
        if (normalized.contains("debug") || normalized.contains("error") ||
            normalized.contains("crash") || normalized.contains("not working") ||
            normalized.contains("fix") || normalized.contains("issue")) {
            return QueryCategory.DEBUGGING;
        }

        // Check for coding
        if (normalized.contains("write") || normalized.contains("create") ||
            normalized.contains("implement") || normalized.contains("code") ||
            normalized.contains("program")) {
            return QueryCategory.CODING;
        }

        // Check for learning
        if (normalized.contains("teach") || normalized.contains("learn") ||
            normalized.contains("start course") || normalized.contains("lesson") ||
            normalized.contains("explain") || normalized.contains("what is") ||
            normalized.contains("what's") || normalized.contains("tell me about")) {
            return QueryCategory.LEARNING;
        }

        // Check for programming (has course/tech entity)
        boolean hasProgrammingEntity = entities.stream()
                .anyMatch(e -> e.getType() == DetectedEntity.EntityType.COURSE ||
                              e.getType() == DetectedEntity.EntityType.TECHNOLOGY ||
                              e.getType() == DetectedEntity.EntityType.TOPIC);
        if (hasProgrammingEntity) {
            return QueryCategory.PROGRAMMING;
        }

        // Check for comparison
        if (normalized.contains(" vs ") || normalized.contains(" versus ") ||
            normalized.contains(" difference between") || normalized.contains("compare")) {
            return QueryCategory.COMPARISON;
        }

        // Check for definition
        if (normalized.startsWith("define ") || normalized.startsWith("what is ") ||
            normalized.startsWith("what's ")) {
            return QueryCategory.DEFINITION;
        }

        // Check for explanation
        if (normalized.startsWith("explain ") || normalized.startsWith("how does ") ||
            normalized.startsWith("how do ")) {
            return QueryCategory.EXPLANATION;
        }

        // Check for lesson navigation
        if (normalized.matches("^(continue|next|repeat|back|previous|resume|go on)$")) {
            return QueryCategory.LESSON_NAV;
        }

        // Check for small talk
        if (normalized.contains("how are you") || normalized.contains("what's up") ||
            normalized.contains("tell me a joke") || normalized.contains("funny")) {
            return QueryCategory.SMALL_TALK;
        }

        return QueryCategory.UNKNOWN;
    }

    /**
     * Predict intent with confidence and matched rule.
     */
    private IntentConfidence predictIntent(String normalized, QueryCategory category, List<DetectedEntity> entities) {
        // Map category to primary intent
        String intent = mapCategoryToIntent(category, normalized, entities);
        String rule = "category:" + category;

        // Adjust confidence based on entity presence
        double baseScore = 0.7;
        if (!entities.isEmpty()) {
            baseScore += 0.1;
            rule += "+entities";
        }
        if (normalized.length() > 3) {
            baseScore += 0.1;
            rule += "+length";
        }

        return new IntentConfidence(intent, Math.min(baseScore, 0.95), rule);
    }

    /**
     * Map query category to intent string.
     */
    private String mapCategoryToIntent(QueryCategory category, String normalized, List<DetectedEntity> entities) {
        return switch (category) {
            case GREETING -> "GREETING";
            case ACKNOWLEDGMENT -> "ACKNOWLEDGMENT";
            case EXIT -> "EXIT_COURSE";
            case IDENTITY -> "WHO_AM_I";
            case MEMORY -> "MEMORY_RECALL";
            case SYSTEM -> "SYSTEM";
            case ROADMAP -> "PLAN";
            case QUIZ -> "START_QUIZ";
            case DEBUGGING -> "DEBUG_CODE";
            case CODING -> "CODE_GENERATION";
            case LEARNING -> {
                // Check for specific learning intents
                if (normalized.contains("teach") || normalized.contains("start course")) {
                    yield "START_COURSE";
                }
                if (normalized.contains("continue") || normalized.contains("next")) {
                    yield "CONTINUE_LESSON";
                }
                if (normalized.contains("repeat")) {
                    yield "REPEAT_LESSON";
                }
                if (normalized.contains("explain") || normalized.contains("what is")) {
                    yield "TEACH_TOPIC";
                }
                yield "LEARN";
            }
            case LESSON_NAV -> {
                if (normalized.equals("continue") || normalized.equals("next")) {
                    yield "CONTINUE_LESSON";
                }
                if (normalized.equals("repeat")) {
                    yield "REPEAT_LESSON";
                }
                if (normalized.equals("back") || normalized.equals("previous")) {
                    yield "PREVIOUS";
                }
                yield "CONTINUE_LESSON";
            }
            case DEFINITION, EXPLANATION -> "TEACH_TOPIC";
            case COMPARISON -> "COMPARISON";
            case PROGRAMMING -> "PROGRAMMING_QUERY";
            case MULTI_ACTION -> "MULTI_ACTION";
            case SMALL_TALK -> "SMALL_TALK";
            case CAREER -> "CAREER_ADVICE";
            case PLANNING -> "PLAN";
            default -> "DEFAULT";
        };
    }

    /**
     * Calculate overall confidence score.
     * Deterministic formula — no LLM.
     */
    private double calculateConfidence(String normalized, QueryCategory category,
                                       IntentConfidence intentConf, List<DetectedEntity> entities) {
        double confidence = intentConf.getScore();

        // Boost confidence for clear signals
        if (category != QueryCategory.UNKNOWN && category != QueryCategory.MULTI_ACTION) {
            confidence += 0.05;
        }

        // Boost for entities
        if (!entities.isEmpty()) {
            confidence += 0.05;
        }

        // Boost for clear intent signals
        if (normalized.length() > 10) {
            confidence += 0.05;
        }

        return Math.min(confidence, 0.99);
    }

    /**
     * Build human-readable reason for classification.
     */
    private String buildReason(QueryCategory category, IntentConfidence intentConf, List<DetectedEntity> entities) {
        StringBuilder reason = new StringBuilder();
        reason.append("Category=").append(category);
        reason.append(", Intent=").append(intentConf.getIntent());
        if (!entities.isEmpty()) {
            reason.append(", Entities=").append(entities.size());
        }
        return reason.toString();
    }
}