package br.jlturismo.voa.modules.solicitacoes.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository repository;
    private final ClienteService clienteService;

    public SolicitacaoService(SolicitacaoRepository repository, ClienteService clienteService) {
        this.repository = repository;
        this.clienteService = clienteService;
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoView> list(VoaUserPrincipal principal, StatusSolicitacao status, Pageable pageable) {
        Page<SolicitacaoEntity> page = status == null
                ? repository.findByOrganizacaoId(principal.getOrganizacaoId(), pageable)
                : repository.findByOrganizacaoIdAndStatus(principal.getOrganizacaoId(), status, pageable);
        return page.map(this::toView);
    }

    @Transactional
    public SolicitacaoView create(VoaUserPrincipal principal, CreateSolicitacaoCommand cmd) {
        ClienteEntity cliente = clienteService.find(principal, cmd.clienteId());
        Instant now = Instant.now();
        SolicitacaoEntity entity = new SolicitacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(principal.getOrganizacaoId());
        entity.setClienteId(cliente.getId());
        entity.setTitulo(cmd.titulo());
        entity.setDestino(cmd.destino());
        entity.setDataIda(cmd.dataIda());
        entity.setDataVolta(cmd.dataVolta());
        entity.setObservacoes(cmd.observacoes());
        entity.setStatus(StatusSolicitacao.RASCUNHO);
        entity.setCriadoPorId(principal.getId());
        entity.setResponsavelId(principal.getId());
        entity.setCriadoEm(now);
        entity.setAtualizadoEm(now);
        return toView(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public SolicitacaoView get(VoaUserPrincipal principal, UUID id) {
        return toView(find(principal, id));
    }

    @Transactional
    public SolicitacaoView update(VoaUserPrincipal principal, UUID id, UpdateSolicitacaoCommand cmd) {
        SolicitacaoEntity entity = find(principal, id);
        if (entity.getStatus() == StatusSolicitacao.CANCELADA || entity.getStatus() == StatusSolicitacao.FECHADA) {
            throw new IllegalArgumentException("Solicitação não pode ser alterada neste status");
        }
        if (cmd.titulo() != null) {
            entity.setTitulo(cmd.titulo());
        }
        if (cmd.destino() != null) {
            entity.setDestino(cmd.destino());
        }
        if (cmd.dataIda() != null) {
            entity.setDataIda(cmd.dataIda());
        }
        if (cmd.dataVolta() != null) {
            entity.setDataVolta(cmd.dataVolta());
        }
        if (cmd.observacoes() != null) {
            entity.setObservacoes(cmd.observacoes());
        }
        if (cmd.status() != null) {
            entity.setStatus(cmd.status());
        }
        if (cmd.responsavelId() != null) {
            entity.setResponsavelId(cmd.responsavelId());
        }
        entity.setAtualizadoEm(Instant.now());
        return toView(repository.save(entity));
    }

    @Transactional
    public SolicitacaoView cancelar(VoaUserPrincipal principal, UUID id, String motivo) {
        SolicitacaoEntity entity = find(principal, id);
        if (entity.getStatus() == StatusSolicitacao.CANCELADA) {
            return toView(entity);
        }
        entity.setStatus(StatusSolicitacao.CANCELADA);
        entity.setMotivoCancelamento(motivo);
        entity.setAtualizadoEm(Instant.now());
        return toView(repository.save(entity));
    }

    private SolicitacaoEntity find(VoaUserPrincipal principal, UUID id) {
        return repository.findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
    }

    private SolicitacaoView toView(SolicitacaoEntity e) {
        return new SolicitacaoView(
                e.getId(),
                e.getClienteId(),
                e.getTitulo(),
                e.getDestino(),
                e.getDataIda(),
                e.getDataVolta(),
                e.getStatus(),
                e.getResponsavelId(),
                e.getCriadoPorId(),
                e.getObservacoes(),
                e.getMotivoCancelamento(),
                e.getCriadoEm(),
                e.getAtualizadoEm());
    }

    public record CreateSolicitacaoCommand(
            UUID clienteId,
            String titulo,
            String destino,
            LocalDate dataIda,
            LocalDate dataVolta,
            String observacoes) {}

    public record UpdateSolicitacaoCommand(
            String titulo,
            String destino,
            LocalDate dataIda,
            LocalDate dataVolta,
            String observacoes,
            StatusSolicitacao status,
            UUID responsavelId) {}

    public record SolicitacaoView(
            UUID id,
            UUID clienteId,
            String titulo,
            String destino,
            LocalDate dataIda,
            LocalDate dataVolta,
            StatusSolicitacao status,
            UUID responsavelId,
            UUID criadoPorId,
            String observacoes,
            String motivoCancelamento,
            Instant criadoEm,
            Instant atualizadoEm) {}
}
