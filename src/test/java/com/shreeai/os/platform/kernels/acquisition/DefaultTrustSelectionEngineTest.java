package com.shreeai.os.platform.kernels.acquisition;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionTarget;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeTopic;
import com.shreeai.os.platform.kernels.acquisition.model.ProviderCapability;
import com.shreeai.os.platform.kernels.acquisition.model.ProviderType;
import com.shreeai.os.platform.kernels.acquisition.model.RequirementPriority;
import com.shreeai.os.platform.kernels.acquisition.model.SelectedSource;
import com.shreeai.os.platform.kernels.acquisition.model.SourceCandidate;
import com.shreeai.os.platform.kernels.acquisition.model.SourceSelectionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.SourceTrustLevel;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultProviderRouter;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultTrustSelectionEngine;
import com.shreeai.os.platform.kernels.acquisition.engine.TrustSelectionEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceStatus;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultTrustSelectionEngine} and the
 * K0.6.3 trust and source selection models.
 *
 * <p>Every test asserts exact, locked behavior: the locked authority policy
 * (OFFICIAL 1.00, ENTERPRISE 0.95, VERIFIED 0.90, COMMUNITY 0.70, UNKNOWN 0.40),
 * ACTIVE-only eligibility, provider filtering, the documented cross-provider
 * topic fallback, deterministic ranking and tie-breaks, canonical plan order,
 * registry read-only usage and full determinism.</p>
 */
class DefaultTrustSelectionEngineTest {

    private final DefaultTrustSelectionEngine engine = new DefaultTrustSelectionEngine();

    // ---- deterministic fixtures ---------------------------------------------

    private static KnowledgeTopic topic(String name, RequirementPriority priority) {
        return new KnowledgeTopic(KnowledgeTopic.deterministicTopicId(name),
                name, priority, 0.90);
    }

    private static KnowledgeRequirementSet set(KnowledgeTopic... topics) {
        return new KnowledgeRequirementSet(List.of(topics), List.of());
    }

    private static AcquisitionTarget target(String topicName, ProviderType provider,
                                            RequirementPriority priority) {
        return new AcquisitionTarget(KnowledgeTopic.deterministicTopicId(topicName),
                topicName, provider, priority);
    }

    private static AcquisitionPlan plan(AcquisitionTarget... targets) {
        return new AcquisitionPlan(List.of(targets));
    }

    /** Registers a source and returns it without activating it. */
    private static KnowledgeSource register(KnowledgeSourceRegistry registry,
                                            KnowledgeSourceType type,
                                            String name,
                                            String location,
                                            Map<String, String> metadata) {
        return registry.register(type, name, location, null, metadata);
    }

    /** Registers and activates a source, returning the registered source. */
    private static KnowledgeSource active(KnowledgeSourceRegistry registry,
                                          KnowledgeSourceType type,
                                          String name,
                                          String location,
                                          Map<String, String> metadata) {
        KnowledgeSource source = register(registry, type, name, location, metadata);
        registry.activate(source.sourceId());
        return source;
    }

    /** Registers and activates a source at a name-derived location. */
    private static KnowledgeSource active(KnowledgeSourceRegistry registry,
                                          KnowledgeSourceType type,
                                          String name,
                                          Map<String, String> metadata) {
        return active(registry, type, name, "loc:" + name, metadata);
    }

    private static void assertAuthority(double expected, SourceTrustLevel level) {
        assertEquals(expected, level.authorityScore(), 1e-9);
    }

    private static List<String> selectedTopics(SourceSelectionPlan selectionPlan) {
        return selectionPlan.selections().stream().map(SelectedSource::topicName).toList();
    }

    // ---- locked trust policy --------------------------------------------------

    @Test
    void sourceTrustLevel_hasLockedAuthorityScores() {
        assertAuthority(1.00, SourceTrustLevel.OFFICIAL);
        assertAuthority(0.95, SourceTrustLevel.ENTERPRISE);
        assertAuthority(0.90, SourceTrustLevel.VERIFIED);
        assertAuthority(0.70, SourceTrustLevel.COMMUNITY);
        assertAuthority(0.40, SourceTrustLevel.UNKNOWN);
    }

    @Test
    void sourceTrustLevel_hasLockedRanksMatchingAuthorityOrder() {
        assertEquals(List.of(SourceTrustLevel.OFFICIAL, SourceTrustLevel.VERIFIED,
                        SourceTrustLevel.ENTERPRISE, SourceTrustLevel.COMMUNITY,
                        SourceTrustLevel.UNKNOWN),
                List.of(SourceTrustLevel.values()));
        assertEquals(0, SourceTrustLevel.OFFICIAL.rank());
        assertEquals(1, SourceTrustLevel.ENTERPRISE.rank());
        assertEquals(2, SourceTrustLevel.VERIFIED.rank());
        assertEquals(3, SourceTrustLevel.COMMUNITY.rank());
        assertEquals(4, SourceTrustLevel.UNKNOWN.rank());
        // The ranking is exactly the authority-score-descending order.
        assertEquals(List.of(SourceTrustLevel.OFFICIAL, SourceTrustLevel.ENTERPRISE,
                        SourceTrustLevel.VERIFIED, SourceTrustLevel.COMMUNITY,
                        SourceTrustLevel.UNKNOWN),
                java.util.stream.Stream.of(SourceTrustLevel.values())
                        .sorted(java.util.Comparator.comparingInt(SourceTrustLevel::rank))
                        .toList());
    }

