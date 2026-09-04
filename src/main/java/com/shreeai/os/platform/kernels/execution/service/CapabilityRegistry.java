package com.shreeai.os.platform.kernels.execution.service;

import com.shreeai.os.platform.kernels.execution.model.Capability;
import com.shreeai.os.platform.kernels.execution.model.CapabilityExecutionType;
import com.shreeai.os.platform.kernels.execution.model.CapabilityHealthStatus;
import com.shreeai.os.platform.kernels.execution.model.CapabilityMatch;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CapabilityRegistry {
    private final Map<String, Capability> capabilities = new ConcurrentHashMap<>();

    public void register(Capability capability) {
        if (capability != null && capability.getName() != null) {
            capabilities.put(capability.getName(), capability);
        }
    }

    public Optional<Capability> find(String name) {
        return Optional.ofNullable(capabilities.get(name));
    }

    public List<Capability> findByType(CapabilityExecutionType type) {
        List<Capability> result = new ArrayList<>();
        for (Capability cap : capabilities.values()) {
            if (cap.getExecutionType() == type) {
                result.add(cap);
            }
        }
        return result;
    }

    public Optional<CapabilityMatch> findBestCapability(String intent) {
        if (intent == null || intent.isBlank()) {
            return Optional.empty();
        }
        Capability best = null;
        double bestConfidence = 0.0;
        for (Capability cap : capabilities.values()) {
            if (!cap.isAvailable()) continue;
            String capName = cap.getName() != null ? cap.getName().toUpperCase() : "";
            String intentUpper = intent.toUpperCase();
            double confidence = 0.0;
            if (capName.equals(intentUpper) || capName.contains(intentUpper)) {
                confidence = 1.0;
            } else if (capName.contains(extractKeyword(intentUpper))) {
                confidence = 0.7;
            }
            if (confidence > bestConfidence) {
                bestConfidence = confidence;
                best = cap;
            }
        }
        if (best != null) {
            return Optional.of(CapabilityMatch.of(best, bestConfidence, "Matched by intent: " + intent));
        }
        return Optional.empty();
    }

    private String extractKeyword(String text) {
        String[] parts = text.split("[_\\s]");
        return parts.length > 0 ? parts[0] : text;
    }

    public Collection<Capability> all() {
        return Collections.unmodifiableCollection(capabilities.values());
    }

    public void clear() {
        capabilities.clear();
    }
}
