package com.shreeai.os.platform.runtime.exceptions;

/**
 * <b>RuntimeException</b>
 *
 * <p>Base exception class for all Runtime Kernel exceptions.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a common base type for all Runtime-related exceptions.</li>
 *   <li>Enables callers to catch Runtime exceptions without knowing specific subtypes.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 *
 * @see InvalidRuntimeStateException
 */
public class RuntimeException extends Exception {

    /**
     * Creates a new RuntimeException with the given message.
     *
     * @param message the detail message
     */
    public RuntimeException(String message) {
        super(message);
    }

    /**
     * Creates a new RuntimeException with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the root cause
     */
    public RuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}