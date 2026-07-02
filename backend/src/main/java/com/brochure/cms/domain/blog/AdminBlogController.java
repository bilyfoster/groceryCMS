package com.brochure.cms.domain.blog;

import com.brochure.cms.domain.auth.User;
import com.brochure.cms.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/admin/blog")
public class AdminBlogController {

    private final BlogService blogService;

    public AdminBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public ApiResponse<List<BlogService.BlogSummaryDto>> list() {
        return ApiResponse.ok(blogService.listAllForAdmin());
    }

    @GetMapping("/{id}")
    public ApiResponse<BlogService.BlogDetailDto> get(@PathVariable UUID id) {
        return ApiResponse.ok(blogService.getByIdForAdmin(id));
    }

    @PostMapping
    public ApiResponse<BlogService.BlogDetailDto> create(
            @Valid @RequestBody BlogService.CreateBlogRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.ok(blogService.create(request, user != null ? user.getId() : null));
    }

    @PutMapping("/{id}")
    public ApiResponse<BlogService.BlogDetailDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody BlogService.UpdateBlogRequest request,
            @AuthenticationPrincipal User user) {
        UUID authorId = user != null ? user.getId() : null;
        return ApiResponse.ok(blogService.update(id, request, authorId));
    }

    @PatchMapping("/{id}/publish")
    public ApiResponse<BlogService.BlogDetailDto> publish(
            @PathVariable UUID id, @Valid @RequestBody PublishRequest request) {
        return ApiResponse.ok(blogService.setPublished(id, request.published()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        blogService.delete(id);
        return ApiResponse.ok(null);
    }

    public record PublishRequest(boolean published) {}
}
