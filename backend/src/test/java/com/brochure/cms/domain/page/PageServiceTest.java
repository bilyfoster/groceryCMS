package com.brochure.cms.domain.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.brochure.cms.domain.auth.JwtService;
import com.brochure.cms.domain.revision.RevisionService;
import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.enums.PageType;
import com.brochure.cms.shared.security.TenantContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PageServiceTest {

    @Mock
    private PageRepository pageRepository;

    @Mock
    private ContentBlockRepository contentBlockRepository;

    @Mock
    private RevisionService revisionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PageService pageService;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listPublishedNav_returnsSummaries() {
        Page page = new Page();
        page.setId(UUID.randomUUID());
        page.setTenantId(tenantId);
        page.setSlug("home");
        page.setTitle("Home");
        page.setPageType(PageType.HOME);
        page.setLayout("hero-centered");
        page.setNavOrder(0);
        page.setPublished(true);

        when(pageRepository.findByTenantIdAndDeletedAtIsNullAndPublishedTrueOrderByNavOrderAsc(tenantId))
                .thenReturn(List.of(page));

        List<PageService.PageSummaryDto> result = pageService.listPublishedNav();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).slug()).isEqualTo("home");
    }

    @Test
    void getBySlug_publishedPage_returnsDetail() {
        UUID pageId = UUID.randomUUID();
        Page page = new Page();
        page.setId(pageId);
        page.setTenantId(tenantId);
        page.setSlug("home");
        page.setTitle("Home");
        page.setPageType(PageType.HOME);
        page.setLayout("default");
        page.setPublished(true);

        when(pageRepository.findByTenantIdAndSlugAndDeletedAtIsNull(tenantId, "home"))
                .thenReturn(Optional.of(page));
        when(contentBlockRepository.findByPageIdOrderBySortOrderAsc(pageId)).thenReturn(List.of());

        PageService.PageDetailDto detail = pageService.getBySlug("home", false, null);

        assertThat(detail.slug()).isEqualTo("home");
        assertThat(detail.published()).isTrue();
    }
}
