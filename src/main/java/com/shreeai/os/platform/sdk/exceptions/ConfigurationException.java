package com.shreeai.os.platform.sdk.exceptions;

/**
 * <b>ConfigurationException</b>
 *
 * <p>Thrown when SDK configuration is invalid.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public class ConfigurationException extends SDKException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}