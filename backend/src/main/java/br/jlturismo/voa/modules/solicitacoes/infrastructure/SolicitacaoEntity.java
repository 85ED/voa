package br.jlturismo.voa.modules.solicitacoes.infrastructure;

import br.jlturismo.voa.modules.solicitacoes.domain.CanalSolicitacao;
import br.jlturismo.voa.modules.solicitacoes.domain.StatusSolicitacao;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "solicitacao")
public class SolicitacaoEntity {

    @Id
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(nullable = false)
    private String titulo;

    private String destino;

    @Column(name = "data_ida")
    private LocalDate dataIda;

    @Column(name = "data_volta")
    private LocalDate dataVolta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @Column(name = "responsavel_id")
    private UUID responsavelId;

    @Column(name = "criado_por_id")
    private UUID criadoPorId;

    private String observacoes;

    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Column(name = "parceiro_id")
    private UUID parceiroId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalSolicitacao canal;

    @Column(name = "parceiro_slug_snapshot")
    private String parceiroSlugSnapshot;

    private String origem;

    @Column(name = "metadados_json")
    private String metadadosJson;

    public UUID getParceiroId() {
        return parceiroId;
    }

    public void setParceiroId(UUID parceiroId) {
        this.parceiroId = parceiroId;
    }

    public CanalSolicitacao getCanal() {
        return canal;
    }

    public void setCanal(CanalSolicitacao canal) {
        this.canal = canal;
    }

    public String getParceiroSlugSnapshot() {
        return parceiroSlugSnapshot;
    }

    public void setParceiroSlugSnapshot(String parceiroSlugSnapshot) {
        this.parceiroSlugSnapshot = parceiroSlugSnapshot;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getMetadadosJson() {
        return metadadosJson;
    }

    public void setMetadadosJson(String metadadosJson) {
        this.metadadosJson = metadadosJson;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public void setClienteId(UUID clienteId) {
        this.clienteId = clienteId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalDate getDataIda() {
        return dataIda;
    }

    public void setDataIda(LocalDate dataIda) {
        this.dataIda = dataIda;
    }

    public LocalDate getDataVolta() {
        return dataVolta;
    }

    public void setDataVolta(LocalDate dataVolta) {
        this.dataVolta = dataVolta;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public UUID getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(UUID responsavelId) {
        this.responsavelId = responsavelId;
    }

    public UUID getCriadoPorId() {
        return criadoPorId;
    }

    public void setCriadoPorId(UUID criadoPorId) {
        this.criadoPorId = criadoPorId;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
