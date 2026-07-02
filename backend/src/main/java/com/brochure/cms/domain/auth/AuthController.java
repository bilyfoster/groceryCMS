package com.brochure.cms.domain.auth;

import com.brochure.cms.shared.dto.ApiResponse;
import com.brochure.cms.shared.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final MagicLinkService magicLinkService;

    public AuthController(AuthService authService, MagicLinkService magicLinkService) {
        this.authService = authService;
        this.magicLinkService = magicLinkService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthService.UserDto> login(
            @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(request.email(), request.password());
        setAuthCookie(response, result.token());
        return ApiResponse.ok(result.user());
    }

    @PostMapping("/magic-link")
    public ApiResponse<Void> magicLink(@Valid @RequestBody MagicLinkRequest request) {
        magicLinkService.sendMagicLink(request.email());
        return ApiResponse.ok(null, "If an account exists, a login link has been sent");
    }

    @GetMapping("/verify")
    public ApiResponse<AuthService.UserDto> verify(
            @RequestParam String token, HttpServletResponse response) {
        String jwt = magicLinkService.verifyMagicLink(token);
        setAuthCookie(response, jwt);
        return ApiResponse.ok(null, "Authenticated");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        clearAuthCookie(response);
        return ApiResponse.ok(null, "Logged out");
    }

    @GetMapping("/me")
    public ApiResponse<AuthService.UserDto> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ApiResponse.error("Not authenticated", null);
        }
        return ApiResponse.ok(authService.currentUser(user));
    }

    private void setAuthCookie(HttpServletResponse response, String jwt) {
        ResponseCookie cookie = ResponseCookie.from(JwtAuthFilter.AUTH_COOKIE, jwt)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(60L * 60 * 24 * 7)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(JwtAuthFilter.AUTH_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record MagicLinkRequest(@NotBlank @Email String email) {}
}
