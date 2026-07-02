package com.brochure.cms.domain.revision;

import com.brochure.cms.shared.dto.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/revisions")
public class RevisionController {

    private final RevisionService revisionService;

    public RevisionController(RevisionService revisionService) {
        this.revisionService = revisionService;
    }

    @GetMapping
    public ApiResponse<List<RevisionService.RevisionSummaryDto>> list(
            @RequestParam String entityType, @RequestParam UUID entityId) {
        return ApiResponse.ok(revisionService.list(entityType, entityId));
    }

    @PostMapping("/{id}/restore")
    public ApiResponse<Void> restore(@PathVariable UUID id) {
        revisionService.restore(id);
        return ApiResponse.ok(null);
    }
}
