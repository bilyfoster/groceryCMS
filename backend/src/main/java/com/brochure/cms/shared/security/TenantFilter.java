package com.brochure.cms.shared.security;

import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.domain.tenant.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);
    private static final String TENANT_HEADER = "X-Tenant-Slug";

    private final TenantRepository tenantRepository;

    @Value("${app.tenant.header-enabled:true}")
    private boolean tenantHeaderEnabled;

    public TenantFilter(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            resolveTenant(request).ifPresent(TenantContext::set);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }

    private Optional<Tenant> resolveTenant(HttpServletRequest request) {
        String host = request.getServerName();
        Optional<Tenant> byHost = tenantRepository.findByDomainAndDeletedAtIsNull(host);
        if (byHost.isPresent()) {
            return byHost;
        }

        String subdomain = extractSubdomain(host);
        if (subdomain != null) {
            Optional<Tenant> bySlug = tenantRepository.findBySlugAndDeletedAtIsNull(subdomain);
            if (bySlug.isPresent()) {
                return bySlug;
            }
        }

        if (tenantHeaderEnabled) {
            String headerSlug = request.getHeader(TENANT_HEADER);
            if (headerSlug != null && !headerSlug.isBlank()) {
                return tenantRepository.findBySlugAndDeletedAtIsNull(headerSlug.trim());
            }
        }

        if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
            return tenantRepository.findBySlugAndDeletedAtIsNull("demo");
        }

        log.debug("No tenant resolved for host {}", host);
        return Optional.empty();
    }

    private String extractSubdomain(String host) {
        if (host == null || !host.contains(".")) {
            return null;
        }
        String[] parts = host.split("\\.");
        if (parts.length >= 3) {
            return parts[0];
        }
        return null;
    }
}
