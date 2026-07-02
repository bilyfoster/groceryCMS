package com.brochure.cms.shared.dto;

import java.util.List;
import java.util.Map;

public record ErrorResponse(
        String message,
        Map<String, List<String>> fieldErrors) {
}
