package com.brochure.cms.domain.faq;

import com.brochure.cms.shared.dto.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faq")
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public ApiResponse<List<FaqService.FaqDto>> list(@RequestParam UUID pageId) {
        return ApiResponse.ok(faqService.listByPage(pageId));
    }
}
