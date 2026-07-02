package com.brochure.cms.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.enums.UserRole;
import com.brochure.cms.shared.security.TenantContext;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setSlug("demo");
        TenantContext.set(tenant);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void login_validCredentials_returnsToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setTenantId(tenantId);
        user.setEmail("admin@demo.local");
        user.setPasswordHash("$2a$hash");
        user.setRole(UserRole.ADMIN);
        user.setActive(true);

        when(userRepository.findByEmailAndTenantIdAndDeletedAtIsNull("admin@demo.local", tenantId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "$2a$hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthService.LoginResult result = authService.login("admin@demo.local", "password");

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().email()).isEqualTo("admin@demo.local");
    }

    @Test
    void login_invalidPassword_throwsBadCredentials() {
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail("admin@demo.local");
        user.setPasswordHash("$2a$hash");
        user.setActive(true);

        when(userRepository.findByEmailAndTenantIdAndDeletedAtIsNull(any(), any()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.login("admin@demo.local", "wrong"))
                .isInstanceOf(BadCredentialsException.class);
    }
}
