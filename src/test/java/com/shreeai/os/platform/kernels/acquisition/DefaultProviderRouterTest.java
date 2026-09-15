package com.shreeai.os.platform.kernels.acquisition;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionTarget;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeTopic;
import com.shreeai.os.platform.kernels.acquisition.model.ProviderCapability;
import com.shreeai.os.platform.kernels.acquisition.model.ProviderType;
import com.shreeai.os.platform.kernels.acquisition.model.RequirementPriority;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultProviderRouter;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultProviderRouter} and the K0.6.2
 * routing models.
 *
 * <p>Every test asserts exact, locked behavior: dictionary-driven routing,
 * the WEB fallback, case-insensitive exact matching (no fuzzy), stable
 * priority-then-name ordering, duplicate handling, deterministic topic ids
 * and full determinism.</p>
 */
class DefaultProviderRouterTest {

    private final DefaultProviderRouter router = new DefaultProviderRouter();

    // ---- deterministic fixtures ---------------------------------------------

    private static KnowledgeTopic topic(String name, RequirementPriority priority) {
        return new KnowledgeTopic(KnowledgeTopic.deterministicTopicId(name),
                name, priority, 0.90);
    }

    private static KnowledgeRequirementSet set(KnowledgeTopic... topics) {
        return new KnowledgeRequirementSet(List.of(topics), List.of());
    }

    private static ProviderType providerOf(AcquisitionPlan plan, String topicName) {
        return plan.targets().stream()
                .filter(t -> t.topicName().equals(topicName))
                .findFirst().orElseThrow().provider();
    }

    private static List<String> targetNames(AcquisitionPlan plan) {
        return plan.targets().stream().map(AcquisitionTarget::topicName).toList();
    }

    // ---- locked model validation ----------------------------------------------

    @Test
    void providerType_coversAllLockedValues() {
        assertEquals(List.of(ProviderType.OFFICIAL_DOCS, ProviderType.GITHUB,
                ProviderType.ENTERPRISE_DOCS, ProviderType.WEB,
                ProviderType.DATABASE, ProviderType.API, ProviderType.LOCAL_FILES),
                List.of(ProviderType.values()));
    }

    @Test
    void providerCapability_isDeeplyImmutable() {
        ProviderCapability capability = ProviderCapability.of(
                ProviderType.OFFICIAL_DOCS, "Java", "OOP");
        assertThrows(UnsupportedOperationException.class,
                () -> capability.supportedTopics().add("Hack"));
    }

    @Test
    void providerCapability_rejectsNullProviderAndHandlesNullTopics() {
        assertThrows(NullPointerException.class,
                () -> new ProviderCapability(null, List.of("Java")));
        ProviderCapability capability = new ProviderCapability(ProviderType.WEB, null);
        assertTrue(capability.supportedTopics().isEmpty());
    }

    @Test
    void providerCapability_supportsIsCaseInsensitiveExactMatch() {
        ProviderCapability capability = ProviderCapability.of(
                ProviderType.OFFICIAL_DOCS, "Spring Boot");
        assertTrue(capability.supports("Spring Boot"));
        assertTrue(capability.supports("spring boot"));
        assertFalse(capability.supports("Spring"));
        assertFalse(capability.supports("Spring Boot Advanced"));
        assertFalse(capability.supports(null));
    }

