package com.brochure.cms.domain.comment;

import com.brochure.cms.shared.dto.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PatchMapping("/{id}/approve")
    public ApiResponse<CommentService.CommentDto> approve(@PathVariable UUID id) {
        return ApiResponse.ok(commentService.approve(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        commentService.delete(id);
        return ApiResponse.ok(null);
    }
}
