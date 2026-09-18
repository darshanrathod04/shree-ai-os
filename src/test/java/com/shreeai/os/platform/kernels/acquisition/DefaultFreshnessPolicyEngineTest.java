package com.shreeai.os.platform.kernels.acquisition;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecision;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionTarget;
import com.shreeai.os.platform.kernels.acquisition.model.FreshnessReason;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeTopic;
import com.shreeai.os.platform.kernels.acquisition.model.ProviderType;
import com.shreeai.os.platform.kernels.acquisition.model.SelectedSource;
import com.shreeai.os.platform.kernels.acquisition.model.SourceCandidate;
import com.shreeai.os.platform.kernels.acquisition.model.SourceSelectionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.SourceTrustLevel;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultFreshnessPolicyEngine;
import com.shreeai.os.platform.kernels.context.model.ConstraintEvidence;
import com.shreeai.os.platform.kernels.context.model.ContextIntelligence;
import com.shreeai.os.platform.kernels.context.model.GoalComplexity;
import com.shreeai.os.platform.kernels.context.model.GoalEvidence;
import com.shreeai.os.platform.kernels.context.model.GoalNode;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.model.FreshnessLevel;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceStatus;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFreshnessPolicyEngineTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final DefaultFreshnessPolicyEngine engine =
            new DefaultFreshnessPolicyEngine(clock);

    private static KnowledgeSource activate(
            DefaultKnowledgeSourceRegistry registry,
            String name, Map<String, String> metadata) {
        KnowledgeSource source = registry.register(KnowledgeSourceType.MARKDOWN,
                name, "loc:" + name, null, metadata);
        registry.activate(source.sourceId());
        return source;
    }

    private static KnowledgeSource aged(DefaultKnowledgeSourceRegistry registry,
                                        String name, long ageDays) {
        Map<String, String> metadata = Map.of(
                DefaultFreshnessPolicyEngine.UPDATED_AT_METADATA_KEY,
                NOW.minus(ageDays, ChronoUnit.DAYS).toString());
        return activate(registry, name, metadata);
    }

    private static KnowledgeSource fresh(DefaultKnowledgeSourceRegistry registry,
                                         String name) {
        return activate(registry, name, Map.of());
    }

    private static KnowledgeSource registered(DefaultKnowledgeSourceRegistry registry,
                                              String name) {
        return registry.register(KnowledgeSourceType.MARKDOWN,
                name, "loc:" + name, null, Map.of());
    }

    private static SelectedSource selected(String topic, KnowledgeSource source) {
        return new SelectedSource(KnowledgeTopic.deterministicTopicId(topic), topic,
                SourceCandidate.forTrust(source.sourceId(), source.name(),
                        ProviderType.WEB, SourceTrustLevel.OFFICIAL));
    }

    private static SelectedSource selected(String topic, String sourceId, String sourceName) {
        return new SelectedSource(KnowledgeTopic.deterministicTopicId(topic), topic,
                SourceCandidate.forTrust(sourceId, sourceName,
                        ProviderType.WEB, SourceTrustLevel.UNKNOWN));
    }

    private static SourceSelectionPlan selPlan(SelectedSource... sources) {
        return new SourceSelectionPlan(List.of(sources));
    }

    private static ContextIntelligence goalContext(String goalTitle) {
        GoalEvidence evidence = new GoalEvidence(goalTitle, 0, goalTitle.length());
        GoalNode goal = new GoalNode(goalTitle, 0.90, evidence);
        GoalStructure goals = GoalStructure.of(goal, List.of(),
                GoalComplexity.SIMPLE, NOW, "TEST");
        return ContextIntelligence.of(null, null, UserConstraints.empty(), goals, null);
    }

    private static ContextIntelligence constraintContext(String matchedText) {
        ConstraintEvidence evidence = new ConstraintEvidence("duration", matchedText, 0,
                matchedText.length());
        UserConstraints constraints = UserConstraints.of(null, null, null, null,
                null, null, List.of(evidence));
        return ContextIntelligence.of(null, null, constraints, null, null);
    }

        private static AcquisitionDecisionTarget target(
            String topicName, AcquisitionDecision decision, String sourceId) {
        return new AcquisitionDecisionTarget(
                KnowledgeTopic.deterministicTopicId(topicName),
                topicName, sourceId, decision);
    }

    // ---- locked decision vocabulary -------------------------------------

    @Test
    void acquisitionDecision_hasLockedDeclarationOrder() {
        assertEquals(List.of(AcquisitionDecision.USE_CACHE,
                             AcquisitionDecision.REFRESH,
                             AcquisitionDecision.ACQUIRE),
                     List.of(AcquisitionDecision.values()));
    }

    @Test
    void acquisitionDecision_requiresAcquisition_coversRefreshAndAcquire() {
        assertFalse(AcquisitionDecision.USE_CACHE.requiresAcquisition());
        assertTrue(AcquisitionDecision.REFRESH.requiresAcquisition());
        assertTrue(AcquisitionDecision.ACQUIRE.requiresAcquisition());
    }

    @Test
    void acquisitionDecision_usesCache_coversUseCacheOnly() {
        assertTrue(AcquisitionDecision.USE_CACHE.usesCache());
        assertFalse(AcquisitionDecision.REFRESH.usesCache());
        assertFalse(AcquisitionDecision.ACQUIRE.usesCache());
    }

    @Test
    void acquisitionDecision_forFreshnessLevel_mapsLockedPolicy() {
        assertEquals(AcquisitionDecision.USE_CACHE,
                AcquisitionDecision.forFreshnessLevel(FreshnessLevel.LATEST));
        assertEquals(AcquisitionDecision.USE_CACHE,
                AcquisitionDecision.forFreshnessLevel(FreshnessLevel.CURRENT));
        assertEquals(AcquisitionDecision.REFRESH,
                AcquisitionDecision.forFreshnessLevel(FreshnessLevel.RECENT));
        assertEquals(AcquisitionDecision.REFRESH,
                AcquisitionDecision.forFreshnessLevel(FreshnessLevel.OUTDATED));
        assertEquals(AcquisitionDecision.REFRESH,
                AcquisitionDecision.forFreshnessLevel(FreshnessLevel.ARCHIVED));
    }

    @Test
    void acquisitionDecision_forFreshnessLevel_rejectsNull() {
        assertThrows(NullPointerException.class,
                () -> AcquisitionDecision.forFreshnessLevel(null));
    }

    @Test
    void cachePolicyConstants_areLockedToK5Boundaries() {
        assertEquals(180L, DefaultFreshnessPolicyEngine.MAX_CACHE_AGE_DAYS);
        assertEquals(FreshnessLevel.MAX_CURRENT_DAYS,
                DefaultFreshnessPolicyEngine.MAX_CACHE_AGE_DAYS);
        assertEquals(List.of("latest", "newest", "current", "today"),
                DefaultFreshnessPolicyEngine.LATEST_REQUEST_KEYWORDS);
        assertEquals("updatedAt", DefaultFreshnessPolicyEngine.UPDATED_AT_METADATA_KEY);
        assertEquals("ingestedAt", DefaultFreshnessPolicyEngine.INGESTED_AT_METADATA_KEY);
    }

    // ---- FreshnessReason -----------------------------------------------

    @Test
    void freshnessReason_validatesInputs() {
        assertThrows(NullPointerException.class,
                () -> new FreshnessReason(null, AcquisitionDecision.USE_CACHE, "ok"));
        assertThrows(NullPointerException.class,
                () -> new FreshnessReason("s", null, "ok"));
        assertThrows(NullPointerException.class,
                () -> new FreshnessReason("s", AcquisitionDecision.USE_CACHE, null));
        assertThrows(IllegalArgumentException.class,
                () -> new FreshnessReason(" ", AcquisitionDecision.USE_CACHE, "ok"));
        assertThrows(IllegalArgumentException.class,
                () -> new FreshnessReason("s", AcquisitionDecision.USE_CACHE, "  "));
    }

    @Test
    void freshnessReason_exposesFields() {
        FreshnessReason r = new FreshnessReason("src-1",
                AcquisitionDecision.REFRESH, "Cache expired: age=290 days");
        assertEquals("src-1", r.sourceId());
        assertEquals(AcquisitionDecision.REFRESH, r.decision());
        assertEquals("Cache expired: age=290 days", r.explanation());
        assertTrue(r.toString().contains("REFRESH"));
        assertEquals(r, new FreshnessReason("src-1", AcquisitionDecision.REFRESH,
                "Cache expired: age=290 days"));
    }

        // ---- AcquisitionDecisionTarget -------------------------------------

    @Test
    void decisionTarget_requiresAcquisition_delegatesToDecision() {
        assertFalse(target("Java", AcquisitionDecision.USE_CACHE, "src").requiresAcquisition());
        assertTrue(target("Java", AcquisitionDecision.REFRESH, "src").requiresAcquisition());
        assertTrue(target("Java", AcquisitionDecision.ACQUIRE, "src").requiresAcquisition());
    }

    @Test
    void decisionTarget_withDecision_preservesIdentity() {
        AcquisitionDecisionTarget orig = target("Java",
                AcquisitionDecision.USE_CACHE, "src");
        AcquisitionDecisionTarget refreshed = orig.withDecision(
                AcquisitionDecision.REFRESH);
        assertEquals(AcquisitionDecision.REFRESH, refreshed.decision());
        assertEquals(orig.topicId(), refreshed.topicId());
        assertEquals(orig.topicName(), refreshed.topicName());
        assertEquals(orig.sourceId(), refreshed.sourceId());
        assertEquals(AcquisitionDecision.USE_CACHE, orig.decision());
        assertThrows(NullPointerException.class, () -> orig.withDecision(null));
    }

    // ---- AcquisitionDecisionPlan ---------------------------------------

    @Test
    void decisionPlan_rejectsNullLists() {
        assertThrows(NullPointerException.class,
                () -> new AcquisitionDecisionPlan(null, List.of()));
        assertThrows(NullPointerException.class,
                () -> new AcquisitionDecisionPlan(List.of(), null));
    }

    @Test
    void decisionPlan_isDeeplyImmutable() {
        AcquisitionDecisionPlan plan = new AcquisitionDecisionPlan(
                List.of(target("Java", AcquisitionDecision.USE_CACHE, "src")),
                List.of(new FreshnessReason("src", AcquisitionDecision.USE_CACHE,
                        "Cache valid")));
        assertThrows(UnsupportedOperationException.class,
                () -> plan.targets().add(null));
        assertThrows(UnsupportedOperationException.class,
                () -> plan.reasons().add(null));
    }

    @Test
    void decisionPlan_emptyHasNoTargetsOrReasons() {
        AcquisitionDecisionPlan plan = AcquisitionDecisionPlan.empty();
        assertEquals(0, plan.size());
        assertTrue(plan.targets().isEmpty());
        assertTrue(plan.reasons().isEmpty());
        assertTrue(plan.targetsRequiringAcquisition().isEmpty());
        assertTrue(plan.cachedTargets().isEmpty());
        assertEquals("AcquisitionDecisionPlan{targets=0, reasons=0}", plan.toString());
    }

    @Test
    void decisionPlan_lookupsAndFilters() {
        AcquisitionDecisionTarget t1 = target("Java", AcquisitionDecision.USE_CACHE, "s1");
        AcquisitionDecisionTarget t2 = target("Spring", AcquisitionDecision.REFRESH, "s2");
        AcquisitionDecisionTarget t3 = target("CRM", AcquisitionDecision.ACQUIRE, "s3");
        AcquisitionDecisionPlan plan = new AcquisitionDecisionPlan(
                List.of(t1, t2, t3),
                List.of(new FreshnessReason("s1", AcquisitionDecision.USE_CACHE,
                                "Cache valid"),
                        new FreshnessReason("s2", AcquisitionDecision.REFRESH,
                                "Cache expired"),
                        new FreshnessReason("s3", AcquisitionDecision.ACQUIRE,
                                "Source missing")));

        assertEquals(3, plan.size());
        assertEquals(List.of("Java", "Spring", "CRM"), plan.decidedTopicNames());
        assertEquals(AcquisitionDecision.REFRESH,
                plan.decisionFor(KnowledgeTopic.deterministicTopicId("Spring"))
                        .orElseThrow().decision());
        assertTrue(plan.decisionFor("missing").isEmpty());
        assertTrue(plan.decisionFor(null).isEmpty());

        assertEquals("Cache expired",
                plan.reasonFor("s2").orElseThrow().explanation());
        assertTrue(plan.reasonFor("ghost").isEmpty());
        assertTrue(plan.reasonFor(null).isEmpty());

        assertEquals(List.of("Spring", "CRM"),
                plan.targetsRequiringAcquisition().stream()
                        .map(AcquisitionDecisionTarget::topicName).toList());
        assertEquals(List.of("Java"),
                plan.cachedTargets().stream()
                        .map(AcquisitionDecisionTarget::topicName).toList());
                assertEquals("AcquisitionDecisionPlan{targets=3, reasons=3}", plan.toString());
    }

    // ---- engine: null safety --------------------------------------------

    @Test
    void decide_rejectsNullSelectionPlanOrRegistry() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        assertThrows(NullPointerException.class,
                () -> engine.decide(null, registry, null));
        assertThrows(NullPointerException.class,
                () -> engine.decide(SourceSelectionPlan.empty(), null, null));
        assertThrows(NullPointerException.class,
                () -> new DefaultFreshnessPolicyEngine(null));
    }

    @Test
    void decide_emptySelectionPlan_producesEmptyPlan() {
        AcquisitionDecisionPlan plan = engine.decide(SourceSelectionPlan.empty(),
                new DefaultKnowledgeSourceRegistry());
        assertEquals(0, plan.size());
        assertTrue(plan.targets().isEmpty());
    }

    // ---- engine: missing / non-active / disabled sources --------------

    @Test
    void decide_missingSource_yieldsAcquire() {
        SourceSelectionPlan selection = selPlan(
                selected("CRM", "ghost-source-id", "Company SOP"));
        AcquisitionDecisionPlan plan = engine.decide(selection,
                new DefaultKnowledgeSourceRegistry());
        assertEquals(AcquisitionDecision.ACQUIRE, plan.targets().get(0).decision());
        assertEquals("ghost-source-id", plan.targets().get(0).sourceId());
        assertTrue(plan.reasons().get(0).explanation().contains("Source missing"));
    }

    @Test
    void decide_registeredButNotActiveSource_yieldsAcquire() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = registered(registry, "Java Docs");
        assertEquals(KnowledgeSourceStatus.REGISTERED,
                registry.findById(source.sourceId()).orElseThrow().status());

        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", source)), registry);
        assertEquals(AcquisitionDecision.ACQUIRE, plan.targets().get(0).decision());
        assertTrue(plan.reasons().get(0).explanation().contains("REGISTERED"));
    }

    @Test
    void decide_disabledSource_yieldsAcquire() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = fresh(registry, "Java Docs");
        registry.disable(source.sourceId());

        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", source)), registry);
        assertEquals(AcquisitionDecision.ACQUIRE, plan.targets().get(0).decision());
        assertEquals(KnowledgeSourceStatus.DISABLED,
                registry.findById(source.sourceId()).orElseThrow().status());
                assertTrue(plan.reasons().get(0).explanation().contains("DISABLED"));
    }

    // ---- engine: locked time policy --------------------------------------

    @Test
    void decide_freshSource_yieldsUseCache() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = aged(registry, "Oracle Docs", 10);
        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", source)), registry);
        assertEquals(AcquisitionDecision.USE_CACHE, plan.targets().get(0).decision());
        assertTrue(plan.reasons().get(0).explanation().contains("Cache valid"));
    }

    @Test
    void decide_200DayOldSource_yieldsRefresh() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = aged(registry, "Spring Docs", 200);
        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Spring", source)), registry);
        assertEquals(AcquisitionDecision.REFRESH, plan.targets().get(0).decision());
        assertTrue(plan.reasons().get(0).explanation().contains("Cache expired"));
    }

    @Test
    void decide_lockedTimePolicyBands_areExact() {
        Map<Long, AcquisitionDecision> expected = new LinkedHashMap<>();
        expected.put(0L, AcquisitionDecision.USE_CACHE);
        expected.put(30L, AcquisitionDecision.USE_CACHE);
        expected.put(31L, AcquisitionDecision.USE_CACHE);
        expected.put(180L, AcquisitionDecision.USE_CACHE);
        expected.put(181L, AcquisitionDecision.REFRESH);
        expected.put(365L, AcquisitionDecision.REFRESH);
        expected.put(366L, AcquisitionDecision.REFRESH);
        expected.put(1095L, AcquisitionDecision.REFRESH);
        expected.put(1096L, AcquisitionDecision.REFRESH);

        for (Map.Entry<Long, AcquisitionDecision> e : expected.entrySet()) {
            DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
            KnowledgeSource source = aged(registry, "Docs", e.getKey());
            AcquisitionDecision decision = engine.decide(
                    selPlan(selected("Java", source)), registry)
                    .targets().get(0).decision();
            assertEquals(e.getValue(), decision,
                    "age=" + e.getKey() + " days");
        }
    }

    @Test
    void decide_futureTimestamp_isTreatedAsZeroAge() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = aged(registry, "Future Docs", -5);
        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", source)), registry);
                assertEquals(AcquisitionDecision.USE_CACHE, plan.targets().get(0).decision());
    }

    // ---- engine: timestamp precedence contract -------------------------

    @Test
    void decide_updatedAtPrecedenceOverIngestedAt() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        // updatedAt 10d ago (fresh) + ingestedAt 400d ago → updatedAt wins
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put(DefaultFreshnessPolicyEngine.UPDATED_AT_METADATA_KEY,
                NOW.minus(10, ChronoUnit.DAYS).toString());
        metadata.put(DefaultFreshnessPolicyEngine.INGESTED_AT_METADATA_KEY,
                NOW.minus(400, ChronoUnit.DAYS).toString());
        KnowledgeSource source = activate(registry, "Docs", metadata);

        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", source)), registry);
        assertEquals(AcquisitionDecision.USE_CACHE, plan.targets().get(0).decision());
    }

    @Test
    void decide_ingestedAtUsedWhenUpdatedAtAbsent() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        Map<String, String> metadata = Map.of(
                DefaultFreshnessPolicyEngine.INGESTED_AT_METADATA_KEY,
                NOW.minus(400, ChronoUnit.DAYS).toString());
        KnowledgeSource source = activate(registry, "Docs", metadata);

        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", source)), registry);
        assertEquals(AcquisitionDecision.REFRESH, plan.targets().get(0).decision());
        assertTrue(plan.reasons().get(0).explanation().contains("age=400"));
    }

    @Test
    void decide_updatedAtPrecedenceOverRegisteredAt() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = registered(registry, "Docs");
        // Re-activate with updated metadata showing stale update
        Map<String, String> metadata = Map.of(
                DefaultFreshnessPolicyEngine.UPDATED_AT_METADATA_KEY,
                NOW.minus(400, ChronoUnit.DAYS).toString());
        KnowledgeSource src2 = activate(registry, "Docs2", metadata);

        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", src2)), registry);
        assertEquals(AcquisitionDecision.REFRESH, plan.targets().get(0).decision());
    }

    @Test
    void decide_unparseableTimestamp_fallsBackToRegisteredAt() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        Map<String, String> metadata = Map.of(
                DefaultFreshnessPolicyEngine.UPDATED_AT_METADATA_KEY, "not-a-date");
        KnowledgeSource source = activate(registry, "Docs", metadata);

        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", source)), registry);
                assertEquals(AcquisitionDecision.USE_CACHE, plan.targets().get(0).decision());
    }

    // ---- engine: latest-request keyword detection ----------------------

    @Test
    void decide_latestRequestInGoalTriggersRefresh() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = aged(registry, "Spring Docs", 10);
        ContextIntelligence context = goalContext("Show me the latest Spring docs");
        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Spring", source)), registry, context);
        assertEquals(AcquisitionDecision.REFRESH, plan.targets().get(0).decision());
        assertTrue(plan.reasons().get(0).explanation().contains("latest"));
    }

    @Test
    void decide_latestRequestInConstraintTriggersRefresh() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = aged(registry, "Spring Docs", 10);
        ContextIntelligence context = constraintContext("I need the newest version today");
        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Spring", source)), registry, context);
        assertEquals(AcquisitionDecision.REFRESH, plan.targets().get(0).decision());
    }

    @Test
    void decide_allLatestKeywordsAreDetected() {
        String[] keywords = {"latest", "newest", "current", "today"};
        for (String kw : keywords) {
            DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
            KnowledgeSource source = aged(registry, "Docs", 10);
            ContextIntelligence context = goalContext("Get " + kw + " info");
            AcquisitionDecisionPlan plan = engine.decide(
                    selPlan(selected("Java", source)), registry, context);
            assertEquals(AcquisitionDecision.REFRESH, plan.targets().get(0).decision(),
                    "keyword: " + kw);
        }
    }

    @Test
    void decide_latestKeywordIsCaseInsensitive() {
        String[] variants = {"LATEST", "Latest", "latest", "NeWeSt", "Current"};
        for (String kw : variants) {
            DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
            KnowledgeSource source = aged(registry, "Docs", 10);
            ContextIntelligence context = goalContext("Find " + kw + " data");
            AcquisitionDecisionPlan plan = engine.decide(
                    selPlan(selected("Java", source)), registry, context);
            assertEquals(AcquisitionDecision.REFRESH, plan.targets().get(0).decision(),
                    "variant: " + kw);
        }
    }

    @Test
    void decide_latestKeywordRequiresWholeWordMatch() {
        String[] falsePositives = {"relatable", "newestly", "currently", "yesterday"};
        for (String word : falsePositives) {
            DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
            KnowledgeSource source = aged(registry, "Docs", 10);
            ContextIntelligence context = goalContext("Talk about " + word);
            AcquisitionDecisionPlan plan = engine.decide(
                    selPlan(selected("Java", source)), registry, context);
            assertEquals(AcquisitionDecision.USE_CACHE, plan.targets().get(0).decision(),
                    "not a match: " + word);
        }
    }

    @Test
    void decide_noContextDefaultsToTimePolicy() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = aged(registry, "Docs", 10);
        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", source)), registry, null);
        assertEquals(AcquisitionDecision.USE_CACHE, plan.targets().get(0).decision());
    }

    @Test
    void decide_latestRequestOverridesStaleCache() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = aged(registry, "Docs", 200);
        ContextIntelligence context = goalContext("I need the latest version");
        AcquisitionDecisionPlan plan = engine.decide(
                selPlan(selected("Java", source)), registry, context);
        assertEquals(AcquisitionDecision.REFRESH, plan.targets().get(0).decision());
    }

    @Test
    void decide_latestRequestDoesNotOverrideMissingSource() {
        SourceSelectionPlan selection = selPlan(
                selected("CRM", "ghost", "Ghost Source"));
        ContextIntelligence context = goalContext("Get the latest info");
        AcquisitionDecisionPlan plan = engine.decide(selection,
                new DefaultKnowledgeSourceRegistry(), context);
        assertEquals(AcquisitionDecision.ACQUIRE, plan.targets().get(0).decision());
    }

    @Test
    void detectsLatestRequest_handlesNullContext() {
        assertFalse(DefaultFreshnessPolicyEngine.detectsLatestRequest(null));
    }

    @Test
    void containsRecencyKeyword_isCaseInsensitiveAndWholeWord() {
        assertTrue(DefaultFreshnessPolicyEngine.containsRecencyKeyword("latest"));
        assertTrue(DefaultFreshnessPolicyEngine.containsRecencyKeyword("LATEST"));
        assertTrue(DefaultFreshnessPolicyEngine.containsRecencyKeyword("the latest"));
        assertFalse(DefaultFreshnessPolicyEngine.containsRecencyKeyword("relatable"));
                assertFalse(DefaultFreshnessPolicyEngine.containsRecencyKeyword(null));
        assertFalse(DefaultFreshnessPolicyEngine.containsRecencyKeyword(""));
    }

    // ---- engine: ordering, determinism, registry read-only ---------------

    @Test
    void decide_preservesCanonicalSelectionOrder() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource s1 = aged(registry, "Source1", 10);
        KnowledgeSource s2 = aged(registry, "Source2", 200);
        AcquisitionDecisionPlan plan = engine.decide(selPlan(
                selected("A", s1), selected("B", s2)), registry);

        assertEquals(List.of("A", "B"), plan.decidedTopicNames());
        assertEquals(List.of(AcquisitionDecision.USE_CACHE, AcquisitionDecision.REFRESH),
                plan.targets().stream()
                        .map(AcquisitionDecisionTarget::decision).toList());
        assertEquals(plan.targets().size(), plan.reasons().size());
    }

    @Test
    void decide_reasonsArePositionAligned() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource s1 = aged(registry, "Source1", 10);
        KnowledgeSource s2 = aged(registry, "Source2", 200);
        AcquisitionDecisionPlan plan = engine.decide(selPlan(
                selected("A", s1), selected("B", s2)), registry);

        for (int i = 0; i < plan.size(); i++) {
            assertEquals(plan.targets().get(i).sourceId(),
                    plan.reasons().get(i).sourceId());
            assertEquals(plan.targets().get(i).decision(),
                    plan.reasons().get(i).decision());
        }
    }

    @Test
    void decide_isDeterministic_sameInputsProduceEqualPlans() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = aged(registry, "Test Docs", 10);
        AcquisitionDecisionPlan plan1 = engine.decide(
                selPlan(selected("Test", source)), registry, null);
        AcquisitionDecisionPlan plan2 = engine.decide(
                selPlan(selected("Test", source)), registry, null);
        assertEquals(plan1, plan2);
    }

    @Test
    void decide_neverMutatesTheRegistry() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = aged(registry, "Docs", 10);
        int beforeCount = registry.snapshot().sources().size();
        engine.decide(selPlan(selected("Java", source)), registry, null);
        assertEquals(beforeCount, registry.snapshot().sources().size());
    }

    @Test
    void engine_isStateless_acrossDifferentInputs() {
        DefaultKnowledgeSourceRegistry r1 = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource s1 = aged(r1, "Fast", 10);
        DefaultKnowledgeSourceRegistry r2 = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource s2 = aged(r2, "Stale", 300);

        AcquisitionDecisionPlan p1 = engine.decide(
                selPlan(selected("A", s1)), r1, null);
        AcquisitionDecisionPlan p2 = engine.decide(
                selPlan(selected("B", s2)), r2, null);
        assertEquals(AcquisitionDecision.USE_CACHE, p1.targets().get(0).decision());
        assertEquals(AcquisitionDecision.REFRESH, p2.targets().get(0).decision());
    }

        @Test
    void engine_defaultConstructor_usesSystemClock() {
        DefaultFreshnessPolicyEngine defaultEngine =
                new DefaultFreshnessPolicyEngine();
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = fresh(registry, "Docs");
        AcquisitionDecisionPlan plan = defaultEngine.decide(
                selPlan(selected("Java", source)), registry, null);
        assertEquals(AcquisitionDecision.USE_CACHE, plan.targets().get(0).decision());
    }

    @Test
    void decide_clockDeterminism_sameClockProducesSameResult() {
        Instant fixed = Instant.parse("2026-06-01T00:00:00Z");
        Clock c = Clock.fixed(fixed, ZoneOffset.UTC);
        DefaultFreshnessPolicyEngine e = new DefaultFreshnessPolicyEngine(c);
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = fresh(registry, "Docs");
        AcquisitionDecisionPlan plan = e.decide(
                selPlan(selected("Java", source)), registry, null);
        assertEquals(AcquisitionDecision.USE_CACHE, plan.targets().get(0).decision());
    }
}