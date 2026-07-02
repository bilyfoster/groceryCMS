package com.brochure.cms.domain.tenant;

import com.brochure.cms.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/settings")
    public ApiResponse<TenantService.TenantSettingsDto> getSettings() {
        return ApiResponse.ok(tenantService.getPublicSettings());
    }
}
