package br.jlturismo.voa.modules.auth.application;

import br.jlturismo.voa.modules.auth.domain.PapelUsuario;
import br.jlturismo.voa.modules.auth.infrastructure.OrganizacaoEntity;
import br.jlturismo.voa.modules.auth.infrastructure.OrganizacaoRepository;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioEntity;
import br.jlturismo.voa.modules.auth.infrastructure.UsuarioRepository;
import br.jlturismo.voa.modules.core.config.VoaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
public class AdminSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeedRunner.class);
    private static final UUID JL_ORG_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    private final UsuarioRepository usuarioRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final VoaProperties properties;

    public AdminSeedRunner(
            UsuarioRepository usuarioRepository,
            OrganizacaoRepository organizacaoRepository,
            PasswordEncoder passwordEncoder,
            VoaProperties properties) {
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (organizacaoRepository.count() == 0) {
            OrganizacaoEntity org = new OrganizacaoEntity();
            org.setId(JL_ORG_ID);
            org.setRazaoSocial("JL Turismo");
            org.setCnpj(null);
            org.setTimezone("America/Sao_Paulo");
            org.setCriadoEm(Instant.now());
            organizacaoRepository.save(org);
            log.info("Organização JL Turismo criada (seed)");
        }

        if (usuarioRepository.count() > 0) {
            return;
        }

        String email = properties.seed().adminEmail();
        String password = properties.seed().adminPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.warn("Seed admin ignorado: VOA_SEED_ADMIN_EMAIL/PASSWORD não configurados");
            return;
        }

        UsuarioEntity admin = new UsuarioEntity();
        admin.setId(UUID.randomUUID());
        admin.setOrganizacaoId(JL_ORG_ID);
        admin.setEmail(email.trim().toLowerCase());
        admin.setNome("Administrador");
        admin.setSenhaHash(passwordEncoder.encode(password));
        admin.setAtivo(true);
        admin.setCriadoEm(Instant.now());
        admin.setPapeis(Set.of(PapelUsuario.ADMIN, PapelUsuario.COMERCIAL));
        usuarioRepository.save(admin);
        log.info("Usuário admin seed criado para {}", admin.getEmail());
    }
}
