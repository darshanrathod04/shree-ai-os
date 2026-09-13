package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRegistrySnapshot;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSource;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceStatus;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeSourceType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>DefaultKnowledgeSourceRegistry</b>
 *
 * <p>Thread-safe, in-memory implementation of {@link KnowledgeSourceRegistry}
 * backed by a {@code ConcurrentHashMap} whose values are deeply immutable
 * {@link KnowledgeSource} instances. Every externally visible value (sources,
 * metadata maps, snapshots) is immutable, so concurrent readers always observe
 * a consistent state without locks.</p>
 *
 * <p><b>Deterministic Identity:</b> Source ids are UUID v5 (SHA-1, name-based)
 * digests of {@code type + normalizedLocation + name}. Identical registration
 * input always yields an identical id - no randomness, no system time.</p>
 *
 * <p><b>Architectural Boundary:</b> No ingestion, no parsing, no indexing, no
 * database. This registry stores source metadata only.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K1 Universal Knowledge Source Registry</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see KnowledgeSourceRegistry
 */
public final class DefaultKnowledgeSourceRegistry implements KnowledgeSourceRegistry {

    /** Fixed namespace used for deterministic UUID v5 source ids. */
    private static final UUID NAMESPACE =
            UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    private final ConcurrentHashMap<String, KnowledgeSource> sources =
            new ConcurrentHashMap<>();

    /**
     * Creates an empty registry. Stateless public API - all state is confined
     * to this instance and never exposed mutably.
     */
    public DefaultKnowledgeSourceRegistry() {
    }

    @Override
    public KnowledgeSource register(KnowledgeSourceType type,
                                    String name,
                                    String location,
                                    String description,
                                    Map<String, String> metadata) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(location, "location must not be null");

        String sourceId = sourceIdFor(type, name, location);
        KnowledgeSource source = new KnowledgeSource(
                sourceId, name.trim(), type,
                KnowledgeSourceStatus.REGISTERED,
                location, description, Instant.now(), metadata);
        KnowledgeSource existing = sources.putIfAbsent(sourceId, source);
        return existing != null ? existing : source;
    }

    @Override
    public Optional<KnowledgeSource> activate(String sourceId) {
        return Optional.ofNullable(sources.computeIfPresent(sourceId,
                (id, source) -> source.status() == KnowledgeSourceStatus.ACTIVE
                        ? source
                        : source.withStatus(KnowledgeSourceStatus.ACTIVE)));
    }

    @Override
    public Optional<KnowledgeSource> disable(String sourceId) {
        return Optional.ofNullable(sources.computeIfPresent(sourceId,
                (id, source) -> source.status() == KnowledgeSourceStatus.DISABLED
                        ? source
                        : source.withStatus(KnowledgeSourceStatus.DISABLED)));
    }

    @Override
    public Optional<KnowledgeSource> findById(String sourceId) {
        return Optional.ofNullable(sources.get(sourceId));
    }

    @Override
    public List<KnowledgeSource> findByType(KnowledgeSourceType type) {
        Objects.requireNonNull(type, "type must not be null");
        return sources.values().stream()
                .filter(source -> source.type() == type)
                .sorted(Comparator.comparing(KnowledgeSource::sourceId))
                .toList();
    }

    @Override
    public KnowledgeRegistrySnapshot snapshot() {
        List<KnowledgeSource> ordered = sources.values().stream()
                .sorted(Comparator.comparing(KnowledgeSource::sourceId))
                .toList();
        return new KnowledgeRegistrySnapshot(ordered, Instant.now());
    }

    /**
     * Computes the deterministic source id for the given registration input.
     *
     * @param type the source family
     * @param name the human-readable source name
     * @param location the physical or logical location
     * @return a deterministic UUID v5 string (never null)
     */
    public static String sourceIdFor(KnowledgeSourceType type, String name, String location) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(location, "location must not be null");
        String seed = type.name() + "|" + normalizeLocation(location) + "|" + name.trim();
        return uuidV5(NAMESPACE, seed.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static String normalizeLocation(String location) {
        return location.trim().toLowerCase()
                .replace('\\', '/')
                .replaceAll("\\s+", " ");
    }

    private static UUID uuidV5(UUID namespace, byte[] name) {
        byte[] nsBytes = toBytes(namespace);
        byte[] input = new byte[nsBytes.length + name.length];
        System.arraycopy(nsBytes, 0, input, 0, nsBytes.length);
        System.arraycopy(name, 0, input, nsBytes.length, name.length);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 digest is unavailable", e);
        }
        byte[] hash = md.digest(input);
        hash[6] &= 0x0f;
        hash[6] |= 0x50;
        hash[8] &= 0x3f;
        hash[8] |= 0x80;
        return fromBytes(hash);
    }

    private static byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static UUID fromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }
}
