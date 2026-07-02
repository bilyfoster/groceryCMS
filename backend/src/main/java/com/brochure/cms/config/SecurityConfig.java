package com.brochure.cms.config;

import com.brochure.cms.shared.security.JwtAuthFilter;
import com.brochure.cms.shared.security.TenantFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TenantFilter tenantFilter;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(TenantFilter tenantFilter, JwtAuthFilter jwtAuthFilter) {
        this.tenantFilter = tenantFilter;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/magic-link", "/api/auth/verify")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tenant/settings")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pages/**", "/api/blog/**", "/api/faq/**",
                                "/api/staff/**", "/api/gallery/**", "/api/taxonomies/**",
                                "/api/products/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contact", "/api/blog/*/comments", "/api/match", "/api/events")
                        .permitAll()
                        .requestMatchers("/api/admin/**")
                        .hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/auth/me")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .addFilterBefore(tenantFilter, CsrfFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
