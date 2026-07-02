package com.brochure.cms.controllers;

import com.brochure.cms.dto.IntakeQuestionnaireDTO;
import com.brochure.cms.dto.MatchResponseDTO;
import com.brochure.cms.services.IntakeService;
import com.brochure.cms.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public allergy-awareness intake questionnaire and anonymous product recommendation endpoint.
 */
@RestController
@RequestMapping("/api/intake")
@Tag(name = "Intake")
public class IntakeController {

    private final IntakeService intakeService;

    public IntakeController(IntakeService intakeService) {
        this.intakeService = intakeService;
    }

    @GetMapping("/questionnaire")
    public ApiResponse<IntakeQuestionnaireDTO.Questionnaire> questionnaire() {
        return ApiResponse.ok(intakeService.getQuestionnaire());
    }

    @PostMapping("/match")
    public ApiResponse<MatchResponseDTO> match(@Valid @RequestBody IntakeQuestionnaireDTO.MatchRequest request) {
        return ApiResponse.ok(intakeService.match(request));
    }
}
