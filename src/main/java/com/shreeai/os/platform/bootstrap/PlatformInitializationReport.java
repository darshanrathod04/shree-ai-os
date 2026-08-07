package com.shreeai.os.platform.bootstrap;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Report generated after platform initialization attempt.
 * Contains detailed information about the bootstrap process.
 */
public class PlatformInitializationReport {
    
    private final BootstrapState finalState;
    private final Duration totalDuration;
    private final Instant startTime;
    private final Instant endTime;
    private final List<ModuleInitializationResult> initializedModules;
    private final List<ModuleInitializationResult> failedModules;
    private final List<String> warnings;
    private final String errorMessage;
    
    /**
     * Private constructor - use Builder pattern
     */
    private PlatformInitializationReport(Builder builder) {
        this.finalState = builder.finalState;
        this.totalDuration = builder.totalDuration;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.initializedModules = new ArrayList<>(builder.initializedModules);
        this.failedModules = new ArrayList<>(builder.failedModules);
        this.warnings = new ArrayList<>(builder.warnings);
        this.errorMessage = builder.errorMessage;
    }
    
    /**
     * Get the final state of the bootstrap process
     * 
     * @return final bootstrap state
     */
    public BootstrapState getFinalState() {
        return finalState;
    }
    
    /**
     * Get the total duration of the bootstrap process
     * 
     * @return total duration
     */
    public Duration getTotalDuration() {
        return totalDuration;
    }
    
    /**
     * Get the start time of the bootstrap process
     * 
     * @return start time
     */
    public Instant getStartTime() {
        return startTime;
    }
    
    /**
     * Get the end time of the bootstrap process
     * 
     * @return end time
     */
    public Instant getEndTime() {
        return endTime;
    }
    
    /**
     * Get the list of successfully initialized modules
     * 
     * @return list of initialized modules
     */
    public List<ModuleInitializationResult> getInitializedModules() {
        return new ArrayList<>(initializedModules);
    }
    
    /**
     * Get the list of failed modules
     * 
     * @return list of failed modules
     */
    public List<ModuleInitializationResult> getFailedModules() {
        return new ArrayList<>(failedModules);
    }
    
    /**
     * Get the list of warnings
     * 
     * @return list of warnings
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    /**
     * Get the error message if initialization failed
     * 
     * @return error message or null if successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Check if the bootstrap was successful
     * 
     * @return true if final state is READY
     */
    public boolean isSuccess() {
        return finalState == BootstrapState.READY;
    }
    
    /**
     * Check if the bootstrap failed
     * 
     * @return true if final state is FAILED
     */
    public boolean isFailure() {
        return finalState == BootstrapState.FAILED;
    }
    
    /**
     * Get the number of successfully initialized modules
     * 
     * @return count of initialized modules
     */
    public int getInitializedModuleCount() {
        return initializedModules.size();
    }
    
    /**
     * Get the number of failed modules
     * 
     * @return count of failed modules
     */
    public int getFailedModuleCount() {
        return failedModules.size();
    }
    
    /**
     * Get the number of warnings
     * 
     * @return count of warnings
     */
    public int getWarningCount() {
        return warnings.size();
    }
    
    /**
     * Create a builder for the report
     * 
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Result of initializing a single module
     */
    public static class ModuleInitializationResult {
        private final String moduleName;
        private final boolean success;
        private final Duration duration;
        private final String errorMessage;
        
        public ModuleInitializationResult(String moduleName, boolean success, Duration duration, String errorMessage) {
            this.moduleName = moduleName;
            this.success = success;
            this.duration = duration;
            this.errorMessage = errorMessage;
        }
        
        public String getModuleName() {
            return moduleName;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public Duration getDuration() {
            return duration;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    /**
     * Builder for PlatformInitializationReport
     */
    public static class Builder {
        private BootstrapState finalState;
        private Duration totalDuration;
        private Instant startTime;
        private Instant endTime;
        private List<ModuleInitializationResult> initializedModules = new ArrayList<>();
        private List<ModuleInitializationResult> failedModules = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private String errorMessage;
        
        public Builder withFinalState(BootstrapState finalState) {
            this.finalState = finalState;
            return this;
        }
        
        public Builder withTotalDuration(Duration totalDuration) {
            this.totalDuration = totalDuration;
            return this;
        }
        
        public Builder withStartTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder withEndTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }
        
        public Builder withInitializedModules(List<ModuleInitializationResult> modules) {
            this.initializedModules = new ArrayList<>(modules);
            return this;
        }
        
        public Builder addInitializedModule(ModuleInitializationResult module) {
            this.initializedModules.add(module);
            return this;
        }
        
        public Builder withFailedModules(List<ModuleInitializationResult> modules) {
            this.failedModules = new ArrayList<>(modules);
            return this;
        }
        
        public Builder addFailedModule(ModuleInitializationResult module) {
            this.failedModules.add(module);
            return this;
        }
        
        public Builder withWarnings(List<String> warnings) {
            this.warnings = new ArrayList<>(warnings);
            return this;
        }
        
        public Builder addWarning(String warning) {
            this.warnings.add(warning);
            return this;
        }
        
        public Builder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public PlatformInitializationReport build() {
            if (finalState == null) {
                throw new IllegalStateException("Final state is required");
            }
            if (startTime == null || endTime == null) {
                throw new IllegalStateException("Start and end times are required");
            }
            if (totalDuration == null) {
                this.totalDuration = Duration.between(startTime, endTime);
            }
            
            return new PlatformInitializationReport(this);
        }
    }
}