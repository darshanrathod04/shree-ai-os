package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionTarget;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeTopic;
import com.shreeai.os.platform.kernels.acquisition.model.ProviderCapability;
import com.shreeai.os.platform.kernels.acquisition.model.ProviderType;
import com.shreeai.os.platform.kernels.acquisition.model.RequirementPriority;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultProviderRouter</b>
 *
 * <p>The K0.6.2 implementation of {@link ProviderRouter}. Performs a
 * five-stage, fully deterministic routing pipeline:</p>
 *
 * <ol>
 *   <li><b>Read Requirements</b> - consumes the canonical
 *       {@link KnowledgeRequirementSet} as-is. No prompt parsing.</li>
 *   <li><b>Dictionary Routing</b> - exact, case-insensitive topic-name
 *       matching against the locked routing dictionary. No fuzzy matching.</li>
 *   <li><b>Default Provider</b> - topics with no mapping route to
 *       {@link ProviderType#WEB}. The router never fails: every topic always
 *       receives a provider.</li>
 *   <li><b>Stable Ordering</b> - targets sorted by priority (CRITICAL&rarr;LOW)
 *       then by topic name (A&rarr;Z).</li>
 *   <li><b>Build Plan</b> - one {@link AcquisitionTarget} per unique topic;
 *       duplicates (same canonical name) keep the first occurrence. No
 *       execution.</li>
 * </ol>
 *
 * <p>The engine is stateless, thread-safe and deterministic - identical inputs
 * always produce structurally equal plans. No LLM, no embeddings, no network
 * calls, no external dependencies.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.2 Provider Router</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultProviderRouter implements ProviderRouter {

    /** Locked deterministic fallback provider for topics with no mapping. */
    public static final ProviderType DEFAULT_PROVIDER = ProviderType.WEB;

    /** Locked routing dictionary: lowercase topic name -&gt; provider type. */
    private static final Map<String, ProviderType> ROUTING_DICTIONARY;

    static {
        Map<String, ProviderType> dict = new LinkedHashMap<>();
        // OFFICIAL_DOCS
        dict.put("java", ProviderType.OFFICIAL_DOCS);
        dict.put("oop", ProviderType.OFFICIAL_DOCS);
        dict.put("collections", ProviderType.OFFICIAL_DOCS);
        dict.put("streams", ProviderType.OFFICIAL_DOCS);
        dict.put("spring", ProviderType.OFFICIAL_DOCS);
        dict.put("spring boot", ProviderType.OFFICIAL_DOCS);
        dict.put("sql", ProviderType.OFFICIAL_DOCS);
        dict.put("jdbc", ProviderType.OFFICIAL_DOCS);
        dict.put("rest api", ProviderType.OFFICIAL_DOCS);
        dict.put("docker", ProviderType.OFFICIAL_DOCS);
        dict.put("kubernetes", ProviderType.OFFICIAL_DOCS);
        dict.put("rag", ProviderType.OFFICIAL_DOCS);
        // WEB
        dict.put("llm", ProviderType.WEB);
        dict.put("interview preparation", ProviderType.WEB);
        dict.put("roadmap", ProviderType.WEB);
        dict.put("best practices", ProviderType.WEB);
        dict.put("fundamentals", ProviderType.WEB);
        // LOCAL_FILES
        dict.put("platform setup", ProviderType.LOCAL_FILES);
        // ENTERPRISE_DOCS
        dict.put("company sop", ProviderType.ENTERPRISE_DOCS);
        dict.put("crm architecture", ProviderType.ENTERPRISE_DOCS);
        ROUTING_DICTIONARY = Map.copyOf(dict);
    }

    @Override
    public AcquisitionPlan route(KnowledgeRequirementSet requirements) {
        Objects.requireNonNull(requirements, "requirements must not be null");

        // ---- Stage 1 + 2 + 3: read, dictionary routing, default provider ----
        Map<String, AcquisitionTarget> byName = new LinkedHashMap<>();
        for (KnowledgeTopic topic : requirements.topics()) {
            if (topic == null) {
                continue;
            }
            String key = topic.name().toLowerCase(Locale.ROOT);
            // Stage 5 deduplication: duplicates keep the first occurrence.
            byName.putIfAbsent(key, new AcquisitionTarget(
                    topic.topicId(), topic.name(), providerFor(topic.name()),
                    topic.priority()));
        }

        // ---- Stage 4 + 5: stable ordering, build plan ------------------------
        List<AcquisitionTarget> ordered = new ArrayList<>(byName.values());
        ordered.sort((a, b) -> {
            int byPriority = Integer.compare(priorityRank(a.priority()),
                    priorityRank(b.priority()));
            if (byPriority != 0) {
                return byPriority;
            }
            return a.topicName().compareTo(b.topicName());
        });
        return new AcquisitionPlan(List.copyOf(ordered));
    }

    /**
     * Resolves the provider for one topic name: exact, case-insensitive
     * dictionary lookup with the locked {@link #DEFAULT_PROVIDER} fallback.
     * Never returns null.
     */
    private static ProviderType providerFor(String topicName) {
        return ROUTING_DICTIONARY.getOrDefault(
                topicName.toLowerCase(Locale.ROOT), DEFAULT_PROVIDER);
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

    /**
     * Returns the locked routing capabilities derived from the dictionary, in
     * provider declaration order. Purely descriptive - routing itself uses the
     * dictionary directly.
     *
     * @return the locked provider capabilities (never null, unmodifiable)
     */
    public static List<ProviderCapability> lockedCapabilities() {
        return List.of(
                ProviderCapability.of(ProviderType.OFFICIAL_DOCS,
                        "Java", "OOP", "Collections", "Streams", "Spring",
                        "Spring Boot", "SQL", "JDBC", "REST API", "Docker",
                        "Kubernetes", "RAG"),
                ProviderCapability.of(ProviderType.WEB,
                        "LLM", "Interview Preparation", "Roadmap",
                        "Best Practices", "Fundamentals"),
                ProviderCapability.of(ProviderType.LOCAL_FILES, "Platform Setup"),
                ProviderCapability.of(ProviderType.ENTERPRISE_DOCS,
                        "Company SOP", "CRM Architecture"));
    }
}
