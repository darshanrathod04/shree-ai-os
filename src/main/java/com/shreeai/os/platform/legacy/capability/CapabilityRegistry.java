package com.shreeai.os.platform.legacy.capability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Capability Registry — SHADOW MODE.
 *
 * Responsibilities:
 * - Register capabilities
 * - Lookup capabilities by intent
 * - Find best capability for an intent
 * - Health checks
 * - Duplicate intent detection
 *
 * CRITICAL:
 * - This is SHADOW MODE only — it NEVER executes capabilities
 * - It NEVER changes production routing
 * - It ONLY predicts which capability SHOULD execute
 *
 * Performance target: < 1ms per lookup
 */
@Component
public class CapabilityRegistry {

    private static final Logger log = LoggerFactory.getLogger(CapabilityRegistry.class);

    // Thread-safe storage: intent -> sorted list of capabilities (by priority desc)
    private final Map<String, List<Capability>> intentIndex = new ConcurrentHashMap<>();
    private final Map<String, Capability> nameIndex = new ConcurrentHashMap<>();
    private final Set<String> duplicateIntentWarnings = ConcurrentHashMap.newKeySet();

    /**
     * Register a capability.
     * Validates no duplicate intents (logs warning if found).
     */
    public synchronized void register(Capability capability) {
        if (capability == null) {
            log.warn("[CAPABILITY] Attempted to register null capability");
            return;
        }

        String name = capability.getName();
        if (nameIndex.containsKey(name)) {
            log.warn("[CAPABILITY] Duplicate capability name detected: {}", name);
            return;
        }

        nameIndex.put(name, capability);

        // Index by supported intents
        for (String intent : capability.getSupportedIntents()) {
            intentIndex.computeIfAbsent(intent, k -> new ArrayList<>()).add(capability);
        }

        // Sort each intent list by priority (descending)
        intentIndex.values().forEach(list ->
                list.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
        );

        log.info("[CAPABILITY] Registered: {} (priority={}, intents={})",
                name, capability.getPriority(), capability.getSupportedIntents().size());
    }

    /**
     * Unregister a capability by name.
     */
    public synchronized boolean unregister(String capabilityName) {
        Capability removed = nameIndex.remove(capabilityName);
        if (removed == null) {
            return false;
        }

        // Remove from intent index
        intentIndex.values().forEach(list ->
                list.removeIf(cap -> cap.getName().equals(capabilityName))
        );

        log.info("[CAPABILITY] Unregistered: {}", capabilityName);
        return true;
    }

    /**
     * Find the best capability for a given intent.
     * Returns the highest-priority enabled capability that supports the intent.
     */
    public Optional<Capability> lookup(String intent) {
        if (intent == null || intent.isBlank()) {
            return Optional.empty();
        }

        List<Capability> capabilities = intentIndex.get(intent);
        if (capabilities == null || capabilities.isEmpty()) {
            return Optional.empty();
        }

        // Return first enabled capability (list is sorted by priority desc)
        return capabilities.stream()
                .filter(Capability::isEnabled)
                .filter(cap -> cap.getHealthStatus() == Capability.HealthStatus.HEALTHY
                        || cap.getHealthStatus() == Capability.HealthStatus.UNKNOWN)
                .findFirst();
    }

    /**
     * Find the best capability match with confidence.
     * Returns CapabilityMatch or null if no match.
     */
    public CapabilityMatch findBestCapability(String intent) {
        long startTime = System.nanoTime();

        Optional<Capability> capOpt = lookup(intent);
        if (capOpt.isEmpty()) {
            return null;
        }

        Capability capability = capOpt.get();
        long processingTime = System.nanoTime() - startTime;

        String reason = "Priority=" + capability.getPriority() +
                ", Health=" + capability.getHealthStatus();

        return new CapabilityMatch(
                capability,
                0.95, // High confidence for direct intent match
                intent,
                reason,
                processingTime
        );
    }

    /**
     * List all registered capabilities.
     */
    public List<Capability> listAll() {
        return new ArrayList<>(nameIndex.values());
    }

    /**
     * List all registered capability names.
     */
    public List<String> listNames() {
        return nameIndex.values().stream()
                .map(Capability::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get capability by name.
     */
    public Optional<Capability> getByName(String name) {
        return Optional.ofNullable(nameIndex.get(name));
    }

    /**
     * Health check — returns summary of registry state.
     */
    public RegistryHealth health() {
        int total = nameIndex.size();
        long healthy = nameIndex.values().stream()
                .filter(cap -> cap.getHealthStatus() == Capability.HealthStatus.HEALTHY)
                .count();
        long enabled = nameIndex.values().stream()
                .filter(Capability::isEnabled)
                .count();
        int duplicateIntents = duplicateIntentWarnings.size();

        return new RegistryHealth(total, (int) healthy, (int) enabled, duplicateIntents);
    }

    /**
     * Detect duplicate intents across capabilities.
     * Logs warnings for intents handled by multiple capabilities.
     */
    public void detectDuplicateIntents() {
        duplicateIntentWarnings.clear();

        for (Map.Entry<String, List<Capability>> entry : intentIndex.entrySet()) {
            String intent = entry.getKey();
            List<Capability> capabilities = entry.getValue();

            if (capabilities.size() > 1) {
                List<String> names = capabilities.stream()
                        .map(Capability::getName)
                        .collect(Collectors.toList());
                String warning = String.format("Intent '%s' handled by multiple capabilities: %s",
                        intent, String.join(", ", names));
                duplicateIntentWarnings.add(warning);
                log.warn("[CAPABILITY] {}", warning);
            }
        }
    }

    /**
     * Get all intents that have duplicate handlers.
     */
    public Set<String> getDuplicateIntents() {
        return Collections.unmodifiableSet(duplicateIntentWarnings);
    }

    /**
     * Clear all registrations (for testing).
     */
    public synchronized void clear() {
        intentIndex.clear();
        nameIndex.clear();
        duplicateIntentWarnings.clear();
        log.info("[CAPABILITY] Registry cleared");
    }

    /**
     * Registry health summary.
     */
    public record RegistryHealth(int totalCapabilities, int healthyCapabilities,
                                  int enabledCapabilities, int duplicateIntents) {
    }
}