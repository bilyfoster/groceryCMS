package com.brochure.cms.domain.faq;

import com.brochure.cms.shared.util.TenantIds;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public List<FaqDto> listByPage(UUID pageId) {
        UUID tenantId = TenantIds.current();
        return faqRepository
                .findByTenantIdAndPageIdAndDeletedAtIsNullAndPublishedTrueOrderBySortOrderAsc(tenantId, pageId)
                .stream()
                .map(FaqDto::from)
                .toList();
    }

    public record FaqDto(UUID id, String question, String answer, int sortOrder) {
        static FaqDto from(FaqItem item) {
            return new FaqDto(item.getId(), item.getQuestion(), item.getAnswer(), item.getSortOrder());
        }
    }
}
