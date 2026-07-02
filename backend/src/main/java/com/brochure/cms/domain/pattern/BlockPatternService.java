package com.brochure.cms.domain.pattern;

import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlockPatternService {

    private final BlockPatternRepository blockPatternRepository;

    public BlockPatternService(BlockPatternRepository blockPatternRepository) {
        this.blockPatternRepository = blockPatternRepository;
    }

    @Transactional(readOnly = true)
    public List<BlockPatternDto> list() {
        UUID tenantId = TenantIds.current();
        return blockPatternRepository.findAvailableForTenant(tenantId).stream()
                .map(BlockPatternDto::from)
                .toList();
    }

    public BlockPatternDto create(CreatePatternRequest request) {
        UUID tenantId = TenantIds.current();
        BlockPattern pattern = new BlockPattern();
        pattern.setTenantId(tenantId);
        pattern.setName(request.name());
        pattern.setCategory(request.category());
        pattern.setThumbnailUrl(request.thumbnailUrl());
        pattern.setBlocks(request.blocks());
        pattern.setSystem(false);
        blockPatternRepository.save(pattern);
        return BlockPatternDto.from(pattern);
    }

    public void delete(UUID id) {
        UUID tenantId = TenantIds.current();
        BlockPattern pattern = blockPatternRepository
                .findByIdAndTenantIdAndDeletedAtIsNullAndSystemFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Pattern not found"));
        pattern.softDelete();
        blockPatternRepository.save(pattern);
    }

    public record BlockPatternDto(
            UUID id,
            String name,
            String category,
            String thumbnailUrl,
            List<Map<String, Object>> blocks,
            boolean system) {
        static BlockPatternDto from(BlockPattern p) {
            return new BlockPatternDto(
                    p.getId(), p.getName(), p.getCategory(), p.getThumbnailUrl(), p.getBlocks(), p.isSystem());
        }
    }

    public record CreatePatternRequest(
            String name, String category, String thumbnailUrl, List<Map<String, Object>> blocks) {}
}
