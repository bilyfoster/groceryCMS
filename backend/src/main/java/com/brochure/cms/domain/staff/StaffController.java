package com.brochure.cms.domain.staff;

import com.brochure.cms.shared.dto.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ApiResponse<List<StaffService.StaffDto>> list(@RequestParam UUID pageId) {
        return ApiResponse.ok(staffService.listByPage(pageId));
    }
}
