package com.brochure.cms.domain.page;

import com.brochure.cms.enums.PageType;
import com.brochure.cms.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Page extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "page_type", nullable = false, length = 100)
    @Convert(converter = PageTypeConverter.class)
    private PageType pageType;

    @Builder.Default
    @Column(nullable = false, length = 100)
    private String layout = "default";

    @Column(name = "nav_order")
    private Integer navOrder;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "is_front_page", nullable = false)
    private boolean frontPage;

    @Column(name = "is_posts_page", nullable = false)
    private boolean postsPage;

    @Column(name = "page_password")
    private String pagePassword;

    @Column(name = "meta_title", length = 500)
    private String metaTitle;

    @Column(name = "meta_description", length = 1000)
    private String metaDescription;

    @Column(name = "og_image_url", length = 2000)
    private String ogImageUrl;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> config = new HashMap<>();

    public void setConfig(Map<String, Object> config) {
        this.config = config != null ? config : new HashMap<>();
    }
}
