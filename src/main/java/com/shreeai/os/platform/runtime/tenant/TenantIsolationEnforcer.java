package com.shreeai.os.platform.runtime.tenant;

import java.util.Objects;

/**
 * <b>TenantIsolationEnforcer</b>
 *
 * <p>Validates tenant access on every operation. Throws
 * {@link TenantIsolationException} when cross-tenant access is attempted.</p>
 *
 * <p><b>Ownership:</b> Runtime — Multi-Tenancy</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class TenantIsolationEnforcer {

    private final TenantResolver tenantResolver;

    public TenantIsolationEnforcer(TenantResolver tenantResolver) {
        this.tenantResolver = Objects.requireNonNull(tenantResolver, "tenantResolver must not be null");
    }

    /**
     * Validates that the requested tenant matches the current tenant context.
     *
     * @param requestedTenantId the tenant being accessed
     * @throws TenantIsolationException if cross-tenant access is attempted
     */
    public void validateAccess(String requestedTenantId) {
        Objects.requireNonNull(requestedTenantId, "requestedTenantId must not be null");

        String currentTenant = tenantResolver.resolveTenantId()
                .orElse("system");

        if (!currentTenant.equals(requestedTenantId)) {
            throw new TenantIsolationException(
                    "Cross-tenant access denied: current=" + currentTenant
                            + ", requested=" + requestedTenantId
            );
        }
    }

    /**
     * Returns the current tenant context for repository operations.
     *
     * @return the current tenant context
     */
    public TenantContext currentContext() {
        return tenantResolver.resolveContext();
    }
}