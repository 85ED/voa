package br.jlturismo.voa.modules.solicitacoes.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteEntity;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ClienteView> list(VoaUserPrincipal principal, String q, Pageable pageable) {
        Page<ClienteEntity> page = (q == null || q.isBlank())
                ? repository.findByOrganizacaoIdAndDeletedAtIsNull(principal.getOrganizacaoId(), pageable)
                : repository.search(principal.getOrganizacaoId(), q.trim(), pageable);
        return page.map(this::toView);
    }

    @Transactional
    public ClienteView create(VoaUserPrincipal principal, CreateClienteCommand cmd) {
        ClienteEntity entity = new ClienteEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(principal.getOrganizacaoId());
        entity.setNome(cmd.nome());
        entity.setEmail(cmd.email());
        entity.setTelefone(cmd.telefone());
        entity.setDocumento(cmd.documento());
        Instant now = Instant.now();
        entity.setCriadoEm(now);
        entity.setAtualizadoEm(now);
        return toView(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ClienteView get(VoaUserPrincipal principal, UUID id) {
        return toView(find(principal, id));
    }

    @Transactional
    public ClienteView update(VoaUserPrincipal principal, UUID id, UpdateClienteCommand cmd) {
        ClienteEntity entity = find(principal, id);
        if (cmd.nome() != null) {
            entity.setNome(cmd.nome());
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
        entity.setAtualizadoEm(Instant.now());
        return toView(repository.save(entity));
    }

    ClienteEntity find(VoaUserPrincipal principal, UUID id) {
        return repository.findByIdAndOrganizacaoIdAndDeletedAtIsNull(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
    }

    private ClienteView toView(ClienteEntity e) {
        return new ClienteView(e.getId(), e.getNome(), e.getEmail(), e.getTelefone(), e.getDocumento(), e.getCriadoEm());
    }

    public record CreateClienteCommand(String nome, String email, String telefone, String documento) {}

    public record UpdateClienteCommand(String nome, String email, String telefone, String documento) {}

    public record ClienteView(UUID id, String nome, String email, String telefone, String documento, Instant criadoEm) {}
}
