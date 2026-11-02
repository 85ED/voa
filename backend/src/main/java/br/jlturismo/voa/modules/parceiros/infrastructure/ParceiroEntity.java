package br.jlturismo.voa.modules.parceiros.infrastructure;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parceiro")
public class ParceiroEntity {

    @Id
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String slug;

    @Column(name = "nome_comercial")
    private String nomeComercial;

    private String subtitulo;

    private String email;
    private String telefone;
    private String documento;

    @Column(name = "percentual_referencia")
    private BigDecimal percentualReferencia;

    @Column(nullable = false)
    private boolean ativo;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getNomeComercial() {
        return nomeComercial;
    }

    public void setNomeComercial(String nomeComercial) {
        this.nomeComercial = nomeComercial;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public BigDecimal getPercentualReferencia() {
        return percentualReferencia;
    }

    public void setPercentualReferencia(BigDecimal percentualReferencia) {
        this.percentualReferencia = percentualReferencia;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
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
