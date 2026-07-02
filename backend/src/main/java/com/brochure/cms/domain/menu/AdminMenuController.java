package com.brochure.cms.domain.menu;

import com.brochure.cms.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/menus")
public class AdminMenuController {

    private final MenuService menuService;

    public AdminMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/{location}")
    public ApiResponse<MenuService.AdminMenuDto> get(@PathVariable String location) {
        return ApiResponse.ok(menuService.getAdminMenu(location));
    }

    @PutMapping("/{location}")
    public ApiResponse<MenuService.AdminMenuDto> save(
            @PathVariable String location, @Valid @RequestBody MenuService.SaveMenuRequest request) {
        return ApiResponse.ok(menuService.saveAdminMenu(location, request));
    }
}
