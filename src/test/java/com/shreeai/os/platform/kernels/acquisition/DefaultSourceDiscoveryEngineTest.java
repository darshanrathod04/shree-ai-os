package com.shreeai.os.platform.kernels.acquisition;

import com.shreeai.os.platform.kernels.acquisition.model.DiscoveryReason;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeTopic;
import com.shreeai.os.platform.kernels.acquisition.model.RequirementPriority;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultSourceDiscoveryEngine;
import com.shreeai.os.platform.kernels.context.model.ContextIntelligence;
import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.ExperienceLevel;
import com.shreeai.os.platform.kernels.context.model.GoalComplexity;
import com.shreeai.os.platform.kernels.context.model.GoalEvidence;
import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.PrimaryDomain;
import com.shreeai.os.platform.kernels.context.model.PrimaryIntent;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultSourceDiscoveryEngine} and the
 * K0.6.1 acquisition models.
 *
 * <p>Every test asserts exact, locked behavior: dictionary-driven domain
 * expansion, whole-token goal keyword expansion, explicit-only constraint
 * expansion, locked priority assignment, canonical ordering, deterministic
 * SHA-256 topic ids, preserved discovery reasons and full determinism.</p>
 */
class DefaultSourceDiscoveryEngineTest {

    private static final Instant FIXED = Instant.parse("2026-01-01T00:00:00Z");

    private final DefaultSourceDiscoveryEngine engine = new DefaultSourceDiscoveryEngine();

    // ---- deterministic fixtures ---------------------------------------------

    private static ContextIntelligence context(PrimaryDomain domain,
                                               double domainConfidence,
                                               String goalTitle,
                                               double goalConfidence,
                                               UserConstraints constraints) {
        GoalEvidence goalEvidence = new GoalEvidence(goalTitle, 1, 1);
        GoalNode goalNode = new GoalNode(goalTitle, goalConfidence, goalEvidence);
        GoalStructure goals = new GoalStructure(goalNode, List.of(),
                GoalComplexity.SIMPLE, FIXED, "test");
        return new ContextIntelligence(
                new IntentProfile(PrimaryIntent.LEARN, 0.95, List.of(), FIXED, "test"),
                new DomainProfile(domain, domainConfidence, List.of(), FIXED, "test"),
                constraints, goals, null);
    }

    private static ContextIntelligence context(PrimaryDomain domain, String goalTitle,
                                               UserConstraints constraints) {
        return context(domain, 0.90, goalTitle, 0.90, constraints);
    }

    private static List<String> names(KnowledgeRequirementSet set) {
        return set.topics().stream().map(KnowledgeTopic::name).toList();
    }

    private static KnowledgeTopic topic(KnowledgeRequirementSet set, String name) {
        return set.topics().stream()
                .filter(t -> t.name().equals(name))
                .findFirst().orElseThrow();
    }

    private static boolean hasTopic(KnowledgeRequirementSet set, String name) {
        return set.topics().stream().anyMatch(t -> t.name().equals(name));
    }

    // ---- locked model validation ----------------------------------------------

    @Test
    void requirementPriority_coversAllLockedValues() {
        assertEquals(List.of(RequirementPriority.LOW, RequirementPriority.MEDIUM,
                RequirementPriority.HIGH, RequirementPriority.CRITICAL),
                List.of(RequirementPriority.values()));
    }

    @Test
    void knowledgeTopic_rejectsNullFields() {
        assertThrows(NullPointerException.class,
                () -> new KnowledgeTopic(null, "Java", RequirementPriority.CRITICAL, 1.0));
        assertThrows(NullPointerException.class,
                () -> new KnowledgeTopic("id", null, RequirementPriority.CRITICAL, 1.0));
        assertThrows(NullPointerException.class,
                () -> new KnowledgeTopic("id", "Java", null, 1.0));
    }

