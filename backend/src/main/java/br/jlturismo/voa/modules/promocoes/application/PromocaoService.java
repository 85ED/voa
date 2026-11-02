package br.jlturismo.voa.modules.promocoes.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.core.config.OrgConstants;
import br.jlturismo.voa.modules.promocoes.infrastructure.PromocaoEntity;
import br.jlturismo.voa.modules.promocoes.infrastructure.PromocaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PromocaoService {

    private final PromocaoRepository repository;

    public PromocaoService(PromocaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<PromocaoView> listAdmin(VoaUserPrincipal principal, Pageable pageable) {
        return repository.findByOrganizacaoId(principal.getOrganizacaoId(), pageable).map(this::toView);
    }

    @Transactional(readOnly = true)
    public List<PromocaoView> listPublicas() {
        return repository.findPublicas(OrgConstants.JL_ORG_ID, LocalDate.now()).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public PromocaoView create(VoaUserPrincipal principal, UpsertPromocaoCommand cmd) {
        Instant now = Instant.now();
        PromocaoEntity entity = new PromocaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(principal.getOrganizacaoId());
        apply(entity, cmd);
        entity.setCriadoEm(now);
        entity.setAtualizadoEm(now);
        return toView(repository.save(entity));
    }

    @Transactional
    public PromocaoView update(VoaUserPrincipal principal, UUID id, UpsertPromocaoCommand cmd) {
        PromocaoEntity entity = find(principal, id);
        apply(entity, cmd);
        entity.setAtualizadoEm(Instant.now());
        return toView(repository.save(entity));
    }

    @Transactional
    public void delete(VoaUserPrincipal principal, UUID id) {
        PromocaoEntity entity = find(principal, id);
        entity.setAtivo(false);
        entity.setAtualizadoEm(Instant.now());
        repository.save(entity);
    }

    private void apply(PromocaoEntity entity, UpsertPromocaoCommand cmd) {
        entity.setTitulo(cmd.titulo());
        entity.setDescricao(cmd.descricao());
        entity.setImagemUrl(cmd.imagemUrl());
        entity.setSlug(slugify(cmd.slug() != null ? cmd.slug() : cmd.titulo()));
        entity.setDestaque(cmd.destaque() != null && cmd.destaque());
        entity.setAtivo(cmd.ativo() == null || cmd.ativo());
        entity.setValidoDe(cmd.validoDe());
        entity.setValidoAte(cmd.validoAte());
    }

    private PromocaoEntity find(VoaUserPrincipal principal, UUID id) {
        return repository.findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Promoção não encontrada"));
    }

    static String slugify(String input) {
        String normalized = input.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9\\s-]", "");
        return normalized.replaceAll("\\s+", "-").replaceAll("-+", "-");
    }

    private PromocaoView toView(PromocaoEntity e) {
        return new PromocaoView(
                e.getId(),
                e.getTitulo(),
                e.getDescricao(),
                e.getImagemUrl(),
                e.getSlug(),
                e.isDestaque(),
                e.isAtivo(),
                e.getValidoDe(),
                e.getValidoAte());
    }

    public record UpsertPromocaoCommand(
            String titulo,
            String descricao,
            String imagemUrl,
            String slug,
            Boolean destaque,
            Boolean ativo,
            LocalDate validoDe,
            LocalDate validoAte) {}

    public record PromocaoView(
            UUID id,
            String titulo,
            String descricao,
            String imagemUrl,
            String slug,
            boolean destaque,
            boolean ativo,
            LocalDate validoDe,
            LocalDate validoAte) {}
}
