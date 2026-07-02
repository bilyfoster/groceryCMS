package com.brochure.cms.domain.staff;

import com.brochure.cms.shared.domain.BaseEntity;
import jakarta.persistence.Column;
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
@Table(name = "staff_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffMember extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "page_id", nullable = false, columnDefinition = "uuid")
    private UUID pageId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(name = "photo_url", length = 2000)
    private String photoUrl;

    @Column(length = 320)
    private String email;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder.Default
    @Column(nullable = false)
    private boolean published = true;

    @Builder.Default
    @Column(name = "is_therapist", nullable = false)
    private boolean therapist = false;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> socialLinks = new HashMap<>();

    public void setSocialLinks(Map<String, Object> socialLinks) {
        this.socialLinks = socialLinks != null ? socialLinks : new HashMap<>();
    }
}
