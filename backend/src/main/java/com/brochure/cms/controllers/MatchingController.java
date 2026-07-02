package com.brochure.cms.controllers;

import com.brochure.cms.dto.IntakeRequestDTO;
import com.brochure.cms.dto.MatchResponseDTO;
import com.brochure.cms.services.MatchingService;
import com.brochure.cms.shared.captcha.HcaptchaService;
import com.brochure.cms.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public matching endpoint. Clients submit intake preferences and receive a ranked,
 * explainable list of therapists. Protected by hCaptcha when configured.
 */
@RestController
@RequestMapping("/api/match")
@Tag(name = "Matching")
public class MatchingController {

    private final MatchingService matchingService;
    private final HcaptchaService hcaptchaService;

    public MatchingController(MatchingService matchingService, HcaptchaService hcaptchaService) {
        this.matchingService = matchingService;
        this.hcaptchaService = hcaptchaService;
    }

    @PostMapping
    @Operation(summary = "Submit intake preferences and receive ranked therapist matches")
    public ApiResponse<MatchResponseDTO> match(
            @Valid @RequestBody IntakeRequestDTO request,
            @RequestParam(value = "captchaToken", required = false) String captchaToken) {
        hcaptchaService.verifyIfConfigured(captchaToken);
        return ApiResponse.ok(matchingService.findMatches(request));
    }
}
