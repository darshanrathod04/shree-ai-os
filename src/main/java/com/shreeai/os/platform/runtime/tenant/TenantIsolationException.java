package com.shreeai.os.platform.runtime.tenant;

/**
 * <b>TenantIsolationException</b>
 *
 * <p>Thrown when cross-tenant access is attempted.</p>
 *
 * <p><b>Ownership:</b> Runtime — Multi-Tenancy</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class TenantIsolationException extends RuntimeException {

    public TenantIsolationException(String message) {
        super(message);
    }

    public TenantIsolationException(String message, Throwable cause) {
        super(message, cause);
    }
}