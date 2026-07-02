package com.brochure.cms.domain.comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTenantIdAndPostIdAndDeletedAtIsNullAndApprovedTrueOrderByCreatedAtAsc(
            UUID tenantId, UUID postId);

    Optional<Comment> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
