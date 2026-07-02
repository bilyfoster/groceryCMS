package com.brochure.cms.domain.search;

import com.brochure.cms.domain.blog.BlogPost;
import com.brochure.cms.domain.blog.BlogPostRepository;
import com.brochure.cms.domain.page.Page;
import com.brochure.cms.domain.page.PageRepository;
import com.brochure.cms.shared.util.TenantIds;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private final PageRepository pageRepository;
    private final BlogPostRepository blogPostRepository;

    public SearchService(PageRepository pageRepository, BlogPostRepository blogPostRepository) {
        this.pageRepository = pageRepository;
        this.blogPostRepository = blogPostRepository;
    }

    public SearchResultsDto search(String query) {
        if (query == null || query.isBlank()) {
            return new SearchResultsDto(List.of(), List.of());
        }
        String q = query.trim();
        UUID tenantId = TenantIds.current();
        List<SearchHitDto> pages = pageRepository.searchPublished(tenantId, q).stream()
                .map(SearchService::pageHit)
                .toList();
        List<SearchHitDto> posts = blogPostRepository.searchPublished(tenantId, q).stream()
                .map(SearchService::postHit)
                .toList();
        return new SearchResultsDto(pages, posts);
    }

    private static SearchHitDto pageHit(Page page) {
        String href = page.isFrontPage() ? "/" : "/" + page.getSlug();
        return new SearchHitDto("page", page.getId(), page.getTitle(), href, page.getMetaDescription());
    }

    private static SearchHitDto postHit(BlogPost post) {
        return new SearchHitDto(
                "post",
                post.getId(),
                post.getTitle(),
                "/blog/" + post.getSlug(),
                post.getExcerpt());
    }

    public record SearchHitDto(String type, UUID id, String title, String href, String excerpt) {}

    public record SearchResultsDto(List<SearchHitDto> pages, List<SearchHitDto> posts) {}
}
