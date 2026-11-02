package br.jlturismo.voa.modules.auth.application;

import br.jlturismo.voa.modules.auth.domain.PapelUsuario;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioEntity;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioRepository;
import br.jlturismo.voa.modules.core.config.VoaProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final VoaProperties properties;

    public AuthService(
            UsuarioRepository usuarioRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder,
            VoaProperties properties) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String password, HttpServletResponse response) {
        UsuarioEntity user = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!user.isAtivo() || user.getDeletedAt() != null) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        if (!passwordEncoder.matches(password, user.getSenhaHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        return issueTokens(user, response);
    }

    @Transactional
    public AuthResult refresh(String rawRefresh, HttpServletResponse response) {
        UUID userId = refreshTokenService.rotate(rawRefresh);
        UsuarioEntity user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return issueTokens(user, response);
    }

    @Transactional
    public void logout(String rawRefresh, HttpServletResponse response) {
        if (rawRefresh != null) {
            refreshTokenService.revokeByRaw(rawRefresh);
        }
        clearCookies(response);
    }

    @Transactional(readOnly = true)
    public UserView me(UUID userId) {
        UsuarioEntity user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return toView(user);
    }

    private AuthResult issueTokens(UsuarioEntity user, HttpServletResponse response) {
        String access = jwtService.createAccessToken(user.getId(), user.getOrganizacaoId(), user.getPapeis());
        String refresh = refreshTokenService.issue(user.getId());
        writeCookies(response, access, refresh);
        return new AuthResult(toView(user), jwtService.accessTtlSeconds());
    }

    private void writeCookies(HttpServletResponse response, String access, String refresh) {
        boolean secure = properties.cookies().secure();
        ResponseCookie accessCookie = ResponseCookie.from(AuthCookies.ACCESS, access)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(jwtService.accessTtlSeconds()))
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from(AuthCookies.REFRESH, refresh)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(properties.jwt().refreshTtlDays()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void clearCookies(HttpServletResponse response) {
        boolean secure = properties.cookies().secure();
        ResponseCookie access = ResponseCookie.from(AuthCookies.ACCESS, "")
                .httpOnly(true).secure(secure).sameSite("Lax").path("/").maxAge(0).build();
        ResponseCookie refresh = ResponseCookie.from(AuthCookies.REFRESH, "")
                .httpOnly(true).secure(secure).sameSite("Lax").path("/api/v1/auth").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, access.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
    }

    private UserView toView(UsuarioEntity user) {
        Set<String> roles = user.getPapeis().stream().map(Enum::name).collect(Collectors.toSet());
        return new UserView(user.getId(), user.getOrganizacaoId(), user.getEmail(), user.getNome(), roles);
    }

    public record AuthResult(UserView user, int expiresIn) {}

    public record UserView(UUID id, UUID organizacaoId, String email, String nome, Set<String> roles) {}
}
