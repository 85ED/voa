package br.jlturismo.voa.modules.parceiros.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.core.config.OrgConstants;
import br.jlturismo.voa.modules.core.config.VoaProperties;
import br.jlturismo.voa.modules.parceiros.infrastructure.ParceiroEntity;
import br.jlturismo.voa.modules.parceiros.infrastructure.ParceiroRepository;
import br.jlturismo.voa.modules.parceiros.infrastructure.ParceiroSlugHistoricoEntity;
import br.jlturismo.voa.modules.parceiros.infrastructure.ParceiroSlugHistoricoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class ParceiroService {

    private final ParceiroRepository repository;
    private final ParceiroSlugHistoricoRepository slugHistoricoRepository;
    private final VoaProperties properties;

    public ParceiroService(
            ParceiroRepository repository,
            ParceiroSlugHistoricoRepository slugHistoricoRepository,
            VoaProperties properties) {
        this.repository = repository;
        this.slugHistoricoRepository = slugHistoricoRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public Page<ParceiroView> list(VoaUserPrincipal principal, Boolean ativo, Pageable pageable) {
        Page<ParceiroEntity> page = ativo == null
                ? repository.findByOrganizacaoId(principal.getOrganizacaoId(), pageable)
                : repository.findByOrganizacaoIdAndAtivo(principal.getOrganizacaoId(), ativo, pageable);
        return page.map(this::toView);
    }

    @Transactional(readOnly = true)
    public ParceiroView get(VoaUserPrincipal principal, UUID id) {
        return toView(find(principal, id));
    }

    @Transactional(readOnly = true)
    public PublicParceiroView resolvePublicBySlug(String slug) {
        ParceiroEntity entity = repository
                .findByOrganizacaoIdAndSlug(OrgConstants.JL_ORG_ID, ParceiroSlugRules.normalize(slug))
                .orElseThrow(() -> new IllegalArgumentException("Parceiro não encontrado"));
        if (!entity.isAtivo()) {
            throw new IllegalStateException("Parceiro indisponível");
        }
        return new PublicParceiroView(
                entity.getNomeComercial() != null ? entity.getNomeComercial() : entity.getNome(),
                entity.getSubtitulo());
    }

    @Transactional(readOnly = true)
    public ParceiroEntity findActiveBySlug(String slug) {
        ParceiroEntity entity = repository
                .findByOrganizacaoIdAndSlug(OrgConstants.JL_ORG_ID, ParceiroSlugRules.normalize(slug))
                .orElseThrow(() -> new IllegalArgumentException("Parceiro não encontrado"));
        if (!entity.isAtivo()) {
            throw new IllegalStateException("Parceiro indisponível");
        }
        return entity;
    }

    @Transactional
    public ParceiroView create(VoaUserPrincipal principal, CreateParceiroCommand cmd) {
        Instant now = Instant.now();
        String slug = cmd.slug() != null ? ParceiroSlugRules.normalize(cmd.slug()) : ParceiroSlugRules.fromNome(cmd.nome());
        assertSlugUnique(principal.getOrganizacaoId(), slug, null);

        ParceiroEntity entity = new ParceiroEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(principal.getOrganizacaoId());
        entity.setNome(cmd.nome());
        entity.setSlug(slug);
        entity.setNomeComercial(cmd.nomeComercial());
        entity.setSubtitulo(cmd.subtitulo());
        entity.setEmail(cmd.email());
        entity.setTelefone(cmd.telefone());
        entity.setDocumento(cmd.documento());
        entity.setPercentualReferencia(cmd.percentualReferencia());
        entity.setAtivo(cmd.ativo() == null || cmd.ativo());
        entity.setCriadoEm(now);
        entity.setAtualizadoEm(now);
        return toView(repository.save(entity));
    }

    @Transactional
    public ParceiroView update(VoaUserPrincipal principal, UUID id, UpdateParceiroCommand cmd) {
        ParceiroEntity entity = find(principal, id);
        if (cmd.nome() != null) {
            entity.setNome(cmd.nome());
        }
        if (cmd.nomeComercial() != null) {
            entity.setNomeComercial(cmd.nomeComercial());
        }
        if (cmd.subtitulo() != null) {
            entity.setSubtitulo(cmd.subtitulo());
        }
        if (cmd.email() != null) {
            entity.setEmail(cmd.email());
        }
        if (cmd.telefone() != null) {
            entity.setTelefone(cmd.telefone());
        }
        if (cmd.documento() != null) {
            entity.setDocumento(cmd.documento());
        }
        if (cmd.percentualReferencia() != null) {
            entity.setPercentualReferencia(cmd.percentualReferencia());
        }
        if (cmd.ativo() != null) {
            entity.setAtivo(cmd.ativo());
        }
        entity.setAtualizadoEm(Instant.now());
        return toView(repository.save(entity));
    }

    @Transactional
    public ParceiroView alterarSlug(VoaUserPrincipal principal, UUID id, String novoSlugRaw) {
        ParceiroEntity entity = find(principal, id);
        String novoSlug = ParceiroSlugRules.normalize(novoSlugRaw);
        if (novoSlug.equals(entity.getSlug())) {
            return toView(entity);
        }
        assertSlugUnique(principal.getOrganizacaoId(), novoSlug, entity.getId());
        recordSlugHistory(entity.getId(), entity.getSlug());
        entity.setSlug(novoSlug);
        entity.setAtualizadoEm(Instant.now());
        return toView(repository.save(entity));
    }

    public ParceiroEntity find(VoaUserPrincipal principal, UUID id) {
        return repository.findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Parceiro não encontrado"));
    }

    public ParceiroEntity findInOrg(UUID orgId, UUID id) {
        return repository.findByIdAndOrganizacaoId(id, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Parceiro não encontrado"));
    }

    private void assertSlugUnique(UUID orgId, String slug, UUID exceptId) {
        if (exceptId == null) {
            if (repository.findByOrganizacaoIdAndSlug(orgId, slug).isPresent()) {
                throw new IllegalArgumentException("Slug já em uso");
            }
        } else if (repository.existsByOrganizacaoIdAndSlugAndIdNot(orgId, slug, exceptId)) {
            throw new IllegalArgumentException("Slug já em uso");
        }
    }

    private void recordSlugHistory(UUID parceiroId, String slug) {
        ParceiroSlugHistoricoEntity hist = new ParceiroSlugHistoricoEntity();
        hist.setId(UUID.randomUUID());
        hist.setParceiroId(parceiroId);
        hist.setSlug(slug);
        hist.setCriadoEm(Instant.now());
        slugHistoricoRepository.save(hist);
    }

    private ParceiroView toView(ParceiroEntity e) {
        String base = properties.publicBaseUrl();
        String publicPath = base.endsWith("/") ? base + "p/" + e.getSlug() : base + "/p/" + e.getSlug();
        return new ParceiroView(
                e.getId(),
                e.getNome(),
                e.getSlug(),
                e.getNomeComercial(),
                e.getSubtitulo(),
                e.getEmail(),
                e.getTelefone(),
                e.getDocumento(),
                e.getPercentualReferencia(),
                e.isAtivo(),
                publicPath,
                e.getCriadoEm());
    }

    public record CreateParceiroCommand(
            String nome,
            String slug,
            String nomeComercial,
            String subtitulo,
            String email,
            String telefone,
            String documento,
            BigDecimal percentualReferencia,
            Boolean ativo) {}

    public record UpdateParceiroCommand(
            String nome,
            String nomeComercial,
            String subtitulo,
            String email,
            String telefone,
            String documento,
            BigDecimal percentualReferencia,
            Boolean ativo) {}

    public record ParceiroView(
            UUID id,
            String nome,
            String slug,
            String nomeComercial,
            String subtitulo,
            String email,
            String telefone,
            String documento,
            BigDecimal percentualReferencia,
            boolean ativo,
            String publicLink,
            Instant criadoEm) {}

    public record PublicParceiroView(String nomeExibicao, String subtitulo) {}
}
