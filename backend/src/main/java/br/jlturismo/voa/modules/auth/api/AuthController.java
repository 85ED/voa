package br.jlturismo.voa.modules.auth.api;

import br.jlturismo.voa.modules.auth.application.AuthCookies;
import br.jlturismo.voa.modules.auth.application.AuthService;
import br.jlturismo.voa.modules.auth.application.PasswordResetService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request.email(), request.password(), response);
        return ResponseEntity.ok(Map.of(
                "user", result.user(),
                "expiresIn", result.expiresIn()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        String raw = readCookie(request, AuthCookies.REFRESH);
        if (raw == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthService.AuthResult result = authService.refresh(raw, response);
        return ResponseEntity.ok(Map.of(
                "user", result.user(),
                "expiresIn", result.expiresIn()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        authService.logout(readCookie(request, AuthCookies.REFRESH), response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AuthService.UserView me(@AuthenticationPrincipal VoaUserPrincipal principal) {
        return authService.me(principal.getId());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        PasswordResetService.ForgotPasswordResult result = passwordResetService.requestReset(request.email());
        if (result.devResetUrl() != null) {
            return ResponseEntity.ok(Map.of(
                    "message", result.message(),
                    "devResetUrl", result.devResetUrl()));
        }
        return ResponseEntity.ok(Map.of("message", result.message()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Senha atualizada. Você já pode entrar."));
    }

    private static String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    public record ForgotPasswordRequest(@NotBlank @Email String email) {}

    public record ResetPasswordRequest(@NotBlank String token, @NotBlank String newPassword) {}
}
