package com.brochure.cms.domain.staff;

import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.models.Therapist;
import com.brochure.cms.repositories.TherapistRepository;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService {

    private static final List<String> LICENSED_PATTERNS = List.of("lcs", "lpc", "lac", "lms", "lamft", "liac");

    private final StaffRepository staffRepository;
    private final TherapistRepository therapistRepository;

    public StaffService(StaffRepository staffRepository, TherapistRepository therapistRepository) {
        this.staffRepository = staffRepository;
        this.therapistRepository = therapistRepository;
    }

    @Transactional(readOnly = true)
    public List<StaffDto> listByPage(UUID pageId) {
        UUID tenantId = TenantIds.current();
        List<StaffMember> members = staffRepository
                .findByTenantIdAndPageIdAndDeletedAtIsNullAndPublishedTrueOrderBySortOrderAsc(tenantId, pageId);
        return mapToDtos(members, tenantId);
    }

    @Transactional(readOnly = true)
    public List<StaffDto> listAllByPageForAdmin(UUID pageId) {
        UUID tenantId = TenantIds.current();
        List<StaffMember> members = staffRepository
                .findByTenantIdAndPageIdAndDeletedAtIsNullOrderBySortOrderAsc(tenantId, pageId);
        return mapToDtos(members, tenantId);
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
        if (saved.isTherapist()) {
            createLinkedTherapist(saved);
        }
        return mapToDto(saved, saved.getTenantId());
    }

    @Transactional
    public StaffDto update(UUID id, UpdateStaffRequest request) {
        UUID tenantId = TenantIds.current();
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
        Optional<Therapist> existing = therapistRepository.findByStaffMemberIdAndTenantIdAndDeletedAtIsNull(id, tenantId);
        if (saved.isTherapist()) {
            if (existing.isPresent()) {
                updateLinkedTherapist(saved, existing.get());
            } else {
                createLinkedTherapist(saved);
            }
        } else {
            existing.ifPresent(t -> {
                t.softDelete();
                therapistRepository.save(t);
            });
        }
        return mapToDto(saved, tenantId);
    }

    @Transactional
    public void delete(UUID id) {
        StaffMember member = findForTenant(id);
        member.softDelete();
        staffRepository.save(member);
        therapistRepository.findByStaffMemberIdAndTenantIdAndDeletedAtIsNull(id, member.getTenantId())
                .ifPresent(t -> {
                    t.softDelete();
                    therapistRepository.save(t);
                });
    }

    @Transactional
    public int backfillTherapists() {
        UUID tenantId = TenantIds.current();
        List<StaffMember> candidates = staffRepository.findAllByTenantIdAndDeletedAtIsNullOrderBySortOrderAsc(tenantId)
                .stream()
                .filter(s -> isLicensedClinician(s.getName(), s.getTitle()))
                .filter(s -> therapistRepository.findByStaffMemberIdAndTenantIdAndDeletedAtIsNull(s.getId(), tenantId).isEmpty())
                .toList();
        int created = 0;
        for (StaffMember staff : candidates) {
            createLinkedTherapist(staff);
            staff.setTherapist(true);
            staffRepository.save(staff);
            created++;
        }
        return created;
    }

    private StaffMember findForTenant(UUID id) {
        UUID tenantId = TenantIds.current();
        return staffRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));
    }

    private List<StaffDto> mapToDtos(List<StaffMember> members, UUID tenantId) {
        if (members.isEmpty()) {
            return List.of();
        }
        Map<UUID, UUID> therapistIdByStaffId = therapistRepository
                .findByStaffMemberIdInAndTenantIdAndDeletedAtIsNull(
                        members.stream().map(StaffMember::getId).toList(), tenantId)
                .stream()
                .collect(Collectors.toMap(Therapist::getStaffMemberId, Therapist::getId));
        return members.stream()
                .map(m -> StaffDto.from(m, therapistIdByStaffId.get(m.getId())))
                .toList();
    }

    private StaffDto mapToDto(StaffMember member, UUID tenantId) {
        UUID therapistId = therapistRepository
                .findByStaffMemberIdAndTenantIdAndDeletedAtIsNull(member.getId(), tenantId)
                .map(Therapist::getId)
                .orElse(null);
        return StaffDto.from(member, therapistId);
    }

    private void createLinkedTherapist(StaffMember staff) {
        UUID tenantId = staff.getTenantId();
        String[] names = splitFirstLast(staff.getName());
        String slug = uniqueSlug(slugify(names[0] + " " + names[1]), tenantId);
        Therapist therapist = Therapist.builder()
                .tenantId(tenantId)
                .staffMemberId(staff.getId())
                .firstName(names[0])
                .lastName(names[1])
                .credentials(extractCredentials(staff.getName(), staff.getTitle()))
                .photoUrl(staff.getPhotoUrl())
                .slug(slug)
                .bio(staff.getBio())
                .serviceDelivery(ServiceDelivery.HYBRID)
                .availabilityStatus(AvailabilityStatus.ACCEPTING)
                .published(false)
                .sortOrder(staff.getSortOrder())
                .terms(Set.of())
                .build();
        therapistRepository.save(therapist);
    }

    private void updateLinkedTherapist(StaffMember staff, Therapist therapist) {
        String[] names = splitFirstLast(staff.getName());
        therapist.setFirstName(names[0]);
        therapist.setLastName(names[1]);
        therapist.setCredentials(extractCredentials(staff.getName(), staff.getTitle()));
        therapist.setPhotoUrl(staff.getPhotoUrl());
        therapist.setBio(staff.getBio());
        therapistRepository.save(therapist);
    }

    private boolean isLicensedClinician(String name, String title) {
        String combined = ((name != null ? name : "") + " " + (title != null ? title : "")).toLowerCase();
        if (combined.contains("intern")) {
            return false;
        }
        return LICENSED_PATTERNS.stream().anyMatch(combined::contains);
    }

    private String[] splitFirstLast(String fullName) {
        String display = fullName == null ? "" : fullName.split(",")[0].trim();
        int idx = display.indexOf(' ');
        if (idx <= 0) {
            return new String[]{display, display};
        }
        return new String[]{display.substring(0, idx).trim(), display.substring(idx + 1).trim()};
    }

    private String extractCredentials(String name, String title) {
        if (name != null) {
            int comma = name.indexOf(',');
            if (comma > 0) {
                return name.substring(comma + 1).trim();
            }
        }
        return title;
    }

    private String slugify(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }

    private String uniqueSlug(String baseSlug, UUID tenantId) {
        String slug = baseSlug;
        int counter = 2;
        while (therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNull(slug, tenantId)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
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
            UUID therapistId,
            Map<String, Object> socialLinks) {
        static StaffDto from(StaffMember member, UUID therapistId) {
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
                    therapistId,
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
