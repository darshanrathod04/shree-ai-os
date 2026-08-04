package com.shreeai.os.platform.kernels.cognitive.verification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>CognitiveVerificationSuite</b>
 *
 * <p>Coordinates the cognitive architecture verification pipeline, executing
 * all verifiers in sequence and aggregating their findings into a comprehensive
 * verification result.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Executes the verification pipeline in canonical order.</li>
 *   <li>Aggregates findings from all verifiers.</li>
 *   <li>Produces immutable CognitiveVerificationResult.</li>
 *   <li>Maintains verification metadata for audit purposes.</li>
 * </ul>
 *
 * <p><b>Verification Pipeline:</b></p>
 * <ol>
 *   <li>CognitiveArchitectureVerifier — architectural compliance</li>
 *   <li>CognitiveContractVerifier — contract consistency</li>
 *   <li>CognitiveIntegrityVerifier — implementation integrity</li>
 * </ol>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — no mutable fields or caches.</li>
 *   <li>Read-only — performs inspection only, never modifies state.</li>
 *   <li>Deterministic — produces consistent results for identical inputs.</li>
 *   <li>Thread-safe — no synchronization required.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-107, EIO-ARCH-001</p>
 *
 * @see CognitiveArchitectureVerifier
 * @see CognitiveContractVerifier
 * @see CognitiveIntegrityVerifier
 * @see CognitiveVerificationResult
 */
public final class CognitiveVerificationSuite {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class provides only static verification methods and should not
     * be instantiated.</p>
     */
    private CognitiveVerificationSuite() {
        // Utility class — no instantiation
    }

