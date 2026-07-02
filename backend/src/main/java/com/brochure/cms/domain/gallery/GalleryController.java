package com.brochure.cms.domain.gallery;

import com.brochure.cms.shared.dto.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping
    public ApiResponse<List<GalleryService.GalleryDto>> list(@RequestParam UUID pageId) {
        return ApiResponse.ok(galleryService.listByPage(pageId));
    }
}
