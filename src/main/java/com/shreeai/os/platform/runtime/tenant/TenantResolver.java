package com.shreeai.os.platform.runtime.tenant;

import java.util.Optional;

/**
 * <b>TenantResolver</b>
 *
 * <p>SPI for resolving the current tenant context. Implementations extract
 * tenant identity from HTTP headers, security context, configuration, or
 * any other source.</p>
 *
 * <p><b>Ownership:</b> Runtime — Multi-Tenancy</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface TenantResolver {

    /**
     * Resolves the current tenant identifier.
     *
     * @return the tenant identifier, or empty if no tenant is in context
     */
    Optional<String> resolveTenantId();

    /**
     * Resolves the current organization identifier.
     *
     * @return the organization identifier, or empty if not available
     */
    Optional<String> resolveOrganizationId();

    /**
     * Returns the full tenant context.
     *
     * @return the tenant context (never null, may contain defaults)
     */
    TenantContext resolveContext();
}