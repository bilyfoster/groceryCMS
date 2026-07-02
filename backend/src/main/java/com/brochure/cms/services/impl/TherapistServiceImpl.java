package com.brochure.cms.services.impl;

import com.brochure.cms.dto.TherapistRequestDTO;
import com.brochure.cms.dto.TherapistResponseDTO;
import com.brochure.cms.dto.TherapistSelfServiceDTO;
import com.brochure.cms.dto.TherapistSummaryDTO;
import com.brochure.cms.dto.TaxonomyTermResponseDTO;
import com.brochure.cms.enums.AvailabilityStatus;
import com.brochure.cms.enums.ServiceDelivery;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.models.Therapist;
import com.brochure.cms.repositories.TaxonomyTermRepository;
import com.brochure.cms.repositories.TherapistRepository;
import com.brochure.cms.services.TherapistService;
import com.brochure.cms.shared.dto.PagedResponse;
import com.brochure.cms.shared.exception.AccessDeniedException;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link TherapistService} implementation backed by JPA. Every query is
 * scoped to {@link TenantIds#current()} and therapist self-service is enforced
 * by matching the therapist's {@code userId} to the authenticated user.
 */
@Service
@Transactional(readOnly = true)
public class TherapistServiceImpl implements TherapistService {

    private static final Logger log = LoggerFactory.getLogger(TherapistServiceImpl.class);

    private static final Set<TaxonomyType> ALLOWED_TERM_TYPES = EnumSet.of(
            TaxonomyType.FOCUS_AREA, TaxonomyType.MODALITY, TaxonomyType.DEMOGRAPHIC);

    private static final String THERAPIST_NOT_FOUND = "Therapist not found: ";
    private static final String SLUG_ALREADY_EXISTS = "A therapist with slug '%s' already exists in this tenant";

    private final TherapistRepository therapistRepository;
    private final TaxonomyTermRepository taxonomyTermRepository;

    public TherapistServiceImpl(TherapistRepository therapistRepository,
                                TaxonomyTermRepository taxonomyTermRepository) {
        this.therapistRepository = therapistRepository;
        this.taxonomyTermRepository = taxonomyTermRepository;
    }

    @Override
    public List<TherapistResponseDTO> listAll() {
        UUID tenantId = TenantIds.current();
        return therapistRepository.findAllByTenantIdFetchTerms(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TherapistResponseDTO getById(UUID id) {
        UUID tenantId = TenantIds.current();
        Therapist therapist = therapistRepository.findByIdAndTenantIdFetchTerms(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND + id));
        return toResponse(therapist);
    }

    @Override
    @Transactional
    public TherapistResponseDTO create(TherapistRequestDTO request) {
        UUID tenantId = TenantIds.current();
        ensureSlugUnique(request.getSlug(), tenantId, null);
        Set<TaxonomyTerm> terms = resolveAndValidateTerms(request.getTermIds(), tenantId);

        Therapist therapist = Therapist.builder()
                .tenantId(tenantId)
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .credentials(request.getCredentials())
                .pronouns(request.getPronouns())
                .photoUrl(request.getPhotoUrl())
                .slug(request.getSlug())
                .bio(request.getBio())
                .yearsOfExperience(request.getYearsOfExperience())
                .education(request.getEducation())
                .licensure(request.getLicensure())
                .serviceDelivery(request.getServiceDelivery())
                .availabilityStatus(request.getAvailabilityStatus())
                .schedulingUrl(request.getSchedulingUrl())
                .bookingPlatformRef(request.getBookingPlatformRef())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .ogImageUrl(request.getOgImageUrl())
                .canonicalUrl(request.getCanonicalUrl())
                .published(request.getPublished())
                .sortOrder(request.getSortOrder())
                .terms(terms)
                .build();

        Therapist saved = therapistRepository.save(therapist);
        log.info("Created therapist {} with slug '{}' for tenant {}", saved.getId(), saved.getSlug(), tenantId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TherapistResponseDTO update(UUID id, TherapistRequestDTO request) {
        UUID tenantId = TenantIds.current();
        Therapist therapist = therapistRepository.findByIdAndTenantIdFetchTerms(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND + id));

        if (!therapist.getSlug().equals(request.getSlug())) {
            ensureSlugUnique(request.getSlug(), tenantId, id);
        }
        Set<TaxonomyTerm> terms = resolveAndValidateTerms(request.getTermIds(), tenantId);

        therapist.setUserId(request.getUserId());
        therapist.setFirstName(request.getFirstName());
        therapist.setLastName(request.getLastName());
        therapist.setCredentials(request.getCredentials());
        therapist.setPronouns(request.getPronouns());
        therapist.setPhotoUrl(request.getPhotoUrl());
        therapist.setSlug(request.getSlug());
        therapist.setBio(request.getBio());
        therapist.setYearsOfExperience(request.getYearsOfExperience());
        therapist.setEducation(request.getEducation());
        therapist.setLicensure(request.getLicensure());
        therapist.setServiceDelivery(request.getServiceDelivery());
        therapist.setAvailabilityStatus(request.getAvailabilityStatus());
        therapist.setSchedulingUrl(request.getSchedulingUrl());
        therapist.setBookingPlatformRef(request.getBookingPlatformRef());
        therapist.setMetaTitle(request.getMetaTitle());
        therapist.setMetaDescription(request.getMetaDescription());
        therapist.setOgImageUrl(request.getOgImageUrl());
        therapist.setCanonicalUrl(request.getCanonicalUrl());
        therapist.setPublished(request.getPublished());
        therapist.setSortOrder(request.getSortOrder());
        therapist.setTerms(terms);

        Therapist saved = therapistRepository.save(therapist);
        log.info("Updated therapist {} for tenant {}", id, tenantId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantIds.current();
        Therapist therapist = therapistRepository.findByIdAndTenantIdFetchTerms(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND + id));
        therapist.softDelete();
        therapistRepository.save(therapist);
        log.info("Soft-deleted therapist {} for tenant {}", id, tenantId);
    }

    @Override
    @Transactional
    public TherapistResponseDTO updatePublishStatus(UUID id, boolean published) {
        UUID tenantId = TenantIds.current();
        Therapist therapist = therapistRepository.findByIdAndTenantIdFetchTerms(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(THERAPIST_NOT_FOUND + id));
        therapist.setPublished(published);
        Therapist saved = therapistRepository.save(therapist);
        log.info("Set published={} for therapist {} in tenant {}", published, id, tenantId);
        return toResponse(saved);
    }

    @Override
    public PagedResponse<TherapistSummaryDTO> findPublishedDirectory(
            UUID focusAreaId,
            UUID modalityId,
            UUID demographicId,
            ServiceDelivery delivery,
            AvailabilityStatus availability,
            String search,
            Pageable pageable) {
        UUID tenantId = TenantIds.current();
        String searchPattern = (search == null || search.isBlank())
                ? null
                : "%" + search.trim().toLowerCase() + "%";
        Page<UUID> idPage = therapistRepository.findPublishedDirectoryIds(
                tenantId,
                focusAreaId,
                TaxonomyType.FOCUS_AREA,
                modalityId,
                TaxonomyType.MODALITY,
                demographicId,
                TaxonomyType.DEMOGRAPHIC,
                delivery,
                availability,
                searchPattern,
                pageable);

        if (idPage.isEmpty()) {
            return PagedResponse.of(List.of(), idPage.getNumber(), idPage.getSize(), idPage.getTotalElements());
        }

        List<Therapist> therapists = therapistRepository.findAllByIdsFetchTerms(tenantId, idPage.getContent());
        Map<UUID, Therapist> byId = therapists.stream()
                .collect(Collectors.toMap(Therapist::getId, Function.identity()));
        List<TherapistSummaryDTO> content = idPage.getContent().stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(this::toSummary)
                .toList();

        return PagedResponse.of(content, idPage.getNumber(), idPage.getSize(), idPage.getTotalElements());
    }

    @Override
    public TherapistResponseDTO findPublishedBySlug(String slug) {
        UUID tenantId = TenantIds.current();
        Therapist therapist = therapistRepository.findPublishedBySlugAndTenantIdFetchTerms(slug, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found: " + slug));
        return toResponse(therapist);
    }

    @Override
    public TherapistResponseDTO getOwnProfile(UUID userId) {
        UUID tenantId = TenantIds.current();
        Therapist therapist = therapistRepository.findByUserIdAndTenantIdFetchTerms(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist profile not found for user: " + userId));
        enforceOwnership(therapist, userId);
        return toResponse(therapist);
    }

    @Override
    @Transactional
    public TherapistResponseDTO updateOwnProfile(UUID userId, TherapistSelfServiceDTO request) {
        UUID tenantId = TenantIds.current();
        Therapist therapist = therapistRepository.findByUserIdAndTenantIdFetchTerms(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist profile not found for user: " + userId));
        enforceOwnership(therapist, userId);

        if (!therapist.getSlug().equals(request.getSlug())) {
            ensureSlugUnique(request.getSlug(), tenantId, therapist.getId());
        }
        Set<TaxonomyTerm> terms = resolveAndValidateTerms(request.getTermIds(), tenantId);

        therapist.setFirstName(request.getFirstName());
        therapist.setLastName(request.getLastName());
        therapist.setCredentials(request.getCredentials());
        therapist.setPronouns(request.getPronouns());
        therapist.setPhotoUrl(request.getPhotoUrl());
        therapist.setSlug(request.getSlug());
        therapist.setBio(request.getBio());
        therapist.setYearsOfExperience(request.getYearsOfExperience());
        therapist.setEducation(request.getEducation());
        therapist.setLicensure(request.getLicensure());
        therapist.setServiceDelivery(request.getServiceDelivery());
        therapist.setAvailabilityStatus(request.getAvailabilityStatus());
        therapist.setSchedulingUrl(request.getSchedulingUrl());
        therapist.setBookingPlatformRef(request.getBookingPlatformRef());
        therapist.setMetaTitle(request.getMetaTitle());
        therapist.setMetaDescription(request.getMetaDescription());
        therapist.setOgImageUrl(request.getOgImageUrl());
        therapist.setCanonicalUrl(request.getCanonicalUrl());
        therapist.setTerms(terms);

        Therapist saved = therapistRepository.save(therapist);
        log.info("Updated own profile for therapist {} by user {} in tenant {}", saved.getId(), userId, tenantId);
        return toResponse(saved);
    }

    private void ensureSlugUnique(String slug, UUID tenantId, UUID excludeId) {
        boolean exists;
        if (excludeId == null) {
            exists = therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNull(slug, tenantId);
        } else {
            exists = therapistRepository.existsBySlugAndTenantIdAndDeletedAtIsNullAndIdNot(slug, tenantId, excludeId);
        }
        if (exists) {
            throw new ValidationException(SLUG_ALREADY_EXISTS.formatted(slug));
        }
    }

    private Set<TaxonomyTerm> resolveAndValidateTerms(Set<UUID> termIds, UUID tenantId) {
        if (termIds == null || termIds.isEmpty()) {
            return Set.of();
        }
        Set<TaxonomyTerm> resolved = new HashSet<>();
        for (UUID termId : termIds) {
            TaxonomyTerm term = taxonomyTermRepository.findByIdAndTenantIdAndDeletedAtIsNull(termId, tenantId)
                    .orElseThrow(() -> new ValidationException("Invalid taxonomy term id: " + termId));
            if (!ALLOWED_TERM_TYPES.contains(term.getType())) {
                throw new ValidationException("Term " + termId + " is not a valid therapist classification");
            }
            resolved.add(term);
        }
        return resolved;
    }

    private void enforceOwnership(Therapist therapist, UUID userId) {
        if (therapist.getUserId() == null || !therapist.getUserId().equals(userId)) {
            throw new AccessDeniedException("Therapist profile does not belong to the current user");
        }
    }

    private TherapistResponseDTO toResponse(Therapist therapist) {
        Map<TaxonomyType, List<TaxonomyTermResponseDTO>> grouped = groupTerms(therapist.getTerms());
        return TherapistResponseDTO.builder()
                .id(therapist.getId())
                .userId(therapist.getUserId())
                .firstName(therapist.getFirstName())
                .lastName(therapist.getLastName())
                .credentials(therapist.getCredentials())
                .pronouns(therapist.getPronouns())
                .photoUrl(therapist.getPhotoUrl())
                .slug(therapist.getSlug())
                .bio(therapist.getBio())
                .yearsOfExperience(therapist.getYearsOfExperience())
                .education(therapist.getEducation())
                .licensure(therapist.getLicensure())
                .serviceDelivery(therapist.getServiceDelivery())
                .availabilityStatus(therapist.getAvailabilityStatus())
                .schedulingUrl(therapist.getSchedulingUrl())
                .bookingPlatformRef(therapist.getBookingPlatformRef())
                .metaTitle(therapist.getMetaTitle())
                .metaDescription(therapist.getMetaDescription())
                .ogImageUrl(therapist.getOgImageUrl())
                .canonicalUrl(therapist.getCanonicalUrl())
                .published(therapist.isPublished())
                .sortOrder(therapist.getSortOrder())
                .focusAreas(grouped.getOrDefault(TaxonomyType.FOCUS_AREA, List.of()))
                .modalities(grouped.getOrDefault(TaxonomyType.MODALITY, List.of()))
                .demographics(grouped.getOrDefault(TaxonomyType.DEMOGRAPHIC, List.of()))
                .build();
    }

    private TherapistSummaryDTO toSummary(Therapist therapist) {
        Map<TaxonomyType, List<TaxonomyTermResponseDTO>> grouped = groupTerms(therapist.getTerms());
        return TherapistSummaryDTO.builder()
                .id(therapist.getId())
                .firstName(therapist.getFirstName())
                .lastName(therapist.getLastName())
                .credentials(therapist.getCredentials())
                .pronouns(therapist.getPronouns())
                .photoUrl(therapist.getPhotoUrl())
                .slug(therapist.getSlug())
                .availabilityStatus(therapist.getAvailabilityStatus())
                .focusAreas(grouped.getOrDefault(TaxonomyType.FOCUS_AREA, List.of()))
                .modalities(grouped.getOrDefault(TaxonomyType.MODALITY, List.of()))
                .demographics(grouped.getOrDefault(TaxonomyType.DEMOGRAPHIC, List.of()))
                .sortOrder(therapist.getSortOrder())
                .build();
    }

    private Map<TaxonomyType, List<TaxonomyTermResponseDTO>> groupTerms(Collection<TaxonomyTerm> terms) {
        if (terms == null || terms.isEmpty()) {
            return Map.of();
        }
        return terms.stream()
                .sorted(Comparator.comparingInt(TaxonomyTerm::getSortOrder).thenComparing(TaxonomyTerm::getLabel))
                .collect(Collectors.groupingBy(
                        TaxonomyTerm::getType,
                        Collectors.mapping(TaxonomyTermResponseDTO::from, Collectors.toList())));
    }
}
