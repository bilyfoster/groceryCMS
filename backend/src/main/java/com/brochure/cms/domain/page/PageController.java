package com.brochure.cms.domain.page;

import com.brochure.cms.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pages")
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping
    public ApiResponse<List<PageService.PageSummaryDto>> list() {
        return ApiResponse.ok(pageService.listPublishedNav());
    }

    @GetMapping("/front")
    public ApiResponse<PageService.PageDetailDto> front() {
        return ApiResponse.ok(pageService.getFrontPage());
    }

    @GetMapping("/not-found")
    public ApiResponse<PageService.PageDetailDto> notFound() {
        return ApiResponse.ok(pageService.getNotFoundPage());
    }

    @GetMapping("/{slug}")
    public ApiResponse<PageService.PageDetailDto> get(
            @PathVariable String slug,
            @CookieValue(name = PageService.PAGE_UNLOCK_COOKIE, required = false) String unlockToken) {
        return ApiResponse.ok(pageService.getBySlug(slug, false, unlockToken));
    }

    @PostMapping("/{slug}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlock(
            @PathVariable String slug, @Valid @RequestBody UnlockRequest request) {
        String token = pageService.unlockPage(slug, request.password());
        ResponseCookie cookie = ResponseCookie.from(PageService.PAGE_UNLOCK_COOKIE, token)
                .httpOnly(true)
                .path("/")
                .maxAge(86400)
                .sameSite("Lax")
                .build();
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(ApiResponse.ok(null));
    }

    public record UnlockRequest(@NotBlank String password) {}
}
