package com.brochure.cms.domain.pattern;

import com.brochure.cms.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;
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
@Table(name = "block_patterns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockPattern extends BaseEntity {

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "thumbnail_url", length = 2000)
    private String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> blocks;

    @Column(name = "is_system", nullable = false)
    private boolean system;
}
