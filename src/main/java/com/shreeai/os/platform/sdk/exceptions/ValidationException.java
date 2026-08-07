package com.shreeai.os.platform.sdk.exceptions;

/**
 * <b>ValidationException</b>
 *
 * <p>Thrown when SDK request validation fails.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public class ValidationException extends SDKException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}