package com.shreeai.os.developer.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <b>SecurityConfig</b>
 *
 * <p>Disables Spring Security for local development.
 * All endpoints are open — no authentication required.
 *
 * <p>For production, replace this with a proper security configuration
 * that requires authentication and authorizes access.</p>
 *
 * <p>Registered before the auto-configuration to take precedence.
 * The {@code UserDetailsServiceAutoConfiguration} warning is benign in dev.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());
        return http.build();
    }
}
