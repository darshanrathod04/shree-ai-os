package platform.kernels.context.verification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>ContextVerificationSuite</b>
 *
 * <p>The orchestration layer for the Context Kernel Verification Suite.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates all verifiers in the correct order.</li>
 *   <li>Aggregates verification outcomes from all verifiers.</li>
 *   <li>Produces a single immutable ContextVerificationResult.</li>
 *   <li>Never modifies Context models, services, validators, or engine state.</li>
 *   <li>Reports verification results only - never repairs failures.</li>
 * </ul>
 *
 * <p><b>Verification Flow:</b></p>
 * <pre>
 * ContextArchitectureVerifier
 *           │
 *           ▼
 * ContextContractVerifier
 *           │
 *           ▼
 * ContextIntegrityVerifier
 *           │
 *           ▼
 * ContextVerificationResult
 * </pre>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Read-only - never modifies the kernel.</li>
 *   <li>Stateless - no mutable instance fields.</li>
 *   <li>Thread-safe - deterministic verification logic.</li>
 *   <li>No business logic - pure verification coordination.</li>
 *   <li>No mutation - aggregates verification results only.</li>
 *   <li>No persistence - produces immutable verification results.</li>
 * </ul>
 *
 * <p><b>Verification Principles:</b></p>
 * <ul>
 *   <li>Verification may inspect package organization, contracts, models, validators, services, engines, and error architecture.</li>
 *   <li>Verification must never modify application state, mutate Context objects, access repositories, perform persistence, invoke AI, perform networking, publish events, create threads, schedule work, or modify files.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel - Verification Suite</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-107, EIO-ARCH-001</p>
 *
 * @see ContextArchitectureVerifier
 * @see ContextContractVerifier
 * @see ContextIntegrityVerifier
 * @see ContextVerificationResult
 */
public final class ContextVerificationSuite {

    /**
     * Private constructor to prevent instantiation.
     */
    private ContextVerificationSuite() {
        // Utility class - prevent instantiation
    }

    /**
     * Executes the full Context Kernel verification suite.
     *
     * <p><b>Verification Flow:</b></p>
     * <ol>
     *   <li>ContextArchitectureVerifier.verify() - verifies architectural compliance</li>
     *   <li>ContextContractVerifier.verify() - verifies contract compliance</li>
     *   <li>ContextIntegrityVerifier.verify() - verifies implementation integrity</li>
     *   <li>Aggregate all findings into a single immutable ContextVerificationResult</li>
     * </ol>
     *
     * <p><b>Read-Only:</b> This method performs inspection only and never modifies
     * the kernel or its components.</p>
     *
     * <p><b>Thread Safety:</b> This method is thread-safe and deterministic.</p>
     *
     * <p><b>Stateless:</b> This method has no side effects and maintains no state.</p>
     *
     * @return an immutable ContextVerificationResult containing all verification findings
     */
    public static ContextVerificationResult run() {
        Instant verifiedAt = Instant.now();

        // Step 1: Verify architecture
        List<String> architectureFindings = ContextArchitectureVerifier.verify();

        // Step 2: Verify contracts
        List<String> contractFindings = ContextContractVerifier.verify();

        // Step 3: Verify integrity
        List<String> integrityFindings = ContextIntegrityVerifier.verify();

        // Aggregate all findings
        List<String> passedChecks = new ArrayList<>();
        List<String> failedChecks = new ArrayList<>();

        // Categorize architecture findings
        categorizeFindings(architectureFindings, passedChecks, failedChecks);

        // Categorize contract findings
        categorizeFindings(contractFindings, passedChecks, failedChecks);

        // Categorize integrity findings
        categorizeFindings(integrityFindings, passedChecks, failedChecks);

        // Determine overall success
        boolean successful = failedChecks.isEmpty();

        // Build metadata
        Map<String, Object> metadata = Map.of(
                "verificationSuite", "ContextVerificationSuite",
                "architectureVerifier", "ContextArchitectureVerifier",
                "contractVerifier", "ContextContractVerifier",
                "integrityVerifier", "ContextIntegrityVerifier",
                "totalChecks", passedChecks.size() + failedChecks.size(),
                "passedCount", passedChecks.size(),
                "failedCount", failedChecks.size()
        );

        return new ContextVerificationResult(
                successful,
                verifiedAt,
                List.copyOf(passedChecks),
                List.copyOf(failedChecks),
                metadata
        );
    }

    /**
     * Categorizes verification findings into passed and failed checks.
     *
     * <p>Findings starting with "PASS:" are categorized as passed checks.
     * All other findings are categorized as failed checks.</p>
     *
     * <p><b>Read-Only:</b> This method performs no mutation of kernel state.</p>
     *
     * @param findings the list of verification findings to categorize
     * @param passedChecks the list to add passed checks to (must not be null)
     * @param failedChecks the list to add failed checks to (must not be null)
     */
    private static void categorizeFindings(
            List<String> findings,
            List<String> passedChecks,
            List<String> failedChecks) {
        for (String finding : findings) {
            if (finding.startsWith("PASS:")) {
                passedChecks.add(finding);
            } else {
                failedChecks.add(finding);
            }
        }
    }

    /**
     * Executes a specific verifier by name.
     *
     * <p><b>Supported Verifiers:</b></p>
     * <ul>
     *   <li>"architecture" - ContextArchitectureVerifier</li>
     *   <li>"contract" - ContextContractVerifier</li>
     *   <li>"integrity" - ContextIntegrityVerifier</li>
     * </ul>
     *
     * <p><b>Read-Only:</b> This method performs inspection only.</p>
     *
     * @param verifierName the name of the verifier to execute (must not be null)
     * @return an immutable ContextVerificationResult for the specified verifier
     * @throws IllegalArgumentException if the verifier name is not recognized
     */
    public static ContextVerificationResult runVerifier(String verifierName) {
        Instant verifiedAt = Instant.now();
        List<String> findings;

        switch (verifierName.toLowerCase()) {
            case "architecture":
                findings = ContextArchitectureVerifier.verify();
                break;
            case "contract":
                findings = ContextContractVerifier.verify();
                break;
            case "integrity":
                findings = ContextIntegrityVerifier.verify();
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown verifier: '" + verifierName + "'. Supported verifiers: architecture, contract, integrity");
        }

        List<String> passedChecks = new ArrayList<>();
        List<String> failedChecks = new ArrayList<>();
        categorizeFindings(findings, passedChecks, failedChecks);

        boolean successful = failedChecks.isEmpty();

        Map<String, Object> metadata = Map.of(
                "verificationSuite", "ContextVerificationSuite",
                "verifier", verifierName,
                "totalChecks", passedChecks.size() + failedChecks.size(),
                "passedCount", passedChecks.size(),
                "failedCount", failedChecks.size()
        );

        return new ContextVerificationResult(
                successful,
                verifiedAt,
                List.copyOf(passedChecks),
                List.copyOf(failedChecks),
                metadata
        );
    }
}