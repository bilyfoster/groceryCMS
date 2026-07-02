package com.brochure.cms.domain.faq;

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
@Table(name = "faq_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqItem extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "page_id", nullable = false, columnDefinition = "uuid")
    private UUID pageId;

    @Column(nullable = false, length = 2000)
    private String question;

    @Column(nullable = false, columnDefinition = "text")
    private String answer;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder.Default
    @Column(nullable = false)
    private boolean published = true;
}
