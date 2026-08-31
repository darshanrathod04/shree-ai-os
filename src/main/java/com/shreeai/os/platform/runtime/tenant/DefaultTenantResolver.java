package com.shreeai.os.platform.runtime.tenant;

import java.util.Objects;
import java.util.Optional;

/**
 * <b>DefaultTenantResolver</b>
 *
 * <p>Default implementation that resolves tenant context from a thread-local
 * holder. Falls back to system defaults when no tenant is explicitly set.</p>
 *
 * <p><b>Ownership:</b> Runtime — Multi-Tenancy</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultTenantResolver implements TenantResolver {

    @Override
    public Optional<String> resolveTenantId() {
        return Optional.ofNullable(TenantContext.current().tenantId());
    }

    @Override
    public Optional<String> resolveOrganizationId() {
        return Optional.ofNullable(TenantContext.current().organizationId());
    }

    @Override
    public TenantContext resolveContext() {
        return TenantContext.current();
    }

    /**
     * Sets the current tenant context for this thread.
     *
     * @param context the tenant context (never null)
     */
    public void setContext(TenantContext context) {
        Objects.requireNonNull(context, "context must not be null");
        TenantContext.setCurrent(context);
    }

    /**
     * Clears the current tenant context for this thread.
     */
    public void clear() {
        TenantContext.clear();
    }
}