package com.brochure.cms.services;

import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;

/**
 * Matches shopper intake preferences against published grocery products.
 */
public interface MatchingService {

    /**
     * Finds the default top-N product recommendations for the given intake request.
     *
     * @param request the shopper preferences; may be {@code null} or empty
     * @return ranked list of product recommendations
     */
    MatchResponseDTO findMatches(IntakeRequestDTO request);

    /**
     * Finds the top-N product recommendations for the given intake request.
     *
     * @param request the shopper preferences; may be {@code null} or empty
     * @param topN    maximum number of results to return
     * @return ranked list of product recommendations
     */
    MatchResponseDTO findMatches(IntakeRequestDTO request, int topN);
}
