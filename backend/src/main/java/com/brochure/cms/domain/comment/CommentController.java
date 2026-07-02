package com.brochure.cms.domain.comment;

import com.brochure.cms.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<List<CommentService.CommentDto>> list(@PathVariable UUID postId) {
        return ApiResponse.ok(commentService.listApproved(postId));
    }

    @PostMapping("/{postId}/comments")
    public ApiResponse<CommentService.CommentDto> submit(
            @PathVariable UUID postId, @Valid @RequestBody SubmitCommentRequest request) {
        return ApiResponse.ok(commentService.submit(postId, request.toService()));
    }

    public record SubmitCommentRequest(UUID parentId, String authorName, String authorEmail, @NotBlank String body) {
        CommentService.SubmitCommentRequest toService() {
            return new CommentService.SubmitCommentRequest(parentId, authorName, authorEmail, body);
        }
    }
}
