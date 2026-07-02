package com.brochure.cms.domain.contact;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.mail.MailService;
import com.brochure.cms.shared.security.TenantContext;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ContactService contactService;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setSlug("demo");
        tenant.setSettings(new HashMap<>());
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void checkRateLimit_exceedsMax_throwsValidationException() {
        String ip = "192.168.1.100";
        for (int i = 0; i < 5; i++) {
            contactService.checkRateLimit(ip);
        }

        assertThatThrownBy(() -> contactService.checkRateLimit(ip)).isInstanceOf(ValidationException.class);
    }

    @Test
    void submit_validRequest_savesSubmission() {
        when(contactRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContactService.ContactDto dto = contactService.submit(
                new ContactService.SubmitContactRequest(
                        "Jane", "jane@example.com", null, "Hello", "Message body"),
                "10.0.0.1");

        org.assertj.core.api.Assertions.assertThat(dto.name()).isEqualTo("Jane");
    }
}
