package com.shreeai.os.platform.kernels.memory.verification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>MemoryVerificationSuite</b>
 *
 * <p>The orchestration layer for Memory Kernel verification.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Invokes MemoryArchitectureVerifier.</li>
 *   <li>Invokes MemoryContractVerifier.</li>
 *   <li>Invokes MemoryIntegrityVerifier.</li>
 *   <li>Aggregates all verification results.</li>
 *   <li>Returns one immutable MemoryVerificationResult.</li>
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
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-MEM-107</p>
 *
 * @see MemoryArchitectureVerifier
 * @see MemoryContractVerifier
 * @see MemoryIntegrityVerifier
 * @see MemoryVerificationResult
 */
public final class MemoryVerificationSuite {

    private final MemoryArchitectureVerifier architectureVerifier;
    private final MemoryContractVerifier contractVerifier;
    private final MemoryIntegrityVerifier integrityVerifier;

    /**
     * Constructs a new {@code MemoryVerificationSuite} with the given verifiers.
     *
     * <p>Constructor injection is the only allowed injection mechanism.</p>
     *
     * @param architectureVerifier the architecture verifier (must not be null)
     * @param contractVerifier the contract verifier (must not be null)
     * @param integrityVerifier the integrity verifier (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public MemoryVerificationSuite(
            MemoryArchitectureVerifier architectureVerifier,
            MemoryContractVerifier contractVerifier,
            MemoryIntegrityVerifier integrityVerifier) {
        this.architectureVerifier = java.util.Objects.requireNonNull(architectureVerifier, "architectureVerifier must not be null");
        this.contractVerifier = java.util.Objects.requireNonNull(contractVerifier, "contractVerifier must not be null");
        this.integrityVerifier = java.util.Objects.requireNonNull(integrityVerifier, "integrityVerifier must not be null");
    }

    /**
     * Constructs a new {@code MemoryVerificationSuite} with default verifiers.
     *
     * <p>This constructor creates default instances of all verifiers.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     */
    public MemoryVerificationSuite() {
        this(new MemoryArchitectureVerifier(), new MemoryContractVerifier(), new MemoryIntegrityVerifier());
    }

    /**
     * Performs all verifications and returns the aggregated result.
     *
     * <p>Invokes all verifiers, aggregates their results, and returns a single
     * immutable MemoryVerificationResult.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not modify application state.</p>
     *
     * @return the aggregated verification result (never null)
     */
    public MemoryVerificationResult verifyAll() {
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

        return new MemoryVerificationResult(
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
     * <p>Invokes only the MemoryArchitectureVerifier and returns the result.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not modify application state.</p>
     *
     * @return the architecture verification result (never null)
     */
    public MemoryVerificationResult verifyArchitecture() {
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

        return new MemoryVerificationResult(
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
     * <p>Invokes only the MemoryContractVerifier and returns the result.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not modify application state.</p>
     *
     * @return the contract verification result (never null)
     */
    public MemoryVerificationResult verifyContracts() {
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

        return new MemoryVerificationResult(
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
     * <p>Invokes only the MemoryIntegrityVerifier and returns the result.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Side Effects:</b> None. This method does not modify application state.</p>
     *
     * @return the integrity verification result (never null)
     */
    public MemoryVerificationResult verifyIntegrity() {
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

        return new MemoryVerificationResult(
                failedChecks.isEmpty(),
                Instant.now(),
                passedChecks,
                failedChecks,
                metadata
        );
    }
}