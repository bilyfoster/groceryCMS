package com.brochure.cms.domain.comment;

import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listApproved(UUID postId) {
        UUID tenantId = TenantIds.current();
        return commentRepository
                .findByTenantIdAndPostIdAndDeletedAtIsNullAndApprovedTrueOrderByCreatedAtAsc(tenantId, postId)
                .stream()
                .map(CommentDto::from)
                .toList();
    }

    public CommentDto submit(UUID postId, SubmitCommentRequest request) {
        UUID tenantId = TenantIds.current();
        Comment comment = new Comment();
        comment.setTenantId(tenantId);
        comment.setPostId(postId);
        comment.setParentId(request.parentId());
        comment.setAuthorName(request.authorName());
        comment.setAuthorEmail(request.authorEmail());
        comment.setBody(request.body());
        comment.setApproved(false);
        commentRepository.save(comment);
        return CommentDto.from(comment);
    }

    public CommentDto approve(UUID id) {
        Comment comment = findForTenant(id);
        comment.setApproved(true);
        commentRepository.save(comment);
        return CommentDto.from(comment);
    }

    public void delete(UUID id) {
        Comment comment = findForTenant(id);
        comment.softDelete();
        commentRepository.save(comment);
    }

    private Comment findForTenant(UUID id) {
        return commentRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, TenantIds.current())
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
    }

    public record CommentDto(
            UUID id,
            UUID postId,
            UUID parentId,
            String authorName,
            String body,
            boolean approved) {
        static CommentDto from(Comment comment) {
            return new CommentDto(
                    comment.getId(),
                    comment.getPostId(),
                    comment.getParentId(),
                    comment.getAuthorName(),
                    comment.getBody(),
                    comment.isApproved());
        }
    }

    public record SubmitCommentRequest(UUID parentId, String authorName, String authorEmail, String body) {}
}
