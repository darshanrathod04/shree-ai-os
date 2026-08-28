package com.shreeai.os.platform.kernels.knowledge.verification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>KnowledgeVerificationSuite</b>
 *
 * <p>The orchestration layer for Knowledge Kernel architectural verification.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates all verifiers in the verification pipeline.</li>
 *   <li>Aggregates verification findings from all verifiers.</li>
 *   <li>Produces one immutable KnowledgeVerificationResult.</li>
 *   <li>Never invokes services or business logic.</li>
 *   <li>Never modifies the kernel.</li>
 *   <li>Reports only — never repairs failures automatically.</li>
 * </ul>
 *
 * <p><b>Verification Pipeline:</b></p>
 * <pre>
 * KnowledgeArchitectureVerifier
 *             │
 *             ▼
 * KnowledgeContractVerifier
 *             │
 *             ▼
 * KnowledgeIntegrityVerifier
 *             │
 *             ▼
 * KnowledgeVerificationResult
 * </pre>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Read-only — never modifies the kernel.</li>
 *   <li>Stateless — no mutable instance state.</li>
 *   <li>Thread-safe — immutable after construction.</li>
 *   <li>Deterministic — same inputs always produce same outputs.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-107, EIO-ARCH-001</p>
 *
 * @see KnowledgeArchitectureVerifier
 * @see KnowledgeContractVerifier
 * @see KnowledgeIntegrityVerifier
 * @see KnowledgeVerificationResult
 */
public final class KnowledgeVerificationSuite {

    private final KnowledgeArchitectureVerifier architectureVerifier;
    private final KnowledgeContractVerifier contractVerifier;
    private final KnowledgeIntegrityVerifier integrityVerifier;

    /**
     * Creates a new KnowledgeVerificationSuite with default verifiers.
     *
     * <p>Uses constructor injection to provide verifier dependencies.
     * All verifiers are stateless and thread-safe.</p>
     */
    public KnowledgeVerificationSuite() {
        this(
                new KnowledgeArchitectureVerifier(),
                new KnowledgeContractVerifier(),
                new KnowledgeIntegrityVerifier()
        );
    }

    /**
     * Creates a new KnowledgeVerificationSuite with custom verifiers.
     *
     * <p>Uses constructor injection to provide verifier dependencies.
     * All verifiers must be non-null.</p>
     *
     * @param architectureVerifier the architecture verifier (must not be null)
     * @param contractVerifier     the contract verifier (must not be null)
     * @param integrityVerifier    the integrity verifier (must not be null)
     * @throws NullPointerException if any verifier is null
     */
    public KnowledgeVerificationSuite(
            KnowledgeArchitectureVerifier architectureVerifier,
            KnowledgeContractVerifier contractVerifier,
            KnowledgeIntegrityVerifier integrityVerifier) {
        this.architectureVerifier = java.util.Objects.requireNonNull(architectureVerifier, "architectureVerifier must not be null");
        this.contractVerifier = java.util.Objects.requireNonNull(contractVerifier, "contractVerifier must not be null");
        this.integrityVerifier = java.util.Objects.requireNonNull(integrityVerifier, "integrityVerifier must not be null");
    }

    /**
     * Executes the complete verification suite.
     *
     * <p>Coordinates all verifiers in the pipeline and aggregates their findings
     * into a single immutable KnowledgeVerificationResult.</p>
     *
     * <p><b>Verification Pipeline:</b></p>
     * <ol>
     *   <li>Architecture verification — package boundaries, dependency direction, service/engine separation.</li>
     *   <li>Contract verification — API contracts, service contracts, engine contracts, validator contracts, error contracts.</li>
     *   <li>Integrity verification — immutability, defensive copying, constructor validation, thread safety.</li>
     * </ol>
     *
     * <p><b>Semantic Boundary:</b> This method only verifies architecture. It never
     * invokes services, performs business logic, or modifies the kernel.</p>
     *
     * @return an immutable KnowledgeVerificationResult containing all findings
     */
    public KnowledgeVerificationResult verify() {
        List<String> allFindings = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        // Phase 1: Architecture verification
        metadata.put("phase", "architecture");
        List<String> architectureFindings = architectureVerifier.verify();
        allFindings.addAll(architectureFindings);
        metadata.put("architectureFindingsCount", architectureFindings.size());

        // Phase 2: Contract verification
        metadata.put("phase", "contracts");
        List<String> contractFindings = contractVerifier.verify();
        allFindings.addAll(contractFindings);
        metadata.put("contractFindingsCount", contractFindings.size());

        // Phase 3: Integrity verification
        metadata.put("phase", "integrity");
        List<String> integrityFindings = integrityVerifier.verify();
        allFindings.addAll(integrityFindings);
        metadata.put("integrityFindingsCount", integrityFindings.size());

        // Determine overall success (no critical findings)
        boolean successful = allFindings.stream()
                .noneMatch(finding -> finding.startsWith("ERROR") || finding.startsWith("CRITICAL"));

        metadata.put("totalFindings", allFindings.size());
        metadata.put("successful", successful);
        metadata.put("verifiedAt", Instant.now().toString());

        return KnowledgeVerificationResult.of(successful, allFindings, Instant.now(), metadata);
    }

    /**
     * Returns the architecture verifier.
     *
     * @return the architecture verifier
     */
    public KnowledgeArchitectureVerifier getArchitectureVerifier() {
        return architectureVerifier;
    }

    /**
     * Returns the contract verifier.
     *
     * @return the contract verifier
     */
    public KnowledgeContractVerifier getContractVerifier() {
        return contractVerifier;
    }

    /**
     * Returns the integrity verifier.
     *
     * @return the integrity verifier
     */
    public KnowledgeIntegrityVerifier getIntegrityVerifier() {
        return integrityVerifier;
    }
}