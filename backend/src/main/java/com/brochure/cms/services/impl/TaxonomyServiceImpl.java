package com.brochure.cms.services.impl;

import com.brochure.cms.dto.TaxonomyTermRequestDTO;
import com.brochure.cms.dto.TaxonomyTermResponseDTO;
import com.brochure.cms.enums.TaxonomyType;
import com.brochure.cms.models.TaxonomyTerm;
import com.brochure.cms.repositories.TaxonomyTermRepository;
import com.brochure.cms.services.TaxonomyService;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link TaxonomyService} implementation backed by JPA. Every query is
 * scoped to {@link TenantIds#current()} to preserve multi-tenant isolation.
 */
@Service
@Transactional(readOnly = true)
public class TaxonomyServiceImpl implements TaxonomyService {

    private static final Logger log = LoggerFactory.getLogger(TaxonomyServiceImpl.class);

    private final TaxonomyTermRepository taxonomyTermRepository;

    public TaxonomyServiceImpl(TaxonomyTermRepository taxonomyTermRepository) {
        this.taxonomyTermRepository = taxonomyTermRepository;
    }

    @Override
    public List<TaxonomyTermResponseDTO> listActive(TaxonomyType type) {
        UUID tenantId = TenantIds.current();
        return taxonomyTermRepository
                .findByTenantIdAndTypeAndActiveTrueAndDeletedAtIsNullOrderBySortOrderAscLabelAsc(tenantId, type)
                .stream()
                .map(TaxonomyTermResponseDTO::from)
                .toList();
    }

    @Override
    public List<TaxonomyTermResponseDTO> listAll(TaxonomyType type) {
        UUID tenantId = TenantIds.current();
        return taxonomyTermRepository
                .findByTenantIdAndTypeAndDeletedAtIsNullOrderBySortOrderAscLabelAsc(tenantId, type)
                .stream()
                .map(TaxonomyTermResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public TaxonomyTermResponseDTO create(TaxonomyTermRequestDTO request) {
        UUID tenantId = TenantIds.current();
        if (taxonomyTermRepository.existsByTenantIdAndTypeAndSlugAndDeletedAtIsNull(
                tenantId, request.getType(), request.getSlug())) {
            throw new ValidationException(
                    "A %s term with slug '%s' already exists".formatted(request.getType(), request.getSlug()));
        }

        TaxonomyTerm term = TaxonomyTerm.builder()
                .tenantId(tenantId)
                .type(request.getType())
                .label(request.getLabel())
                .slug(request.getSlug())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .active(request.getActive() == null || request.getActive())
                .build();

        TaxonomyTerm saved = taxonomyTermRepository.save(term);
        log.info("Created taxonomy term {} ({} '{}') for tenant {}",
                saved.getId(), saved.getType(), saved.getSlug(), tenantId);
        return TaxonomyTermResponseDTO.from(saved);
    }

    @Override
    @Transactional
    public TaxonomyTermResponseDTO update(UUID id, TaxonomyTermRequestDTO request) {
        UUID tenantId = TenantIds.current();
        TaxonomyTerm term = taxonomyTermRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Taxonomy term not found: " + id));

        term.setType(request.getType());
        term.setLabel(request.getLabel());
        term.setSlug(request.getSlug());
        term.setDescription(request.getDescription());
        term.setSortOrder(request.getSortOrder());
        if (request.getActive() != null) {
            term.setActive(request.getActive());
        }

        TaxonomyTerm saved = taxonomyTermRepository.save(term);
        log.info("Updated taxonomy term {} for tenant {}", id, tenantId);
        return TaxonomyTermResponseDTO.from(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantIds.current();
        TaxonomyTerm term = taxonomyTermRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Taxonomy term not found: " + id));
        term.softDelete();
        taxonomyTermRepository.save(term);
        log.info("Soft-deleted taxonomy term {} for tenant {}", id, tenantId);
    }
}
