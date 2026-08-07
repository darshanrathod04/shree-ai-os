package com.shreeai.os.platform.core.plugin.verification;

import com.shreeai.os.platform.core.plugin.model.PluginDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <b>PluginDependencyChecker</b>
 *
 * <p>Stateless checker that verifies a plugin's dependencies are satisfied
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Checks whether a plugin's declared dependencies are available.</li>
 *   <li>Does not load or resolve dependencies — only verifies availability.</li>
 *   <li>Stateless and thread-safe.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see PluginVerifier
 * @see VerificationResult
 */
public final class PluginDependencyChecker {

    private final Set<String> availableDependencies;

    /**
     * Constructs a new {@code PluginDependencyChecker} with the set of
     * available dependency identifiers.
     *
     * @param availableDependencies the set of available dependency IDs (must not be null)
     */
    public PluginDependencyChecker(Set<String> availableDependencies) {
        Objects.requireNonNull(availableDependencies, "availableDependencies must not be null");
        this.availableDependencies = Collections.unmodifiableSet(Set.copyOf(availableDependencies));
    }

    /**
     * Checks that all dependencies declared in the descriptor are available.
     *
     * <p>A descriptor with a null or empty dependency list passes trivially.</p>
     *
     * @param descriptor        the plugin descriptor to check
     * @param dependencyIds     the dependency IDs declared by the plugin (may be null or empty)
     * @return a {@link VerificationResult} with any missing dependency errors
     */
    public VerificationResult checkDependencies(PluginDescriptor descriptor, List<String> dependencyIds) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");

        if (dependencyIds == null || dependencyIds.isEmpty()) {
            return VerificationResult.builder().build();
        }

        VerificationResult.Builder builder = VerificationResult.builder();
        for (String depId : dependencyIds) {
            if (!availableDependencies.contains(depId)) {
                builder.addError("Missing dependency: " + depId);
            }
        }
        return builder.build();
    }

    /**
     * Returns the unmodifiable set of available dependency identifiers.
     *
     * @return available dependencies
     */
    public Set<String> availableDependencies() {
        return availableDependencies;
    }
}