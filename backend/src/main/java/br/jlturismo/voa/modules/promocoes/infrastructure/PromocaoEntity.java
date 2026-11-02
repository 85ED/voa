package br.jlturismo.voa.modules.promocoes.infrastructure;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "promocao")
public class PromocaoEntity {

    @Id
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(nullable = false)
    private String titulo;

    private String descricao;

    @Column(name = "imagem_url")
    private String imagemUrl;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private boolean destaque;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "valido_de")
    private LocalDate validoDe;

    @Column(name = "valido_ate")
    private LocalDate validoAte;

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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public boolean isDestaque() {
        return destaque;
    }

    public void setDestaque(boolean destaque) {
        this.destaque = destaque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDate getValidoDe() {
        return validoDe;
    }

    public void setValidoDe(LocalDate validoDe) {
        this.validoDe = validoDe;
    }

    public LocalDate getValidoAte() {
        return validoAte;
    }

    public void setValidoAte(LocalDate validoAte) {
        this.validoAte = validoAte;
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
