package com.shreeai.os.platform.bootstrap;

/**
 * Represents the lifecycle states of the platform bootstrap process.
 * 
 * The bootstrap process follows a deterministic state machine:
 * OFFLINE → INITIALIZING → STARTING_CORE → STARTING_RUNTIME → 
 * STARTING_KERNELS → VERIFYING → READY → FAILED → SHUTTING_DOWN → STOPPED
 */
public enum BootstrapState {
    
    /**
     * Initial state - platform is not initialized
     */
    OFFLINE("Platform is offline and not initialized"),
    
    /**
     * Bootstrap process has started
     */
    INITIALIZING("Platform initialization in progress"),
    
    /**
     * Core modules are being initialized
     */
    STARTING_CORE("Core modules initialization in progress"),
    
    /**
     * Runtime is being started
     */
    STARTING_RUNTIME("Runtime initialization in progress"),
    
    /**
     * Kernels are being registered and started
     */
    STARTING_KERNELS("Kernels initialization in progress"),
    
    /**
     * System is being verified
     */
    VERIFYING("System verification in progress"),
    
    /**
     * Platform is fully operational
     */
    READY("Platform is ready and operational"),
    
    /**
     * Initialization failed
     */
    FAILED("Platform initialization failed"),
    
    /**
     * Shutdown process in progress
     */
    SHUTTING_DOWN("Platform shutdown in progress"),
    
    /**
     * Platform is stopped
     */
    STOPPED("Platform is stopped");

    private final String description;

    BootstrapState(String description) {
        this.description = description;
    }

    /**
     * Get the human-readable description of this state
     * 
     * @return state description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if this state is terminal
     * 
     * @return true if state is READY, FAILED, or STOPPED
     */
    public boolean isTerminal() {
        return this == READY || this == FAILED || this == STOPPED;
    }

    /**
     * Check if this state represents an operational platform
     * 
     * @return true if state is READY
     */
    public boolean isOperational() {
        return this == READY;
    }

    /**
     * Check if this state represents a failure
     * 
     * @return true if state is FAILED
     */
    public boolean isFailure() {
        return this == FAILED;
    }

    /**
     * Get the next state in the bootstrap sequence
     * 
     * @return next state
     * @throws IllegalStateException if current state is terminal
     */
    public BootstrapState next() {
        switch (this) {
            case OFFLINE:
                return INITIALIZING;
            case INITIALIZING:
                return STARTING_CORE;
            case STARTING_CORE:
                return STARTING_RUNTIME;
            case STARTING_RUNTIME:
                return STARTING_KERNELS;
            case STARTING_KERNELS:
                return VERIFYING;
            case VERIFYING:
                return READY;
            case READY:
                return SHUTTING_DOWN;
            case SHUTTING_DOWN:
                return STOPPED;
            case FAILED:
                return SHUTTING_DOWN;
            case STOPPED:
                throw new IllegalStateException("Cannot transition from STOPPED state");
            default:
                throw new IllegalStateException("Unknown state: " + this);
        }
    }

    /**
     * Get the previous state in the bootstrap sequence
     * 
     * @return previous state
     * @throws IllegalStateException if current state is OFFLINE
     */
    public BootstrapState previous() {
        switch (this) {
            case OFFLINE:
                throw new IllegalStateException("Cannot transition from OFFLINE state");
            case INITIALIZING:
                return OFFLINE;
            case STARTING_CORE:
                return INITIALIZING;
            case STARTING_RUNTIME:
                return STARTING_CORE;
            case STARTING_KERNELS:
                return STARTING_RUNTIME;
            case VERIFYING:
                return STARTING_KERNELS;
            case READY:
                return VERIFYING;
            case FAILED:
                return VERIFYING;
            case SHUTTING_DOWN:
                return READY;
            case STOPPED:
                return SHUTTING_DOWN;
            default:
                throw new IllegalStateException("Unknown state: " + this);
        }
    }
}