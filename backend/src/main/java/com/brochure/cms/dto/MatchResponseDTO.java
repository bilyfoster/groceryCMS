package com.brochure.cms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for the list of ranked therapist matches produced by the matching engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDTO {

    private List<MatchResultDTO> matches;
}
