package com.brochure.cms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for the list of ranked product recommendations produced by the
 * recommendation engine, plus an optional allergy-awareness note.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDTO {

    private List<MatchResultDTO> matches;
    private List<String> awarenessNotes;
}
