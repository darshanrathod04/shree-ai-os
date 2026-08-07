package com.shreeai.os.platform.sdk.exceptions;

/**
 * <b>SDKException</b>
 *
 * <p>Base exception for all Shree AI OS SDK errors.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public class SDKException extends RuntimeException {

    public SDKException(String message) {
        super(message);
    }

    public SDKException(String message, Throwable cause) {
        super(message, cause);
    }
}