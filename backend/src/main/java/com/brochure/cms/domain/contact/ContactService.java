package com.brochure.cms.domain.contact;

import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.mail.MailService;
import com.brochure.cms.shared.security.TenantContext;
import com.brochure.cms.shared.util.TenantIds;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);
    private static final int MAX_PER_HOUR = 5;

    private final ContactRepository contactRepository;
    private final MailService mailService;
    private final Map<String, RateBucket> rateLimits = new ConcurrentHashMap<>();

    public ContactService(ContactRepository contactRepository, MailService mailService) {
        this.contactRepository = contactRepository;
        this.mailService = mailService;
    }

    public ContactDto submit(SubmitContactRequest request, String ipAddress) {
        checkRateLimit(ipAddress);
        UUID tenantId = TenantIds.current();
        ContactSubmission submission = new ContactSubmission();
        submission.setTenantId(tenantId);
        submission.setName(request.name());
        submission.setEmail(request.email());
        submission.setPhone(request.phone());
        submission.setSubject(request.subject());
        submission.setMessage(request.message());
        submission.setIpAddress(ipAddress);
        contactRepository.save(submission);

        Tenant tenant = TenantContext.get();
        String notifyEmail = tenant != null && tenant.getSettings().get("contactEmail") instanceof String contactEmail
                ? contactEmail
                : "admin@demo.local";
        try {
            mailService.sendContactNotification(notifyEmail, submission);
        } catch (RuntimeException e) {
            log.warn("Contact notification email failed: {}", e.getMessage());
        }
        return ContactDto.from(submission);
    }

    @Transactional(readOnly = true)
    public List<ContactDto> listAll() {
        UUID tenantId = TenantIds.current();
        return contactRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(ContactDto::from)
                .toList();
    }

    public ContactDto markRead(UUID id) {
        ContactSubmission submission = findForTenant(id);
        submission.setReadAt(OffsetDateTime.now());
        contactRepository.save(submission);
        return ContactDto.from(submission);
    }

    private ContactSubmission findForTenant(UUID id) {
        return contactRepository
                .findByIdAndTenantId(id, TenantIds.current())
                .orElseThrow(() -> new ResourceNotFoundException("Contact submission not found"));
    }

    void checkRateLimit(String ipAddress) {
        String key = ipAddress != null ? ipAddress : "unknown";
        long windowStart = System.currentTimeMillis() - 3_600_000L;
        RateBucket bucket = rateLimits.computeIfAbsent(key, k -> new RateBucket());
        synchronized (bucket) {
            if (bucket.windowStart < windowStart) {
                bucket.windowStart = System.currentTimeMillis();
                bucket.count.set(0);
            }
            if (bucket.count.incrementAndGet() > MAX_PER_HOUR) {
                throw new ValidationException("Too many contact form submissions. Please try again later.");
            }
        }
    }

    private static final class RateBucket {
        volatile long windowStart = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);
    }

    public record ContactDto(
            UUID id,
            String name,
            String email,
            String phone,
            String subject,
            String message,
            OffsetDateTime readAt,
            OffsetDateTime createdAt) {
        static ContactDto from(ContactSubmission submission) {
            return new ContactDto(
                    submission.getId(),
                    submission.getName(),
                    submission.getEmail(),
                    submission.getPhone(),
                    submission.getSubject(),
                    submission.getMessage(),
                    submission.getReadAt(),
                    submission.getCreatedAt());
        }
    }

    public record SubmitContactRequest(
            String name, String email, String phone, String subject, String message) {}
}
