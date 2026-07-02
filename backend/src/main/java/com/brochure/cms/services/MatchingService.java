package com.brochure.cms.services;

import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;

/**
 * Matches client intake preferences against published therapists.
 */
public interface MatchingService {

    /**
     * Finds the default top-N therapist matches for the given intake request.
     *
     * @param request the intake preferences; may be {@code null} or empty
     * @return ranked list of matches
     */
    MatchResponseDTO findMatches(IntakeRequestDTO request);

    /**
     * Finds the top-N therapist matches for the given intake request.
     *
     * @param request the intake preferences; may be {@code null} or empty
     * @param topN    maximum number of results to return
     * @return ranked list of matches
     */
    MatchResponseDTO findMatches(IntakeRequestDTO request, int topN);
}
