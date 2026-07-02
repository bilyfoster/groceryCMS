package com.brochure.cms.domain.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_categories")
@IdClass(PostCategory.PostCategoryId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCategory {

    @Id
    @Column(name = "post_id", columnDefinition = "uuid")
    private UUID postId;

    @Id
    @Column(name = "category_id", columnDefinition = "uuid")
    private UUID categoryId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PostCategoryId implements Serializable {
        private UUID postId;
        private UUID categoryId;
    }
}
