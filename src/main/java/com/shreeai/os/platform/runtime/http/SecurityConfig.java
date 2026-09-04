package com.shreeai.os.platform.runtime.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <b>Trust Validation SecurityConfig</b>
 *
 * <p>Permits all HTTP requests without authentication for local development
 * and trust validation testing. This is the equivalent of the
 * {@code shree-developer-intelligence} module's SecurityConfig, applied to
 * the main {@code ShreeAiOsApplication}.
 *
 * <p>The {@code spring.security.enabled=false} property does not disable
 * Spring Security auto-configuration in this Spring Boot version, so an
 * explicit {@code SecurityFilterChain} bean is required.
 *
 * @since Sprint 19
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