    @Test
    void knowledgeTopic_rejectsBlankNameAndId() {
        assertThrows(IllegalArgumentException.class,
                () -> new KnowledgeTopic("id", "  ", RequirementPriority.CRITICAL, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new KnowledgeTopic(" ", "Java", RequirementPriority.CRITICAL, 1.0));
    }

    @Test
    void knowledgeTopic_rejectsConfidenceOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new KnowledgeTopic("id", "Java", RequirementPriority.CRITICAL, -0.01));
        assertThrows(IllegalArgumentException.class,
                () -> new KnowledgeTopic("id", "Java", RequirementPriority.CRITICAL, 1.01));
        // Boundary values are valid.
        assertDoesNotThrow(() -> new KnowledgeTopic(
                KnowledgeTopic.deterministicTopicId("Java"), "Java",
                RequirementPriority.CRITICAL, 0.0));
        assertDoesNotThrow(() -> new KnowledgeTopic(
                KnowledgeTopic.deterministicTopicId("Java"), "Java",
                RequirementPriority.CRITICAL, 1.0));
    }

    @Test
    void discoveryReason_rejectsNullAndBlankFields() {
        assertThrows(NullPointerException.class, () -> new DiscoveryReason(null, "e", "s"));
        assertThrows(NullPointerException.class, () -> new DiscoveryReason("t", null, "s"));
        assertThrows(NullPointerException.class, () -> new DiscoveryReason("t", "e", null));
        assertThrows(IllegalArgumentException.class, () -> new DiscoveryReason(" ", "e", "s"));
        assertThrows(IllegalArgumentException.class, () -> new DiscoveryReason("t", " ", "s"));
        assertThrows(IllegalArgumentException.class, () -> new DiscoveryReason("t", "e", " "));
    }

    @Test
    void requirementSet_isDeeplyImmutable() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Become Java Developer", UserConstraints.empty()));
        assertThrows(UnsupportedOperationException.class, () -> set.topics().add(null));
        assertThrows(UnsupportedOperationException.class, () -> set.reasons().add(null));
    }

    @Test
    void discover_nullContext_throwsNpe() {
        assertThrows(NullPointerException.class, () -> engine.discover(null));
    }

    // ---- Stage 1: domain expansion ---------------------------------------------

    @Test
    void javaDomain_expandsToLockedTopics() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Learn programming", UserConstraints.empty()));
        assertTrue(hasTopic(set, "Java"));
        assertTrue(hasTopic(set, "OOP"));
        assertTrue(hasTopic(set, "Collections"));
        assertEquals(RequirementPriority.CRITICAL, topic(set, "Java").priority());
        assertEquals(RequirementPriority.CRITICAL, topic(set, "OOP").priority());
        assertEquals(RequirementPriority.CRITICAL, topic(set, "Collections").priority());
    }

    @Test
    void springDomain_expandsCorrectly() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.SPRING, "Build web services", UserConstraints.empty()));
        assertTrue(hasTopic(set, "Spring"));
        assertTrue(hasTopic(set, "Spring Boot"));
        assertEquals(RequirementPriority.CRITICAL, topic(set, "Spring Boot").priority());
    }

    @Test
    void databaseDomain_expandsCorrectly() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.DATABASE, "Learn programming", UserConstraints.empty()));
        assertTrue(hasTopic(set, "SQL"));
        assertTrue(hasTopic(set, "JDBC"));
        assertEquals(RequirementPriority.CRITICAL, topic(set, "SQL").priority());
    }

    @Test
    void aiDomain_expandsCorrectly() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.AI, "Learn programming", UserConstraints.empty()));
        assertTrue(hasTopic(set, "LLM"));
        assertTrue(hasTopic(set, "RAG"));
        assertEquals(RequirementPriority.CRITICAL, topic(set, "RAG").priority());
    }

    @Test
    void unknownDomain_yieldsNoDomainTopics() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.UNKNOWN, "Learn programming", UserConstraints.empty()));
        assertTrue(set.topics().isEmpty());
        assertTrue(set.reasons().isEmpty());
    }

    @Test
    void generalDomain_yieldsLowPriorityEnrichment() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.GENERAL, "Learn programming", UserConstraints.empty()));
        assertEquals(1, set.topics().size());
        assertEquals(DefaultSourceDiscoveryEngine.TOPIC_GENERAL_RESEARCH,
                set.topics().get(0).name());
        assertEquals(RequirementPriority.LOW, set.topics().get(0).priority());
    }

    @Test
    void domainTopics_carryProfileConfidence() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, 0.85, "Learn programming", 0.90,
                        UserConstraints.empty()));
        assertEquals(0.85, topic(set, "Java").confidence(), 0.0);
    }

    @Test
    void domainTopics_useFallbackConfidenceWhenZero() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, 0.0, "Learn programming", 0.90,
                        UserConstraints.empty()));
        assertEquals(DefaultSourceDiscoveryEngine.FALLBACK_CONFIDENCE,
                topic(set, "Java").confidence(), 0.0);
    }

    // ---- Stage 2: goal expansion ----------------------------------------------

    @Test
    void javaDeveloperGoal_expandsToLockedTopics() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Become Java Developer",
                        UserConstraints.empty()));
        assertTrue(hasTopic(set, "Streams"));
        assertTrue(hasTopic(set, "JDBC"));
        assertTrue(hasTopic(set, "Spring"));
        assertTrue(hasTopic(set, "Interview Preparation"));
        assertEquals(RequirementPriority.HIGH, topic(set, "Streams").priority());
        assertEquals(RequirementPriority.HIGH, topic(set, "JDBC").priority());
        assertEquals(RequirementPriority.HIGH, topic(set, "Spring").priority());
        assertEquals(RequirementPriority.HIGH, topic(set, "Interview Preparation").priority());
    }

    @Test
    void goalExpansion_multiWordKeywordMatchesContiguousTokens() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.AI, "Learn machine learning",
                        UserConstraints.empty()));
        assertTrue(hasTopic(set, "Model Training"));
        assertTrue(hasTopic(set, "Prompt Engineering"));
        assertEquals(RequirementPriority.HIGH, topic(set, "Model Training").priority());
    }

    @Test
    void goalExpansion_wholeTokenMatchingOnly_noSubstringFalsePositives() {
        // "train" contains "ai" as a substring but NOT as a whole token.
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Professional training program",
                        UserConstraints.empty()));
        assertFalse(hasTopic(set, "Prompt Engineering"));
        assertFalse(hasTopic(set, "RAG"));
    }

    @Test
    void goalExpansion_caseInsensitive() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "BECOME A JAVA DEVELOPER",
                        UserConstraints.empty()));
        assertTrue(hasTopic(set, "Streams"));
        assertTrue(hasTopic(set, "Interview Preparation"));
    }

    @Test
    void goalExpansion_usesSubGoalsToo() {
        GoalEvidence evidence = new GoalEvidence("Learn Spring framework", 1, 1);
        GoalNode primary = new GoalNode("Become Java Developer", 0.90, evidence);
        GoalNode subGoal = new GoalNode("Learn Spring framework", 0.80, evidence);
        GoalStructure goals = new GoalStructure(primary, List.of(subGoal),
                GoalComplexity.MODERATE, FIXED, "test");
        ContextIntelligence ctx = new ContextIntelligence(
                new IntentProfile(PrimaryIntent.LEARN, 0.95, List.of(), FIXED, "test"),
                new DomainProfile(PrimaryDomain.JAVA, 0.90, List.of(), FIXED, "test"),
                UserConstraints.empty(), goals, null);
        KnowledgeRequirementSet set = engine.discover(ctx);
        assertTrue(hasTopic(set, "REST API"));
        assertEquals(0.80, topic(set, "REST API").confidence(), 0.0);
    }

    @Test
    void goalExpansion_noneStructureYieldsNoGoalTopics() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "No goal identified", UserConstraints.empty()));
        assertFalse(hasTopic(set, "Streams"));
        assertFalse(hasTopic(set, "Interview Preparation"));
    }

    @Test
    void goalTopics_carryGoalConfidence() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, 0.90, "Become Java Developer", 0.70,
                        UserConstraints.empty()));
        assertEquals(0.70, topic(set, "Streams").confidence(), 0.0);
    }

    // ---- Stage 3: constraint expansion ------------------------------------------

    @Test
    void durationConstraint_producesRoadmapTopic() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Become Java Developer",
                        UserConstraints.of("30 days", null, null, null, null, null,
                                List.of())));
        assertTrue(hasTopic(set, DefaultSourceDiscoveryEngine.TOPIC_ROADMAP));
        assertEquals(RequirementPriority.MEDIUM,
                topic(set, DefaultSourceDiscoveryEngine.TOPIC_ROADMAP).priority());
        DiscoveryReason reason = set.reasons().stream()
                .filter(r -> r.topicName()
                        .equals(DefaultSourceDiscoveryEngine.TOPIC_ROADMAP))
                .findFirst().orElseThrow();
        assertEquals("30 days", reason.evidence());
        assertEquals(DiscoveryReason.SOURCE_USER_CONSTRAINTS, reason.sourceArtifact());
    }

    @Test
    void beginnerExperience_producesFundamentalsTopic() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Become Java Developer",
                        UserConstraints.of(null, null, ExperienceLevel.BEGINNER, null,
                                null, null, List.of())));
        assertTrue(hasTopic(set, "Fundamentals"));
        assertEquals(RequirementPriority.MEDIUM, topic(set, "Fundamentals").priority());
        assertEquals("BEGINNER",
                set.reasons().stream()
                        .filter(r -> r.topicName().equals("Fundamentals"))
                        .findFirst().orElseThrow().evidence());
    }

    @Test
    void experienceLevels_mapToLockedTopics() {
        assertEquals("Best Practices", experienceTopic(ExperienceLevel.INTERMEDIATE));
        assertEquals("Advanced Concepts", experienceTopic(ExperienceLevel.ADVANCED));
        assertEquals("Expert Techniques", experienceTopic(ExperienceLevel.EXPERT));
    }

    private String experienceTopic(ExperienceLevel level) {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Become Java Developer",
                        UserConstraints.of(null, null, level, null, null, null,
                                List.of())));
        return set.topics().stream()
                .filter(t -> t.priority() == RequirementPriority.MEDIUM)
                .map(KnowledgeTopic::name)
                .findFirst().orElseThrow();
    }

    @Test
    void budgetConstraint_producesFreeResourcesTopic() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Become Java Developer",
                        UserConstraints.of(null, "$200", null, null, null, null,
                                List.of())));
        assertTrue(hasTopic(set, DefaultSourceDiscoveryEngine.TOPIC_FREE_RESOURCES));
        assertEquals(RequirementPriority.MEDIUM,
                topic(set, DefaultSourceDiscoveryEngine.TOPIC_FREE_RESOURCES).priority());
    }

    @Test
    void platformConstraint_producesPlatformSetupTopic() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Become Java Developer",
                        UserConstraints.of(null, null, null,
                                com.shreeai.os.platform.kernels.context.model.PlatformType.WINDOWS,
                                null, null, List.of())));
        assertTrue(hasTopic(set, DefaultSourceDiscoveryEngine.TOPIC_PLATFORM_SETUP));
        assertEquals("WINDOWS",
                set.reasons().stream()
                        .filter(r -> r.topicName()
                                .equals(DefaultSourceDiscoveryEngine.TOPIC_PLATFORM_SETUP))
                        .findFirst().orElseThrow().evidence());
    }

    @Test
    void emptyConstraints_produceNoConstraintTopics() {
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Learn programming", UserConstraints.empty()));
        assertFalse(names(set).contains(DefaultSourceDiscoveryEngine.TOPIC_ROADMAP));
        assertFalse(names(set).contains("Fundamentals"));
    }

    @Test
    void languageAndOutputPreferences_areNotExpanded() {
        // Delivery preferences shape output, not required knowledge.
        KnowledgeRequirementSet set = engine.discover(
                context(PrimaryDomain.JAVA, "Become Java Developer",
                        UserConstraints.of("30 days", null, null, null, "English",
                                com.shreeai.os.platform.kernels.context.model.OutputPreference.ROADMAP,
                                List.of())));
        assertEquals(List.of("Collections", "Java", "OOP",
                        "Interview Preparation", "JDBC", "Spring", "Streams",
                        "Roadmap"),
                names(set));
    }

    // ---- Stage 4 + 5: priority and canonicalization -----------------------------

    @Test
    void lockedExample_javaDeveloper30Days_producesExactCanonicalSet() {
        KnowledgeRequirementSet set = engine.discover(context(
                PrimaryDomain.JAVA, 0.90, "Become Java Developer", 0.90,
                UserConstraints.of("30 days", null, ExperienceLevel.BEGINNER, null,
                        null, null, List.of())));
        assertEquals(List.of(
                "Collections", "Java", "OOP",
                "Interview Preparation", "JDBC", "Spring", "Streams",
                "Fundamentals", "Roadmap"),
                names(set));
        assertEquals(RequirementPriority.CRITICAL, topic(set, "Java").priority());
        assertEquals(RequirementPriority.HIGH, topic(set, "Streams").priority());
        assertEquals(RequirementPriority.MEDIUM, topic(set, "Roadmap").priority());
        // 3 domain + 4 goal + 2 constraint discovery reasons preserved.
        assertEquals(9, set.reasons().size());
    }

    @Test
    void duplicateTopics_removedKeepingHighestPriority() {
        // DATABASE domain produces SQL + JDBC (CRITICAL); a "database" goal
        // also produces SQL + JDBC (HIGH). CRITICAL wins, topics are not doubled.
        KnowledgeRequirementSet set = engine.discover(context(
                PrimaryDomain.DATABASE, 0.90, "Learn database design", 0.90,
                UserConstraints.empty()));
        assertTrue(hasTopic(set, "SQL"));
        assertTrue(hasTopic(set, "JDBC"));
        assertEquals(2, set.topics().size());
        assertEquals(RequirementPriority.CRITICAL, topic(set, "SQL").priority());
        assertEquals(RequirementPriority.CRITICAL, topic(set, "JDBC").priority());
    }

    @Test
    void stableOrdering_byPriorityThenName() {
        KnowledgeRequirementSet set = engine.discover(context(
                PrimaryDomain.JAVA, 0.90, "Become Java Developer", 0.90,
                UserConstraints.of("30 days", null, ExperienceLevel.BEGINNER, null,
                        null, null, List.of())));
        List<RequirementPriority> priorities = set.topics().stream()
                .map(KnowledgeTopic::priority).toList();
        List<Integer> ranks = priorities.stream()
                .map(p -> switch (p) {
                    case CRITICAL -> 0;
                    case HIGH -> 1;
                    case MEDIUM -> 2;
                    case LOW -> 3;
                }).toList();
        List<Integer> sortedRanks = new ArrayList<>(ranks);
        sortedRanks.sort(Integer::compareTo);
        assertEquals(sortedRanks, ranks);
        // Names must be ascending within each priority block.
        for (int i = 1; i < set.topics().size(); i++) {
            if (set.topics().get(i).priority()
                    == set.topics().get(i - 1).priority()) {
                assertTrue(set.topics().get(i).name()
                        .compareTo(set.topics().get(i - 1).name()) > 0);
            }
        }
    }

    @Test
    void sha256Ids_deterministicAndStable() {
        KnowledgeRequirementSet first = engine.discover(context(
                PrimaryDomain.JAVA, "Become Java Developer", UserConstraints.empty()));
        KnowledgeRequirementSet second = engine.discover(context(
                PrimaryDomain.JAVA, "Become Java Developer", UserConstraints.empty()));
        assertEquals(KnowledgeTopic.deterministicTopicId("Java"),
                topic(first, "Java").topicId());
        assertEquals(topic(first, "Java").topicId(), topic(second, "Java").topicId());
        assertNotEquals(topic(first, "Java").topicId(), topic(first, "OOP").topicId());
        assertEquals(64, topic(first, "Java").topicId().length());
    }

    @Test
    void discoveryReasons_preservedWithSourceArtifacts() {
        KnowledgeRequirementSet set = engine.discover(context(
                PrimaryDomain.JAVA, 0.90, "Become Java Developer", 0.90,
                UserConstraints.of("30 days", null, ExperienceLevel.BEGINNER, null,
                        null, null, List.of())));
        assertTrue(set.reasons().stream().anyMatch(r ->
                r.topicName().equals("Java") && r.evidence().equals("JAVA")
                        && r.sourceArtifact().equals(DiscoveryReason.SOURCE_DOMAIN_PROFILE)));
        assertTrue(set.reasons().stream().anyMatch(r ->
                r.topicName().equals("Streams")
                        && r.evidence().equals("Become Java Developer")
                        && r.sourceArtifact().equals(DiscoveryReason.SOURCE_GOAL_STRUCTURE)));
        assertTrue(set.reasons().stream().anyMatch(r ->
                r.topicName().equals("Roadmap") && r.evidence().equals("30 days")
                        && r.sourceArtifact()
                        .equals(DiscoveryReason.SOURCE_USER_CONSTRAINTS)));
    }

    @Test
    void duplicateReasons_removedExactMatchesOnly() {
        // A repeated sub-goal with the same title must not duplicate reasons.
        GoalEvidence evidence = new GoalEvidence("Learn Java streams", 1, 1);
        GoalNode primary = new GoalNode("Learn Java streams", 0.90, evidence);
        GoalStructure goals = new GoalStructure(primary,
                List.of(new GoalNode("Learn Java streams", 0.90, evidence)),
                GoalComplexity.SIMPLE, FIXED, "test");
        ContextIntelligence ctx = new ContextIntelligence(
                new IntentProfile(PrimaryIntent.LEARN, 0.95, List.of(), FIXED, "test"),
                new DomainProfile(PrimaryDomain.JAVA, 0.90, List.of(), FIXED, "test"),
                UserConstraints.empty(), goals, null);
        KnowledgeRequirementSet set = engine.discover(ctx);
        long streamsReasons = set.reasons().stream()
                .filter(r -> r.topicName().equals("Streams")).count();
        assertEquals(1, streamsReasons);
    }

    @Test
    void sameInput_producesStructurallyIdenticalOutput() {
        ContextIntelligence ctx = context(PrimaryDomain.JAVA, 0.90,
                "Become Java Developer", 0.90,
                UserConstraints.of("30 days", null, ExperienceLevel.BEGINNER, null,
                        null, null, List.of()));
        assertEquals(engine.discover(ctx), engine.discover(ctx));
    }

    @Test
    void engine_isStateless_acrossRepeatedCalls() {
        ContextIntelligence java = context(PrimaryDomain.JAVA,
                "Become Java Developer", UserConstraints.empty());
        ContextIntelligence ai = context(PrimaryDomain.AI,
                "Learn machine learning", UserConstraints.empty());
        KnowledgeRequirementSet firstJava = engine.discover(java);
        engine.discover(ai);
        KnowledgeRequirementSet secondJava = engine.discover(java);
        assertEquals(firstJava, secondJava);
    }

    // ---- CognitiveState integration --------------------------------------------

    @Test
    void cognitiveState_withKnowledgeRequirements_storesArtifact() {
        KnowledgeRequirementSet set = engine.discover(context(
                PrimaryDomain.JAVA, "Become Java Developer", UserConstraints.empty()));
        CognitiveState state = CognitiveState.empty()
                .withKnowledgeRequirements(set);
        assertEquals(set, state.knowledgeRequirements());
    }

    @Test
    void cognitiveState_backwardCompatible_constructorsLeaveRequirementsNull() {
        CognitiveState legacy16Arg = new CognitiveState(null, null, null, null, 0,
                List.of(), null, null, null, null, null, null, null, null, null, null);
        assertNull(legacy16Arg.knowledgeRequirements());
        CognitiveState legacy17Arg = new CognitiveState(null, null, null, null, 0,
                List.of(), null, null, null, null, null, null, null, null, null, null,
                null);
        assertNull(legacy17Arg.knowledgeRequirements());
    }

    @Test
    void cognitiveState_withKnowledgeRequirements_preservesOtherArtifacts() {
        KnowledgeRequirementSet set = engine.discover(context(
                PrimaryDomain.JAVA, "Become Java Developer", UserConstraints.empty()));
        CognitiveState state = CognitiveState.empty()
                .withIntentProfile(new IntentProfile(PrimaryIntent.LEARN, 0.95,
                        List.of(), FIXED, "test"))
                .withKnowledgeRequirements(set);
        assertNotNull(state.intentProfile());
        assertEquals(set, state.knowledgeRequirements());
        assertNull(state.reasoningGraph());
    }

    @Test
    void cognitiveState_rejectsNullRequirementSet() {
        assertThrows(NullPointerException.class,
                () -> CognitiveState.empty().withKnowledgeRequirements(null));
    }
}
