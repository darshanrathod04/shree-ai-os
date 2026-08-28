package com.shreeai.os.platform.bootstrap;

/**
 * Exception thrown during platform bootstrap operations.
 * 
 * This exception is used to indicate failures during the bootstrap process,
 * including initialization failures, verification failures, and shutdown failures.
 */
public class BootstrapException extends RuntimeException {
    
    /**
     * Constructs a new BootstrapException with the specified detail message
     * 
     * @param message the detail message
     */
    public BootstrapException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new BootstrapException with the specified detail message and cause
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public BootstrapException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new BootstrapException with the specified cause
     * 
     * @param cause the cause of the exception
     */
    public BootstrapException(Throwable cause) {
        super(cause);
    }
}