    @Test
    void sourceTrustLevel_fromLabel_isTrimmedCaseInsensitiveAndNeverFails() {
        assertEquals(SourceTrustLevel.OFFICIAL, SourceTrustLevel.fromLabel("official"));
        assertEquals(SourceTrustLevel.OFFICIAL, SourceTrustLevel.fromLabel("  OFFICIAL "));
        assertEquals(SourceTrustLevel.ENTERPRISE, SourceTrustLevel.fromLabel("Enterprise"));
        assertEquals(SourceTrustLevel.UNKNOWN, SourceTrustLevel.fromLabel("bogus"));
        assertEquals(SourceTrustLevel.UNKNOWN, SourceTrustLevel.fromLabel(null));
        assertEquals(SourceTrustLevel.UNKNOWN, SourceTrustLevel.fromLabel("  "));
    }

    @Test
    void sourceTrustLevel_parse_distinguishesDeclaredFromUndeclared() {
        assertTrue(SourceTrustLevel.parse("verified").isPresent());
        assertTrue(SourceTrustLevel.parse(null).isEmpty());
        assertTrue(SourceTrustLevel.parse("high").isEmpty());
    }

    @Test
    void sourceTrustLevel_lockedAuthorityScoreRejectsNull() {
        assertEquals(0.70, SourceTrustLevel.lockedAuthorityScore(
                SourceTrustLevel.COMMUNITY), 1e-9);
        assertThrows(NullPointerException.class,
                () -> SourceTrustLevel.lockedAuthorityScore(null));
    }

    @Test
    void sourceTrustLevel_toString_isDeterministic() {
        assertEquals("OFFICIAL(1.00)", SourceTrustLevel.OFFICIAL.toString());
        assertEquals("UNKNOWN(0.40)", SourceTrustLevel.UNKNOWN.toString());
    }

    // ---- locked selection models -----------------------------------------------

    @Test
    void sourceCandidate_forTrust_usesLockedScoreAndCarriesProvider() {
        SourceCandidate candidate = SourceCandidate.forTrust("src-1", "Spring Docs",
                ProviderType.OFFICIAL_DOCS, SourceTrustLevel.VERIFIED);
        assertEquals("src-1", candidate.sourceId());
        assertEquals("Spring Docs", candidate.sourceName());
        assertEquals(ProviderType.OFFICIAL_DOCS, candidate.provider());
        assertEquals(SourceTrustLevel.VERIFIED, candidate.trustLevel());
        assertEquals(0.90, candidate.authorityScore(), 1e-9);
    }

