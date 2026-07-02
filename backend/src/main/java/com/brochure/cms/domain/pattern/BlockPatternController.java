package com.brochure.cms.domain.pattern;

import com.brochure.cms.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/block-patterns")
public class BlockPatternController {

    private final BlockPatternService blockPatternService;

    public BlockPatternController(BlockPatternService blockPatternService) {
        this.blockPatternService = blockPatternService;
    }

    @GetMapping
    public ApiResponse<List<BlockPatternService.BlockPatternDto>> list() {
        return ApiResponse.ok(blockPatternService.list());
    }

    @PostMapping
    public ApiResponse<BlockPatternService.BlockPatternDto> create(
            @Valid @RequestBody BlockPatternService.CreatePatternRequest request) {
        return ApiResponse.ok(blockPatternService.create(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        blockPatternService.delete(id);
        return ApiResponse.ok(null);
    }
}
