package br.jlturismo.voa.modules.bilhetes.infrastructure;

import br.jlturismo.voa.modules.bilhetes.domain.StatusBilhete;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bilhete")
public class BilheteEntity {

    @Id
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "venda_id", nullable = false)
    private UUID vendaId;

    @Column(nullable = false)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusBilhete status;

    private String localizador;
    private String emissor;

    @Column(name = "arquivo_path")
    private String arquivoPath;

    @Column(name = "arquivo_nome")
    private String arquivoNome;

    @Column(name = "arquivo_mime")
    private String arquivoMime;

    @Column(name = "agendado_para")
    private Instant agendadoPara;

    @Column(name = "emitido_em")
    private Instant emitidoEm;

    @Column(name = "enviado_cliente_em")
    private Instant enviadoClienteEm;

    @Column(name = "metadados_json")
    private String metadadosJson;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

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

    public UUID getVendaId() {
        return vendaId;
    }

    public void setVendaId(UUID vendaId) {
        this.vendaId = vendaId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public StatusBilhete getStatus() {
        return status;
    }

    public void setStatus(StatusBilhete status) {
        this.status = status;
    }

    public String getLocalizador() {
        return localizador;
    }

    public void setLocalizador(String localizador) {
        this.localizador = localizador;
    }

    public String getEmissor() {
        return emissor;
    }

    public void setEmissor(String emissor) {
        this.emissor = emissor;
    }

    public String getArquivoPath() {
        return arquivoPath;
    }

    public void setArquivoPath(String arquivoPath) {
        this.arquivoPath = arquivoPath;
    }

    public String getArquivoNome() {
        return arquivoNome;
    }

    public void setArquivoNome(String arquivoNome) {
        this.arquivoNome = arquivoNome;
    }

    public String getArquivoMime() {
        return arquivoMime;
    }

    public void setArquivoMime(String arquivoMime) {
        this.arquivoMime = arquivoMime;
    }

    public Instant getAgendadoPara() {
        return agendadoPara;
    }

    public void setAgendadoPara(Instant agendadoPara) {
        this.agendadoPara = agendadoPara;
    }

    public Instant getEmitidoEm() {
        return emitidoEm;
    }

    public void setEmitidoEm(Instant emitidoEm) {
        this.emitidoEm = emitidoEm;
    }

    public Instant getEnviadoClienteEm() {
        return enviadoClienteEm;
    }

    public void setEnviadoClienteEm(Instant enviadoClienteEm) {
        this.enviadoClienteEm = enviadoClienteEm;
    }

    public String getMetadadosJson() {
        return metadadosJson;
    }

    public void setMetadadosJson(String metadadosJson) {
        this.metadadosJson = metadadosJson;
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
