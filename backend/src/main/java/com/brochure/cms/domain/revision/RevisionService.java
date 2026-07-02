package com.brochure.cms.domain.revision;

import com.brochure.cms.domain.blog.BlogPost;
import com.brochure.cms.domain.blog.BlogPostRepository;
import com.brochure.cms.domain.page.ContentBlock;
import com.brochure.cms.domain.page.ContentBlockRepository;
import com.brochure.cms.domain.page.Page;
import com.brochure.cms.domain.page.PageRepository;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RevisionService {

    public static final String ENTITY_PAGE = "page";
    public static final String ENTITY_BLOG_POST = "blog_post";

    private final RevisionRepository revisionRepository;
    private final PageRepository pageRepository;
    private final ContentBlockRepository contentBlockRepository;
    private final BlogPostRepository blogPostRepository;

    public RevisionService(
            RevisionRepository revisionRepository,
            PageRepository pageRepository,
            ContentBlockRepository contentBlockRepository,
            BlogPostRepository blogPostRepository) {
        this.revisionRepository = revisionRepository;
        this.pageRepository = pageRepository;
        this.contentBlockRepository = contentBlockRepository;
        this.blogPostRepository = blogPostRepository;
    }

    public void recordPageRevision(Page page, List<ContentBlock> blocks, UUID authorId) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("page", pageSnapshot(page));
        snapshot.put(
                "blocks",
                blocks.stream().map(this::blockSnapshot).toList());
        saveRevision(ENTITY_PAGE, page.getId(), authorId, snapshot);
    }

    public void recordBlogRevision(BlogPost post, UUID authorId) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("post", blogSnapshot(post));
        saveRevision(ENTITY_BLOG_POST, post.getId(), authorId, snapshot);
    }

    @Transactional(readOnly = true)
    public List<RevisionSummaryDto> list(String entityType, UUID entityId) {
        UUID tenantId = TenantIds.current();
        return revisionRepository
                .findByTenantIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(tenantId, entityType, entityId)
                .stream()
                .map(RevisionSummaryDto::from)
                .toList();
    }

    public void restore(UUID revisionId) {
        UUID tenantId = TenantIds.current();
        Revision revision = revisionRepository
                .findByIdAndTenantId(revisionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found"));

        if (ENTITY_PAGE.equals(revision.getEntityType())) {
            restorePage(revision);
        } else if (ENTITY_BLOG_POST.equals(revision.getEntityType())) {
            restoreBlogPost(revision);
        } else {
            throw new ResourceNotFoundException("Unknown entity type");
        }
    }

    @SuppressWarnings("unchecked")
    private void restorePage(Revision revision) {
        Map<String, Object> snapshot = revision.getSnapshot();
        Map<String, Object> pageData = (Map<String, Object>) snapshot.get("page");
        Page page = pageRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(revision.getEntityId(), revision.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        applyPageSnapshot(page, pageData);
        pageRepository.save(page);

        List<ContentBlock> existing =
                contentBlockRepository.findByPageIdOrderBySortOrderAsc(page.getId());
        contentBlockRepository.deleteAll(existing);

        List<Map<String, Object>> blocks = (List<Map<String, Object>>) snapshot.get("blocks");
        if (blocks != null) {
            for (Map<String, Object> blockData : blocks) {
                ContentBlock block = new ContentBlock();
                block.setPageId(page.getId());
                block.setBlockType(
                        com.brochure.cms.enums.BlockType.fromDb((String) blockData.get("blockType")));
                block.setSortOrder(((Number) blockData.get("sortOrder")).intValue());
                block.setContent((Map<String, Object>) blockData.get("content"));
                block.setPublished(Boolean.TRUE.equals(blockData.get("published")));
                contentBlockRepository.save(block);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void restoreBlogPost(Revision revision) {
        Map<String, Object> snapshot = revision.getSnapshot();
        Map<String, Object> postData = (Map<String, Object>) snapshot.get("post");
        BlogPost post = blogPostRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(revision.getEntityId(), revision.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found"));
        if (postData.get("title") != null) {
            post.setTitle((String) postData.get("title"));
        }
        if (postData.get("excerpt") != null) {
            post.setExcerpt((String) postData.get("excerpt"));
        }
        if (postData.get("body") != null) {
            post.setBody((String) postData.get("body"));
        }
        if (postData.get("featuredImage") != null) {
            post.setFeaturedImage((String) postData.get("featuredImage"));
        }
        blogPostRepository.save(post);
    }

    private void saveRevision(String entityType, UUID entityId, UUID authorId, Map<String, Object> snapshot) {
        Revision revision = new Revision();
        revision.setTenantId(TenantIds.current());
        revision.setEntityType(entityType);
        revision.setEntityId(entityId);
        revision.setAuthorId(authorId);
        revision.setSnapshot(snapshot);
        revisionRepository.save(revision);
    }

    private Map<String, Object> pageSnapshot(Page page) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", page.getTitle());
        m.put("layout", page.getLayout());
        m.put("navOrder", page.getNavOrder());
        m.put("metaTitle", page.getMetaTitle());
        m.put("metaDescription", page.getMetaDescription());
        m.put("config", page.getConfig());
        return m;
    }

    private Map<String, Object> blockSnapshot(ContentBlock block) {
        Map<String, Object> m = new HashMap<>();
        m.put("blockType", block.getBlockType().toDb());
        m.put("sortOrder", block.getSortOrder());
        m.put("content", block.getContent());
        m.put("published", block.isPublished());
        return m;
    }

    private Map<String, Object> blogSnapshot(BlogPost post) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", post.getTitle());
        m.put("excerpt", post.getExcerpt());
        m.put("body", post.getBody());
        m.put("featuredImage", post.getFeaturedImage());
        m.put("metaTitle", post.getMetaTitle());
        m.put("metaDescription", post.getMetaDescription());
        return m;
    }

    @SuppressWarnings("unchecked")
    private void applyPageSnapshot(Page page, Map<String, Object> data) {
        if (data.get("title") != null) {
            page.setTitle((String) data.get("title"));
        }
        if (data.get("layout") != null) {
            page.setLayout((String) data.get("layout"));
        }
        if (data.get("navOrder") != null) {
            page.setNavOrder(((Number) data.get("navOrder")).intValue());
        }
        if (data.get("metaTitle") != null) {
            page.setMetaTitle((String) data.get("metaTitle"));
        }
        if (data.get("metaDescription") != null) {
            page.setMetaDescription((String) data.get("metaDescription"));
        }
        if (data.get("config") != null) {
            page.setConfig((Map<String, Object>) data.get("config"));
        }
    }

    public record RevisionSummaryDto(UUID id, UUID authorId, String createdAt) {
        static RevisionSummaryDto from(Revision r) {
            return new RevisionSummaryDto(
                    r.getId(), r.getAuthorId(), r.getCreatedAt().toString());
        }
    }
}
