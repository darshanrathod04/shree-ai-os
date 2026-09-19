package com.shreeai.os.platform.runtime.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SecurityConfig.AdminTokenFilter} verifying that management
 * endpoints are secured while public endpoints remain accessible.
 */
public class SecurityConfigTest {

    private SecurityConfig.AdminTokenFilter strictFilter;
    private SecurityConfig.AdminTokenFilter devFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        strictFilter = new SecurityConfig.AdminTokenFilter("test-secret-token", true);
        devFilter = new SecurityConfig.AdminTokenFilter("test-secret-token", false);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void protectedPath_withValidHeader_authenticatesAndPasses() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/settings/providers");
        request.setServletPath("/api/settings/providers");
        request.addHeader("X-Shree-Admin-Token", "test-secret-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        FilterChain chain = (req, res) -> chainInvoked.set(true);

        strictFilter.doFilter(request, response, chain);

        assertTrue(chainInvoked.get(), "Chain should be invoked when token is valid");
        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void protectedPath_withBearerAuth_authenticatesAndPasses() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/byok/providers");
        request.setServletPath("/api/v1/byok/providers");
        request.addHeader("Authorization", "Bearer test-secret-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        FilterChain chain = (req, res) -> chainInvoked.set(true);

        strictFilter.doFilter(request, response, chain);

        assertTrue(chainInvoked.get());
        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void protectedPath_withInvalidToken_returns401Unauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/settings/providers");
        request.setServletPath("/api/settings/providers");
        request.addHeader("X-Shree-Admin-Token", "wrong-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        FilterChain chain = (req, res) -> chainInvoked.set(true);

        strictFilter.doFilter(request, response, chain);

        assertFalse(chainInvoked.get(), "Chain must NOT be invoked when token is invalid");
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid admin token"));
    }

    @Test
    void protectedPath_noToken_whenRequireAdminTokenTrue_returns401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/settings/providers");
        request.setServletPath("/api/settings/providers");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        FilterChain chain = (req, res) -> chainInvoked.set(true);

        strictFilter.doFilter(request, response, chain);

        assertFalse(chainInvoked.get(), "Chain must NOT be invoked when required token is absent");
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Admin token required"));
    }

    @Test
    void protectedPath_noToken_whenRequireAdminTokenFalse_allowsDevAccess() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/settings/providers");
        request.setServletPath("/api/settings/providers");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        FilterChain chain = (req, res) -> chainInvoked.set(true);

        devFilter.doFilter(request, response, chain);

        assertTrue(chainInvoked.get(), "Chain should be invoked in dev mode");
        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void publicPath_bypassesFilterChecks() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/chat");
        request.setServletPath("/api/v1/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        FilterChain chain = (req, res) -> chainInvoked.set(true);

        strictFilter.doFilter(request, response, chain);

        assertTrue(chainInvoked.get(), "Chain should be invoked for public path");
        assertEquals(200, response.getStatus());
    }
}
