/**
 * <b>Plugin Verification</b>
 *
 * <p>This package provides the plugin verification framework for Shree AI OS.
 * Before any plugin can be loaded, it must pass verification — similar to how
 * Android verifies APKs, VS Code verifies extensions, and IntelliJ verifies plugins.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link platform.core.plugin.verification.PluginVerifier} — The orchestrator
 *       that runs the full verification pipeline.</li>
 *   <li>{@link platform.core.plugin.verification.VerificationResult} — Immutable
 *       result containing a validity flag and a list of issues.</li>
 *   <li>{@link platform.core.plugin.verification.VerificationIssue} — Immutable
 *       issue with a severity level and message.</li>
 *   <li>{@link platform.core.plugin.verification.VerificationSeverity} — Severity
 *       levels: INFO, WARNING, ERROR.</li>
 *   <li>{@link platform.core.plugin.verification.PluginCompatibilityChecker} —
 *       Checks Java version, platform version, and plugin API compatibility.</li>
 *   <li>{@link platform.core.plugin.verification.PluginDependencyChecker} —
 *       Checks that all declared dependencies are available.</li>
 * </ul>
 *
 * <h2>Verification Pipeline</h2>
 * <ol>
 *   <li><strong>Metadata</strong> — checks required fields (id, name, version, provider)</li>
 *   <li><strong>Version</strong> — validates semantic versioning (X.Y.Z)</li>
 *   <li><strong>Dependencies</strong> — checks all declared dependencies are available</li>
 *   <li><strong>Compatibility</strong> — checks Java version, platform version, plugin API version</li>
 *   <li><strong>Duplicate IDs</strong> — rejects duplicate plugin IDs</li>
 * </ol>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><strong>Stateless</strong> — no mutable state.</li>
 *   <li><strong>Thread-safe</strong> — all classes immutable.</li>
 *   <li><strong>Deterministic</strong> — same input always produces the same output.</li>
 *   <li><strong>Zero side effects</strong> — no logging, no loading, no filesystem, no network.</li>
 *   <li><strong>No Spring, Lombok, or JPA</strong> — pure Java 21.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see platform.core.plugin.verification.PluginVerifier
 * @see platform.core.plugin.verification.VerificationResult
 * @see platform.core.plugin.verification.VerificationIssue
 * @see platform.core.plugin.verification.VerificationSeverity
 * @see platform.core.plugin.verification.PluginCompatibilityChecker
 * @see platform.core.plugin.verification.PluginDependencyChecker
 */
package platform.core.plugin.verification;