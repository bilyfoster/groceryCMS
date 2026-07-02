package com.brochure.cms.domain.tenant;

import com.brochure.cms.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminTenantController {

    private final TenantService tenantService;

    public AdminTenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public ApiResponse<TenantService.TenantSettingsDto> get() {
        return ApiResponse.ok(tenantService.getPublicSettings());
    }

    @PutMapping
    public ApiResponse<TenantService.TenantSettingsDto> update(
            @Valid @RequestBody UpdateSettingsRequest request) {
        return ApiResponse.ok(tenantService.updateSettings(request.name(), request.settings()));
    }

    public record UpdateSettingsRequest(@NotBlank String name, Map<String, Object> settings) {}
}
