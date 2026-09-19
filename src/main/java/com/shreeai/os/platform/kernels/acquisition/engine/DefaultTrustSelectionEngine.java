package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionTarget;
import com.shreeai.os.platform.kernels.acquisition.model.ProviderCapability;
import com.shreeai.os.platform.kernels.acquisition.model.ProviderType;
import com.shreeai.os.platform.kernels.acquisition.model.SelectedSource;
import com.shreeai.os.platform.kernels.acquisition.model.SourceCandidate;
import com.shreeai.os.platform.kernels.acquisition.model.SourceSelectionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.SourceTrustLevel;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceStatus;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>DefaultTrustSelectionEngine</b>
 *
 * <p>The K0.6.3 implementation of {@link TrustSelectionEngine}. Performs a
 * five-stage, fully deterministic selection pipeline over the K1
 * {@link KnowledgeSourceRegistry}:</p>
 *
 * <ol>
 *   <li><b>Provider Filtering</b> - only candidates that can serve the routed
 *       {@link ProviderType} remain (and, where the source declares an explicit
 *       topic restriction, only for the topics it declares).</li>
 *   <li><b>Status Filtering</b> - only {@link KnowledgeSourceStatus#ACTIVE}
 *       sources are eligible. Disabled and merely registered sources are
 *       ignored.</li>
 *   <li><b>Trust Ranking</b> - candidates are ranked by authority score
 *       (descending), then trust level, then source name (A&rarr;Z), then
 *       source id. Never random, never request-time dependent.</li>
 *   <li><b>Select Winner</b> - the highest-ranked candidate is selected for the
 *       topic.</li>
 *   <li><b>Build Selection Plan</b> - one {@link SelectedSource} per target, in
 *       the canonical order of the acquisition plan. No downloads, no network
 *       calls, no ingestion.</li>
 * </ol>
 *
 * <p><b>Locked authority policy:</b> the authority score of a source is the
 * locked constant of its {@link SourceTrustLevel} (OFFICIAL {@code 1.00},
 * ENTERPRISE {@code 0.95}, VERIFIED {@code 0.90}, COMMUNITY {@code 0.70},
 * UNKNOWN {@code 0.40}). Scores are never computed, weighted or inferred.</p>
 *
 * <p><b>Registry metadata contract (read-only):</b> the registry is never
 * modified by this engine. Trust, provider compatibility and topic scope are
 * declared through the immutable source metadata:</p>
 * <ul>
 *   <li>{@code authority} - trust label ({@code official}, {@code enterprise},
 *       {@code verified}, {@code community}). This is the same key and label
 *       vocabulary as the K5 reliability engine.</li>
 *   <li>{@code trust} - alias accepted when {@code authority} is absent.</li>
 *   <li>{@code provider} - single provider type name (e.g.
 *       {@code OFFICIAL_DOCS}).</li>
 *   <li>{@code providers} - comma-separated provider type names.</li>
 *   <li>{@code supportedTopics} / {@code topics} - comma-separated topic names
 *       this source may serve. When absent the source serves every topic of its
 *       providers.</li>
 * </ul>
 *
 * <p>When no provider is declared, a deterministic default is derived from the
 * K1 {@link KnowledgeSourceType} ({@code WEB}&rarr;WEB,
 * {@code DATABASE}&rarr;DATABASE, {@code API}&rarr;API, all document types
 * &rarr;LOCAL_FILES). Undeclared trust always resolves to
 * {@link SourceTrustLevel#UNKNOWN} - trust is never inferred from a name,
 * location or guesswork.</p>
 *
 * <p><b>Never fails:</b> topics for which the registry exposes no compatible
 * topic-scoped source fall back to any topic-compatible active source
 * (cross-provider fallback). Topics that still cannot be satisfied are omitted
 * from the plan - a source is never fabricated.</p>
 *
 * <p>The engine is stateless, thread-safe and deterministic - identical inputs
 * always produce structurally equal plans. No LLM, no embeddings, no network
 * calls, no external dependencies.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.3 Trust &amp; Source Selection</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see TrustSelectionEngine
 */
public final class DefaultTrustSelectionEngine implements TrustSelectionEngine {

    /** Metadata key declaring a source trust label (K5-compatible). */
    public static final String AUTHORITY_METADATA_KEY = "authority";

    /** Metadata key alias declaring a source trust label. */
    public static final String TRUST_METADATA_KEY = "trust";

    /** Metadata key declaring a single provider type. */
    public static final String PROVIDER_METADATA_KEY = "provider";

    /** Metadata key declaring a comma-separated list of provider types. */
    public static final String PROVIDERS_METADATA_KEY = "providers";

    /** Metadata key declaring the topic names a source may serve. */
    public static final String SUPPORTED_TOPICS_METADATA_KEY = "supportedTopics";

    /** Metadata key alias declaring the topic names a source may serve. */
    public static final String TOPICS_METADATA_KEY = "topics";

    /** Locked trust family of any source that declares no trust. */
    public static final SourceTrustLevel DEFAULT_TRUST_LEVEL = SourceTrustLevel.UNKNOWN;

    /**
     * Locked deterministic ranking: authority score descending, then trust
     * level, then source name (A&rarr;Z), then source id. The final source-id
     * tie-break guarantees a total order, so selection is never ambiguous.
     */
    private static final Comparator<CatalogEntry> AUTHORITY_ORDER =
            Comparator.comparingDouble(
                            (CatalogEntry entry) -> entry.trustLevel().authorityScore())
                    .reversed()
                    .thenComparingInt(entry -> entry.trustLevel().rank())
                    .thenComparing(entry -> entry.source().name().toLowerCase(Locale.ROOT))
                    .thenComparing(entry -> entry.source().name())
                    .thenComparing(entry -> entry.source().sourceId());

    @Override
    public SourceSelectionPlan select(AcquisitionPlan acquisitionPlan,
                                      KnowledgeSourceRegistry registry) {
        Objects.requireNonNull(acquisitionPlan, "acquisitionPlan must not be null");
        Objects.requireNonNull(registry, "registry must not be null");

        // Stage 1 + 2: build the eligible catalog (declared capabilities,
        // ACTIVE sources only) from a deterministic point-in-time snapshot.
        List<CatalogEntry> catalog = activeCatalog(registry);

        // Stages 3-5: rank and select one winner per target, preserving the
        // canonical order of the acquisition plan.
        List<SelectedSource> selections = new ArrayList<>();
        for (AcquisitionTarget target : acquisitionPlan.targets()) {
            CatalogEntry winner = selectFor(target, catalog);
            if (winner != null) {
                selections.add(new SelectedSource(target.topicId(), target.topicName(),
                        winner.candidateFor(target.provider())));
            }
        }
        return new SourceSelectionPlan(List.copyOf(selections));
    }

    /**
     * Builds the eligible catalog: every {@code ACTIVE} registered source,
     * materialized with its declared capabilities and locked trust level.
     *
     * @param registry the K1 source registry (may hold any lifecycle states)
     * @return an immutable, deterministically ordered list of catalog entries
     */
    private static List<CatalogEntry> activeCatalog(KnowledgeSourceRegistry registry) {
        return registry.snapshot().sources().stream()
                .filter(source -> source.status() == KnowledgeSourceStatus.ACTIVE)
                .map(DefaultTrustSelectionEngine::toEntry)
                .toList();
    }

    /** Materializes one registered source as a catalog entry. */
    private static CatalogEntry toEntry(KnowledgeSource source) {
        return new CatalogEntry(source, providersOf(source),
                supportedTopicsOf(source), trustLevelOf(source));
    }

    /**
     * Selects the winning candidate for one target: provider-and-topic
     * compatible candidates first, then topic-compatible candidates as the
     * documented cross-provider fallback, then no selection.
     *
     * @param target  the acquisition target
     * @param catalog the eligible catalog
     * @return the winning entry, or {@code null} when nothing can serve it
     */
    private static CatalogEntry selectFor(AcquisitionTarget target, List<CatalogEntry> catalog) {
        List<CatalogEntry> providerCompatible = catalog.stream()
                .filter(entry -> entry.servesProvider(target.provider())
                        && entry.servesTopic(target.topicName()))
                .toList();
        if (!providerCompatible.isEmpty()) {
            return bestOf(providerCompatible);
        }
        List<CatalogEntry> topicCompatible = catalog.stream()
                .filter(entry -> entry.servesTopic(target.topicName()))
                .toList();
        return bestOf(topicCompatible);
    }

    /** Returns the highest-ranked entry, or {@code null} when none exist. */
    private static CatalogEntry bestOf(List<CatalogEntry> entries) {
        return entries.stream().min(AUTHORITY_ORDER).orElse(null);
    }
/**
     * Derives the provider compatibility of a registered source: declared
     * metadata first ({@code providers} then {@code provider}), otherwise the
     * deterministic default derived from the K1 source type.
     *
     * @param source the registered source
     * @return an immutable, declaration-ordered provider list (never null)
     */
    private static List<ProviderType> providersOf(KnowledgeSource source) {
        LinkedHashSet<ProviderType> declared = new LinkedHashSet<>();
        declared.addAll(parseProviderList(source.metadata().get(PROVIDERS_METADATA_KEY)));
        parseProvider(source.metadata().get(PROVIDER_METADATA_KEY)).ifPresent(declared::add);
        if (!declared.isEmpty()) {
            return List.copyOf(declared);
        }
        return List.of(defaultProviderFor(source.type()));
    }

    /**
     * Locked default provider derivation from the K1 source type. Used only when
     * a source declares no provider metadata.
     *
     * @param type the K1 source type (must not be null)
     * @return the locked default provider type (never null)
     */
    private static ProviderType defaultProviderFor(KnowledgeSourceType type) {
        return switch (type) {
            case WEB -> ProviderType.WEB;
            case API -> ProviderType.API;
            case DATABASE -> ProviderType.DATABASE;
            case PDF, MARKDOWN, FOLDER, JSON, TEXT -> ProviderType.LOCAL_FILES;
        };
    }

    /**
     * Derives the explicit topic scope of a registered source.
     *
     * @param source the registered source
     * @return an immutable list of supported topic names; empty means "serves
     *         every topic of its providers" (never null)
     */
    private static List<String> supportedTopicsOf(KnowledgeSource source) {
        String raw = source.metadata().get(SUPPORTED_TOPICS_METADATA_KEY);
        if (raw == null) {
            raw = source.metadata().get(TOPICS_METADATA_KEY);
        }
        return parseCsv(raw);
    }

    /**
     * Derives the trust level of a registered source: the {@code authority}
     * declaration (K5-compatible) first, then the {@code trust} alias,
     * otherwise {@link #DEFAULT_TRUST_LEVEL}. Trust is never inferred.
     *
     * @param source the registered source
     * @return the resolved trust family (never null)
     */
    private static SourceTrustLevel trustLevelOf(KnowledgeSource source) {
        Map<String, String> metadata = source.metadata();
        return SourceTrustLevel.parse(metadata.get(AUTHORITY_METADATA_KEY))
                .or(() -> SourceTrustLevel.parse(metadata.get(TRUST_METADATA_KEY)))
                .orElse(DEFAULT_TRUST_LEVEL);
    }

    /** Parses one provider type name; case-insensitive, exact, never fuzzy. */
    private static Optional<ProviderType> parseProvider(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        for (ProviderType type : ProviderType.values()) {
            if (type.name().equals(normalized)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /** Parses a comma-separated provider type list, dropping unrecognized entries. */
    private static List<ProviderType> parseProviderList(String raw) {
        List<ProviderType> providers = new ArrayList<>();
        for (String token : parseCsv(raw)) {
            parseProvider(token).ifPresent(providers::add);
        }
        return List.copyOf(providers);
    }

    /**
     * Splits a comma-separated metadata value into trimmed, non-blank tokens,
     * removing case-insensitive duplicates while preserving declaration order.
     *
     * @param raw the raw metadata value (may be null)
     * @return an immutable token list (never null)
     */
    private static List<String> parseCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String token : raw.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty() && seen.add(trimmed.toLowerCase(Locale.ROOT))) {
                tokens.add(trimmed);
            }
        }
        return List.copyOf(tokens);
    }

    /**
     * Describes the declared capabilities of a registered source, for
     * observability. Purely descriptive - selection derives its eligibility
     * directly.
     *
     * @param source the registered source (must not be null)
     * @return one capability per compatible provider (never null)
     * @throws NullPointerException if source is null
     */
    public static List<ProviderCapability> capabilitiesOf(KnowledgeSource source) {
        Objects.requireNonNull(source, "source must not be null");
        List<String> topics = supportedTopicsOf(source);
        return providersOf(source).stream()
                .map(provider -> new ProviderCapability(provider, topics))
                .toList();
    }

    /**
     * One registered source materialized as a selection input: the immutable
     * source, the providers it can serve, its explicit topic scope and its
     * locked trust family.
     */
    private record CatalogEntry(
            KnowledgeSource source,
            List<ProviderType> providers,
            List<String> supportedTopics,
            SourceTrustLevel trustLevel) {

        /** Returns true when this source can serve the given provider. */
        boolean servesProvider(ProviderType provider) {
            return providers.contains(provider);
        }

        /**
         * Returns true when this source may serve the given topic: either it
         * declares no topic scope, or the topic is explicitly declared
         * (case-insensitive, exact - no fuzzy matching).
         */
        boolean servesTopic(String topicName) {
            if (topicName == null) {
                return false;
            }
            if (supportedTopics.isEmpty()) {
                return true;
            }
            String normalized = topicName.toLowerCase(Locale.ROOT);
            return supportedTopics.stream()
                    .anyMatch(topic -> topic.toLowerCase(Locale.ROOT).equals(normalized));
        }

        /** Materializes this entry as an immutable candidate for one provider. */
        SourceCandidate candidateFor(ProviderType provider) {
            return SourceCandidate.forTrust(source.sourceId(), source.name(),
                    provider, trustLevel);
        }
    }
}