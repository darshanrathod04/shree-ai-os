package platform.runtime.api;

import platform.runtime.config.RuntimeConfiguration;
import platform.runtime.contracts.RuntimeContract;
import platform.runtime.internal.DefaultRuntime;

/**
 * <b>RuntimeBuilder</b>
 *
 * <p>Fluent builder for constructing {@link Runtime} instances.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a builder API for assembling Runtime instances with explicit configuration.</li>
 *   <li>Validates that all required components are provided before building.</li>
 *   <li>Ensures that Runtime instances are created in a valid initial state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * Runtime runtime = RuntimeBuilder.newInstance()
 *     .configuration(config)
 *     .contract(contract)
 *     .build();
 * }</pre>
 *
 * @see Runtime
 * @see RuntimeConfiguration
 * @see RuntimeContract
 */
public final class RuntimeBuilder {

    private RuntimeConfiguration configuration;
    private RuntimeContract contract;

    private RuntimeBuilder() {
    }

    /**
     * Creates a new RuntimeBuilder instance.
     *
     * @return a new RuntimeBuilder
     */
    public static RuntimeBuilder newInstance() {
        return new RuntimeBuilder();
    }

    /**
     * Sets the RuntimeConfiguration for the Runtime under construction.
     *
     * @param configuration the runtime configuration
     * @return this builder instance
     */
    public RuntimeBuilder configuration(RuntimeConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the RuntimeContract for the Runtime under construction.
     *
     * @param contract the runtime contract
     * @return this builder instance
     */
    public RuntimeBuilder contract(RuntimeContract contract) {
        this.contract = contract;
        return this;
    }

    /**
     * Builds a new Runtime instance based on the configured parameters.
     *
     * @return a new Runtime instance
     * @throws IllegalStateException if configuration or contract are missing
     */
    public Runtime build() {
        if (configuration == null) {
            throw new IllegalStateException("RuntimeConfiguration is required");
        }
        if (contract == null) {
            throw new IllegalStateException("RuntimeContract is required");
        }
        // Delegate to the internal implementation
        return new DefaultRuntime(configuration, contract);
    }
}