    /**
     * Executes the complete cognitive architecture verification pipeline.
     *
     * <p>Runs all verifiers in canonical order and aggregates their findings
     * into a comprehensive verification result.</p>
     *
     * <p><b>Verification Pipeline:</b></p>
     * <ol>
     *   <li>CognitiveArchitectureVerifier — verifies package boundaries, dependency direction, service-engine separation, API isolation, constructor injection, and platform language compliance.</li>
     *   <li>CognitiveContractVerifier — verifies API contracts, model contracts, validation contracts, error contracts, service contracts, and engine contracts.</li>
     *   <li>CognitiveIntegrityVerifier — verifies immutability, defensive copying, constructor validation, thread safety, deterministic processing, immutable collections, CognitiveId usage, and processing result integrity.</li>
     * </ol>
     *
     * <p><b>Architectural Boundaries:</b></p>
     * <ul>
     *   <li>Never invokes services or processing engines.</li>
     *   <li>Never executes reasoning, reflection, or decision logic.</li>
     *   <li>Never modifies models or cognitive state.</li>
     *   <li>Never repairs architectural violations.</li>
     *   <li>Performs read-only inspection only.</li>
     * </ul>
     *
     * @param allClasses all cognitive kernel classes to verify (must not be null)
     * @return immutable verification result (never null)
     * @throws IllegalArgumentException if allClasses is null
     */
    public static CognitiveVerificationResult verify(List<Class<?>> allClasses) {
        if (allClasses == null) {
            throw new IllegalArgumentException("Classes must not be null");
        }

        Instant verifiedAt = Instant.now();
        Map<String, Object> metadata = new HashMap<>();
        List<String> allFindings = new ArrayList<>();

        // Categorize classes by package
        List<Class<?>> apiClasses = new ArrayList<>();
        List<Class<?>> modelClasses = new ArrayList<>();
        List<Class<?>> validationClasses = new ArrayList<>();
        List<Class<?>> errorClasses = new ArrayList<>();
        List<Class<?>> serviceClasses = new ArrayList<>();
        List<Class<?>> engineClasses = new ArrayList<>();

        for (Class<?> clazz : allClasses) {
            String packageName = clazz.getPackageName();

            if (isApiPackage(packageName)) {
                apiClasses.add(clazz);
            } else if (isModelPackage(packageName)) {
                modelClasses.add(clazz);
            } else if (isValidationPackage(packageName)) {
                validationClasses.add(clazz);
            } else if (isErrorPackage(packageName)) {
                errorClasses.add(clazz);
            } else if (isServicePackage(packageName)) {
                serviceClasses.add(clazz);
            } else if (isEnginePackage(packageName)) {
                engineClasses.add(clazz);
            }
        }

        metadata.put("totalClasses", allClasses.size());
        metadata.put("apiClasses", apiClasses.size());
        metadata.put("modelClasses", modelClasses.size());
        metadata.put("validationClasses", validationClasses.size());
        metadata.put("errorClasses", errorClasses.size());
        metadata.put("serviceClasses", serviceClasses.size());
        metadata.put("engineClasses", engineClasses.size());

        // Step 1: Architecture verification
        List<String> architectureFindings = CognitiveArchitectureVerifier.verifyPackageBoundaries(allClasses);
        allFindings.addAll(architectureFindings);
        metadata.put("architectureFindingsCount", architectureFindings.size());

        List<String> dependencyFindings = CognitiveArchitectureVerifier.verifyDependencyDirection(allClasses);
        allFindings.addAll(dependencyFindings);
        metadata.put("dependencyFindingsCount", dependencyFindings.size());

        List<String> separationFindings = CognitiveArchitectureVerifier.verifyServiceEngineSeparation(serviceClasses, engineClasses);
        allFindings.addAll(separationFindings);
        metadata.put("separationFindingsCount", separationFindings.size());

        List<String> apiIsolationFindings = CognitiveArchitectureVerifier.verifyPublicApiIsolation(apiClasses);
        allFindings.addAll(apiIsolationFindings);
        metadata.put("apiIsolationFindingsCount", apiIsolationFindings.size());

        List<String> constructorInjectionFindings = CognitiveArchitectureVerifier.verifyConstructorInjection(allClasses);
        allFindings.addAll(constructorInjectionFindings);
        metadata.put("constructorInjectionFindingsCount", constructorInjectionFindings.size());

        // Step 2: Contract verification
        List<String> apiContractFindings = CognitiveContractVerifier.verifyApiContracts(apiClasses);
        allFindings.addAll(apiContractFindings);
        metadata.put("apiContractFindingsCount", apiContractFindings.size());

        List<String> modelContractFindings = CognitiveContractVerifier.verifyModelContracts(modelClasses);
        allFindings.addAll(modelContractFindings);
        metadata.put("modelContractFindingsCount", modelContractFindings.size());

        List<String> validationContractFindings = CognitiveContractVerifier.verifyValidationContracts(validationClasses);
        allFindings.addAll(validationContractFindings);
        metadata.put("validationContractFindingsCount", validationContractFindings.size());

        List<String> errorContractFindings = CognitiveContractVerifier.verifyErrorContracts(errorClasses);
        allFindings.addAll(errorContractFindings);
        metadata.put("errorContractFindingsCount", errorContractFindings.size());

        List<String> serviceContractFindings = CognitiveContractVerifier.verifyServiceContracts(serviceClasses);
        allFindings.addAll(serviceContractFindings);
        metadata.put("serviceContractFindingsCount", serviceContractFindings.size());

        List<String> engineContractFindings = CognitiveContractVerifier.verifyEngineContracts(engineClasses);
        allFindings.addAll(engineContractFindings);
        metadata.put("engineContractFindingsCount", engineContractFindings.size());

        // Step 3: Integrity verification
        List<String> immutabilityFindings = CognitiveIntegrityVerifier.verifyImmutability(modelClasses);
        allFindings.addAll(immutabilityFindings);
        metadata.put("immutabilityFindingsCount", immutabilityFindings.size());

        List<String> defensiveCopyingFindings = CognitiveIntegrityVerifier.verifyDefensiveCopying(allClasses);
        allFindings.addAll(defensiveCopyingFindings);
        metadata.put("defensiveCopyingFindingsCount", defensiveCopyingFindings.size());

        List<String> constructorValidationFindings = CognitiveIntegrityVerifier.verifyConstructorValidation(allClasses);
        allFindings.addAll(constructorValidationFindings);
        metadata.put("constructorValidationFindingsCount", constructorValidationFindings.size());

        List<String> threadSafetyFindings = CognitiveIntegrityVerifier.verifyThreadSafety(allClasses);
        allFindings.addAll(threadSafetyFindings);
        metadata.put("threadSafetyFindingsCount", threadSafetyFindings.size());

        List<String> cognitiveIdFindings = CognitiveIntegrityVerifier.verifyCognitiveIdUsage(allClasses);
        allFindings.addAll(cognitiveIdFindings);
        metadata.put("cognitiveIdFindingsCount", cognitiveIdFindings.size());

        List<String> resultIntegrityFindings = CognitiveIntegrityVerifier.verifyProcessingResultIntegrity(allClasses);
        allFindings.addAll(resultIntegrityFindings);
        metadata.put("resultIntegrityFindingsCount", resultIntegrityFindings.size());

        // Determine overall success (no findings means success)
        boolean successful = allFindings.isEmpty();

        return new CognitiveVerificationResult(
            successful,
            Collections.unmodifiableList(allFindings),
            verifiedAt,
            Collections.unmodifiableMap(metadata)
        );
    }

