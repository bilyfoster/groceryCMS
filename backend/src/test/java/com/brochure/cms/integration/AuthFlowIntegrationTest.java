package com.brochure.cms.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brochure.cms.domain.auth.User;
import com.brochure.cms.domain.auth.UserRepository;
import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.domain.tenant.TenantRepository;
import com.brochure.cms.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant tenant;

    @BeforeEach
    void seed() {
        tenant = new Tenant();
        tenant.setSlug("demo");
        tenant.setName("Demo");
        tenant.setDomain("localhost");
        tenant.setActive(true);
        tenant = tenantRepository.save(tenant);

        User user = new User();
        user.setTenantId(tenant.getId());
        user.setEmail("admin@demo.local");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setDisplayName("Admin");
        user.setRole(UserRole.ADMIN);
        user.setEmailVerified(true);
        user.setActive(true);
        userRepository.save(user);
    }

    @Test
    void login_validCredentials_setsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Tenant-Slug", "demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@demo.local\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("admin@demo.local"));
    }

    @Test
    void me_withoutAuth_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("X-Tenant-Slug", "demo"))
                .andExpect(status().isForbidden());
    }
}
