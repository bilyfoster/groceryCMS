package com.brochure.cms.shared.security;

import com.brochure.cms.domain.auth.JwtService;
import com.brochure.cms.domain.auth.User;
import com.brochure.cms.domain.auth.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    public static final String AUTH_COOKIE = "auth_token";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        extractToken(request)
                .flatMap(jwtService::parseToken)
                .ifPresent(claims -> authenticate(claims.userId(), claims.tenantId(), claims.role()));

        chain.doFilter(request, response);
    }

    private void authenticate(UUID userId, UUID tenantId, String role) {
        var ctxTenant = TenantContext.get();
        if (ctxTenant != null && !ctxTenant.getId().equals(tenantId)) {
            log.warn("JWT tenant {} does not match request tenant {}", tenantId, ctxTenant.getId());
            return;
        }

        Optional<User> user = userRepository.findByIdAndTenantIdAndDeletedAtIsNull(userId, tenantId);
        user.filter(User::isActive).ifPresent(u -> {
            var authorities = java.util.List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
            var auth = new UsernamePasswordAuthenticationToken(u, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        });
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (AUTH_COOKIE.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
