package platform.kernels.identity.verification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>IdentityVerificationSuite</b>
 *
 * <p>The orchestration layer for Identity Kernel verification.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Invokes IdentityArchitectureVerifier.</li>
 *   <li>Invokes IdentityContractVerifier.</li>
 *   <li>Invokes IdentityIntegrityVerifier.</li>
 *   <li>Aggregates all verification results.</li>
 *   <li>Returns one immutable IdentityVerificationResult.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Read-only — never modifies application state.</li>
 *   <li>No persistence — does not store verification results.</li>
 *   <li>No business logic — coordinates verifiers only.</li>
 *   <li>Stateless — no instance fields, no mutable state.</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. It contains no mutable state
 * and all operations are pure functions.</p>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-ID-107</p>
 *
 * @see IdentityArchitectureVerifier
 * @see IdentityContractVerifier
 * @see IdentityIntegrityVerifier
 * @see IdentityVerificationResult
 */
public final class IdentityVerificationSuite {

    private final IdentityArchitectureVerifier architectureVerifier;
    private final IdentityContractVerifier contractVerifier;
    private final IdentityIntegrityVerifier integrityVerifier;

    /**
     * Constructs a new {@code IdentityVerificationSuite} with the given verifiers.
     *
     * <p>Constructor injection is the only allowed injection mechanism.</p>
     *
     * @param architectureVerifier the architecture verifier (must not be null)
     * @param contractVerifier the contract verifier (must not be null)
     * @param integrityVerifier the integrity verifier (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public IdentityVerificationSuite(
            IdentityArchitectureVerifier architectureVerifier,
            IdentityContractVerifier contractVerifier,
            IdentityIntegrityVerifier integrityVerifier) {
        this.architectureVerifier = java.util.Objects.requireNonNull(architectureVerifier, "architectureVerifier must not be null");
        this.contractVerifier = java.util.Objects.requireNonNull(contractVerifier, "contractVerifier must not be null");
        this.integrityVerifier = java.util.Objects.requireNonNull(integrityVerifier, "integrityVerifier must not be null");
    }

    /**
     * Constructs a new {@code IdentityVerificationSuite} with default verifiers.
     *
     * <p>This constructor creates default instances of all verifiers.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     */
    public IdentityVerificationSuite() {
        this(new IdentityArchitectureVerifier(), new IdentityContractVerifier(), new IdentityIntegrityVerifier());
    }

    /**
     * Performs all verifications and returns the aggregated result.
     *
     * <p>Invokes all verifiers, aggregates their results, and returns a single
     * immutable IdentityVerificationResult.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not modify application state.</p>
     *
     * @return the aggregated verification result (never null)
     */
    public IdentityVerificationResult verifyAll() {
        List<String> passedChecks = new ArrayList<>();
        List<String> failedChecks = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        // Invoke architecture verifier
        List<String> architectureChecks = architectureVerifier.verifyAll();
        for (String check : architectureChecks) {
            if (check.startsWith("PASS")) {
                passedChecks.add(check);
            } else {
                failedChecks.add(check);
            }
        }

        // Invoke contract verifier
        List<String> contractChecks = contractVerifier.verifyAll();
        for (String check : contractChecks) {
            if (check.startsWith("PASS")) {
                passedChecks.add(check);
            } else {
                failedChecks.add(check);
            }
        }

        // Invoke integrity verifier
        List<String> integrityChecks = integrityVerifier.verifyAll();
        for (String check : integrityChecks) {
            if (check.startsWith("PASS")) {
                passedChecks.add(check);
            } else {
                failedChecks.add(check);
            }
        }

        // Add metadata
        metadata.put("totalChecks", passedChecks.size() + failedChecks.size());
        metadata.put("passedCount", passedChecks.size());
        metadata.put("failedCount", failedChecks.size());
        metadata.put("architectureChecks", architectureChecks.size());
        metadata.put("contractChecks", contractChecks.size());
        metadata.put("integrityChecks", integrityChecks.size());

        boolean successful = failedChecks.isEmpty();

        return new IdentityVerificationResult(
                successful,
                Instant.now(),
                passedChecks,
                failedChecks,
                metadata
        );
    }

    /**
     * Performs architecture verification only.
     *
     * <p>Invokes only the IdentityArchitectureVerifier and returns the result.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not modify application state.</p>
     *
     * @return the architecture verification result (never null)
     */
    public IdentityVerificationResult verifyArchitecture() {
        List<String> passedChecks = new ArrayList<>();
        List<String> failedChecks = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        List<String> architectureChecks = architectureVerifier.verifyAll();
        for (String check : architectureChecks) {
            if (check.startsWith("PASS")) {
                passedChecks.add(check);
            } else {
                failedChecks.add(check);
            }
        }

        metadata.put("totalChecks", passedChecks.size() + failedChecks.size());
        metadata.put("passedCount", passedChecks.size());
        metadata.put("failedCount", failedChecks.size());
        metadata.put("verificationType", "architecture");

        return new IdentityVerificationResult(
                failedChecks.isEmpty(),
                Instant.now(),
                passedChecks,
                failedChecks,
                metadata
        );
    }

    /**
     * Performs contract verification only.
     *
     * <p>Invokes only the IdentityContractVerifier and returns the result.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not modify application state.</p>
     *
     * @return the contract verification result (never null)
     */
    public IdentityVerificationResult verifyContracts() {
        List<String> passedChecks = new ArrayList<>();
        List<String> failedChecks = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        List<String> contractChecks = contractVerifier.verifyAll();
        for (String check : contractChecks) {
            if (check.startsWith("PASS")) {
                passedChecks.add(check);
            } else {
                failedChecks.add(check);
            }
        }

        metadata.put("totalChecks", passedChecks.size() + failedChecks.size());
        metadata.put("passedCount", passedChecks.size());
        metadata.put("failedCount", failedChecks.size());
        metadata.put("verificationType", "contracts");

        return new IdentityVerificationResult(
                failedChecks.isEmpty(),
                Instant.now(),
                passedChecks,
                failedChecks,
                metadata
        );
    }

    /**
     * Performs integrity verification only.
     *
     * <p>Invokes only the IdentityIntegrityVerifier and returns the result.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not modify application state.</p>
     *
     * @return the integrity verification result (never null)
     */
    public IdentityVerificationResult verifyIntegrity() {
        List<String> passedChecks = new ArrayList<>();
        List<String> failedChecks = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        List<String> integrityChecks = integrityVerifier.verifyAll();
        for (String check : integrityChecks) {
            if (check.startsWith("PASS")) {
                passedChecks.add(check);
            } else {
                failedChecks.add(check);
            }
        }

        metadata.put("totalChecks", passedChecks.size() + failedChecks.size());
        metadata.put("passedCount", passedChecks.size());
        metadata.put("failedCount", failedChecks.size());
        metadata.put("verificationType", "integrity");

        return new IdentityVerificationResult(
                failedChecks.isEmpty(),
                Instant.now(),
                passedChecks,
                failedChecks,
                metadata
        );
    }
}