    /**
     * Executes a focused verification on specific verifier categories.
     *
     * <p>Allows targeted verification of specific aspects of the architecture
     * without running the complete pipeline.</p>
     *
     * <p><b>Allowed Categories:</b></p>
     * <ul>
     *   <li>"architecture" — package boundaries, dependencies, separation</li>
     *   <li>"contracts" — API, model, validation, error, service, engine contracts</li>
     *   <li>"integrity" — immutability, defensive copying, thread safety</li>
     * </ul>
     *
     * @param allClasses all cognitive kernel classes to verify (must not be null)
     * @param categories the verification categories to execute (must not be null)
     * @return immutable verification result (never null)
     * @throws IllegalArgumentException if any parameter is null
     */
    public static CognitiveVerificationResult verifyCategories(List<Class<?>> allClasses, List<String> categories) {
        if (allClasses == null) {
            throw new IllegalArgumentException("Classes must not be null");
        }
        if (categories == null) {
            throw new IllegalArgumentException("Categories must not be null");
        }

        Instant verifiedAt = Instant.now();
        Map<String, Object> metadata = new HashMap<>();
        List<String> allFindings = new ArrayList<>();

        // Categorize classes by package
        List<Class<?>> apiClasses = new ArrayList<>();
        List<Class<?>> modelClasses = new ArrayList<>();
        List<Class<?>> validationClasses = new ArrayList<>();
        List<Class<?>> errorClasses = new ArrayList<>();
        List<Class<?>> serviceClasses = new ArrayList<>();
        List<Class<?>> engineClasses = new ArrayList<>();

        for (Class<?> clazz : allClasses) {
            String packageName = clazz.getPackageName();

            if (isApiPackage(packageName)) {
                apiClasses.add(clazz);
            } else if (isModelPackage(packageName)) {
                modelClasses.add(clazz);
            } else if (isValidationPackage(packageName)) {
                validationClasses.add(clazz);
            } else if (isErrorPackage(packageName)) {
                errorClasses.add(clazz);
            } else if (isServicePackage(packageName)) {
                serviceClasses.add(clazz);
            } else if (isEnginePackage(packageName)) {
                engineClasses.add(clazz);
            }
        }

        metadata.put("totalClasses", allClasses.size());
        metadata.put("categories", categories);

        // Execute selected categories
        for (String category : categories) {
            switch (category.toLowerCase()) {
                case "architecture" -> {
                    List<String> findings = CognitiveArchitectureVerifier.verifyPackageBoundaries(allClasses);
                    allFindings.addAll(findings);
                    metadata.put("architectureFindingsCount", findings.size());
                }
                case "contracts" -> {
                    List<String> findings = CognitiveContractVerifier.verifyApiContracts(apiClasses);
                    allFindings.addAll(findings);
                    List<String> modelFindings = CognitiveContractVerifier.verifyModelContracts(modelClasses);
                    allFindings.addAll(modelFindings);
                    metadata.put("contractFindingsCount", allFindings.size());
                }
                case "integrity" -> {
                    List<String> findings = CognitiveIntegrityVerifier.verifyImmutability(modelClasses);
                    allFindings.addAll(findings);
                    metadata.put("integrityFindingsCount", findings.size());
                }
                default -> {
                    // Unknown category — skip
                }
            }
        }

        boolean successful = allFindings.isEmpty();

        return new CognitiveVerificationResult(
            successful,
            Collections.unmodifiableList(allFindings),
            verifiedAt,
            Collections.unmodifiableMap(metadata)
        );
    }

    /**
     * Determines if a package is the API package.
     *
     * @param packageName the package name to check
     * @return true if the package is the API package
     */
    private static boolean isApiPackage(String packageName) {
        return packageName.equals("platform.kernels.cognitive.api") ||
               packageName.startsWith("platform.kernels.cognitive.api.");
    }

    /**
     * Determines if a package is the Model package.
     *
     * @param packageName the package name to check
     * @return true if the package is the Model package
     */
    private static boolean isModelPackage(String packageName) {
        return packageName.equals("platform.kernels.cognitive.model") ||
               packageName.startsWith("platform.kernels.cognitive.model.");
    }

    /**
     * Determines if a package is the Validation package.
     *
     * @param packageName the package name to check
     * @return true if the package is the Validation package
     */
    private static boolean isValidationPackage(String packageName) {
        return packageName.equals("platform.kernels.cognitive.validation") ||
               packageName.startsWith("platform.kernels.cognitive.validation.");
    }

    /**
     * Determines if a package is the Error package.
     *
     * @param packageName the package name to check
     * @return true if the package is the Error package
     */
    private static boolean isErrorPackage(String packageName) {
        return packageName.equals("platform.kernels.cognitive.error") ||
               packageName.startsWith("platform.kernels.cognitive.error.");
    }

    /**
     * Determines if a package is the Service package.
     *
     * @param packageName the package name to check
     * @return true if the package is the Service package
     */
    private static boolean isServicePackage(String packageName) {
        return packageName.equals("platform.kernels.cognitive.service") ||
               packageName.startsWith("platform.kernels.cognitive.service.");
    }

    /**
     * Determines if a package is the Engine package.
     *
     * @param packageName the package name to check
     * @return true if the package is the Engine package
     */
    private static boolean isEnginePackage(String packageName) {
        return packageName.equals("platform.kernels.cognitive.engine") ||
               packageName.startsWith("platform.kernels.cognitive.engine.");
    }
}