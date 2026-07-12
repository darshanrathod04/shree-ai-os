package platform.runtime.execution;

import platform.runtime.config.RuntimeConfiguration;
import platform.runtime.contracts.RuntimeContract;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>ExecutionContext</b>
 *
 * <p>Provides contextual information and services for an active execution session.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Bridges a session to the Runtime's configuration, contract, and shared state.</li>
 *   <li>Provides thread-safe access to session-scoped attributes.</li>
 *   <li>Is isolated per session to prevent cross-session interference.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Invariant:</b> Every ExecutionContext MUST be associated with exactly one ExecutionSession.</p>
 */
public final class ExecutionContext {

    private final ExecutionSession session;
    private final RuntimeConfiguration configuration;
    private final RuntimeContract contract;
    private final Map<String, Object> attributes;

    private ExecutionContext(Builder builder) {
        this.session = builder.session;
        this.configuration = builder.configuration;
        this.contract = builder.contract;
        this.attributes = new ConcurrentHashMap<>(builder.attributes);
    }

    /**
     * Returns the session associated with this context.
     *
     * @return the execution session
     */
    public ExecutionSession session() {
        return session;
    }

    /**
     * Returns the RuntimeConfiguration for the executing Runtime.
     *
     * @return the runtime configuration
     */
    public RuntimeConfiguration configuration() {
        return configuration;
    }

    /**
     * Returns the RuntimeContract governing this execution.
     *
     * @return the runtime contract
     */
    public RuntimeContract contract() {
        return contract;
    }

    /**
     * Retrieves a context attribute by name.
     *
     * @param name the attribute name
     * @param <T>  the expected type
     * @return the attribute value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T attribute(String name) {
        return (T) attributes.get(name);
    }

    /**
     * Sets a context attribute.
     *
     * @param name  the attribute name
     * @param value the attribute value
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Creates a new builder for ExecutionContext.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link ExecutionContext}.
     */
    public static final class Builder {

        private ExecutionSession session;
        private RuntimeConfiguration configuration;
        private RuntimeContract contract;
        private Map<String, Object> attributes = new ConcurrentHashMap<>();

        private Builder() {
        }

        /**
         * Sets the execution session.
         *
         * @param session the session
         * @return this builder
         */
        public Builder session(ExecutionSession session) {
            this.session = session;
            return this;
        }

        /**
         * Sets the runtime configuration.
         *
         * @param configuration the configuration
         * @return this builder
         */
        public Builder configuration(RuntimeConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        /**
         * Sets the runtime contract.
         *
         * @param contract the contract
         * @return this builder
         */
        public Builder contract(RuntimeContract contract) {
            this.contract = contract;
            return this;
        }

        /**
         * Adds an attribute to the context.
         *
         * @param name  the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public Builder attribute(String name, Object value) {
            this.attributes.put(name, value);
            return this;
        }

        /**
         * Builds a new ExecutionContext.
         *
         * @return a new context instance
         * @throws IllegalStateException if session is null
         */
        public ExecutionContext build() {
            if (session == null) {
                throw new IllegalStateException("ExecutionSession is required");
            }
            return new ExecutionContext(this);
        }
    }
}