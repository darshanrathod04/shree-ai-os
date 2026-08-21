package com.shreeai.os.platform.sdk;

import java.util.Objects;

/**
 * Canonical developer-facing SDK exception.
 *
 * <p>This is the root runtime exception exposed by the Shree AI OS SDK.
 * It carries structured error information including the pipeline stage,
 * error code, and request identifier.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0</p>
 */
public class SDKException extends RuntimeException {

    private final SDKErrorCode code;
    private final String stage;
    private final String requestId;

    /**
     * Creates a structured SDK exception.
     *
     * @param code      canonical SDK error code
     * @param stage     runtime stage where the failure occurred
     * @param requestId request identifier
     * @param message   developer-facing message
     */
    public SDKException(
            SDKErrorCode code,
            String stage,
            String requestId,
            String message
    ) {
        super(message);
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.stage = stage;
        this.requestId = requestId;
    }

    /**
     * Creates a structured SDK exception with a root cause.
     */
    public SDKException(
            SDKErrorCode code,
            String stage,
            String requestId,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.stage = stage;
        this.requestId = requestId;
    }

    /**
     * Returns the canonical SDK error code.
     */
    public SDKErrorCode code() {
        return code;
    }

    /**
     * Returns the runtime stage that failed.
     */
    public String stage() {
        return stage;
    }

    /**
     * Returns the request identifier associated with this failure.
     */
    public String requestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return "SDKException{" +
                "code=" + code +
                ", stage='" + stage + '\'' +
                ", requestId='" + requestId + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}