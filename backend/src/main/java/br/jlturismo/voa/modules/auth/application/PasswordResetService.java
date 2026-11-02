package br.jlturismo.voa.modules.auth.application;

import br.jlturismo.voa.modules.auth.infrastructure.PasswordResetTokenEntity;
import br.jlturismo.voa.modules.auth.infrastructure.PasswordResetTokenRepository;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioEntity;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioRepository;
import br.jlturismo.voa.modules.core.config.VoaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    static final String GENERIC_MESSAGE =
            "Se o e-mail estiver cadastrado, enviaremos instruções para redefinir a senha.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final VoaProperties properties;

    public PasswordResetService(
            UsuarioRepository usuarioRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            VoaProperties properties) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Transactional
    public ForgotPasswordResult requestReset(String email) {
        String normalized = email.trim().toLowerCase();
        Optional<UsuarioEntity> userOpt = usuarioRepository.findByEmailIgnoreCase(normalized);
        String devResetUrl = null;

        if (userOpt.isPresent()) {
            UsuarioEntity user = userOpt.get();
            if (user.isAtivo() && user.getDeletedAt() == null) {
                String raw = issueToken(user.getId());
                if (properties.passwordReset().devExposeResetLink()) {
                    String encoded = URLEncoder.encode(raw, StandardCharsets.UTF_8);
                    devResetUrl = properties.publicBaseUrl() + "/login/redefinir-senha?token=" + encoded;
                    log.info("VOA dev: link de redefinição gerado para {}", normalized);
                } else {
                    log.info("VOA: solicitação de redefinição registrada para {} (envio de e-mail não configurado)", normalized);
                }
            }
        }

        return new ForgotPasswordResult(GENERIC_MESSAGE, devResetUrl);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Senha deve ter ao menos 8 caracteres");
        }

        PasswordResetTokenEntity token = tokenRepository
                .findByTokenHashAndUsadoEmIsNull(hash(rawToken.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));

        if (token.getExpiraEm().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }

        UsuarioEntity user = usuarioRepository
                .findById(token.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));

        if (!user.isAtivo() || user.getDeletedAt() != null) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }

        user.setSenhaHash(passwordEncoder.encode(newPassword));
        usuarioRepository.save(user);

        Instant now = Instant.now();
        token.setUsadoEm(now);
        tokenRepository.save(token);
        tokenRepository.invalidateActiveForUser(user.getId(), now);
    }

    private String issueToken(UUID usuarioId) {
        Instant now = Instant.now();
        tokenRepository.invalidateActiveForUser(usuarioId, now);

        String raw = UUID.randomUUID().toString() + UUID.randomUUID();
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioId(usuarioId);
        entity.setTokenHash(hash(raw));
        entity.setExpiraEm(now.plusSeconds(properties.passwordReset().tokenTtlMinutes() * 60L));
        entity.setCriadoEm(now);
        tokenRepository.save(entity);
        return raw;
    }

    static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public record ForgotPasswordResult(String message, String devResetUrl) {}
}
