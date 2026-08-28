package com.shreeai.os.platform.sdk.exceptions;

import com.shreeai.os.platform.sdk.SDKErrorCode;
import com.shreeai.os.platform.sdk.SDKException;

/**
 * SDK configuration failure.
 */
public final class ConfigurationException extends SDKException {

    public ConfigurationException(String message) {
        super(
                SDKErrorCode.VALIDATION_ERROR,
                "Configuration",
                null,
                message
        );
    }

    public ConfigurationException(String message, Throwable cause) {
        super(
                SDKErrorCode.VALIDATION_ERROR,
                "Configuration",
                null,
                message,
                cause
        );
    }
}