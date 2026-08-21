package com.shreeai.os.platform.sdk.exceptions;

import com.shreeai.os.platform.sdk.SDKErrorCode;
import com.shreeai.os.platform.sdk.SDKException;

/**
 * SDK validation failure.
 */
public final class ValidationException extends SDKException {

    public ValidationException(String message) {
        super(
                SDKErrorCode.VALIDATION_ERROR,
                "Validation",
                null,
                message
        );
    }

    public ValidationException(String message, Throwable cause) {
        super(
                SDKErrorCode.VALIDATION_ERROR,
                "Validation",
                null,
                message,
                cause
        );
    }
}