package com.brochure.cms.dto;

import com.brochure.cms.enums.AvailabilityStatus;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single ranked therapist result returned by the matching engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultDTO {

    private UUID therapistId;
    private String slug;
    private String firstName;
    private String lastName;
    private String credentials;
    private String photoUrl;
    private AvailabilityStatus availabilityStatus;
    private double score;
    private int rank;
    private List<String> explanations;
}
