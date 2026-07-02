package com.brochure.cms.domain.media;

import com.brochure.cms.domain.auth.User;
import com.brochure.cms.shared.dto.ApiResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping
    public ApiResponse<MediaService.MediaDto> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String altText,
            @AuthenticationPrincipal User user)
            throws IOException {
        return ApiResponse.ok(mediaService.upload(file, user != null ? user.getId() : null, altText));
    }

    @GetMapping
    public ApiResponse<List<MediaService.MediaDto>> list() {
        return ApiResponse.ok(mediaService.list());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        mediaService.delete(id);
        return ApiResponse.ok(null);
    }
}
