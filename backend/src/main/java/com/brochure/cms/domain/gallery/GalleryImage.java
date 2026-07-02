package com.brochure.cms.domain.gallery;

import com.brochure.cms.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gallery_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryImage extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "page_id", nullable = false, columnDefinition = "uuid")
    private UUID pageId;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(name = "alt_text", length = 1000)
    private String altText;

    @Column(length = 2000)
    private String caption;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder.Default
    @Column(nullable = false)
    private boolean published = true;
}
