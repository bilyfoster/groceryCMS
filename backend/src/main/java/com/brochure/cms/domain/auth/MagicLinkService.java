package com.brochure.cms.domain.auth;

import com.brochure.cms.enums.AuthTokenPurpose;
import com.brochure.cms.enums.UserRole;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.mail.MailService;
import com.brochure.cms.shared.util.TenantIds;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MagicLinkService {

    private static final Logger log = LoggerFactory.getLogger(MagicLinkService.class);
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

    private final AuthTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final JwtService jwtService;

    public MagicLinkService(
            AuthTokenRepository tokenRepository,
            UserRepository userRepository,
            MailService mailService,
            JwtService jwtService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.jwtService = jwtService;
    }

    public void sendMagicLink(String email) {
        UUID tenantId = TenantIds.current();
        String normalized = email.toLowerCase().trim();
        User user = userRepository
                .findByEmailAndTenantIdAndDeletedAtIsNull(normalized, tenantId)
                .orElseGet(() -> createUnverifiedUser(normalized, tenantId));

        tokenRepository.invalidatePurposeTokens(
                user.getId(), AuthTokenPurpose.MAGIC_LINK.toDb());

        byte[] rawBytes = new byte[32];
        new SecureRandom().nextBytes(rawBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes);
        String tokenHash = sha256(rawToken);

        AuthToken token = new AuthToken();
        token.setUserId(user.getId());
        token.setTokenHash(tokenHash);
        token.setPurpose(AuthTokenPurpose.MAGIC_LINK);
        token.setExpiresAt(OffsetDateTime.now().plus(TOKEN_TTL));
        tokenRepository.save(token);

        mailService.sendMagicLink(user.getEmail(), rawToken);
        log.info("Magic link sent to user {}", user.getId());
    }

    public String verifyMagicLink(String rawToken) {
        String tokenHash = sha256(rawToken);
        AuthToken token = tokenRepository
                .findActiveByTokenHashAndPurpose(tokenHash, AuthTokenPurpose.MAGIC_LINK.toDb())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired link"));

        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ValidationException("Magic link has expired");
        }
        if (token.getUsedAt() != null) {
            throw new ValidationException("Magic link has already been used");
        }

        token.setUsedAt(OffsetDateTime.now());
        tokenRepository.save(token);

        User user = userRepository.findById(token.getUserId()).orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);

        return jwtService.generateToken(user);
    }

    private User createUnverifiedUser(String email, UUID tenantId) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(email);
        user.setRole(UserRole.VIEWER);
        user.setEmailVerified(false);
        user.setActive(true);
        return userRepository.save(user);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