    @Test
    void sourceCandidate_rejectsBadInput() {
        assertThrows(NullPointerException.class,
                () -> new SourceCandidate(null, "n", ProviderType.WEB,
                        SourceTrustLevel.UNKNOWN, 0.40));
        assertThrows(NullPointerException.class,
                () -> new SourceCandidate("id", "n", null,
                        SourceTrustLevel.UNKNOWN, 0.40));
        assertThrows(NullPointerException.class,
                () -> new SourceCandidate("id", "n", ProviderType.WEB, null, 0.40));
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCandidate(" ", "n", ProviderType.WEB,
                        SourceTrustLevel.UNKNOWN, 0.40));
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCandidate("id", "  ", ProviderType.WEB,
                        SourceTrustLevel.UNKNOWN, 0.40));
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCandidate("id", "n", ProviderType.WEB,
                        SourceTrustLevel.UNKNOWN, 1.01));
        assertThrows(NullPointerException.class,
                () -> SourceCandidate.forTrust("id", "n", ProviderType.WEB, null));
    }

    @Test
    void selectedSource_exposesSourceAccessors() {
        SourceCandidate candidate = SourceCandidate.forTrust("src-9", "Java Docs",
                ProviderType.OFFICIAL_DOCS, SourceTrustLevel.OFFICIAL);
        SelectedSource selection = new SelectedSource("topic-1", "Java", candidate);
        assertEquals("topic-1", selection.topicId());
        assertEquals("Java", selection.topicName());
        assertEquals("src-9", selection.sourceId());
        assertEquals("Java Docs", selection.sourceName());
        assertEquals(1.00, selection.authorityScore(), 1e-9);
        assertEquals(candidate, selection.source());
    }

    @Test
    void selectedSource_rejectsNullsAndBlanks() {
        SourceCandidate candidate = SourceCandidate.forTrust("id", "n",
                ProviderType.WEB, SourceTrustLevel.UNKNOWN);
        assertThrows(NullPointerException.class,
                () -> new SelectedSource(null, "Java", candidate));
        assertThrows(NullPointerException.class,
                () -> new SelectedSource("t", null, candidate));
        assertThrows(NullPointerException.class,
                () -> new SelectedSource("t", "Java", null));
        assertThrows(IllegalArgumentException.class,
                () -> new SelectedSource(" ", "Java", candidate));
        assertThrows(IllegalArgumentException.class,
                () -> new SelectedSource("t", " ", candidate));
    }

    // ---- selection: null safety, empty inputs, lifecycle filtering ---------------

    @Test
    void select_rejectsNullPlanOrRegistry() {
        assertThrows(NullPointerException.class,
                () -> engine.select(null, new DefaultKnowledgeSourceRegistry()));
        assertThrows(NullPointerException.class,
                () -> engine.select(AcquisitionPlan.empty(), null));
        assertThrows(NullPointerException.class,
                () -> engine.select(null, null));
    }

    @Test
    void select_emptyPlan_producesEmptySelectionPlan() {
        assertEquals(0, engine.select(AcquisitionPlan.empty(),
                new DefaultKnowledgeSourceRegistry()).size());
    }

    @Test
    void select_emptyRegistry_omitsEveryTopicAndNeverFabricates() {
        AcquisitionPlan acquisitionPlan = plan(
                target("Java", ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL),
                target("Spring", ProviderType.WEB, RequirementPriority.HIGH));
        SourceSelectionPlan selectionPlan = engine.select(acquisitionPlan,
                new DefaultKnowledgeSourceRegistry());
        assertEquals(0, selectionPlan.size());
        assertTrue(selectionPlan.selections().isEmpty());
    }

    @Test
    void select_registeredButNotActiveSource_isIgnored() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = register(registry, KnowledgeSourceType.MARKDOWN,
                "Java Docs", "loc:java",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS"));
        assertEquals(KnowledgeSourceStatus.REGISTERED,
                registry.findById(source.sourceId()).orElseThrow().status());

        AcquisitionPlan acquisitionPlan = plan(
                target("Java", ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL));
        assertEquals(0, engine.select(acquisitionPlan, registry).size());

        registry.activate(source.sourceId());
        assertEquals(1, engine.select(acquisitionPlan, registry).size());
    }

    @Test
    void select_disabledSource_isIgnored() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = active(registry, KnowledgeSourceType.MARKDOWN,
                "Java Docs", Map.of("authority", "official", "provider", "OFFICIAL_DOCS"));
        registry.disable(source.sourceId());

        assertEquals(0, engine.select(plan(target("Java", ProviderType.OFFICIAL_DOCS,
                RequirementPriority.CRITICAL)), registry).size());
        assertEquals(KnowledgeSourceStatus.DISABLED,
                registry.findById(source.sourceId()).orElseThrow().status());
    }

    @Test
    void select_singleActiveCompatibleSource_isSelectedWithFullProvenance() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource official = active(registry, KnowledgeSourceType.MARKDOWN,
                "Java Official Docs",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS"));
        AcquisitionPlan acquisitionPlan = plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL));

        SourceSelectionPlan selectionPlan = engine.select(acquisitionPlan, registry);

        assertEquals(1, selectionPlan.size());
        SelectedSource selection = selectionPlan.selections().get(0);
        assertEquals(KnowledgeTopic.deterministicTopicId("Java"), selection.topicId());
        assertEquals("Java", selection.topicName());
        assertEquals(official.sourceId(), selection.sourceId());
        assertEquals("Java Official Docs", selection.sourceName());
        assertEquals(ProviderType.OFFICIAL_DOCS, selection.source().provider());
        assertEquals(SourceTrustLevel.OFFICIAL, selection.source().trustLevel());
        assertEquals(1.00, selection.authorityScore(), 1e-9);
        assertEquals(List.of("Java"), selectionPlan.selectedTopicNames());
        assertEquals(List.of("Java Official Docs"), selectionPlan.selectedSourceNames());
        assertEquals(selection, selectionPlan
                .selectionFor(KnowledgeTopic.deterministicTopicId("Java")).orElseThrow());
    }

    // ---- selection: deterministic ranking and tie-breaks -------------------------

    @Test
    void select_prefersHighestAuthorityScore() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.WEB, "Community Wiki",
                Map.of("authority", "community", "provider", "OFFICIAL_DOCS"));
        KnowledgeSource official = active(registry, KnowledgeSourceType.MARKDOWN,
                "Official Docs",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS"));

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);

        assertEquals(1, selectionPlan.size());
        assertEquals(official.sourceId(), selectionPlan.selections().get(0).sourceId());
        assertEquals(SourceTrustLevel.OFFICIAL,
                selectionPlan.selections().get(0).source().trustLevel());
        assertEquals(1.00, selectionPlan.selections().get(0).authorityScore(), 1e-9);
    }

    @Test
    void select_ranksEveryTrustLevelByLockedAuthority_deterministically() {
        Map<String, String> community = Map.of("authority", "community",
                "provider", "OFFICIAL_DOCS");
        Map<String, String> verified = Map.of("authority", "verified",
                "provider", "OFFICIAL_DOCS");
        Map<String, String> enterprise = Map.of("authority", "enterprise",
                "provider", "OFFICIAL_DOCS");
        Map<String, String> unknown = Map.of("provider", "OFFICIAL_DOCS");
        AcquisitionPlan acquisitionPlan = plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL));

        DefaultKnowledgeSourceRegistry ascending = new DefaultKnowledgeSourceRegistry();
        active(ascending, KnowledgeSourceType.WEB, "A Community", community);
        active(ascending, KnowledgeSourceType.WEB, "B Verified", verified);
        active(ascending, KnowledgeSourceType.WEB, "C Enterprise", enterprise);
        active(ascending, KnowledgeSourceType.WEB, "D Unknown", unknown);

        DefaultKnowledgeSourceRegistry descending = new DefaultKnowledgeSourceRegistry();
        active(descending, KnowledgeSourceType.WEB, "D Unknown", unknown);
        active(descending, KnowledgeSourceType.WEB, "C Enterprise", enterprise);
        active(descending, KnowledgeSourceType.WEB, "B Verified", verified);
        active(descending, KnowledgeSourceType.WEB, "A Community", community);

        SourceSelectionPlan fromAscending = engine.select(acquisitionPlan, ascending);
        SourceSelectionPlan fromDescending = engine.select(acquisitionPlan, descending);

        assertEquals("C Enterprise", fromAscending.selections().get(0).sourceName());
        assertEquals(SourceTrustLevel.ENTERPRISE,
                fromAscending.selections().get(0).source().trustLevel());
        assertEquals(0.95, fromAscending.selections().get(0).authorityScore(), 1e-9);
        assertEquals(fromAscending, fromDescending);
    }

    @Test
    void select_sameTrustLevel_prefersSourceNameCaseInsensitivelyAscending() {
        Map<String, String> community = Map.of("authority", "community",
                "provider", "OFFICIAL_DOCS");
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.WEB, "Zeta Wiki", community);
        active(registry, KnowledgeSourceType.WEB, "alpha docs", community);
        active(registry, KnowledgeSourceType.WEB, "Beta Docs", community);

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);

        assertEquals("alpha docs", selectionPlan.selections().get(0).sourceName());
        assertEquals(SourceTrustLevel.COMMUNITY,
                selectionPlan.selections().get(0).source().trustLevel());
    }

    @Test
    void select_fullTie_prefersLowestSourceId() {
        Map<String, String> official = Map.of("authority", "official",
                "provider", "OFFICIAL_DOCS");
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource first = active(registry, KnowledgeSourceType.MARKDOWN,
                "Shared Docs", "loc:a", official);
        KnowledgeSource second = active(registry, KnowledgeSourceType.MARKDOWN,
                "Shared Docs", "loc:b", official);

        assertNotEquals(first.sourceId(), second.sourceId());
        String expectedId = first.sourceId().compareTo(second.sourceId()) <= 0
                ? first.sourceId() : second.sourceId();
        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);

        assertEquals(expectedId, selectionPlan.selections().get(0).sourceId());
        assertEquals(1, selectionPlan.size());
    }

    // ---- selection: provider filtering and cross-provider fallback ---------------

    @Test
    void select_providerCompatibleSourceBeatsHigherAuthorityFallback() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Official GitHub Docs",
                Map.of("authority", "official", "provider", "GITHUB"));
        KnowledgeSource community = active(registry, KnowledgeSourceType.WEB,
                "Community Wiki",
                Map.of("authority", "community", "provider", "OFFICIAL_DOCS"));

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);

        assertEquals(community.sourceId(), selectionPlan.selections().get(0).sourceId());
        assertEquals(SourceTrustLevel.COMMUNITY,
                selectionPlan.selections().get(0).source().trustLevel());
    }

    @Test
    void select_crossProviderFallback_selectsTopicCompatibleSource() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource api = active(registry, KnowledgeSourceType.API, "Java API Service",
                Map.of("authority", "official", "provider", "API"));

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);

        assertEquals(1, selectionPlan.size());
        SelectedSource selection = selectionPlan.selections().get(0);
        assertEquals(api.sourceId(), selection.sourceId());
        assertEquals(SourceTrustLevel.OFFICIAL, selection.source().trustLevel());
        assertEquals(ProviderType.OFFICIAL_DOCS, selection.source().provider());
    }

    @Test
    void select_providerCompatibleSetIsRankedIndependentlyOfTopicOnlySet() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Github Only",
                Map.of("authority", "official", "provider", "GITHUB"));
        active(registry, KnowledgeSourceType.WEB, "Web Community",
                Map.of("authority", "community", "provider", "WEB"));
        KnowledgeSource verified = active(registry, KnowledgeSourceType.WEB,
                "Web Verified", Map.of("authority", "verified", "provider", "WEB"));

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.WEB, RequirementPriority.HIGH)), registry);

        assertEquals(verified.sourceId(), selectionPlan.selections().get(0).sourceId());
        assertEquals(0.90, selectionPlan.selections().get(0).authorityScore(), 1e-9);
    }

    @Test
    void select_declaredProvidersList_isHonoredWithProviderOverride() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource multi = active(registry, KnowledgeSourceType.MARKDOWN, "Multi Docs",
                Map.of("authority", "verified", "providers", "GITHUB, API",
                        "provider", "OFFICIAL_DOCS"));

        SourceSelectionPlan selectionPlan = engine.select(plan(
                target("Java", ProviderType.GITHUB, RequirementPriority.CRITICAL),
                target("Spring", ProviderType.API, RequirementPriority.HIGH),
                target("Roadmap", ProviderType.OFFICIAL_DOCS,
                        RequirementPriority.MEDIUM)), registry);

        assertEquals(3, selectionPlan.size());
        assertEquals(multi.sourceId(), selectionPlan.selections().get(0).sourceId());
        assertEquals(ProviderType.GITHUB, selectionPlan.selections().get(0).source().provider());
        assertEquals(ProviderType.API, selectionPlan.selections().get(1).source().provider());
        assertEquals(ProviderType.OFFICIAL_DOCS,
                selectionPlan.selections().get(2).source().provider());
    }

    // ---- selection: explicit topic scope -----------------------------------------

    @Test
    void select_topicScopedSource_onlyServesDeclaredTopics() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Java Only Docs",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS",
                        "supportedTopics", "Java"));

        SourceSelectionPlan selectionPlan = engine.select(plan(
                target("Java", ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL),
                target("Spring", ProviderType.OFFICIAL_DOCS, RequirementPriority.HIGH)),
                registry);

        assertEquals(1, selectionPlan.size());
        assertEquals(List.of("Java"), selectedTopics(selectionPlan));
    }

    @Test
    void select_topicScope_isTrimmedCaseInsensitiveExactMatchWithoutFuzzy() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Scoped Docs",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS",
                        "supportedTopics", " java , Spring "));

        SourceSelectionPlan matched = engine.select(plan(
                target("Java", ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL),
                target("SPRING", ProviderType.OFFICIAL_DOCS, RequirementPriority.HIGH)),
                registry);
        assertEquals(List.of("Java", "SPRING"), selectedTopics(matched));

        SourceSelectionPlan fuzzy = engine.select(plan(target("Java Advanced",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.HIGH)), registry);
        assertEquals(0, fuzzy.size());
    }

    @Test
    void select_topicsAliasKey_isAccepted() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Alias Docs",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS",
                        "topics", "Java"));

        SourceSelectionPlan selectionPlan = engine.select(plan(
                target("Java", ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL),
                target("Spring", ProviderType.OFFICIAL_DOCS, RequirementPriority.HIGH)),
                registry);

        assertEquals(List.of("Java"), selectedTopics(selectionPlan));
    }

    @Test
    void select_unscopedSource_servesEveryTopicOfItsProvider() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource unscoped = active(registry, KnowledgeSourceType.MARKDOWN,
                "Unscoped Docs",
                Map.of("authority", "verified", "provider", "OFFICIAL_DOCS"));

        SourceSelectionPlan selectionPlan = engine.select(plan(
                target("Java", ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL),
                target("Spring", ProviderType.OFFICIAL_DOCS, RequirementPriority.HIGH),
                target("Roadmap", ProviderType.OFFICIAL_DOCS, RequirementPriority.MEDIUM)),
                registry);

        assertEquals(3, selectionPlan.size());
        assertEquals(List.of("Java", "Spring", "Roadmap"),
                selectedTopics(selectionPlan));
        assertTrue(selectionPlan.selections().stream()
                .allMatch(selection -> selection.sourceId().equals(unscoped.sourceId())));
    }

    // ---- selection: provider compatibility derivation ----------------------------

    /** The single derived provider of a source declaring no provider metadata. */
    private static ProviderType derivedProvider(KnowledgeSourceType type) {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = active(registry, type, "Derived " + type,
                Map.of("authority", "official"));
        assertEquals(1, DefaultTrustSelectionEngine.capabilitiesOf(source).size());
        return DefaultTrustSelectionEngine.capabilitiesOf(source).get(0).provider();
    }

    @Test
    void select_defaultProvider_isDerivedFromKnowledgeSourceType() {
        assertEquals(ProviderType.LOCAL_FILES, derivedProvider(KnowledgeSourceType.PDF));
        assertEquals(ProviderType.LOCAL_FILES, derivedProvider(KnowledgeSourceType.MARKDOWN));
        assertEquals(ProviderType.LOCAL_FILES, derivedProvider(KnowledgeSourceType.FOLDER));
        assertEquals(ProviderType.LOCAL_FILES, derivedProvider(KnowledgeSourceType.JSON));
        assertEquals(ProviderType.LOCAL_FILES, derivedProvider(KnowledgeSourceType.TEXT));
        assertEquals(ProviderType.WEB, derivedProvider(KnowledgeSourceType.WEB));
        assertEquals(ProviderType.API, derivedProvider(KnowledgeSourceType.API));
        assertEquals(ProviderType.DATABASE, derivedProvider(KnowledgeSourceType.DATABASE));
    }

    @Test
    void select_undeclaredProviderMetadataUsesTypeDefault() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource pdf = active(registry, KnowledgeSourceType.PDF, "Local Pdf",
                Map.of("authority", "official"));
        KnowledgeSource web = active(registry, KnowledgeSourceType.WEB, "Web Source",
                Map.of("authority", "official"));
        KnowledgeSource database = active(registry, KnowledgeSourceType.DATABASE,
                "Database Source", Map.of("authority", "official"));
        KnowledgeSource api = active(registry, KnowledgeSourceType.API, "Api Source",
                Map.of("authority", "official"));

        SourceSelectionPlan selectionPlan = engine.select(plan(
                target("Java", ProviderType.LOCAL_FILES, RequirementPriority.CRITICAL),
                target("Spring", ProviderType.WEB, RequirementPriority.HIGH),
                target("Schema", ProviderType.DATABASE, RequirementPriority.MEDIUM),
                target("Endpoint", ProviderType.API, RequirementPriority.LOW)), registry);

        assertEquals(4, selectionPlan.size());
        assertEquals(List.of(pdf.sourceId(), web.sourceId(), database.sourceId(),
                        api.sourceId()),
                selectionPlan.selections().stream().map(SelectedSource::sourceId).toList());
    }

    @Test
    void select_declaredProvider_overridesTypeDefault() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource overridden = active(registry, KnowledgeSourceType.MARKDOWN,
                "Markdown Override",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS"));

        assertEquals(ProviderType.OFFICIAL_DOCS,
                DefaultTrustSelectionEngine.capabilitiesOf(overridden).get(0).provider());

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);
        assertEquals(overridden.sourceId(), selectionPlan.selections().get(0).sourceId());

        // The overridden type default (LOCAL_FILES) is no longer advertised, but
        // the documented cross-provider topic fallback still applies.
        SourceSelectionPlan fallback = engine.select(plan(target("Java",
                ProviderType.LOCAL_FILES, RequirementPriority.CRITICAL)), registry);
        assertEquals(1, fallback.size());
        assertEquals(ProviderType.LOCAL_FILES,
                fallback.selections().get(0).source().provider());
    }

    @Test
    void select_unrecognizedProviderMetadata_fallsBackToTypeDefault() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = active(registry, KnowledgeSourceType.MARKDOWN,
                "Garbage Provider",
                Map.of("authority", "official", "provider", "banana"));

        assertEquals(ProviderType.LOCAL_FILES,
                DefaultTrustSelectionEngine.capabilitiesOf(source).get(0).provider());
        assertEquals(1, engine.select(plan(target("Java", ProviderType.LOCAL_FILES,
                RequirementPriority.CRITICAL)), registry).size());
    }

    @Test
    void select_providerMetadata_isCaseInsensitiveExactWithoutFuzzy() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource hyphenated = active(registry, KnowledgeSourceType.WEB,
                "Hyphenated", Map.of("authority", "official", "provider", "official-docs"));
        KnowledgeSource spaced = active(registry, KnowledgeSourceType.WEB,
                "Spaced", Map.of("authority", "official", "provider", " OFFICIAL DOCS "));

        assertEquals(ProviderType.OFFICIAL_DOCS,
                DefaultTrustSelectionEngine.capabilitiesOf(hyphenated).get(0).provider());
        assertEquals(ProviderType.OFFICIAL_DOCS,
                DefaultTrustSelectionEngine.capabilitiesOf(spaced).get(0).provider());

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);
        assertEquals(1, selectionPlan.size());
        assertEquals("Hyphenated", selectionPlan.selections().get(0).sourceName());
    }

    // ---- trust declaration aliases and observability -----------------------------

    @Test
    void select_trustAlias_isAcceptedWhenAuthorityIsAbsent() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Alias Trust",
                Map.of("trust", "verified", "provider", "OFFICIAL_DOCS"));

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);

        assertEquals(SourceTrustLevel.VERIFIED,
                selectionPlan.selections().get(0).source().trustLevel());
        assertEquals(0.90, selectionPlan.selections().get(0).authorityScore(), 1e-9);
    }

    @Test
    void select_authorityDeclarationWinsOverTrustAlias() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Conflicting Trust",
                Map.of("authority", "official", "trust", "community",
                        "provider", "OFFICIAL_DOCS"));

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);

        assertEquals(SourceTrustLevel.OFFICIAL,
                selectionPlan.selections().get(0).source().trustLevel());
        assertEquals(1.00, selectionPlan.selections().get(0).authorityScore(), 1e-9);
    }

    @Test
    void select_undeclaredTrust_resolvesToUnknownNeverInferred() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Java Official Docs",
                Map.of("provider", "OFFICIAL_DOCS"));

        SourceSelectionPlan selectionPlan = engine.select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);

        assertEquals(SourceTrustLevel.UNKNOWN,
                selectionPlan.selections().get(0).source().trustLevel());
        assertEquals(0.40, selectionPlan.selections().get(0).authorityScore(), 1e-9);
    }

    @Test
    void capabilitiesOf_materializesDeclaredCapabilities() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = register(registry, KnowledgeSourceType.MARKDOWN,
                "Multi Docs", "loc:multi",
                Map.of("authority", "verified", "providers", "GITHUB, API",
                        "supportedTopics", "Java, Spring"));

        List<ProviderCapability> capabilities =
                DefaultTrustSelectionEngine.capabilitiesOf(source);

        assertEquals(2, capabilities.size());
        assertEquals(ProviderType.GITHUB, capabilities.get(0).provider());
        assertEquals(ProviderType.API, capabilities.get(1).provider());
        assertEquals(List.of("Java", "Spring"), capabilities.get(0).supportedTopics());
        assertTrue(capabilities.get(0).supports("java"));
        assertFalse(capabilities.get(0).supports("Java Advanced"));
    }

    @Test
    void capabilitiesOf_rejectsNull() {
        assertThrows(NullPointerException.class,
                () -> DefaultTrustSelectionEngine.capabilitiesOf(null));
    }

    // ---- ordering, determinism and registry read-only usage ----------------------

    @Test
    void engine_implementsTheTrustSelectionEnginePort() {
        TrustSelectionEngine port = new DefaultTrustSelectionEngine();
        SourceSelectionPlan selectionPlan = port.select(AcquisitionPlan.empty(),
                new DefaultKnowledgeSourceRegistry());
        assertEquals(0, selectionPlan.size());
        assertEquals(SourceSelectionPlan.empty(), selectionPlan);
    }

    @Test
    void select_preservesCanonicalPlanOrderAndOmitsUnsatisfiableTopics() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Scoped Docs",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS",
                        "supportedTopics", "Java, Roadmap"));

        SourceSelectionPlan selectionPlan = engine.select(plan(
                target("Java", ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL),
                target("Spring", ProviderType.OFFICIAL_DOCS, RequirementPriority.HIGH),
                target("Roadmap", ProviderType.OFFICIAL_DOCS, RequirementPriority.MEDIUM)),
                registry);

        assertEquals(List.of("Java", "Roadmap"), selectedTopics(selectionPlan));
        assertEquals(List.of("Scoped Docs", "Scoped Docs"),
                selectionPlan.selectedSourceNames());
        assertEquals(KnowledgeTopic.deterministicTopicId("Java"),
                selectionPlan.selections().get(0).topicId());
        assertEquals(KnowledgeTopic.deterministicTopicId("Roadmap"),
                selectionPlan.selections().get(1).topicId());
    }

    @Test
    void select_isDeterministic_regardlessOfRegistrationOrder() {
        Map<String, String> official = Map.of("authority", "official",
                "provider", "OFFICIAL_DOCS");
        Map<String, String> community = Map.of("authority", "community",
                "provider", "OFFICIAL_DOCS");

        DefaultKnowledgeSourceRegistry first = new DefaultKnowledgeSourceRegistry();
        active(first, KnowledgeSourceType.MARKDOWN, "Java Docs", "loc:java", official);
        active(first, KnowledgeSourceType.WEB, "Community Wiki", "loc:wiki", community);

        DefaultKnowledgeSourceRegistry second = new DefaultKnowledgeSourceRegistry();
        active(second, KnowledgeSourceType.WEB, "Community Wiki", "loc:wiki", community);
        active(second, KnowledgeSourceType.MARKDOWN, "Java Docs", "loc:java", official);

        AcquisitionPlan acquisitionPlan = plan(
                target("Java", ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL),
                target("Spring", ProviderType.OFFICIAL_DOCS, RequirementPriority.HIGH));

        SourceSelectionPlan fromFirst = engine.select(acquisitionPlan, first);
        SourceSelectionPlan repeated = engine.select(acquisitionPlan, first);
        SourceSelectionPlan fromSecond = engine.select(acquisitionPlan, second);

        assertEquals(2, fromFirst.size());
        assertEquals(fromFirst, repeated);
        assertEquals(fromFirst, fromSecond);
        assertEquals(List.of("Java Docs", "Java Docs"),
                fromFirst.selectedSourceNames());
    }

    @Test
    void select_neverMutatesTheRegistry() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource source = active(registry, KnowledgeSourceType.MARKDOWN,
                "Java Docs", Map.of("authority", "official", "provider", "OFFICIAL_DOCS"));
        int sizeBefore = registry.snapshot().size();

        engine.select(plan(target("Java", ProviderType.OFFICIAL_DOCS,
                RequirementPriority.CRITICAL)), registry);

        assertEquals(sizeBefore, registry.snapshot().size());
        KnowledgeSource after = registry.findById(source.sourceId()).orElseThrow();
        assertEquals(source.sourceId(), after.sourceId());
        assertEquals(KnowledgeSourceStatus.ACTIVE, after.status());
        assertEquals(source.metadata(), after.metadata());
    }

    // ---- CognitiveState integration ---------------------------------------------

    private static SourceSelectionPlan oneSelectionPlan() {
        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        active(registry, KnowledgeSourceType.MARKDOWN, "Java Docs",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS"));
        return new DefaultTrustSelectionEngine().select(plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL)), registry);
    }

    @Test
    void cognitiveState_withSourceSelectionPlan_storesArtifact() {
        SourceSelectionPlan selectionPlan = oneSelectionPlan();
        CognitiveState state = CognitiveState.empty().withSourceSelectionPlan(selectionPlan);
        assertEquals(selectionPlan, state.sourceSelectionPlan());
        assertEquals(1, state.sourceSelectionPlan().size());
    }

    @Test
    void cognitiveState_withSourceSelectionPlan_preservesOtherArtifacts() {
        KnowledgeRequirementSet requirements = set(topic("Java",
                RequirementPriority.CRITICAL));
        AcquisitionPlan acquisitionPlan = plan(target("Java",
                ProviderType.OFFICIAL_DOCS, RequirementPriority.CRITICAL));

        CognitiveState state = CognitiveState.empty()
                .withKnowledgeRequirements(requirements)
                .withAcquisitionPlan(acquisitionPlan)
                .withSourceSelectionPlan(oneSelectionPlan());

        assertEquals(requirements, state.knowledgeRequirements());
        assertEquals(acquisitionPlan, state.acquisitionPlan());
        assertEquals(1, state.sourceSelectionPlan().size());
        assertEquals("Java Docs",
                state.sourceSelectionPlan().selectedSourceNames().get(0));
    }

    @Test
    void cognitiveState_withSourceSelectionPlan_rejectsNull() {
        assertThrows(NullPointerException.class,
                () -> CognitiveState.empty().withSourceSelectionPlan(null));
    }

    @Test
    void cognitiveState_emptyAndLegacyConstructors_leaveSelectionPlanNull() {
        assertNull(CognitiveState.empty().sourceSelectionPlan());

        CognitiveState legacy17 = new CognitiveState(null, null, null, null, 0, List.of(),
                null, null, null, null, null, null, null, null, null, null, null);
        assertNull(legacy17.sourceSelectionPlan());

        CognitiveState legacy18 = new CognitiveState(null, null, null, null, 0, List.of(),
                null, null, null, null, null, null, null, null, null, null, null, null);
        assertNull(legacy18.sourceSelectionPlan());

        CognitiveState legacy19 = new CognitiveState(null, null, null, null, 0, List.of(),
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertNull(legacy19.sourceSelectionPlan());
    }

    @Test
    void cognitiveState_explicitPlanConstructor_storesArtifact() {
        SourceSelectionPlan selectionPlan = oneSelectionPlan();
        CognitiveState state = new CognitiveState(null, null, null, null, 0, List.of(),
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                selectionPlan);
        assertEquals(selectionPlan, state.sourceSelectionPlan());
    }

    // ---- end-to-end K0.6.1 -> K0.6.3 ---------------------------------------------

    @Test
    void endToEnd_k061ToK063_routesThenSelectsTrustedSources() {
        KnowledgeRequirementSet requirements = set(
                topic("Java", RequirementPriority.CRITICAL),
                topic("Spring", RequirementPriority.HIGH),
                topic("Roadmap", RequirementPriority.MEDIUM),
                topic("Fundamentals", RequirementPriority.MEDIUM));
        AcquisitionPlan acquisitionPlan = new DefaultProviderRouter().route(requirements);

        DefaultKnowledgeSourceRegistry registry = new DefaultKnowledgeSourceRegistry();
        KnowledgeSource official = active(registry, KnowledgeSourceType.MARKDOWN,
                "Official Java Docs",
                Map.of("authority", "official", "provider", "OFFICIAL_DOCS"));
        KnowledgeSource community = active(registry, KnowledgeSourceType.WEB,
                "Community Wiki",
                Map.of("authority", "community", "provider", "WEB"));

        SourceSelectionPlan selectionPlan = engine.select(acquisitionPlan, registry);

        assertEquals(List.of("Java", "Spring", "Fundamentals", "Roadmap"),
                selectedTopics(selectionPlan));
        assertEquals(acquisitionPlan.targets().size(), selectionPlan.size());
        assertEquals(official.sourceId(), selectionPlan.selections().get(0).sourceId());
        assertEquals(official.sourceId(), selectionPlan.selections().get(1).sourceId());
        assertEquals(community.sourceId(), selectionPlan.selections().get(2).sourceId());
        assertEquals(community.sourceId(), selectionPlan.selections().get(3).sourceId());
        assertEquals(selectionPlan, engine.select(acquisitionPlan, registry));
    }
}
