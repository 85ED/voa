package br.jlturismo.voa.modules.auth.application;

import br.jlturismo.voa.modules.auth.infrastructure.PasswordResetTokenEntity;
import br.jlturismo.voa.modules.auth.infrastructure.PasswordResetTokenRepository;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioEntity;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioRepository;
import br.jlturismo.voa.modules.core.config.VoaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    PasswordResetTokenRepository tokenRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    PasswordResetService service;

    @BeforeEach
    void setUp() {
        VoaProperties properties = new VoaProperties(
                new VoaProperties.Jwt("secret-key-at-least-32-characters-long", 15, 7),
                new VoaProperties.Cors(java.util.List.of("http://localhost:5173")),
                new VoaProperties.Seed("admin@test.local", "AdminSenhaSegura12"),
                new VoaProperties.Cookies(false),
                new VoaProperties.Comissao(false),
                new VoaProperties.Auth(20),
                new VoaProperties.PublicApi(30, 8),
                new VoaProperties.Security("default-src 'none'"),
                "http://localhost:5173",
                new VoaProperties.PasswordReset(60, true));
        service = new PasswordResetService(usuarioRepository, tokenRepository, passwordEncoder, properties);
    }

    @Test
    void requestResetExpoeLinkDevQuandoUsuarioExiste() {
        UsuarioEntity user = activeUser();
        when(usuarioRepository.findByEmailIgnoreCase("admin@test.local")).thenReturn(Optional.of(user));

        PasswordResetService.ForgotPasswordResult result = service.requestReset("admin@test.local");

        assertThat(result.message()).isEqualTo(PasswordResetService.GENERIC_MESSAGE);
        assertThat(result.devResetUrl()).contains("/login/redefinir-senha?token=");
        verify(tokenRepository).save(any(PasswordResetTokenEntity.class));
    }

    @Test
    void resetPasswordAtualizaHashEMarcaToken() {
        String raw = "raw-token-value";
        UUID userId = UUID.randomUUID();
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setId(UUID.randomUUID());
        token.setUsuarioId(userId);
        token.setTokenHash(PasswordResetService.hash(raw));
        token.setExpiraEm(Instant.now().plusSeconds(3600));

        UsuarioEntity user = activeUser();
        user.setId(userId);

        when(tokenRepository.findByTokenHashAndUsadoEmIsNull(PasswordResetService.hash(raw)))
                .thenReturn(Optional.of(token));
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NovaSenhaSegura99")).thenReturn("hash-nova");

        service.resetPassword(raw, "NovaSenhaSegura99");

        ArgumentCaptor<UsuarioEntity> userCaptor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getSenhaHash()).isEqualTo("hash-nova");
        assertThat(token.getUsadoEm()).isNotNull();
    }

    @Test
    void resetPasswordRejeitaTokenExpirado() {
        String raw = "expired";
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setUsuarioId(UUID.randomUUID());
        token.setTokenHash(PasswordResetService.hash(raw));
        token.setExpiraEm(Instant.now().minusSeconds(60));

        when(tokenRepository.findByTokenHashAndUsadoEmIsNull(PasswordResetService.hash(raw)))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(raw, "NovaSenhaSegura99"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token inválido");
    }

    private static UsuarioEntity activeUser() {
        UsuarioEntity user = new UsuarioEntity();
        user.setId(UUID.randomUUID());
        user.setAtivo(true);
        user.setEmail("admin@test.local");
        user.setSenhaHash("old");
        return user;
    }
}
