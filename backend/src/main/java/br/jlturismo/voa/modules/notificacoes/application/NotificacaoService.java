package br.jlturismo.voa.modules.notificacoes.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.notificacoes.infrastructure.NotificacaoInternaEntity;
import br.jlturismo.voa.modules.notificacoes.infrastructure.NotificacaoInternaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificacaoService {

    private final NotificacaoInternaRepository repository;

    public NotificacaoService(NotificacaoInternaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<NotificacaoView> list(VoaUserPrincipal principal, Pageable pageable) {
        return repository.findForUser(principal.getOrganizacaoId(), principal.getId(), pageable)
                .map(this::toView);
    }

    @Transactional(readOnly = true)
    public long countNaoLidas(VoaUserPrincipal principal) {
        return repository.countNaoLidas(principal.getOrganizacaoId(), principal.getId());
    }

    @Transactional
    public NotificacaoView marcarLida(VoaUserPrincipal principal, UUID id) {
        NotificacaoInternaEntity entity = repository.findByIdAndOrganizacaoId(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada"));
        if (entity.getUsuarioId() != null && !entity.getUsuarioId().equals(principal.getId())) {
            throw new IllegalArgumentException("Notificação não pertence ao usuário");
        }
        entity.setLida(true);
        return toView(repository.save(entity));
    }

    @Transactional
    public int marcarTodasLidas(VoaUserPrincipal principal) {
        return repository.marcarTodasLidas(principal.getOrganizacaoId(), principal.getId());
    }

    @Transactional
    public void criarParaOrganizacao(UUID orgId, String titulo, String corpo, String entidadeTipo, UUID entidadeId) {
        NotificacaoInternaEntity entity = new NotificacaoInternaEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(orgId);
        entity.setUsuarioId(null);
        entity.setTitulo(titulo);
        entity.setCorpo(corpo);
        entity.setLida(false);
        entity.setEntidadeTipo(entidadeTipo);
        entity.setEntidadeId(entidadeId);
        entity.setCriadoEm(Instant.now());
        repository.save(entity);
    }

    private NotificacaoView toView(NotificacaoInternaEntity e) {
        return new NotificacaoView(
                e.getId(), e.getTitulo(), e.getCorpo(), e.isLida(), e.getEntidadeTipo(), e.getEntidadeId(), e.getCriadoEm());
    }

    public record NotificacaoView(
            UUID id,
            String titulo,
            String corpo,
            boolean lida,
            String entidadeTipo,
            UUID entidadeId,
            Instant criadoEm) {}
}
