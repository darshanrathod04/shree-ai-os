package platform.core.plugin.verification;

import platform.core.plugin.model.PluginDescriptor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * <b>PluginCompatibilityChecker</b>
 *
 * <p>Stateless checker that verifies a plugin is compatible with the
 * current runtime environment within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Checks minimum Java version, minimum/maximum platform version, and plugin API version.</li>
 *   <li>Does not load or execute plugins.</li>
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
public final class PluginCompatibilityChecker {

    private static final Pattern SEMVER_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private static final Pattern JAVA_VERSION_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    private final String currentJavaVersion;
    private final String currentPlatformVersion;
    private final String currentPluginApiVersion;

    /**
     * Constructs a new {@code PluginCompatibilityChecker} with the current
     * runtime environment details.
     *
     * @param currentJavaVersion     the current Java version (e.g. "21")
     * @param currentPlatformVersion the current platform version (e.g. "1.0.0")
     * @param currentPluginApiVersion the current plugin API version (e.g. "1.0.0")
     * @throws NullPointerException if any parameter is null
     */
    public PluginCompatibilityChecker(
            String currentJavaVersion,
            String currentPlatformVersion,
            String currentPluginApiVersion
    ) {
        this.currentJavaVersion = Objects.requireNonNull(currentJavaVersion, "currentJavaVersion must not be null");
        this.currentPlatformVersion = Objects.requireNonNull(currentPlatformVersion, "currentPlatformVersion must not be null");
        this.currentPluginApiVersion = Objects.requireNonNull(currentPluginApiVersion, "currentPluginApiVersion must not be null");
    }

    /**
     * Checks that the plugin is compatible with the current runtime environment.
     *
     * @param descriptor          the plugin descriptor to check
     * @param minJavaVersion      the minimum Java version required (e.g. "21"); may be null
     * @param minPlatformVersion  the minimum platform version required (e.g. "1.0.0"); may be null
     * @param maxPlatformVersion  the maximum platform version required (e.g. "2.0.0"); may be null
     * @param pluginApiVersion    the plugin API version the plugin was built against (e.g. "1.0.0"); may be null
     * @return a {@link VerificationResult} with any compatibility errors
     */
    public VerificationResult checkCompatibility(
            PluginDescriptor descriptor,
            String minJavaVersion,
            String minPlatformVersion,
            String maxPlatformVersion,
            String pluginApiVersion
    ) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");

        VerificationResult.Builder builder = VerificationResult.builder();

        // Check minimum Java version
        if (minJavaVersion != null) {
            if (!isValidJavaVersion(minJavaVersion)) {
                builder.addError("Invalid minimum Java version: " + minJavaVersion);
            } else if (compareVersions(currentJavaVersion, minJavaVersion) < 0) {
                builder.addError("Plugin requires Java " + minJavaVersion
                        + " but current runtime is Java " + currentJavaVersion);
            }
        }

        // Check minimum platform version
        if (minPlatformVersion != null) {
            if (!isValidSemver(minPlatformVersion)) {
                builder.addError("Invalid minimum platform version: " + minPlatformVersion);
            } else if (compareVersions(currentPlatformVersion, minPlatformVersion) < 0) {
                builder.addError("Plugin requires minimum platform version "
                        + minPlatformVersion + " but current is " + currentPlatformVersion);
            }
        }

        // Check maximum platform version
        if (maxPlatformVersion != null) {
            if (!isValidSemver(maxPlatformVersion)) {
                builder.addError("Invalid maximum platform version: " + maxPlatformVersion);
            } else if (compareVersions(currentPlatformVersion, maxPlatformVersion) > 0) {
                builder.addError("Plugin requires maximum platform version "
                        + maxPlatformVersion + " but current is " + currentPlatformVersion);
            }
        }

        // Check plugin API version compatibility
        if (pluginApiVersion != null) {
            if (!isValidSemver(pluginApiVersion)) {
                builder.addError("Invalid plugin API version: " + pluginApiVersion);
            } else if (!pluginApiVersion.equals(currentPluginApiVersion)) {
                builder.addWarning("Plugin compiled for plugin API " + pluginApiVersion
                        + " but current is " + currentPluginApiVersion);
            }
        }

        return builder.build();
    }

    /**
     * Returns the current Java version string.
     *
     * @return the Java version
     */
    public String currentJavaVersion() {
        return currentJavaVersion;
    }

    /**
     * Returns the current platform version string.
     *
     * @return the platform version
     */
    public String currentPlatformVersion() {
        return currentPlatformVersion;
    }

    /**
     * Returns the current plugin API version string.
     *
     * @return the plugin API version
     */
    public String currentPluginApiVersion() {
        return currentPluginApiVersion;
    }

    /**
     * Validates that the given string is a valid semver (X.Y.Z).
     */
    private static boolean isValidSemver(String version) {
        return version != null && SEMVER_PATTERN.matcher(version).matches();
    }

    /**
     * Validates that the given string is a valid Java version (N or N.N).
     */
    private static boolean isValidJavaVersion(String version) {
        return version != null && JAVA_VERSION_PATTERN.matcher(version).matches();
    }

    /**
     * Compares two version strings numerically.
     * Supports semver (X.Y.Z) and Java versions (N or N.N).
     * Returns negative if v1 < v2, positive if v1 > v2, zero if equal.
     */
    private static int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int maxLen = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLen; i++) {
            int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
        }
        return 0;
    }
}