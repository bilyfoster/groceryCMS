package com.brochure.cms.domain.blog;

import com.brochure.cms.domain.category.Category;
import com.brochure.cms.domain.category.CategoryRepository;
import com.brochure.cms.domain.category.CategoryService;
import com.brochure.cms.domain.category.PostCategoryRepository;
import com.brochure.cms.domain.revision.RevisionService;
import com.brochure.cms.shared.dto.PagedResponse;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.util.BlockNoteRenderer;
import com.brochure.cms.shared.util.TenantIds;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlogService {

    private static final PolicyFactory HTML_SANITIZER =
            Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(Sanitizers.LINKS).and(Sanitizers.IMAGES);

    private final BlogPostRepository blogPostRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final PostCategoryRepository postCategoryRepository;
    private final RevisionService revisionService;

    public BlogService(
            BlogPostRepository blogPostRepository,
            CategoryRepository categoryRepository,
            CategoryService categoryService,
            PostCategoryRepository postCategoryRepository,
            RevisionService revisionService) {
        this.blogPostRepository = blogPostRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.postCategoryRepository = postCategoryRepository;
        this.revisionService = revisionService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<BlogSummaryDto> listPublished(int page, int size) {
        UUID tenantId = TenantIds.current();
        Page<BlogPost> result =
                blogPostRepository.findPublishedOrdered(tenantId, PageRequest.of(page, size));
        return toPaged(result, page, size);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BlogSummaryDto> listByCategory(String categorySlug, int page, int size) {
        UUID tenantId = TenantIds.current();
        Category category = categoryRepository
                .findByTenantIdAndSlugAndDeletedAtIsNull(tenantId, categorySlug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Page<BlogPost> result = blogPostRepository.findPublishedByCategory(
                tenantId, category.getId(), PageRequest.of(page, size));
        return toPaged(result, page, size);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BlogSummaryDto> listByTag(String tag, int page, int size) {
        UUID tenantId = TenantIds.current();
        Page<BlogPost> result =
                blogPostRepository.findPublishedByTag(tenantId, tag, PageRequest.of(page, size));
        return toPaged(result, page, size);
    }

    @Transactional(readOnly = true)
    public List<BlogSummaryDto> listAllForAdmin() {
        UUID tenantId = TenantIds.current();
        return blogPostRepository.findByTenantIdAndDeletedAtIsNullOrderByUpdatedAtDesc(tenantId).stream()
                .map(BlogSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BlogDetailDto getBySlug(String slug) {
        UUID tenantId = TenantIds.current();
        BlogPost post = blogPostRepository
                .findByTenantIdAndSlugAndDeletedAtIsNull(tenantId, slug)
                .filter(BlogPost::isPublished)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found"));
        return toDetail(post);
    }

    /** Increment and return the view count for a published post. */
    public int incrementView(String slug) {
        BlogPost post = findPublishedBySlug(slug);
        post.setViewCount(post.getViewCount() + 1);
        blogPostRepository.save(post);
        return post.getViewCount();
    }

    /** Apply a like (+1) or unlike (-1) and return the new like count. */
    public int like(String slug, boolean liked) {
        BlogPost post = findPublishedBySlug(slug);
        post.setLikeCount(Math.max(0, post.getLikeCount() + (liked ? 1 : -1)));
        blogPostRepository.save(post);
        return post.getLikeCount();
    }

    private BlogPost findPublishedBySlug(String slug) {
        return blogPostRepository
                .findByTenantIdAndSlugAndDeletedAtIsNull(TenantIds.current(), slug)
                .filter(BlogPost::isPublished)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found"));
    }

    @Transactional(readOnly = true)
    public BlogDetailDto getByIdForAdmin(UUID id) {
        BlogPost post = findForTenant(id);
        return toDetail(post);
    }

    public BlogDetailDto create(CreateBlogRequest request, UUID authorId) {
        UUID tenantId = TenantIds.current();
        BlogPost post = new BlogPost();
        post.setTenantId(tenantId);
        post.setPageId(request.pageId());
        post.setAuthorId(authorId);
        post.setSlug(request.slug());
        post.setTitle(request.title());
        post.setExcerpt(request.excerpt());
        post.setBody(request.body());
        post.setFeaturedImage(request.featuredImage());
        post.setTags(request.tags() != null ? request.tags() : new String[0]);
        post.setMetaTitle(request.metaTitle());
        post.setMetaDescription(request.metaDescription());
        post.setSticky(request.sticky());
        post.setAllowComments(request.allowComments() == null || request.allowComments());
        post.setPublished(false);
        blogPostRepository.save(post);
        if (request.categoryIds() != null) {
            categoryService.assignPostCategories(post.getId(), request.categoryIds());
        }
        return toDetail(post);
    }

    public BlogDetailDto update(UUID id, UpdateBlogRequest request, UUID authorId) {
        BlogPost post = findForTenant(id);
        revisionService.recordBlogRevision(post, authorId);

        if (request.title() != null) {
            post.setTitle(request.title());
        }
        if (request.excerpt() != null) {
            post.setExcerpt(request.excerpt());
        }
        if (request.body() != null) {
            post.setBody(request.body());
        }
        if (request.featuredImage() != null) {
            post.setFeaturedImage(request.featuredImage());
        }
        if (request.tags() != null) {
            post.setTags(request.tags());
        }
        if (request.metaTitle() != null) {
            post.setMetaTitle(request.metaTitle());
        }
        if (request.metaDescription() != null) {
            post.setMetaDescription(request.metaDescription());
        }
        if (request.sticky() != null) {
            post.setSticky(request.sticky());
        }
        if (request.allowComments() != null) {
            post.setAllowComments(request.allowComments());
        }
        blogPostRepository.save(post);
        if (request.categoryIds() != null) {
            categoryService.assignPostCategories(post.getId(), request.categoryIds());
        }
        return toDetail(post);
    }

    public BlogDetailDto setPublished(UUID id, boolean published) {
        BlogPost post = findForTenant(id);
        post.setPublished(published);
        post.setPublishedAt(published ? OffsetDateTime.now() : null);
        blogPostRepository.save(post);
        return toDetail(post);
    }

    public void delete(UUID id) {
        BlogPost post = findForTenant(id);
        post.softDelete();
        blogPostRepository.save(post);
    }

    private BlogPost findForTenant(UUID id) {
        return blogPostRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, TenantIds.current())
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found"));
    }

    private BlogDetailDto toDetail(BlogPost post) {
        List<CategoryService.CategoryDto> categories = categoryService.categoriesForPost(post.getId());
        return BlogDetailDto.from(post, sanitizeBody(post.getBody()), categories);
    }

    private PagedResponse<BlogSummaryDto> toPaged(Page<BlogPost> result, int page, int size) {
        List<BlogSummaryDto> items =
                result.getContent().stream().map(BlogSummaryDto::from).toList();
        return PagedResponse.of(items, page, size, result.getTotalElements());
    }

    String sanitizeBody(String body) {
        if (body == null) {
            return "";
        }
        String html = body.trim().startsWith("{") ? renderBlockNoteJson(body) : body;
        return HTML_SANITIZER.sanitize(html);
    }

    private String renderBlockNoteJson(String json) {
        return BlockNoteRenderer.render(json);
    }

    @Transactional(readOnly = true)
    public String buildRssFeed(String siteBaseUrl) {
        UUID tenantId = TenantIds.current();
        var posts = blogPostRepository
                .findPublishedOrdered(tenantId, PageRequest.of(0, 50))
                .getContent();
        String base = siteBaseUrl != null ? siteBaseUrl.replaceAll("/$", "") : "";
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\"><channel>\n");
        xml.append("<title>Blog</title>\n");
        xml.append("<link>").append(base).append("/blog</link>\n");
        for (BlogPost post : posts) {
            xml.append("<item>\n");
            xml.append("<title>").append(escapeXml(post.getTitle())).append("</title>\n");
            xml.append("<link>")
                    .append(base)
                    .append("/blog/")
                    .append(post.getSlug())
                    .append("</link>\n");
            if (post.getPublishedAt() != null) {
                xml.append("<pubDate>").append(post.getPublishedAt()).append("</pubDate>\n");
            }
            if (post.getExcerpt() != null) {
                xml.append("<description>")
                        .append(escapeXml(post.getExcerpt()))
                        .append("</description>\n");
            }
            xml.append("</item>\n");
        }
        xml.append("</channel></rss>");
        return xml.toString();
    }

    private static String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public record BlogSummaryDto(
            UUID id, String slug, String title, String excerpt, String featuredImage,
            OffsetDateTime publishedAt, boolean sticky, boolean published,
            int viewCount, int likeCount, int readingMinutes) {
        static BlogSummaryDto from(BlogPost post) {
            return new BlogSummaryDto(
                    post.getId(),
                    post.getSlug(),
                    post.getTitle(),
                    post.getExcerpt(),
                    post.getFeaturedImage(),
                    post.getPublishedAt(),
                    post.isSticky(),
                    post.isPublished(),
                    post.getViewCount(),
                    post.getLikeCount(),
                    BlogService.readingMinutes(post.getBody()));
        }
    }

    /** Estimate reading time in minutes from post body (~200 words/min). */
    static int readingMinutes(String body) {
        if (body == null || body.isBlank()) {
            return 1;
        }
        String text = body.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        if (text.isEmpty()) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil(text.split("\\s+").length / 200.0));
    }

    public record BlogDetailDto(
            UUID id,
            String slug,
            String title,
            String excerpt,
            String bodyHtml,
            String featuredImage,
            boolean published,
            boolean sticky,
            boolean allowComments,
            OffsetDateTime publishedAt,
            int viewCount,
            int likeCount,
            int readingMinutes,
            List<String> tags,
            List<CategoryService.CategoryDto> categories,
            String metaTitle,
            String metaDescription) {
        static BlogDetailDto from(
                BlogPost post, String bodyHtml, List<CategoryService.CategoryDto> categories) {
            return new BlogDetailDto(
                    post.getId(),
                    post.getSlug(),
                    post.getTitle(),
                    post.getExcerpt(),
                    bodyHtml,
                    post.getFeaturedImage(),
                    post.isPublished(),
                    post.isSticky(),
                    post.isAllowComments(),
                    post.getPublishedAt(),
                    post.getViewCount(),
                    post.getLikeCount(),
                    BlogService.readingMinutes(post.getBody()),
                    Arrays.asList(post.getTags()),
                    categories,
                    post.getMetaTitle(),
                    post.getMetaDescription());
        }
    }

    public record CreateBlogRequest(
            UUID pageId,
            String slug,
            String title,
            String excerpt,
            String body,
            String featuredImage,
            String[] tags,
            List<UUID> categoryIds,
            Boolean sticky,
            Boolean allowComments,
            String metaTitle,
            String metaDescription) {}

    public record UpdateBlogRequest(
            String title,
            String excerpt,
            String body,
            String featuredImage,
            String[] tags,
            List<UUID> categoryIds,
            Boolean sticky,
            Boolean allowComments,
            String metaTitle,
            String metaDescription) {}
}
