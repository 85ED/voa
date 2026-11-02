package br.jlturismo.voa.modules.auth.application;

import br.jlturismo.voa.modules.auth.infrastructure.RefreshTokenEntity;
import br.jlturismo.voa.modules.auth.infrastructure.RefreshTokenRepository;
import br.jlturismo.voa.modules.core.config.VoaProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final int refreshTtlDays;

    public RefreshTokenService(RefreshTokenRepository repository, VoaProperties properties) {
        this.repository = repository;
        this.refreshTtlDays = properties.jwt().refreshTtlDays();
    }

    @Transactional
    public String issue(UUID usuarioId) {
        String raw = UUID.randomUUID().toString() + UUID.randomUUID();
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioId(usuarioId);
        entity.setTokenHash(hash(raw));
        entity.setExpiraEm(Instant.now().plusSeconds(refreshTtlDays * 86400L));
        entity.setRevogado(false);
        entity.setCriadoEm(Instant.now());
        repository.save(entity);
        return raw;
    }

    @Transactional
    public UUID rotate(String rawToken) {
        RefreshTokenEntity existing = repository.findByTokenHashAndRevogadoFalse(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));
        if (existing.getExpiraEm().isBefore(Instant.now())) {
            existing.setRevogado(true);
            repository.save(existing);
            throw new IllegalArgumentException("Refresh token expirado");
        }
        existing.setRevogado(true);
        repository.save(existing);
        return existing.getUsuarioId();
    }

    @Transactional
    public void revokeByRaw(String rawToken) {
        repository.findByTokenHashAndRevogadoFalse(hash(rawToken)).ifPresent(token -> {
            token.setRevogado(true);
            repository.save(token);
        });
    }

    @Transactional
    public void revokeAllForUser(UUID usuarioId) {
        repository.revokeAllForUser(usuarioId);
    }

    static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
