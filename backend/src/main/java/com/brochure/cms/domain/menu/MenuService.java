package com.brochure.cms.domain.menu;

import com.brochure.cms.domain.page.Page;
import com.brochure.cms.domain.page.PageRepository;
import com.brochure.cms.shared.util.TenantIds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final PageRepository pageRepository;

    public MenuService(
            MenuRepository menuRepository,
            MenuItemRepository menuItemRepository,
            PageRepository pageRepository) {
        this.menuRepository = menuRepository;
        this.menuItemRepository = menuItemRepository;
        this.pageRepository = pageRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItemDto> getByLocation(String location) {
        UUID tenantId = TenantIds.current();
        return menuRepository
                .findByTenantIdAndLocation(tenantId, location)
                .map(menu -> buildMenu(menu.getId(), tenantId))
                .orElse(List.of());
    }

    private List<MenuItemDto> buildMenu(UUID menuId, UUID tenantId) {
        List<MenuItem> items = menuItemRepository.findByMenuIdOrderBySortOrderAsc(menuId);
        if (items.isEmpty()) {
            return List.of();
        }
        Map<UUID, Page> pagesById = new HashMap<>();
        for (MenuItem item : items) {
            if (item.getPageId() != null) {
                pageRepository
                        .findByIdAndTenantIdAndDeletedAtIsNull(item.getPageId(), tenantId)
                        .ifPresent(p -> pagesById.put(p.getId(), p));
            }
        }
        return buildTree(items, null, pagesById);
    }

    private List<MenuItemDto> buildTree(
            List<MenuItem> items, UUID parentId, Map<UUID, Page> pagesById) {
        List<MenuItemDto> result = new ArrayList<>();
        for (MenuItem item : items) {
            boolean isChild = parentId == null ? item.getParentId() == null : parentId.equals(item.getParentId());
            if (!isChild) {
                continue;
            }
            List<MenuItemDto> children = buildTree(items, item.getId(), pagesById);
            result.add(new MenuItemDto(
                    item.getId(),
                    item.getLabel(),
                    resolveHref(item, pagesById),
                    item.getTarget(),
                    children.isEmpty() ? null : children));
        }
        return result;
    }

    private String resolveHref(MenuItem item, Map<UUID, Page> pagesById) {
        if (item.getUrl() != null && !item.getUrl().isBlank()) {
            return item.getUrl();
        }
        if (item.getPageId() != null) {
            Page page = pagesById.get(item.getPageId());
            if (page != null) {
                if (page.isFrontPage()) {
                    return "/";
                }
                return "/" + page.getSlug();
            }
        }
        return "#";
    }

    @Transactional(readOnly = true)
    public AdminMenuDto getAdminMenu(String location) {
        UUID tenantId = TenantIds.current();
        Menu menu = menuRepository
                .findByTenantIdAndLocation(tenantId, location)
                .orElse(null);
        if (menu == null) {
            return new AdminMenuDto(null, location, capitalize(location), List.of());
        }
        List<AdminMenuItemDto> items = menuItemRepository.findByMenuIdOrderBySortOrderAsc(menu.getId()).stream()
                .map(i -> new AdminMenuItemDto(
                        i.getId(), i.getParentId(), i.getLabel(), i.getUrl(), i.getPageId(), i.getTarget(), i.getSortOrder()))
                .toList();
        return new AdminMenuDto(menu.getId(), menu.getLocation(), menu.getName(), items);
    }

    public AdminMenuDto saveAdminMenu(String location, SaveMenuRequest request) {
        UUID tenantId = TenantIds.current();
        Menu menu = menuRepository
                .findByTenantIdAndLocation(tenantId, location)
                .orElseGet(() -> {
                    Menu m = new Menu();
                    m.setTenantId(tenantId);
                    m.setLocation(location);
                    m.setName(request.name() != null ? request.name() : capitalize(location));
                    return menuRepository.save(m);
                });
        if (request.name() != null) {
            menu.setName(request.name());
            menuRepository.save(menu);
        }
        List<MenuItem> existing = menuItemRepository.findByMenuIdOrderBySortOrderAsc(menu.getId());
        menuItemRepository.deleteAll(existing);
        if (request.items() != null) {
            int order = 0;
            for (SaveMenuItemRequest item : request.items()) {
                MenuItem mi = new MenuItem();
                mi.setMenuId(menu.getId());
                mi.setParentId(item.parentId());
                mi.setLabel(item.label());
                mi.setUrl(item.url());
                mi.setPageId(item.pageId());
                mi.setTarget(item.target() != null ? item.target() : "_self");
                mi.setSortOrder(item.sortOrder() != null ? item.sortOrder() : order++);
                menuItemRepository.save(mi);
            }
        }
        return getAdminMenu(location);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public record MenuItemDto(UUID id, String label, String href, String target, List<MenuItemDto> children) {}

    public record AdminMenuDto(UUID id, String location, String name, List<AdminMenuItemDto> items) {}

    public record AdminMenuItemDto(
            UUID id, UUID parentId, String label, String url, UUID pageId, String target, int sortOrder) {}

    public record SaveMenuRequest(String name, List<SaveMenuItemRequest> items) {}

    public record SaveMenuItemRequest(
            UUID parentId, String label, String url, UUID pageId, String target, Integer sortOrder) {}
}
