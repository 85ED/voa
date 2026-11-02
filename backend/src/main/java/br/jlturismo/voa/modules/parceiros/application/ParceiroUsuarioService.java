package br.jlturismo.voa.modules.parceiros.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.auth.domain.PapelUsuario;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioEntity;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class ParceiroUsuarioService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private final UsuarioRepository usuarioRepository;
    private final ParceiroService parceiroService;
    private final PasswordEncoder passwordEncoder;

    public ParceiroUsuarioService(
            UsuarioRepository usuarioRepository,
            ParceiroService parceiroService,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.parceiroService = parceiroService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AcessoParceiroView criarAcesso(VoaUserPrincipal principal, UUID parceiroId, CriarAcessoCommand cmd) {
        parceiroService.find(principal, parceiroId);
        if (usuarioRepository.findByEmailIgnoreCase(cmd.email().trim()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        String senhaTemporaria = gerarSenhaTemporaria();
        Instant now = Instant.now();
        UsuarioEntity user = new UsuarioEntity();
        user.setId(UUID.randomUUID());
        user.setOrganizacaoId(principal.getOrganizacaoId());
        user.setParceiroId(parceiroId);
        user.setEmail(cmd.email().trim().toLowerCase());
        user.setNome(cmd.nome());
        user.setSenhaHash(passwordEncoder.encode(senhaTemporaria));
        user.setAtivo(true);
        user.setCriadoEm(now);
        user.setPapeis(Set.of(PapelUsuario.PARTNER));
        usuarioRepository.save(user);
        return new AcessoParceiroView(user.getId(), user.getEmail(), senhaTemporaria);
    }

    @Transactional
    public ResetSenhaView resetSenha(VoaUserPrincipal principal, UUID parceiroId, UUID usuarioId) {
        parceiroService.find(principal, parceiroId);
        UsuarioEntity user = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (!parceiroId.equals(user.getParceiroId())) {
            throw new IllegalArgumentException("Usuário não pertence ao parceiro");
        }
        String senhaTemporaria = gerarSenhaTemporaria();
        user.setSenhaHash(passwordEncoder.encode(senhaTemporaria));
        usuarioRepository.save(user);
        return new ResetSenhaView(user.getId(), senhaTemporaria);
    }

    static String gerarSenhaTemporaria() {
        StringBuilder sb = new StringBuilder(14);
        for (int i = 0; i < 14; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public record CriarAcessoCommand(String email, String nome) {}

    public record AcessoParceiroView(UUID usuarioId, String email, String senhaTemporaria) {}

    public record ResetSenhaView(UUID usuarioId, String senhaTemporaria) {}
}
