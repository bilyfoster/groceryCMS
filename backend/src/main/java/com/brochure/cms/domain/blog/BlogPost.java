package com.brochure.cms.domain.blog;

import com.brochure.cms.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPost extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "page_id", nullable = false, columnDefinition = "uuid")
    private UUID pageId;

    @Column(name = "author_id", columnDefinition = "uuid")
    private UUID authorId;

    @Column(nullable = false, length = 500)
    private String slug;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(columnDefinition = "text")
    private String excerpt;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "featured_image", length = 2000)
    private String featuredImage;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "is_sticky", nullable = false)
    private boolean sticky;

    @Builder.Default
    @Column(name = "allow_comments", nullable = false)
    private boolean allowComments = true;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] tags = new String[0];

    @Column(name = "meta_title", length = 500)
    private String metaTitle;

    @Column(name = "meta_description", length = 1000)
    private String metaDescription;

    public void setTags(String[] tags) {
        this.tags = tags != null ? tags : new String[0];
    }
}
