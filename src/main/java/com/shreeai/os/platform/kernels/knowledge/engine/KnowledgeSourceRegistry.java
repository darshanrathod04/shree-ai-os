package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRegistrySnapshot;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>KnowledgeSourceRegistry</b>
 *
 * <p>The universal registry contract for every knowledge source Shree AI OS
 * can consume. It performs no ingestion, parsing, or retrieval - it only
 * stores source metadata and manages lifecycle state.</p>
 *
 * <p><b>Architectural Responsibility:</b> Every future ingestion pipeline must
 * register its source here first. The registry is the single entry point that
 * turns a developer-declared source into an addressable, immutable
 * {@link KnowledgeSource}.</p>
 *
 * <p><b>Contract:</b> Implementations must be thread-safe, expose a stateless
 * public API, keep an immutable storage model, hold no global mutable
 * singleton and require no database dependency.</p>
 *
 * <p><b>Determinism:</b> Source identifiers must be derived deterministically
 * from {@code type + normalizedLocation + name} - never from randomness.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K1 Universal Knowledge Source Registry</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultKnowledgeSourceRegistry
 */
public interface KnowledgeSourceRegistry {

    /**
     * Registers a new knowledge source. The source is created with status
     * {@code REGISTERED}. Registering a source that already exists (same
     * deterministic source id) is idempotent and returns the previously
     * registered source unchanged.
     *
     * @param type the source family (must not be null)
     * @param name the human-readable source name (must not be null)
     * @param location the physical or logical location (must not be null)
     * @param description optional human-readable description (may be null)
     * @param metadata arbitrary source metadata (copied defensively)
     * @return the immutable registered KnowledgeSource (never null)
     */
    KnowledgeSource register(KnowledgeSourceType type,
                             String name,
                             String location,
                             String description,
                             Map<String, String> metadata);

    /**
     * Changes the lifecycle status of the source with the given id to
     * {@code ACTIVE}.
     *
     * @param sourceId the deterministic source id
     * @return the updated source, or empty if no such source exists
     */
    Optional<KnowledgeSource> activate(String sourceId);

    /**
     * Changes the lifecycle status of the source with the given id to
     * {@code DISABLED}.
     *
     * @param sourceId the deterministic source id
     * @return the updated source, or empty if no such source exists
     */
    Optional<KnowledgeSource> disable(String sourceId);

    /**
     * Looks up a source by its deterministic id.
     *
     * @param sourceId the deterministic source id
     * @return the matching source, or empty if none is registered
     */
    Optional<KnowledgeSource> findById(String sourceId);

    /**
     * Returns every registered source of the given type, ordered
     * deterministically by source id.
     *
     * @param type the source family to filter by (must not be null)
     * @return an immutable list of matching sources (never null)
     */
    List<KnowledgeSource> findByType(KnowledgeSourceType type);

    /**
     * Returns an immutable, point-in-time view of the whole registry.
     *
     * @return an immutable KnowledgeRegistrySnapshot (never null)
     */
    KnowledgeRegistrySnapshot snapshot();
}