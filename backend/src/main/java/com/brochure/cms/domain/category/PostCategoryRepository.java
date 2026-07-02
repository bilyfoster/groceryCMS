package com.brochure.cms.domain.category;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCategoryRepository extends JpaRepository<PostCategory, PostCategory.PostCategoryId> {

    List<PostCategory> findByPostId(UUID postId);

    void deleteByPostId(UUID postId);

    long countByCategoryId(UUID categoryId);
}
