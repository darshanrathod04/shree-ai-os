package com.shreeai.os.platform.runtime.tenant;

import java.util.Objects;
import java.util.Optional;

/**
 * <b>TenantContext</b>
 *
 * <p>Immutable holder for the current tenant and organization identifiers.
 * This is the canonical source of tenant identity at runtime.</p>
 *
 * <p>Also provides a static thread-local accessor so that services outside
 * the dependency graph (e.g. recovery, bootstrap) can set and clear the
 * current tenant for the executing thread without holding a resolver
 * reference. {@link DefaultTenantResolver} delegates to this same store so
 * there is a single source of truth.</p>
 *
 * <p><b>Ownership:</b> Runtime — Multi-Tenancy</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param tenantId       the tenant identifier (never null)
 * @param organizationId the organization identifier (never null)
 */
public record TenantContext(String tenantId, String organizationId) {

    private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();

    public TenantContext {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(organizationId, "organizationId must not be null");
    }

    /**
     * Creates a system tenant context.
     *
     * @return the default system tenant context
     */
    public static TenantContext system() {
        return new TenantContext("system", "system");
    }

    /**
     * Creates a tenant context with the given tenant and organization.
     *
     * @param tenantId       the tenant identifier
     * @param organizationId the organization identifier
     * @return a new tenant context
     */
    public static TenantContext of(String tenantId, String organizationId) {
        return new TenantContext(tenantId, organizationId);
    }

    /**
     * Sets the current tenant for the executing thread.
     *
     * @param tenantId       the tenant identifier (never null)
     * @param organizationId the organization identifier (never null)
     */
    public static void setCurrentTenant(String tenantId, String organizationId) {
        CURRENT.set(TenantContext.of(tenantId, organizationId));
    }

    /**
     * Sets the current thread tenant context.
     *
     * @param context the tenant context (never null)
     */
    public static void setCurrent(TenantContext context) {
        CURRENT.set(Objects.requireNonNull(context, "context must not be null"));
    }

    /**
     * Returns the current thread tenant context, falling back to the system
     * tenant when none has been set.
     *
     * @return the current tenant context (never null)
     */
    public static TenantContext current() {
        TenantContext ctx = CURRENT.get();
        return ctx != null ? ctx : system();
    }

    /**
     * Returns the current thread tenant context when one has been set.
     *
     * @return the current tenant context, or empty when unset
     */
    public static Optional<TenantContext> currentOptional() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * Clears the current thread tenant context.
     */
    public static void clear() {
        CURRENT.remove();
    }
}