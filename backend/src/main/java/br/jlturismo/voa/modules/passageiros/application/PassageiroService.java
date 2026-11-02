package br.jlturismo.voa.modules.passageiros.application;

import br.jlturismo.voa.modules.auth.api.VoaUserPrincipal;
import br.jlturismo.voa.modules.passageiros.infrastructure.*;
import br.jlturismo.voa.modules.solicitacoes.application.ClienteService;
import br.jlturismo.voa.modules.vendas.infrastructure.VendaRepository;
import br.jlturismo.voa.modules.solicitacoes.infrastructure.SolicitacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PassageiroService {

    private final PassageiroRepository passageiroRepository;
    private final SolicitacaoPassageiroRepository solicitacaoPassageiroRepository;
    private final VendaPassageiroRepository vendaPassageiroRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ClienteService clienteService;
    private final VendaRepository vendaRepository;

    public PassageiroService(
            PassageiroRepository passageiroRepository,
            SolicitacaoPassageiroRepository solicitacaoPassageiroRepository,
            VendaPassageiroRepository vendaPassageiroRepository,
            SolicitacaoRepository solicitacaoRepository,
            ClienteService clienteService,
            VendaRepository vendaRepository) {
        this.passageiroRepository = passageiroRepository;
        this.solicitacaoPassageiroRepository = solicitacaoPassageiroRepository;
        this.vendaPassageiroRepository = vendaPassageiroRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.clienteService = clienteService;
        this.vendaRepository = vendaRepository;
    }

    @Transactional(readOnly = true)
    public Page<PassageiroView> list(VoaUserPrincipal principal, String q, Pageable pageable) {
        Page<PassageiroEntity> page = (q == null || q.isBlank())
                ? passageiroRepository.findByOrganizacaoIdAndDeletedAtIsNull(
                        principal.getOrganizacaoId(), pageable)
                : passageiroRepository.search(principal.getOrganizacaoId(), q.trim(), pageable);
        return page.map(this::toView);
    }

    @Transactional
    public PassageiroView create(VoaUserPrincipal principal, CreatePassageiroCommand cmd) {
        if (cmd.clienteId() != null) {
            clienteService.find(principal, cmd.clienteId());
        }
        Instant now = Instant.now();
        PassageiroEntity entity = new PassageiroEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(principal.getOrganizacaoId());
        entity.setClienteId(cmd.clienteId());
        entity.setNome(cmd.nome());
        entity.setDocumento(cmd.documento());
        entity.setDataNascimento(cmd.dataNascimento());
        entity.setEmail(cmd.email());
        entity.setTelefone(cmd.telefone());
        entity.setCriadoEm(now);
        entity.setAtualizadoEm(now);
        return toView(passageiroRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PassageiroView get(VoaUserPrincipal principal, UUID id) {
        return toView(find(principal, id));
    }

    @Transactional
    public PassageiroView update(VoaUserPrincipal principal, UUID id, UpdatePassageiroCommand cmd) {
        PassageiroEntity entity = find(principal, id);
        if (cmd.clienteId() != null) {
            clienteService.find(principal, cmd.clienteId());
            entity.setClienteId(cmd.clienteId());
        }
        if (cmd.nome() != null) {
            entity.setNome(cmd.nome());
        }
        if (cmd.documento() != null) {
            entity.setDocumento(cmd.documento());
        }
        if (cmd.dataNascimento() != null) {
            entity.setDataNascimento(cmd.dataNascimento());
        }
        if (cmd.email() != null) {
            entity.setEmail(cmd.email());
        }
        if (cmd.telefone() != null) {
            entity.setTelefone(cmd.telefone());
        }
        entity.setAtualizadoEm(Instant.now());
        return toView(passageiroRepository.save(entity));
    }

    @Transactional
    public void linkToSolicitacao(VoaUserPrincipal principal, UUID solicitacaoId, UUID passageiroId) {
        assertSolicitacao(principal, solicitacaoId);
        find(principal, passageiroId);
        SolicitacaoPassageiroEntity link = new SolicitacaoPassageiroEntity();
        link.setSolicitacaoId(solicitacaoId);
        link.setPassageiroId(passageiroId);
        solicitacaoPassageiroRepository.save(link);
    }

    @Transactional
    public void unlinkFromSolicitacao(VoaUserPrincipal principal, UUID solicitacaoId, UUID passageiroId) {
        assertSolicitacao(principal, solicitacaoId);
        solicitacaoPassageiroRepository.deleteBySolicitacaoIdAndPassageiroId(solicitacaoId, passageiroId);
    }

    @Transactional(readOnly = true)
    public List<PassageiroView> listBySolicitacao(VoaUserPrincipal principal, UUID solicitacaoId) {
        assertSolicitacao(principal, solicitacaoId);
        return solicitacaoPassageiroRepository.findBySolicitacaoId(solicitacaoId).stream()
                .map(l -> find(principal, l.getPassageiroId()))
                .map(this::toView)
                .toList();
    }

    @Transactional
    public void copySolicitacaoPassageirosToVenda(UUID solicitacaoId, UUID vendaId) {
        for (SolicitacaoPassageiroEntity link : solicitacaoPassageiroRepository.findBySolicitacaoId(solicitacaoId)) {
            VendaPassageiroEntity vp = new VendaPassageiroEntity();
            vp.setVendaId(vendaId);
            vp.setPassageiroId(link.getPassageiroId());
            vendaPassageiroRepository.save(vp);
        }
    }

    @Transactional
    public void linkPassageirosToVenda(VoaUserPrincipal principal, UUID vendaId, List<UUID> passageiroIds) {
        if (passageiroIds == null) {
            return;
        }
        for (UUID pid : passageiroIds) {
            find(principal, pid);
            VendaPassageiroEntity vp = new VendaPassageiroEntity();
            vp.setVendaId(vendaId);
            vp.setPassageiroId(pid);
            vendaPassageiroRepository.save(vp);
        }
    }

    @Transactional(readOnly = true)
    public List<PassageiroView> listByVenda(VoaUserPrincipal principal, UUID vendaId) {
        vendaRepository
                .findByIdAndOrganizacaoId(vendaId, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
        return vendaPassageiroRepository.findByVendaId(vendaId).stream()
                .map(l -> find(principal, l.getPassageiroId()))
                .map(this::toView)
                .toList();
    }

    PassageiroEntity findEntity(VoaUserPrincipal principal, UUID id) {
        return find(principal, id);
    }

    private PassageiroEntity find(VoaUserPrincipal principal, UUID id) {
        return passageiroRepository
                .findByIdAndOrganizacaoIdAndDeletedAtIsNull(id, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Passageiro não encontrado"));
    }

    private void assertSolicitacao(VoaUserPrincipal principal, UUID solicitacaoId) {
        solicitacaoRepository
                .findByIdAndOrganizacaoId(solicitacaoId, principal.getOrganizacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
    }

    private PassageiroView toView(PassageiroEntity e) {
        return new PassageiroView(
                e.getId(),
                e.getClienteId(),
                e.getNome(),
                e.getDocumento(),
                e.getDataNascimento(),
                e.getEmail(),
                e.getTelefone(),
                e.getCriadoEm());
    }

    public record CreatePassageiroCommand(
            UUID clienteId,
            String nome,
            String documento,
            LocalDate dataNascimento,
            String email,
            String telefone) {}

    public record UpdatePassageiroCommand(
            UUID clienteId,
            String nome,
            String documento,
            LocalDate dataNascimento,
            String email,
            String telefone) {}

    public record PassageiroView(
            UUID id,
            UUID clienteId,
            String nome,
            String documento,
            LocalDate dataNascimento,
            String email,
            String telefone,
            Instant criadoEm) {}
}
