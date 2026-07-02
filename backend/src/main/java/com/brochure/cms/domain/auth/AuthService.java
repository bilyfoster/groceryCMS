package com.brochure.cms.domain.auth;

import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResult login(String email, String password) {
        UUID tenantId = TenantIds.current();
        User user = userRepository
                .findByEmailAndTenantIdAndDeletedAtIsNull(email.toLowerCase().trim(), tenantId)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        log.info("User {} logged in", user.getId());
        return new LoginResult(token, UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public UserDto currentUser(User user) {
        return UserDto.from(user);
    }

    public record LoginResult(String token, UserDto user) {}

    public record UserDto(UUID id, String email, String displayName, String role, UUID tenantId) {
        static UserDto from(User user) {
            return new UserDto(
                    user.getId(),
                    user.getEmail(),
                    user.getDisplayName(),
                    user.getRole().toDb(),
                    user.getTenantId());
        }
    }
}
