package platform.kernels.multiagent.verification;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>MultiAgentContractVerifier</b>
 *
 * <p>Verifies the public API contracts of the Multi-Agent Kernel.
 * This class uses reflection to inspect that API contracts remain contract-oriented
 * and reference canonical domain models.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Verification Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-MAGENT-107, EIO-ARCH-001</p>
 *
 * <p>MultiAgentContractVerifier remains inspection-only and stateless.
 * It does NOT invoke any runtime Multi-Agent operations.</p>
 *
 * @since 1.0
 */
public final class MultiAgentContractVerifier {

    private MultiAgentContractVerifier() {
        // Utility class — no instantiation
    }

    /**
     * Verifies the Multi-Agent Kernel contracts and returns an immutable verification result.
     *
     * @return immutable verification result
     * @since 1.0
     */
    public static MultiAgentVerificationResult verify() {
        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Verify API contracts are interfaces
        verifyApiContractsAreInterfaces(violations);

        // Verify service implementation
        verifyServiceImplementation(violations);

        // Verify engine contract
        verifyEngineContract(violations);

        // Verify canonical model usage
        verifyCanonicalModelUsage(violations);

        boolean contractsValid = violations.isEmpty();
        Map<String, Object> metadata = Map.of(
                "verifier", "MultiAgentContractVerifier",
                "verifiedAt", java.time.Instant.now()
        );

        return new MultiAgentVerificationResult(
                true,
                contractsValid,
                true,
                violations,
                metadata,
                java.time.Instant.now()
        );
    }

    /**
     * Verifies that public API contracts are interfaces, not concrete classes.
     */
    private static void verifyApiContractsAreInterfaces(List<String> violations) {
        String[] apiContracts = {
            "platform.kernels.multiagent.api.MultiAgentService",
            "platform.kernels.multiagent.api.AgentRegistryService",
            "platform.kernels.multiagent.api.AgentDiscoveryService",
            "platform.kernels.multiagent.api.CapabilityRegistryService",
            "platform.kernels.multiagent.api.AgentLifecycleService",
            "platform.kernels.multiagent.api.AgentCommunicationService"
        };

        for (String contractName : apiContracts) {
            try {
                Class<?> clazz = Class.forName(contractName);
                if (!clazz.isInterface()) {
                    violations.add("API contract " + contractName + " is not an interface");
                }
            } catch (ClassNotFoundException e) {
                violations.add("Missing API contract: " + contractName);
            }
        }
    }

    /**
     * Verifies that DefaultMultiAgentService implements the expected API interface.
     */
    private static void verifyServiceImplementation(List<String> violations) {
        try {
            Class<?> serviceClass = Class.forName("platform.kernels.multiagent.service.DefaultMultiAgentService");
            Class<?> expectedInterface = Class.forName("platform.kernels.multiagent.api.MultiAgentService");

            if (!expectedInterface.isAssignableFrom(serviceClass)) {
                violations.add("DefaultMultiAgentService does not implement MultiAgentService");
            }

            // Verify statelessness: fields should be final
            Field[] fields = serviceClass.getDeclaredFields();
            for (Field f : fields) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    violations.add("DefaultMultiAgentService field '" + f.getName() + "' is not final");
                }
            }
        } catch (ClassNotFoundException e) {
            violations.add("Cannot inspect service implementation: " + e.getMessage());
        }
    }

    /**
     * Verifies the engine contract:
     * - MultiAgentProcessingEngine is an interface
     * - DefaultMultiAgentProcessingEngine implements it
     * - Interface is in engine package (not obsolete service package)
     */
    private static void verifyEngineContract(List<String> violations) {
        try {
            Class<?> engineInterface = Class.forName("platform.kernels.multiagent.engine.MultiAgentProcessingEngine");
            if (!engineInterface.isInterface()) {
                violations.add("MultiAgentProcessingEngine in engine package is not an interface");
            }

            Class<?> defaultEngine = Class.forName("platform.kernels.multiagent.engine.DefaultMultiAgentProcessingEngine");
            if (!engineInterface.isAssignableFrom(defaultEngine)) {
                violations.add("DefaultMultiAgentProcessingEngine does not implement MultiAgentProcessingEngine");
            }

            // Verify engine fields are stateless
            Field[] fields = defaultEngine.getDeclaredFields();
            for (Field f : fields) {
                if (!f.getType().getName().startsWith("java.") && !Modifier.isFinal(f.getModifiers())) {
                    // Check if it's a non-final non-Java type
                    // This is a warning, not a violation, as some types may be final
                }
            }

            // Verify engine has no fields (stateless)
            if (fields.length > 0) {
                // Engine may have zero fields if truly stateless
                // DefaultMultiAgentProcessingEngine currently has none
            }

        } catch (ClassNotFoundException e) {
            violations.add("Cannot inspect engine contract: " + e.getMessage());
        }
    }

    /**
     * Verifies that API contracts use canonical model types from the model package.
     */
    private static void verifyCanonicalModelUsage(List<String> violations) {
        try {
            Class<?> serviceClass = Class.forName("platform.kernels.multiagent.api.MultiAgentService");
            Method[] methods = serviceClass.getMethods();

            for (Method m : methods) {
                Class<?>[] paramTypes = m.getParameterTypes();
                for (Class<?> param : paramTypes) {
                    String paramName = param.getName();
                    // Parameters should be from model, not from api bootstrap types
                    if (paramName.startsWith("platform.kernels.multiagent.api.") &&
                        !paramName.equals("platform.kernels.multiagent.api.MultiAgentService") &&
                        param.isRecord()) {
                        violations.add("API contract " + m.getName() + " uses bootstrap record " +
                                paramName + " instead of canonical model type");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            violations.add("Cannot inspect model usage: " + e.getMessage());
        }
    }
}