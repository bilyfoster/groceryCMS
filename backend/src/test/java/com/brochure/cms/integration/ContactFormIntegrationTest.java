package com.brochure.cms.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.domain.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.brochure.cms.shared.mail.MailService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContactFormIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @MockBean
    private MailService mailService;

    @BeforeEach
    void seed() {
        Tenant tenant = new Tenant();
        tenant.setSlug("demo");
        tenant.setName("Demo");
        tenant.setActive(true);
        tenantRepository.save(tenant);
    }

    @Test
    void submitContact_validPayload_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .header("X-Tenant-Slug", "demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "name": "Jane Doe",
                                  "email": "jane@example.com",
                                  "message": "Hello there"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Jane Doe"));
    }
}
