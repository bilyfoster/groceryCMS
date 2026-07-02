package com.brochure.cms.domain.staff;

import com.brochure.cms.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/staff")
public class AdminStaffController {

    private final StaffService staffService;

    public AdminStaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ApiResponse<List<StaffService.StaffDto>> list(@RequestParam UUID pageId) {
        return ApiResponse.ok(staffService.listAllByPageForAdmin(pageId));
    }

    @PostMapping
    public ApiResponse<StaffService.StaffDto> create(@Valid @RequestBody StaffService.CreateStaffRequest request) {
        return ApiResponse.ok(staffService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StaffService.StaffDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody StaffService.UpdateStaffRequest request) {
        return ApiResponse.ok(staffService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        staffService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/backfill-therapists")
    public ApiResponse<Integer> backfillTherapists() {
        int created = staffService.backfillTherapists();
        return ApiResponse.ok(created, created + " therapist profile(s) created");
    }
}
