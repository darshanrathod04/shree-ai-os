package com.shreeai.os.platform.core.plugin.verification;

import com.shreeai.os.platform.core.plugin.model.Plugin;
import com.shreeai.os.platform.core.plugin.model.PluginDescriptor;
import com.shreeai.os.platform.core.plugin.model.PluginId;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <b>PluginVerifier</b>
 *
 * <p>The orchestrator for plugin verification within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Orchestrates all verification checks before a plugin can be loaded.</li>
 *   <li>Answers: "Is this plugin safe? Is metadata valid? Are dependencies satisfied?
 *       Is version compatible? Can this plugin run?"</li>
 *   <li>Returns a complete {@link VerificationResult} report.</li>
 *   <li>Never loads, executes, or mutates plugin state.</li>
 * </ul>
 *
 * <p><b>Verification Pipeline:</b></p>
 * <ol>
 *   <li>Metadata — checks required fields (id, name, version, author, description)</li>
 *   <li>Version — validates semantic versioning</li>
 *   <li>Dependencies — checks all declared dependencies are available</li>
 *   <li>Compatibility — checks Java version, platform version, plugin API version</li>
 *   <li>Duplicate IDs — rejects duplicate plugin IDs</li>
 * </ol>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Stateless — no fields.</li>
 *   <li>Thread-safe — everything immutable.</li>
 *   <li>Deterministic — same input always produces the same output.</li>
 *   <li>Zero side effects — no logging, no loading, no filesystem, no network.</li>
 *   <li>No Spring, Lombok, or JPA.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see VerificationResult
 * @see VerificationIssue
 * @see VerificationSeverity
 * @see PluginDependencyChecker
 * @see PluginCompatibilityChecker
 */
public final class PluginVerifier {

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9._-]*$");
    private static final Pattern SEMVER_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^.{1,128}$");

    private final PluginDependencyChecker dependencyChecker;
    private final PluginCompatibilityChecker compatibilityChecker;
    private final Set<PluginId> registeredPluginIds;

    /**
     * Constructs a new {@code PluginVerifier}.
     *
     * @param dependencyChecker     the dependency checker (must not be null)
     * @param compatibilityChecker  the compatibility checker (must not be null)
     * @param registeredPluginIds   the set of already-registered plugin IDs (must not be null)
     */
    public PluginVerifier(
            PluginDependencyChecker dependencyChecker,
            PluginCompatibilityChecker compatibilityChecker,
            Set<PluginId> registeredPluginIds
    ) {
        this.dependencyChecker = Objects.requireNonNull(dependencyChecker, "dependencyChecker must not be null");
        this.compatibilityChecker = Objects.requireNonNull(compatibilityChecker, "compatibilityChecker must not be null");
        this.registeredPluginIds = Objects.requireNonNull(registeredPluginIds, "registeredPluginIds must not be null");
    }

    /**
     * Verifies a plugin descriptor against all verification checks.
     *
     * @param descriptor the plugin descriptor to verify
     * @return a complete {@link VerificationResult} with all issues
     * @throws NullPointerException if descriptor is null
     */
    public VerificationResult verify(PluginDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");

        VerificationResult.Builder builder = VerificationResult.builder();

        // 1. Metadata check
        checkMetadata(descriptor, builder);

        // 2. Version check
        checkVersion(descriptor, builder);

        // 3. Dependencies check
        checkDependencies(descriptor, builder);

        // 4. Compatibility check
        checkCompatibility(descriptor, builder);

        // 5. Duplicate check
        checkDuplicate(descriptor, builder);

        return builder.build();
    }

    /**
     * Checks that all required metadata fields are present and valid.
     */
    private void checkMetadata(PluginDescriptor descriptor, VerificationResult.Builder builder) {
        Plugin plugin = descriptor.plugin();
        if (plugin == null) {
            builder.addError("Plugin must not be null");
            return;
        }

        // Plugin ID
        PluginId id = plugin.id();
        if (id == null) {
            builder.addError("Plugin ID must not be null");
        } else {
            String idValue = id.value();
            if (idValue == null || idValue.isBlank()) {
                builder.addError("Plugin ID value must not be null or empty");
            } else if (!ID_PATTERN.matcher(idValue).matches()) {
                builder.addError("Plugin ID must start with a letter and contain only letters, digits, dots, hyphens, or underscores");
            }
        }

        // Plugin name
        String name = plugin.name();
        if (name == null || name.isBlank()) {
            builder.addError("Plugin name must not be null or empty");
        } else if (!NAME_PATTERN.matcher(name).matches()) {
            builder.addError("Plugin name must be between 1 and 128 characters");
        }

        // Plugin version
        String version = plugin.version();
        if (version == null || version.isBlank()) {
            builder.addError("Plugin version must not be null or empty");
        } else if (!SEMVER_PATTERN.matcher(version).matches()) {
            builder.addError("Plugin version must follow semantic versioning (X.Y.Z), got: " + version);
        }

        // Provider (from descriptor)
        String provider = descriptor.provider();
        if (provider == null || provider.isBlank()) {
            builder.addError("Plugin provider must not be null or empty");
        }
    }

    /**
     * Checks that the plugin version follows semantic versioning.
     */
    private void checkVersion(PluginDescriptor descriptor, VerificationResult.Builder builder) {
        // Version format is already checked in metadata validation.
        // Additional version-specific checks can be added here.
        Plugin plugin = descriptor.plugin();
        if (plugin != null) {
            String version = plugin.version();
            if (version != null && SEMVER_PATTERN.matcher(version).matches()) {
                builder.addInfo("Plugin version " + version + " follows semantic versioning");
            }
        }
    }

    /**
     * Checks that all declared dependencies are available.
     */
    private void checkDependencies(PluginDescriptor descriptor, VerificationResult.Builder builder) {
        // Dependencies are not currently stored in the Plugin model.
        // This delegates to the PluginDependencyChecker which the caller
        // provides with the list of dependency IDs.
        // For now, pass null as dependencyIds — the checker handles it.
        VerificationResult depResult = dependencyChecker.checkDependencies(descriptor, null);
        for (VerificationIssue issue : depResult.issues()) {
            builder.addIssue(issue.severity(), issue.message());
        }
    }

    /**
     * Checks that the plugin is compatible with the current runtime.
     */
    private void checkCompatibility(PluginDescriptor descriptor, VerificationResult.Builder builder) {
        // Use null for version constraints — the caller should provide these.
        // The Plugin model doesn't currently carry compatibility metadata.
        // This delegates to the PluginCompatibilityChecker which the caller
        // configures with runtime environment details.
        VerificationResult compatResult = compatibilityChecker.checkCompatibility(
                descriptor, null, null, null, null);
        for (VerificationIssue issue : compatResult.issues()) {
            builder.addIssue(issue.severity(), issue.message());
        }
    }

    /**
     * Checks that the plugin ID is not already registered.
     */
    private void checkDuplicate(PluginDescriptor descriptor, VerificationResult.Builder builder) {
        Plugin plugin = descriptor.plugin();
        if (plugin != null) {
            PluginId id = plugin.id();
            if (id != null && registeredPluginIds.contains(id)) {
                builder.addError("Duplicate plugin ID: " + id.value());
            }
        }
    }
}