package com.brochure.cms.domain.gallery;

import com.brochure.cms.shared.util.TenantIds;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GalleryService {

    private final GalleryRepository galleryRepository;

    public GalleryService(GalleryRepository galleryRepository) {
        this.galleryRepository = galleryRepository;
    }

    public List<GalleryDto> listByPage(UUID pageId) {
        UUID tenantId = TenantIds.current();
        return galleryRepository
                .findByTenantIdAndPageIdAndDeletedAtIsNullAndPublishedTrueOrderBySortOrderAsc(tenantId, pageId)
                .stream()
                .map(GalleryDto::from)
                .toList();
    }

    public record GalleryDto(UUID id, String url, String altText, String caption, int sortOrder) {
        static GalleryDto from(GalleryImage image) {
            return new GalleryDto(
                    image.getId(), image.getUrl(), image.getAltText(), image.getCaption(), image.getSortOrder());
        }
    }
}
