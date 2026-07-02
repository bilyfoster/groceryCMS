package com.brochure.cms.services;

import com.brochure.cms.dto.TaxonomyTermRequestDTO;
import com.brochure.cms.dto.TaxonomyTermResponseDTO;
import com.brochure.cms.enums.TaxonomyType;
import java.util.List;
import java.util.UUID;

/**
 * Business operations for admin-managed taxonomy terms (focus areas,
 * modalities, demographics). All operations are scoped to the current tenant.
 */
public interface TaxonomyService {

    /**
     * Lists active terms of a type, for public consumption (filters, intake form).
     *
     * @param type the taxonomy type to list
     * @return active terms ordered by sort order then label
     */
    List<TaxonomyTermResponseDTO> listActive(TaxonomyType type);

    /**
     * Lists all terms of a type including inactive ones, for administration.
     *
     * @param type the taxonomy type to list
     * @return all non-deleted terms ordered by sort order then label
     */
    List<TaxonomyTermResponseDTO> listAll(TaxonomyType type);

    /**
     * Creates a new taxonomy term.
     *
     * @param request the term to create
     * @return the created term
     */
    TaxonomyTermResponseDTO create(TaxonomyTermRequestDTO request);

    /**
     * Updates an existing taxonomy term owned by the current tenant.
     *
     * @param id      the term id
     * @param request the new term values
     * @return the updated term
     */
    TaxonomyTermResponseDTO update(UUID id, TaxonomyTermRequestDTO request);

    /**
     * Soft-deletes a taxonomy term owned by the current tenant.
     *
     * @param id the term id
     */
    void delete(UUID id);
}
