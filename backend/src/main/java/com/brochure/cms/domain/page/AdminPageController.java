package com.brochure.cms.domain.page;

import com.brochure.cms.domain.auth.User;
import com.brochure.cms.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pages")
public class AdminPageController {

    private final PageService pageService;

    public AdminPageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping
    public ApiResponse<List<PageService.PageSummaryDto>> list() {
        return ApiResponse.ok(pageService.listAllForAdmin());
    }

    @GetMapping("/{id}")
    public ApiResponse<PageService.PageDetailDto> get(@PathVariable UUID id) {
        return ApiResponse.ok(pageService.getById(id));
    }

    @PostMapping
    public ApiResponse<PageService.PageDetailDto> create(@Valid @RequestBody PageService.CreatePageRequest request) {
        return ApiResponse.ok(pageService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PageService.PageDetailDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody PageService.UpdatePageRequest request,
            @AuthenticationPrincipal User user) {
        UUID authorId = user != null ? user.getId() : null;
        return ApiResponse.ok(pageService.update(id, request, authorId));
    }

    @PatchMapping("/{id}/publish")
    public ApiResponse<PageService.PageDetailDto> publish(
            @PathVariable UUID id, @Valid @RequestBody PublishRequest request) {
        return ApiResponse.ok(pageService.setPublished(id, request.published()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        pageService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/blocks")
    public ApiResponse<PageService.ContentBlockDto> createBlock(
            @PathVariable UUID id, @Valid @RequestBody PageService.CreateBlockRequest request) {
        return ApiResponse.ok(pageService.createBlock(id, request));
    }

    @PutMapping("/{id}/blocks/{blockId}")
    public ApiResponse<PageService.ContentBlockDto> updateBlock(
            @PathVariable UUID id,
            @PathVariable UUID blockId,
            @Valid @RequestBody PageService.UpdateBlockRequest request) {
        return ApiResponse.ok(pageService.updateBlock(id, blockId, request));
    }

    @DeleteMapping("/{id}/blocks/{blockId}")
    public ApiResponse<Void> deleteBlock(@PathVariable UUID id, @PathVariable UUID blockId) {
        pageService.deleteBlock(id, blockId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/blocks/order")
    public ApiResponse<List<PageService.ContentBlockDto>> reorder(
            @PathVariable UUID id, @Valid @RequestBody ReorderBlocksRequest request) {
        return ApiResponse.ok(pageService.reorderBlocks(id, request.orderedIds()));
    }

    @GetMapping("/reading")
    public ApiResponse<PageService.ReadingSettingsDto> getReading() {
        return ApiResponse.ok(pageService.getReadingSettings());
    }

    @PutMapping("/reading")
    public ApiResponse<PageService.ReadingSettingsDto> updateReading(
            @Valid @RequestBody ReadingSettingsRequest request) {
        return ApiResponse.ok(pageService.updateReadingSettings(request.frontPageId(), request.postsPageId()));
    }

    public record ReadingSettingsRequest(UUID frontPageId, UUID postsPageId) {}

    public record PublishRequest(boolean published) {}

    public record ReorderBlocksRequest(@NotEmpty List<@NotNull UUID> orderedIds) {}
}
