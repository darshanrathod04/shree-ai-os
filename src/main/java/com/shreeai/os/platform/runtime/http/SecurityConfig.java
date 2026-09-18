package com.shreeai.os.platform.runtime.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * <b>Platform Security Configuration</b>
 *
 * <p>Protects management and configuration endpoints with admin token verification
 * while keeping chat, public API, and diagnostics endpoints open.</p>
 *
 * <p><b>Guarded Endpoints:</b></p>
 * <ul>
 *   <li>{@code /api/settings/**}</li>
 *   <li>{@code /api/v1/byok/**}</li>
 * </ul>
 *
 * <p><b>Public Endpoints:</b></p>
 * <ul>
 *   <li>{@code /api/v1/chat/**}</li>
 *   <li>{@code /api/sdk/**}</li>
 *   <li>{@code /actuator/**}</li>
 * </ul>
 *
 * @since Sprint 19 / Batch 3 Hardening
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${shree.security.admin-token:dev-admin-token}")
    private String adminToken;

    @Value("${shree.security.require-admin-token:false}")
    private boolean requireAdminToken;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AdminTokenFilter adminTokenFilter = new AdminTokenFilter(adminToken, requireAdminToken);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(adminTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/settings/**", "/api/v1/byok/**").authenticated()
                        .anyRequest().permitAll());
        return http.build();
    }

    /**
     * Admin token filter guarding management endpoints.
     */
    public static class AdminTokenFilter extends OncePerRequestFilter {

        private final String expectedToken;
        private final boolean requireAdminToken;

        public AdminTokenFilter(String expectedToken, boolean requireAdminToken) {
            this.expectedToken = expectedToken != null ? expectedToken.trim() : "";
            this.requireAdminToken = requireAdminToken;
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            String path = request.getServletPath();
            if (path == null || path.isBlank()) {
                path = request.getRequestURI();
            }

            if (isProtectedPath(path)) {
                String token = resolveToken(request);
                if (token != null && !token.isBlank()) {
                    if (expectedToken.isEmpty() || expectedToken.equals(token)) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                "admin",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized: Invalid admin token\"}");
                        return;
                    }
                } else if (requireAdminToken) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized: Admin token required\"}");
                    return;
                } else {
                    // In development/test mode without requireAdminToken enforced, allow admin access
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            "admin",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            filterChain.doFilter(request, response);
        }

        private boolean isProtectedPath(String path) {
            if (path == null) {
                return false;
            }
            return path.startsWith("/api/settings") || path.startsWith("/api/v1/byok");
        }

        private String resolveToken(HttpServletRequest request) {
            String header = request.getHeader("X-Shree-Admin-Token");
            if (header != null && !header.isBlank()) {
                return header.trim();
            }
            header = request.getHeader("X-Admin-Token");
            if (header != null && !header.isBlank()) {
                return header.trim();
            }
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                return auth.substring(7).trim();
            }
            return null;
        }
    }
}
