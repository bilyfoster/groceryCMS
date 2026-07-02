package com.brochure.cms.domain.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "contact_submissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactSubmission {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 1000)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "replied_at")
    private OffsetDateTime repliedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void ensureId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
