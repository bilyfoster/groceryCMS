package com.brochure.cms.domain.contact;

import com.brochure.cms.shared.captcha.HcaptchaService;
import com.brochure.cms.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;
    private final HcaptchaService hcaptchaService;

    public ContactController(ContactService contactService, HcaptchaService hcaptchaService) {
        this.contactService = contactService;
        this.hcaptchaService = hcaptchaService;
    }

    @PostMapping
    public ApiResponse<ContactService.ContactDto> submit(
            @Valid @RequestBody SubmitContactRequest request, HttpServletRequest httpRequest) {
        hcaptchaService.verifyIfConfigured(request.captchaToken());
        return ApiResponse.ok(contactService.submit(request.toService(), httpRequest.getRemoteAddr()));
    }

    public record SubmitContactRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            String phone,
            String subject,
            @NotBlank String message,
            String captchaToken) {
        ContactService.SubmitContactRequest toService() {
            return new ContactService.SubmitContactRequest(name, email, phone, subject, message);
        }
    }
}
