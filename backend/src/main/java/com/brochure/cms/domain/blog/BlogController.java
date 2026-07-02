package com.brochure.cms.domain.blog;

import com.brochure.cms.shared.dto.ApiResponse;
import com.brochure.cms.shared.dto.PagedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private final BlogService blogService;
    private final String baseUrl;

    public BlogController(BlogService blogService, @Value("${app.base-url:}") String baseUrl) {
        this.blogService = blogService;
        this.baseUrl = baseUrl;
    }

    @GetMapping
    public ApiResponse<PagedResponse<BlogService.BlogSummaryDto>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(blogService.listPublished(page, size));
    }

    @GetMapping(value = "/rss", produces = MediaType.APPLICATION_XML_VALUE)
    public String rss() {
        return blogService.buildRssFeed(baseUrl);
    }

    @PostMapping("/{slug}/view")
    public ApiResponse<Integer> view(@PathVariable String slug) {
        return ApiResponse.ok(blogService.incrementView(slug));
    }

    @PostMapping("/{slug}/like")
    public ApiResponse<Integer> like(@PathVariable String slug) {
        return ApiResponse.ok(blogService.like(slug, true));
    }

    @DeleteMapping("/{slug}/like")
    public ApiResponse<Integer> unlike(@PathVariable String slug) {
        return ApiResponse.ok(blogService.like(slug, false));
    }

    @GetMapping("/category/{slug}")
    public ApiResponse<PagedResponse<BlogService.BlogSummaryDto>> byCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(blogService.listByCategory(slug, page, size));
    }

    @GetMapping("/tag/{tag}")
    public ApiResponse<PagedResponse<BlogService.BlogSummaryDto>> byTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(blogService.listByTag(tag, page, size));
    }

    @GetMapping("/{slug}")
    public ApiResponse<BlogService.BlogDetailDto> get(@PathVariable String slug) {
        return ApiResponse.ok(blogService.getBySlug(slug));
    }
}
