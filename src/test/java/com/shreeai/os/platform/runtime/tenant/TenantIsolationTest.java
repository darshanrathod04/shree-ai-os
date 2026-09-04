package com.shreeai.os.platform.runtime.tenant;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for tenant isolation across the resolver and enforcer.
 */
class TenantIsolationTest {

    private final DefaultTenantResolver resolver = new DefaultTenantResolver();
    private final TenantIsolationEnforcer enforcer = new TenantIsolationEnforcer(resolver);

    @Test
    void sameTenantAccessIsAllowed() {
        resolver.setContext(TenantContext.of("tenant-1", "org-1"));
        try {
            assertDoesNotThrow(() -> enforcer.validateAccess("tenant-1"));
        } finally {
            resolver.clear();
        }
    }

    @Test
    void crossTenantAccessIsDenied() {
        resolver.setContext(TenantContext.of("tenant-1", "org-1"));
        try {
            assertThrows(TenantIsolationException.class,
                    () -> enforcer.validateAccess("tenant-2"));
        } finally {
            resolver.clear();
        }
    }

    @Test
    void organizationIdIsResolvedFromContext() {
        resolver.setContext(TenantContext.of("tenant-1", "org-42"));
        try {
            assertEquals("tenant-1", resolver.resolveTenantId().orElseThrow());
            assertEquals("org-42", resolver.resolveOrganizationId().orElseThrow());
        } finally {
            resolver.clear();
        }
    }

    @Test
    void afterClearFallsBackToSystemTenant() {
        resolver.setContext(TenantContext.of("tenant-1", "org-1"));
        resolver.clear();

        assertEquals("system", resolver.resolveContext().tenantId());
        // System tenant can only access system
        assertDoesNotThrow(() -> enforcer.validateAccess("system"));
        assertThrows(TenantIsolationException.class,
                () -> enforcer.validateAccess("tenant-1"));
    }

    @Test
    void unsetThreadDefaultsToSystem() {
        resolver.clear();
        assertEquals("system", enforcer.currentContext().tenantId());
    }

    @Test
    void tenantContextIsThreadLocal() throws InterruptedException {
        resolver.setContext(TenantContext.of("main-tenant", "main-org"));
        try {
            AtomicReference<String> otherThreadTenant = new AtomicReference<>("unset");
            Thread worker = new Thread(() -> otherThreadTenant.set(TenantContext.current().tenantId()));
            worker.start();
            worker.join();

            assertEquals("main-tenant", TenantContext.current().tenantId());
            assertEquals("system", otherThreadTenant.get());
        } finally {
            resolver.clear();
        }
    }

    @Test
    void staticSetCurrentTenantSynchronizesWithResolver() {
        TenantContext.setCurrentTenant("api-tenant", "api-org");
        try {
            assertEquals("api-tenant", resolver.resolveTenantId().orElseThrow());
            assertEquals("api-org", resolver.resolveOrganizationId().orElseThrow());
        } finally {
            TenantContext.clear();
        }
    }
}