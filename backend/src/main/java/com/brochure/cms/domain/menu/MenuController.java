package com.brochure.cms.domain.menu;

import com.brochure.cms.shared.dto.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/{location}")
    public ApiResponse<List<MenuService.MenuItemDto>> get(@PathVariable String location) {
        return ApiResponse.ok(menuService.getByLocation(location));
    }
}