    @Test
    void acquisitionTarget_rejectsNullsAndBlanks() {
        assertThrows(NullPointerException.class,
                () -> new AcquisitionTarget(null, "Java", ProviderType.WEB,
                        RequirementPriority.HIGH));
        assertThrows(NullPointerException.class,
                () -> new AcquisitionTarget("id", null, ProviderType.WEB,
                        RequirementPriority.HIGH));
        assertThrows(NullPointerException.class,
                () -> new AcquisitionTarget("id", "Java", null,
                        RequirementPriority.HIGH));
        assertThrows(NullPointerException.class,
                () -> new AcquisitionTarget("id", "Java", ProviderType.WEB, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AcquisitionTarget(" ", "Java", ProviderType.WEB,
                        RequirementPriority.HIGH));
        assertThrows(IllegalArgumentException.class,
                () -> new AcquisitionTarget("id", " ", ProviderType.WEB,
                        RequirementPriority.HIGH));
    }

    @Test
    void acquisitionPlan_isDeeplyImmutable() {
        AcquisitionPlan plan = router.route(set(
                topic("Java", RequirementPriority.CRITICAL)));
        assertThrows(UnsupportedOperationException.class,
                () -> plan.targets().add(null));
    }

    @Test
    void acquisitionPlan_emptyHasNoTargets() {
        assertEquals(0, AcquisitionPlan.empty().targets().size());
    }

    @Test
    void route_nullRequirements_throwsNpe() {
        assertThrows(NullPointerException.class, () -> router.route(null));
    }

    // ---- locked dictionary routing ---------------------------------------------

    @Test
    void javaRoutesToOfficialDocs() {
        AcquisitionPlan plan = router.route(set(
                topic("Java", RequirementPriority.CRITICAL)));
        assertEquals(ProviderType.OFFICIAL_DOCS, providerOf(plan, "Java"));
    }

    @Test
    void springRoutesToOfficialDocs() {
        AcquisitionPlan plan = router.route(set(
                topic("Spring", RequirementPriority.CRITICAL)));
        assertEquals(ProviderType.OFFICIAL_DOCS, providerOf(plan, "Spring"));
    }

    @Test
    void officialDocsTopics_routeToOfficialDocs() {
        AcquisitionPlan plan = router.route(set(
                topic("OOP", RequirementPriority.CRITICAL),
                topic("Collections", RequirementPriority.CRITICAL),
                topic("Streams", RequirementPriority.CRITICAL),
                topic("SQL", RequirementPriority.CRITICAL),
                topic("JDBC", RequirementPriority.CRITICAL),
                topic("REST API", RequirementPriority.CRITICAL),
                topic("Docker", RequirementPriority.CRITICAL),
                topic("Kubernetes", RequirementPriority.CRITICAL),
                topic("RAG", RequirementPriority.CRITICAL)));
        for (String name : List.of("OOP", "Collections", "Streams", "SQL", "JDBC",
                "REST API", "Docker", "Kubernetes", "RAG")) {
            assertEquals(ProviderType.OFFICIAL_DOCS, providerOf(plan, name), name);
        }
    }

    @Test
    void webTopics_routeToWeb() {
        AcquisitionPlan plan = router.route(set(
                topic("LLM", RequirementPriority.HIGH),
                topic("Interview Preparation", RequirementPriority.HIGH),
                topic("Roadmap", RequirementPriority.MEDIUM),
                topic("Best Practices", RequirementPriority.MEDIUM),
                topic("Fundamentals", RequirementPriority.MEDIUM)));
        assertEquals(ProviderType.WEB, providerOf(plan, "LLM"));
        assertEquals(ProviderType.WEB, providerOf(plan, "Interview Preparation"));
        assertEquals(ProviderType.WEB, providerOf(plan, "Roadmap"));
        assertEquals(ProviderType.WEB, providerOf(plan, "Best Practices"));
        assertEquals(ProviderType.WEB, providerOf(plan, "Fundamentals"));
    }

    @Test
    void roadmapRoutesToWeb() {
        AcquisitionPlan plan = router.route(set(
                topic("Roadmap", RequirementPriority.MEDIUM)));
        assertEquals(ProviderType.WEB, providerOf(plan, "Roadmap"));
    }

    @Test
    void fundamentalsRoutesToWeb() {
        AcquisitionPlan plan = router.route(set(
                topic("Fundamentals", RequirementPriority.MEDIUM)));
        assertEquals(ProviderType.WEB, providerOf(plan, "Fundamentals"));
    }

    @Test
    void platformSetupRoutesToLocalFiles() {
        AcquisitionPlan plan = router.route(set(
                topic("Platform Setup", RequirementPriority.MEDIUM)));
        assertEquals(ProviderType.LOCAL_FILES, providerOf(plan, "Platform Setup"));
    }

    @Test
    void enterpriseTopics_routeToEnterpriseDocs() {
        AcquisitionPlan plan = router.route(set(
                topic("Company SOP", RequirementPriority.HIGH),
                topic("CRM Architecture", RequirementPriority.HIGH)));
        assertEquals(ProviderType.ENTERPRISE_DOCS, providerOf(plan, "Company SOP"));
        assertEquals(ProviderType.ENTERPRISE_DOCS, providerOf(plan, "CRM Architecture"));
    }

    @Test
    void lockedDictionary_coversExactlyTwentyTopics() {
        List<ProviderCapability> capabilities = DefaultProviderRouter.lockedCapabilities();
        assertEquals(4, capabilities.size());
        assertEquals(12, capabilities.get(0).supportedTopics().size());
        assertEquals(5, capabilities.get(1).supportedTopics().size());
        assertEquals(1, capabilities.get(2).supportedTopics().size());
        assertEquals(2, capabilities.get(3).supportedTopics().size());
        assertEquals(ProviderType.OFFICIAL_DOCS, capabilities.get(0).provider());
        assertEquals(ProviderType.WEB, capabilities.get(1).provider());
        assertEquals(ProviderType.LOCAL_FILES, capabilities.get(2).provider());
        assertEquals(ProviderType.ENTERPRISE_DOCS, capabilities.get(3).provider());
    }

    // ---- fallback and matching semantics ----------------------------------------

    @Test
    void unknownTopic_fallsBackToWeb() {
        AcquisitionPlan plan = router.route(set(
                topic("Quantum Chromodynamics", RequirementPriority.LOW)));
        assertEquals(1, plan.targets().size());
        assertEquals(ProviderType.WEB, plan.targets().get(0).provider());
    }

    @Test
    void unknownTopicNeverFails_everyTopicGetsAProvider() {
        KnowledgeRequirementSet requirements = set(
                topic("Topic One", RequirementPriority.CRITICAL),
                topic("Topic Two", RequirementPriority.HIGH),
                topic("Topic Three", RequirementPriority.MEDIUM),
                topic("Topic Four", RequirementPriority.LOW));
        AcquisitionPlan plan = router.route(requirements);
        assertEquals(4, plan.targets().size());
        for (AcquisitionTarget target : plan.targets()) {
            assertNotNull(target.provider());
            assertEquals(ProviderType.WEB, target.provider());
        }
    }

    @Test
    void routing_isCaseInsensitive() {
        AcquisitionPlan plan = router.route(set(
                topic("JAVA", RequirementPriority.CRITICAL),
                topic("rOaDmAp", RequirementPriority.MEDIUM)));
        assertEquals(ProviderType.OFFICIAL_DOCS, providerOf(plan, "JAVA"));
        assertEquals(ProviderType.WEB, providerOf(plan, "rOaDmAp"));
    }

    @Test
    void routing_hasNoFuzzyMatching() {
        // Near-miss names must NOT match the dictionary - they fall back to WEB.
        AcquisitionPlan plan = router.route(set(
                topic("Javas", RequirementPriority.CRITICAL),
                topic("Spring Boot Advanced", RequirementPriority.HIGH),
                topic("Roadmaps", RequirementPriority.MEDIUM)));
        assertEquals(ProviderType.WEB, providerOf(plan, "Javas"));
        assertEquals(ProviderType.WEB, providerOf(plan, "Spring Boot Advanced"));
        assertEquals(ProviderType.WEB, providerOf(plan, "Roadmaps"));
    }

    @Test
    void multiWordDictionaryKeys_matchExactWordSequence() {
        AcquisitionPlan plan = router.route(set(
                topic("Spring Boot", RequirementPriority.CRITICAL),
                topic("Company SOP", RequirementPriority.HIGH),
                topic("Best Practices", RequirementPriority.MEDIUM)));
        assertEquals(ProviderType.OFFICIAL_DOCS, providerOf(plan, "Spring Boot"));
        assertEquals(ProviderType.ENTERPRISE_DOCS, providerOf(plan, "Company SOP"));
        assertEquals(ProviderType.WEB, providerOf(plan, "Best Practices"));
    }

    @Test
    void emptyRequirementSet_producesEmptyPlan() {
        assertEquals(0, router.route(KnowledgeRequirementSet.empty()).targets().size());
    }

    // ---- stable ordering, duplicates, determinism --------------------------------

    @Test
    void stableOrdering_byPriorityThenName() {
        AcquisitionPlan plan = router.route(set(
                topic("Fundamentals", RequirementPriority.MEDIUM),
                topic("Roadmap", RequirementPriority.MEDIUM),
                topic("Spring", RequirementPriority.HIGH),
                topic("Java", RequirementPriority.CRITICAL)));
        assertEquals(List.of("Java", "Spring", "Fundamentals", "Roadmap"),
                targetNames(plan));
    }

    @Test
    void lockedExample_javaSpringRoadmapFundamentals_producesExactPlan() {
        AcquisitionPlan plan = router.route(set(
                topic("Java", RequirementPriority.CRITICAL),
                topic("Spring", RequirementPriority.HIGH),
                topic("Roadmap", RequirementPriority.MEDIUM),
                topic("Fundamentals", RequirementPriority.MEDIUM)));
        assertEquals(List.of("Java", "Spring", "Fundamentals", "Roadmap"),
                targetNames(plan));
        assertEquals(ProviderType.OFFICIAL_DOCS, providerOf(plan, "Java"));
        assertEquals(ProviderType.OFFICIAL_DOCS, providerOf(plan, "Spring"));
        assertEquals(ProviderType.WEB, providerOf(plan, "Fundamentals"));
        assertEquals(ProviderType.WEB, providerOf(plan, "Roadmap"));
        assertEquals(RequirementPriority.CRITICAL, plan.targets().get(0).priority());
    }

    @Test
    void duplicateTopics_deduplicatedKeepingFirst() {
        AcquisitionPlan plan = router.route(set(
                topic("Java", RequirementPriority.CRITICAL),
                topic("Java", RequirementPriority.CRITICAL)));
        assertEquals(1, plan.targets().size());
        assertEquals("Java", plan.targets().get(0).topicName());
        assertEquals(ProviderType.OFFICIAL_DOCS, plan.targets().get(0).provider());
    }

    @Test
    void targets_preserveTopicIdAndPriority() {
        KnowledgeTopic java = topic("Java", RequirementPriority.CRITICAL);
        KnowledgeTopic roadmap = topic("Roadmap", RequirementPriority.MEDIUM);
        AcquisitionPlan plan = router.route(set(java, roadmap));
        AcquisitionTarget javaTarget = plan.targets().get(0);
        AcquisitionTarget roadmapTarget = plan.targets().get(1);
        assertEquals(java.topicId(), javaTarget.topicId());
        assertEquals(RequirementPriority.CRITICAL, javaTarget.priority());
        assertEquals(roadmap.topicId(), roadmapTarget.topicId());
        assertEquals(RequirementPriority.MEDIUM, roadmapTarget.priority());
    }

    @Test
    void sameInput_producesIdenticalPlan() {
        KnowledgeRequirementSet requirements = set(
                topic("Java", RequirementPriority.CRITICAL),
                topic("Spring", RequirementPriority.HIGH),
                topic("Roadmap", RequirementPriority.MEDIUM),
                topic("Fundamentals", RequirementPriority.MEDIUM));
        assertEquals(router.route(requirements), router.route(requirements));
    }

    @Test
    void router_isStateless_acrossRepeatedCalls() {
        KnowledgeRequirementSet java = set(
                topic("Java", RequirementPriority.CRITICAL));
        KnowledgeRequirementSet ai = set(topic("LLM", RequirementPriority.HIGH));
        AcquisitionPlan firstJava = router.route(java);
        router.route(ai);
        AcquisitionPlan secondJava = router.route(java);
        assertEquals(firstJava, secondJava);
    }

    // ---- CognitiveState integration --------------------------------------------

    @Test
    void cognitiveState_withAcquisitionPlan_storesArtifact() {
        AcquisitionPlan plan = router.route(set(
                topic("Java", RequirementPriority.CRITICAL)));
        CognitiveState state = CognitiveState.empty().withAcquisitionPlan(plan);
        assertEquals(plan, state.acquisitionPlan());
    }

    @Test
    void cognitiveState_backwardCompatible_constructorsLeavePlanNull() {
        CognitiveState legacy17Arg = new CognitiveState(null, null, null, null, 0,
                List.of(), null, null, null, null, null, null, null, null, null, null,
                null);
        assertNull(legacy17Arg.acquisitionPlan());
        CognitiveState legacy18Arg = new CognitiveState(null, null, null, null, 0,
                List.of(), null, null, null, null, null, null, null, null, null, null,
                null, null);
        assertNull(legacy18Arg.acquisitionPlan());
    }

    @Test
    void cognitiveState_withAcquisitionPlan_preservesOtherArtifacts() {
        AcquisitionPlan plan = router.route(set(
                topic("Java", RequirementPriority.CRITICAL)));
        CognitiveState state = CognitiveState.empty().withAcquisitionPlan(plan);
        assertEquals(plan, state.acquisitionPlan());
        assertNull(state.reasoningGraph());
        assertNull(state.knowledgeRequirements());
    }

    @Test
    void cognitiveState_rejectsNullPlan() {
        assertThrows(NullPointerException.class,
                () -> CognitiveState.empty().withAcquisitionPlan(null));
    }
}
