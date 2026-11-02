package br.jlturismo.voa.modules.auth.application;

import br.jlturismo.voa.modules.auth.domain.PapelUsuario;
import br.jlturismo.voa.modules.core.config.VoaProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey key;
    private final int accessTtlMinutes;

    public JwtService(VoaProperties properties) {
        byte[] secretBytes = properties.jwt().accessSecret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT access secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessTtlMinutes = properties.jwt().accessTtlMinutes();
    }

    public String createAccessToken(UUID userId, UUID orgId, Set<PapelUsuario> roles, UUID parceiroId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlMinutes * 60L);
        List<String> roleNames = roles.stream().map(Enum::name).collect(Collectors.toList());
        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim("org", orgId.toString())
                .claim("roles", roleNames)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp));
        if (parceiroId != null) {
            builder.claim("parceiro", parceiroId.toString());
        }
        return builder.signWith(key).compact();
    }

    public JwtUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        UUID userId = UUID.fromString(claims.getSubject());
        UUID orgId = UUID.fromString(claims.get("org", String.class));
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        Set<PapelUsuario> papeis = roles.stream().map(PapelUsuario::valueOf).collect(Collectors.toSet());
        UUID parceiroId = null;
        String parceiroClaim = claims.get("parceiro", String.class);
        if (parceiroClaim != null && !parceiroClaim.isBlank()) {
            parceiroId = UUID.fromString(parceiroClaim);
        }
        return new JwtUser(userId, orgId, papeis, parceiroId);
    }

    public int accessTtlSeconds() {
        return accessTtlMinutes * 60;
    }

    public record JwtUser(UUID userId, UUID orgId, Set<PapelUsuario> roles, UUID parceiroId) {}
}
