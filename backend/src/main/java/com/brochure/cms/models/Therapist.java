package com.brochure.cms.models;

import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A clinical therapist profile stored independently of generic staff records.
 *
 * <p>Uses {@code @Getter}/{@code @Setter} rather than {@code @Data} because the
 * class extends {@link BaseEntity}; {@code @Data} would generate {@code equals}/
 * {@code hashCode} that conflict with the inherited identity (coding guidelines §9).
 */
@Entity
@Table(name = "therapists")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Therapist extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "staff_member_id", columnDefinition = "uuid")
    private UUID staffMemberId;

    @Column(name = "first_name", nullable = false, length = 255)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 255)
    private String lastName;

    @Column(length = 500)
    private String credentials;

    @Column(length = 100)
    private String pronouns;

    @Column(name = "photo_url", length = 2000)
    private String photoUrl;

    @Column(nullable = false, length = 255)
    private String slug;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(columnDefinition = "text")
    private String education;

    @Column(length = 500)
    private String licensure;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_delivery", nullable = false, length = 32)
    private ServiceDelivery serviceDelivery;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 32)
    private AvailabilityStatus availabilityStatus;

    @Column(name = "scheduling_url", length = 2000)
    private String schedulingUrl;

    @Column(name = "booking_platform_ref", length = 100)
    private String bookingPlatformRef;

    @Column(name = "meta_title", length = 500)
    private String metaTitle;

    @Column(name = "meta_description", length = 1000)
    private String metaDescription;

    @Column(name = "og_image_url", length = 2000)
    private String ogImageUrl;

    @Column(name = "canonical_url", length = 2000)
    private String canonicalUrl;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ManyToMany
    @JoinTable(
            name = "therapist_terms",
            joinColumns = @JoinColumn(name = "therapist_id"),
            inverseJoinColumns = @JoinColumn(name = "term_id"))
    private Set<TaxonomyTerm> terms;
}
