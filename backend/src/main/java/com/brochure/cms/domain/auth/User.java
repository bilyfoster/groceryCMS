package com.brochure.cms.domain.auth;

import com.brochure.cms.enums.UserRole;
import com.brochure.cms.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "display_name")
    private String displayName;

    @Builder.Default
    @Column(nullable = false, length = 50)
    @Convert(converter = UserRoleConverter.class)
    private UserRole role = UserRole.VIEWER;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
