package com.shreeai.os.platform.services;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <b>ShreeApiSecurityFilter</b>
 *
 * <p>Hardened HTTP security filter for {@code /api/**} endpoints providing:</p>
 * <ul>
 *   <li>Strict security headers ({@code X-Content-Type-Options}, {@code X-Frame-Options}, {@code Referrer-Policy})</li>
 *   <li>Allowlist-based CORS (defaults to {@code http://localhost:3000,http://127.0.0.1:3000})</li>
 *   <li>Constant-time API key authentication when {@code shree.security.api-key} / {@code SHREE_API_KEY} is set</li>
 *   <li>Per-client sliding-window rate limiting ({@code shree.security.rate-limit.rpm}, default 120)</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShreeApiSecurityFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;
    private static final int DEFAULT_RPM = 120;

    private final Map<String, Deque<Long>> requestWindows = new ConcurrentHashMap<>();
    private final String configuredApiKey;
    private final Set<String> allowedOrigins;
    private final int maxRequestsPerMinute;

    public ShreeApiSecurityFilter() {
        this(
                firstNonBlank(System.getProperty("shree.security.api-key"), System.getenv("SHREE_API_KEY")),
                parseOrigins(firstNonBlank(
                        System.getProperty("shree.security.allowed-origins"),
                        System.getenv("SHREE_ALLOWED_ORIGINS"),
                        "http://localhost:3000,http://127.0.0.1:3000")),
                parseRpm(firstNonBlank(
                        System.getProperty("shree.security.rate-limit.rpm"),
                        System.getenv("SHREE_RATE_LIMIT_RPM"),
                        String.valueOf(DEFAULT_RPM)))
        );
    }

    public ShreeApiSecurityFilter(String configuredApiKey, Set<String> allowedOrigins, int maxRequestsPerMinute) {
        this.configuredApiKey = (configuredApiKey == null || configuredApiKey.isBlank()) ? null : configuredApiKey.trim();
        this.allowedOrigins = allowedOrigins == null ? Set.of() : Set.copyOf(allowedOrigins);
        this.maxRequestsPerMinute = Math.max(1, maxRequestsPerMinute);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        applySecurityHeaders(response);
        applyCorsHeaders(request, response);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/")) {
            if (configuredApiKey != null && !isAuthorized(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized: valid API key required\"}");
                return;
            }

            String clientKey = resolveClientKey(request);
            if (!tryAcquireRateSlot(clientKey, System.currentTimeMillis())) {
                response.setStatus(429);
                response.setHeader("Retry-After", "60");
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too Many Requests: rate limit exceeded\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void applySecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Cache-Control", "no-store");
    }

    private void applyCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && (allowedOrigins.contains("*") || allowedOrigins.contains(origin))) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization,X-API-Key");
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        String headerKey = request.getHeader("X-API-Key");
        if (headerKey == null || headerKey.isBlank()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
                headerKey = auth.substring(7).trim();
            }
        }
        if (headerKey == null || headerKey.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                configuredApiKey.getBytes(StandardCharsets.UTF_8),
                headerKey.trim().getBytes(StandardCharsets.UTF_8));
    }

    boolean tryAcquireRateSlot(String clientKey, long nowMs) {
        Deque<Long> window = requestWindows.computeIfAbsent(clientKey, k -> new ArrayDeque<>());
        synchronized (window) {
            long cutoff = nowMs - WINDOW_MS;
            while (!window.isEmpty() && window.peekFirst() < cutoff) {
                window.pollFirst();
            }
            if (window.size() >= maxRequestsPerMinute) {
                return false;
            }
            window.addLast(nowMs);
            return true;
        }
    }

    private static String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String addr = request.getRemoteAddr();
        return addr == null || addr.isBlank() ? "unknown" : addr;
    }

    private static Set<String> parseOrigins(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static int parseRpm(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_RPM;
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a.trim();
        if (b != null && !b.isBlank()) return b.trim();
        return null;
    }

    private static String firstNonBlank(String a, String b, String fallback) {
        String v = firstNonBlank(a, b);
        return v != null ? v : fallback;
    }
}
