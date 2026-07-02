package com.brochure.cms.domain.page;

import com.brochure.cms.domain.auth.JwtService;
import com.brochure.cms.domain.revision.RevisionService;
import com.brochure.cms.enums.BlockType;
import com.brochure.cms.enums.PageType;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PageService {

    public static final String PAGE_UNLOCK_COOKIE = "page_unlock";

    private final PageRepository pageRepository;
    private final ContentBlockRepository contentBlockRepository;
    private final RevisionService revisionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public PageService(
            PageRepository pageRepository,
            ContentBlockRepository contentBlockRepository,
            RevisionService revisionService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.pageRepository = pageRepository;
        this.contentBlockRepository = contentBlockRepository;
        this.revisionService = revisionService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public List<PageSummaryDto> listPublishedNav() {
        UUID tenantId = TenantIds.current();
        return pageRepository.findByTenantIdAndDeletedAtIsNullAndPublishedTrueOrderByNavOrderAsc(tenantId).stream()
                .map(PageSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PageSummaryDto> listAllForAdmin() {
        UUID tenantId = TenantIds.current();
        return pageRepository.findByTenantIdAndDeletedAtIsNullOrderByUpdatedAtDesc(tenantId).stream()
                .map(PageSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageDetailDto getById(UUID id) {
        Page page = findForTenant(id);
        List<ContentBlockDto> blocks = contentBlockRepository.findByPageIdOrderBySortOrderAsc(page.getId()).stream()
                .map(ContentBlockDto::from)
                .toList();
        return PageDetailDto.from(page, blocks);
    }

    @Transactional(readOnly = true)
    public PageDetailDto getFrontPage() {
        UUID tenantId = TenantIds.current();
        Page page = pageRepository
                .findByTenantIdAndFrontPageTrueAndDeletedAtIsNullAndPublishedTrue(tenantId)
                .or(() -> pageRepository
                        .findByTenantIdAndSlugAndDeletedAtIsNull(tenantId, "home")
                        .filter(Page::isPublished))
                .orElseThrow(() -> new ResourceNotFoundException("Front page not set"));
        return toDetail(page, false, null);
    }

    @Transactional(readOnly = true)
    public PageDetailDto getNotFoundPage() {
        UUID tenantId = TenantIds.current();
        Page page = pageRepository
                .findByTenantIdAndPageTypeAndDeletedAtIsNullAndPublishedTrue(tenantId, PageType.NOT_FOUND)
                .or(() -> pageRepository
                        .findByTenantIdAndSlugAndDeletedAtIsNull(tenantId, "404")
                        .filter(Page::isPublished))
                .orElseThrow(() -> new ResourceNotFoundException("404 page not configured"));
        return toDetail(page, false, null);
    }

    @Transactional(readOnly = true)
    public ReadingSettingsDto getReadingSettings() {
        UUID tenantId = TenantIds.current();
        UUID frontPageId = pageRepository
                .findByTenantIdAndFrontPageTrueAndDeletedAtIsNull(tenantId)
                .stream()
                .findFirst()
                .map(Page::getId)
                .orElse(null);
        UUID postsPageId = pageRepository
                .findByTenantIdAndPostsPageTrueAndDeletedAtIsNull(tenantId)
                .stream()
                .findFirst()
                .map(Page::getId)
                .orElse(null);
        return new ReadingSettingsDto(frontPageId, postsPageId);
    }

    public ReadingSettingsDto updateReadingSettings(UUID frontPageId, UUID postsPageId) {
        UUID tenantId = TenantIds.current();
        pageRepository.findByTenantIdAndFrontPageTrueAndDeletedAtIsNull(tenantId).forEach(p -> {
            p.setFrontPage(false);
            pageRepository.save(p);
        });
        pageRepository.findByTenantIdAndPostsPageTrueAndDeletedAtIsNull(tenantId).forEach(p -> {
            p.setPostsPage(false);
            pageRepository.save(p);
        });
        if (frontPageId != null) {
            Page front = findForTenant(frontPageId);
            front.setFrontPage(true);
            pageRepository.save(front);
        }
        if (postsPageId != null) {
            Page posts = findForTenant(postsPageId);
            posts.setPostsPage(true);
            pageRepository.save(posts);
        }
        return getReadingSettings();
    }

    @Transactional(readOnly = true)
    public PageDetailDto getBySlug(String slug, boolean includeUnpublished, String unlockToken) {
        UUID tenantId = TenantIds.current();
        Page page = pageRepository
                .findByTenantIdAndSlugAndDeletedAtIsNull(tenantId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        if (!includeUnpublished && !page.isPublished()) {
            throw new ResourceNotFoundException("Page not found");
        }
        return toDetail(page, includeUnpublished, unlockToken);
    }

    public String unlockPage(String slug, String password) {
        UUID tenantId = TenantIds.current();
        Page page = pageRepository
                .findByTenantIdAndSlugAndDeletedAtIsNull(tenantId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        if (page.getPagePassword() == null || page.getPagePassword().isBlank()) {
            throw new ValidationException("Page is not password protected");
        }
        if (!passwordEncoder.matches(password, page.getPagePassword())) {
            throw new BadCredentialsException("Incorrect password");
        }
        return jwtService.generatePageUnlockToken(page.getId(), tenantId);
    }

    private PageDetailDto toDetail(Page page, boolean includeUnpublished, String unlockToken) {
        boolean access = hasPageAccess(page, unlockToken);
        List<ContentBlockDto> blocks = access
                ? contentBlockRepository.findByPageIdOrderBySortOrderAsc(page.getId()).stream()
                        .filter(b -> includeUnpublished || b.isPublished())
                        .map(ContentBlockDto::from)
                        .toList()
                : List.of();
        return PageDetailDto.from(page, blocks);
    }

    private boolean hasPageAccess(Page page, String unlockToken) {
        if (page.getPagePassword() == null || page.getPagePassword().isBlank()) {
            return true;
        }
        if (unlockToken == null || unlockToken.isBlank()) {
            return false;
        }
        return jwtService
                .parsePageUnlockToken(unlockToken, page.getTenantId())
                .map(pageId -> pageId.equals(page.getId()))
                .orElse(false);
    }

    public PageDetailDto create(CreatePageRequest request) {
        UUID tenantId = TenantIds.current();
        Page page = new Page();
        page.setTenantId(tenantId);
        page.setSlug(request.slug());
        page.setTitle(request.title());
        page.setPageType(request.pageType());
        page.setLayout(request.layout() != null ? request.layout() : "default");
        page.setNavOrder(request.navOrder());
        page.setPublished(false);
        page.setMetaTitle(request.metaTitle());
        page.setMetaDescription(request.metaDescription());
        page.setConfig(request.config() != null ? request.config() : Map.of());
        pageRepository.save(page);
        return PageDetailDto.from(page, List.of());
    }

    public PageDetailDto update(UUID id, UpdatePageRequest request, UUID authorId) {
        Page page = findForTenant(id);
        List<ContentBlock> existingBlocks =
                contentBlockRepository.findByPageIdOrderBySortOrderAsc(page.getId());
        revisionService.recordPageRevision(page, existingBlocks, authorId);

        if (request.title() != null) {
            page.setTitle(request.title());
        }
        if (request.layout() != null) {
            page.setLayout(request.layout());
        }
        if (request.navOrder() != null) {
            page.setNavOrder(request.navOrder());
        }
        if (request.metaTitle() != null) {
            page.setMetaTitle(request.metaTitle());
        }
        if (request.metaDescription() != null) {
            page.setMetaDescription(request.metaDescription());
        }
        if (request.config() != null) {
            page.setConfig(request.config());
        }
        if (request.pagePassword() != null) {
            if (request.pagePassword().isBlank()) {
                page.setPagePassword(null);
            } else {
                page.setPagePassword(passwordEncoder.encode(request.pagePassword()));
            }
        }
        pageRepository.save(page);
        return getById(page.getId());
    }

    public PageDetailDto setPublished(UUID id, boolean published) {
        Page page = findForTenant(id);
        page.setPublished(published);
        pageRepository.save(page);
        return getById(page.getId());
    }

    public void delete(UUID id) {
        Page page = findForTenant(id);
        page.softDelete();
        pageRepository.save(page);
    }

    public ContentBlockDto createBlock(UUID pageId, CreateBlockRequest request) {
        Page page = findForTenant(pageId);
        int nextOrder = contentBlockRepository.findByPageIdOrderBySortOrderAsc(page.getId()).size();
        ContentBlock block = new ContentBlock();
        block.setPageId(page.getId());
        block.setBlockType(request.blockType());
        block.setSortOrder(request.sortOrder() != null ? request.sortOrder() : nextOrder);
        block.setContent(request.content() != null ? request.content() : Map.of());
        block.setPublished(request.published() == null || request.published());
        contentBlockRepository.save(block);
        return ContentBlockDto.from(block);
    }

    public ContentBlockDto updateBlock(UUID pageId, UUID blockId, UpdateBlockRequest request) {
        findForTenant(pageId);
        ContentBlock block = contentBlockRepository
                .findByIdAndPageId(blockId, pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found"));
        if (request.blockType() != null) {
            block.setBlockType(request.blockType());
        }
        if (request.content() != null) {
            block.setContent(request.content());
        }
        if (request.published() != null) {
            block.setPublished(request.published());
        }
        if (request.sortOrder() != null) {
            block.setSortOrder(request.sortOrder());
        }
        contentBlockRepository.save(block);
        return ContentBlockDto.from(block);
    }

    public void deleteBlock(UUID pageId, UUID blockId) {
        findForTenant(pageId);
        ContentBlock block = contentBlockRepository
                .findByIdAndPageId(blockId, pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found"));
        contentBlockRepository.delete(block);
    }

    public List<ContentBlockDto> reorderBlocks(UUID pageId, List<UUID> orderedIds) {
        Page page = findForTenant(pageId);
        List<ContentBlock> blocks = contentBlockRepository.findByPageIdOrderBySortOrderAsc(page.getId());
        for (int i = 0; i < orderedIds.size(); i++) {
            final int sortOrder = i;
            UUID blockId = orderedIds.get(i);
            blocks.stream()
                    .filter(b -> b.getId().equals(blockId))
                    .findFirst()
                    .ifPresent(b -> b.setSortOrder(sortOrder));
        }
        contentBlockRepository.saveAll(blocks);
        return contentBlockRepository.findByPageIdOrderBySortOrderAsc(page.getId()).stream()
                .map(ContentBlockDto::from)
                .toList();
    }

    private Page findForTenant(UUID id) {
        UUID tenantId = TenantIds.current();
        return pageRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
    }

    public record PageSummaryDto(
            UUID id,
            String slug,
            String title,
            String pageType,
            String layout,
            Integer navOrder,
            boolean published,
            boolean frontPage,
            boolean postsPage) {
        static PageSummaryDto from(Page page) {
            return new PageSummaryDto(
                    page.getId(),
                    page.getSlug(),
                    page.getTitle(),
                    page.getPageType().toDb(),
                    page.getLayout(),
                    page.getNavOrder(),
                    page.isPublished(),
                    page.isFrontPage(),
                    page.isPostsPage());
        }
    }

    public record ReadingSettingsDto(UUID frontPageId, UUID postsPageId) {}

    public record ContentBlockDto(UUID id, String blockType, int sortOrder, Map<String, Object> content, boolean published) {
        static ContentBlockDto from(ContentBlock block) {
            return new ContentBlockDto(
                    block.getId(),
                    block.getBlockType().toDb(),
                    block.getSortOrder(),
                    block.getContent(),
                    block.isPublished());
        }
    }

    public record PageDetailDto(
            UUID id,
            String slug,
            String title,
            String pageType,
            String layout,
            Integer navOrder,
            boolean published,
            boolean frontPage,
            boolean postsPage,
            boolean passwordProtected,
            String metaTitle,
            String metaDescription,
            String ogImageUrl,
            Map<String, Object> config,
            List<ContentBlockDto> blocks) {
        static PageDetailDto from(Page page, List<ContentBlockDto> blocks) {
            return new PageDetailDto(
                    page.getId(),
                    page.getSlug(),
                    page.getTitle(),
                    page.getPageType().toDb(),
                    page.getLayout(),
                    page.getNavOrder(),
                    page.isPublished(),
                    page.isFrontPage(),
                    page.isPostsPage(),
                    page.getPagePassword() != null && !page.getPagePassword().isBlank(),
                    page.getMetaTitle(),
                    page.getMetaDescription(),
                    page.getOgImageUrl(),
                    page.getConfig(),
                    blocks);
        }
    }

    public record CreatePageRequest(
            String slug,
            String title,
            PageType pageType,
            String layout,
            Integer navOrder,
            String metaTitle,
            String metaDescription,
            Map<String, Object> config) {}

    public record UpdatePageRequest(
            String title,
            String layout,
            Integer navOrder,
            String metaTitle,
            String metaDescription,
            Map<String, Object> config,
            String pagePassword) {}

    public record CreateBlockRequest(
            BlockType blockType, Map<String, Object> content, Integer sortOrder, Boolean published) {}

    public record UpdateBlockRequest(
            BlockType blockType, Map<String, Object> content, Integer sortOrder, Boolean published) {}
}
