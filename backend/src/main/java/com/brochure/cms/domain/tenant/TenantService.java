package com.brochure.cms.domain.tenant;

import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public TenantSettingsDto getPublicSettings() {
        UUID tenantId = TenantIds.current();
        Tenant tenant = tenantRepository
                .findById(tenantId)
                .filter(t -> t.getDeletedAt() == null && t.isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        return new TenantSettingsDto(tenant.getSlug(), tenant.getName(), tenant.getSettings());
    }

    public TenantSettingsDto updateSettings(String name, Map<String, Object> settings) {
        UUID tenantId = TenantIds.current();
        Tenant tenant = tenantRepository
                .findById(tenantId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        tenant.setName(name);
        if (settings != null) {
            tenant.setSettings(settings);
        }
        tenantRepository.save(tenant);
        return new TenantSettingsDto(tenant.getSlug(), tenant.getName(), tenant.getSettings());
    }

    public record TenantSettingsDto(String slug, String name, Map<String, Object> settings) {}
}
