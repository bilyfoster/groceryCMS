package com.brochure.cms.domain.staff;

import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business operations for generic staff members.
 *
 * <p>The previous therapist-linking functionality has been removed because this
 * tenant is a grocery store; staff records are now independent of product catalog entries.
 */
@Service
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Transactional(readOnly = true)
    public List<StaffDto> listByPage(UUID pageId) {
        UUID tenantId = TenantIds.current();
        List<StaffMember> members = staffRepository
                .findByTenantIdAndPageIdAndDeletedAtIsNullAndPublishedTrueOrderBySortOrderAsc(tenantId, pageId);
        return members.stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<StaffDto> listAllByPageForAdmin(UUID pageId) {
        UUID tenantId = TenantIds.current();
        List<StaffMember> members = staffRepository
                .findByTenantIdAndPageIdAndDeletedAtIsNullOrderBySortOrderAsc(tenantId, pageId);
        return members.stream().map(this::mapToDto).toList();
    }

    @Transactional
    public StaffDto create(CreateStaffRequest request) {
        StaffMember member = new StaffMember();
        member.setTenantId(TenantIds.current());
        member.setPageId(request.pageId());
        member.setName(request.name());
        member.setTitle(request.title());
        member.setBio(request.bio());
        member.setPhotoUrl(request.photoUrl());
        member.setEmail(request.email());
        member.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        member.setPublished(request.published() != null ? request.published() : true);
        member.setTherapist(Boolean.TRUE.equals(request.isTherapist()));
        member.setSocialLinks(request.socialLinks() != null ? request.socialLinks() : Map.of());
        StaffMember saved = staffRepository.save(member);
        return mapToDto(saved);
    }

    @Transactional
    public StaffDto update(UUID id, UpdateStaffRequest request) {
        StaffMember member = findForTenant(id);
        if (request.name() != null) member.setName(request.name());
        if (request.title() != null) member.setTitle(request.title());
        if (request.bio() != null) member.setBio(request.bio());
        if (request.photoUrl() != null) member.setPhotoUrl(request.photoUrl());
        if (request.email() != null) member.setEmail(request.email());
        if (request.sortOrder() != null) member.setSortOrder(request.sortOrder());
        if (request.published() != null) member.setPublished(request.published());
        if (request.socialLinks() != null) member.setSocialLinks(request.socialLinks());
        if (request.isTherapist() != null) member.setTherapist(request.isTherapist());
        StaffMember saved = staffRepository.save(member);
        return mapToDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        StaffMember member = findForTenant(id);
        member.softDelete();
        staffRepository.save(member);
    }

    @Transactional(readOnly = true)
    public int backfillTherapists() {
        // Therapist profiles no longer exist in this domain; kept as no-op for API compatibility.
        return 0;
    }

    private StaffMember findForTenant(UUID id) {
        UUID tenantId = TenantIds.current();
        return staffRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));
    }

    private StaffDto mapToDto(StaffMember member) {
        return StaffDto.from(member);
    }

    public record StaffDto(
            UUID id,
            UUID pageId,
            String name,
            String title,
            String bio,
            String photoUrl,
            String email,
            int sortOrder,
            boolean published,
            @com.fasterxml.jackson.annotation.JsonProperty("isTherapist") boolean isTherapist,
            Map<String, Object> socialLinks) {
        static StaffDto from(StaffMember member) {
            return new StaffDto(
                    member.getId(),
                    member.getPageId(),
                    member.getName(),
                    member.getTitle(),
                    member.getBio(),
                    member.getPhotoUrl(),
                    member.getEmail(),
                    member.getSortOrder(),
                    member.isPublished(),
                    member.isTherapist(),
                    member.getSocialLinks());
        }
    }

    public record CreateStaffRequest(
            UUID pageId,
            String name,
            String title,
            String bio,
            String photoUrl,
            String email,
            Integer sortOrder,
            Boolean published,
            @com.fasterxml.jackson.annotation.JsonProperty("isTherapist") Boolean isTherapist,
            Map<String, Object> socialLinks) {}

    public record UpdateStaffRequest(
            String name,
            String title,
            String bio,
            String photoUrl,
            String email,
            Integer sortOrder,
            Boolean published,
            @com.fasterxml.jackson.annotation.JsonProperty("isTherapist") Boolean isTherapist,
            Map<String, Object> socialLinks) {}
}
