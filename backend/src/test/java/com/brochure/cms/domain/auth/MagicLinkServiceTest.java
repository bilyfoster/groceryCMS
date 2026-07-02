package com.brochure.cms.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.enums.AuthTokenPurpose;
import com.brochure.cms.enums.UserRole;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.mail.MailService;
import com.brochure.cms.shared.security.TenantContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MagicLinkServiceTest {

    @Mock
    private AuthTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailService mailService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private MagicLinkService magicLinkService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

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
    void verifyMagicLink_expiredToken_throwsValidationException() {
        String raw = "raw-token";
        AuthToken token = new AuthToken();
        token.setUserId(userId);
        token.setExpiresAt(OffsetDateTime.now().minusMinutes(1));

        when(tokenRepository.findActiveByTokenHashAndPurpose(
                        sha256(raw), AuthTokenPurpose.MAGIC_LINK.toDb()))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> magicLinkService.verifyMagicLink(raw)).isInstanceOf(ValidationException.class);
    }

    @Test
    void verifyMagicLink_validToken_returnsJwt() {
        String raw = "valid-token";
        AuthToken token = new AuthToken();
        token.setUserId(userId);
        token.setExpiresAt(OffsetDateTime.now().plusMinutes(10));

        User user = new User();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setEmail("user@demo.local");
        user.setRole(UserRole.VIEWER);

        when(tokenRepository.findActiveByTokenHashAndPurpose(
                        sha256(raw), AuthTokenPurpose.MAGIC_LINK.toDb()))
                .thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt");

        String jwt = magicLinkService.verifyMagicLink(raw);

        assertThat(jwt).isEqualTo("jwt");
        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    void sendMagicLink_existingUser_sendsEmail() {
        User user = new User();
        user.setId(userId);
        user.setEmail("user@demo.local");
        user.setTenantId(tenantId);

        when(userRepository.findByEmailAndTenantIdAndDeletedAtIsNull("user@demo.local", tenantId))
                .thenReturn(Optional.of(user));

        magicLinkService.sendMagicLink("user@demo.local");

        verify(tokenRepository).invalidatePurposeTokens(userId, AuthTokenPurpose.MAGIC_LINK.toDb());
        verify(mailService).sendMagicLink(eq("user@demo.local"), any());
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
