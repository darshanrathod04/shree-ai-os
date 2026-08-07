package com.shreeai.os.platform.bootstrap;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the platform bootstrap process.
 * 
 * This class is framework-agnostic and does not depend on Spring Boot.
 * It contains all configuration needed for platform initialization.
 */
public class BootstrapConfiguration {
    
    private Duration startupTimeout;
    private Duration shutdownTimeout;
    private Duration retryDelay;
    private int maxRetries;
    private boolean strictMode;
    private boolean rollbackOnFailure;
    private List<String> moduleOrder;
    private boolean enableVerification;
    private boolean enableHealthChecks;
    
    /**
     * Private constructor to enforce builder pattern
     */
    private BootstrapConfiguration() {
        this.moduleOrder = new ArrayList<>();
    }
    
    /**
     * Get the startup timeout duration
     * 
     * @return startup timeout
     */
    public Duration getStartupTimeout() {
        return startupTimeout;
    }
    
    /**
     * Get the shutdown timeout duration
     * 
     * @return shutdown timeout
     */
    public Duration getShutdownTimeout() {
        return shutdownTimeout;
    }
    
    /**
     * Get the retry delay duration
     * 
     * @return retry delay
     */
    public Duration getRetryDelay() {
        return retryDelay;
    }
    
    /**
     * Get the maximum number of retries
     * 
     * @return max retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }
    
    /**
     * Check if strict mode is enabled
     * In strict mode, any failure stops the bootstrap process
     * 
     * @return true if strict mode is enabled
     */
    public boolean isStrictMode() {
        return strictMode;
    }
    
    /**
     * Check if rollback on failure is enabled
     * 
     * @return true if rollback is enabled
     */
    public boolean isRollbackOnFailure() {
        return rollbackOnFailure;
    }
    
    /**
     * Get the module initialization order
     * 
     * @return list of module names in order
     */
    public List<String> getModuleOrder() {
        return new ArrayList<>(moduleOrder);
    }
    
    /**
     * Check if verification is enabled
     * 
     * @return true if verification is enabled
     */
    public boolean isEnableVerification() {
        return enableVerification;
    }
    
    /**
     * Check if health checks are enabled
     * 
     * @return true if health checks are enabled
     */
    public boolean isEnableHealthChecks() {
        return enableHealthChecks;
    }
    
    /**
     * Builder for BootstrapConfiguration
     */
    public static class Builder {
        private final BootstrapConfiguration config;
        
        public Builder() {
            config = new BootstrapConfiguration();
            // Set defaults
            config.startupTimeout = Duration.ofSeconds(60);
            config.shutdownTimeout = Duration.ofSeconds(30);
            config.retryDelay = Duration.ofSeconds(1);
            config.maxRetries = 3;
            config.strictMode = true;
            config.rollbackOnFailure = true;
            config.enableVerification = true;
            config.enableHealthChecks = true;
            
            // Default module order
            config.moduleOrder.add("Configuration");
            config.moduleOrder.add("Registry");
            config.moduleOrder.add("Discovery");
            config.moduleOrder.add("EventBus");
            config.moduleOrder.add("Health");
            config.moduleOrder.add("Plugin");
            config.moduleOrder.add("Lifecycle");
            config.moduleOrder.add("Runtime");
            config.moduleOrder.add("Kernels");
            config.moduleOrder.add("Verification");
        }
        
        /**
         * Set the startup timeout
         * 
         * @param timeout timeout duration
         * @return builder instance
         */
        public Builder withStartupTimeout(Duration timeout) {
            config.startupTimeout = timeout;
            return this;
        }
        
        /**
         * Set the shutdown timeout
         * 
         * @param timeout shutdown timeout duration
         * @return builder instance
         */
        public Builder withShutdownTimeout(Duration timeout) {
            config.shutdownTimeout = timeout;
            return this;
        }
        
        /**
         * Set the retry delay
         * 
         * @param delay retry delay duration
         * @return builder instance
         */
        public Builder withRetryDelay(Duration delay) {
            config.retryDelay = delay;
            return this;
        }
        
        /**
         * Set the maximum number of retries
         * 
         * @param maxRetries maximum retries
         * @return builder instance
         */
        public Builder withMaxRetries(int maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("Max retries cannot be negative");
            }
            config.maxRetries = maxRetries;
            return this;
        }
        
        /**
         * Enable or disable strict mode
         * 
         * @param strictMode true to enable strict mode
         * @return builder instance
         */
        public Builder withStrictMode(boolean strictMode) {
            config.strictMode = strictMode;
            return this;
        }
        
        /**
         * Enable or disable rollback on failure
         * 
         * @param rollbackOnFailure true to enable rollback
         * @return builder instance
         */
        public Builder withRollbackOnFailure(boolean rollbackOnFailure) {
            config.rollbackOnFailure = rollbackOnFailure;
            return this;
        }
        
        /**
         * Set the module initialization order
         * 
         * @param moduleOrder list of module names in order
         * @return builder instance
         */
        public Builder withModuleOrder(List<String> moduleOrder) {
            if (moduleOrder == null || moduleOrder.isEmpty()) {
                throw new IllegalArgumentException("Module order cannot be null or empty");
            }
            config.moduleOrder = new ArrayList<>(moduleOrder);
            return this;
        }
        
        /**
         * Add a module to the initialization order
         * 
         * @param moduleName module name
         * @return builder instance
         */
        public Builder withModule(String moduleName) {
            if (moduleName == null || moduleName.trim().isEmpty()) {
                throw new IllegalArgumentException("Module name cannot be null or empty");
            }
            config.moduleOrder.add(moduleName);
            return this;
        }
        
        /**
         * Enable or disable verification
         * 
         * @param enableVerification true to enable verification
         * @return builder instance
         */
        public Builder withVerification(boolean enableVerification) {
            config.enableVerification = enableVerification;
            return this;
        }
        
        /**
         * Enable or disable health checks
         * 
         * @param enableHealthChecks true to enable health checks
         * @return builder instance
         */
        public Builder withHealthChecks(boolean enableHealthChecks) {
            config.enableHealthChecks = enableHealthChecks;
            return this;
        }
        
        /**
         * Build the configuration
         * 
         * @return BootstrapConfiguration instance
         */
        public BootstrapConfiguration build() {
            // Validate configuration
            if (config.startupTimeout.isNegative() || config.startupTimeout.isZero()) {
                throw new IllegalArgumentException("Startup timeout must be positive");
            }
            if (config.shutdownTimeout.isNegative() || config.shutdownTimeout.isZero()) {
                throw new IllegalArgumentException("Shutdown timeout must be positive");
            }
            if (config.retryDelay.isNegative() || config.retryDelay.isZero()) {
                throw new IllegalArgumentException("Retry delay must be positive");
            }
            
            return config;
        }
    }
    
    /**
     * Create a new builder
     * 
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Create a default configuration
     * 
     * @return default configuration
     */
    public static BootstrapConfiguration defaults() {
        return new Builder().build();
    }
}