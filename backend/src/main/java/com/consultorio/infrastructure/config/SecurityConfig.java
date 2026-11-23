package com.consultorio.infrastructure.config;

import com.consultorio.application.service.TokenService;
import com.consultorio.infrastructure.security.BearerTokenAuthenticationFilter;
import com.consultorio.infrastructure.security.HmacTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.SecureRandom;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@Order(2)
public class SecurityConfig {

    @Bean
    public TokenService tokenService(
            @Value("${app.security.jwt-secret:}") String secret,
            @Value("${app.security.jwt-expiration-seconds:86400}") long expirationSeconds) {
        String effectiveSecret = secret;
        if (effectiveSecret == null || effectiveSecret.isBlank()) {
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            effectiveSecret = Base64.getEncoder().encodeToString(random);
        }
        return new HmacTokenService(effectiveSecret, expirationSeconds);
    }

    @Bean
    public BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter(TokenService tokenService) {
        return new BearerTokenAuthenticationFilter(tokenService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   BearerTokenAuthenticationFilter bearerFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/health",
                    "/api/auth/**",
                    "/api/availability",
                    "/",
                    "/index.html",
                    "/login.html",
                    "/favicon.ico",
                    "/css/**",
                    "/js/**",
                    "/static/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(bearerFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
