package br.jlturismo.voa.modules.pagamentos.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "parcela")
public class ParcelaEntity {

    @Id
    private UUID id;

    @Column(name = "pagamento_id", nullable = false)
    private UUID pagamentoId;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false)
    private LocalDate vencimento;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "comprovante_ref")
    private String comprovanteRef;

    @Column(name = "pago_em")
    private Instant pagoEm;

    @Column(name = "confirmado_por_id")
    private UUID confirmadoPorId;

    @Column(name = "confirmado_em")
    private Instant confirmadoEm;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPagamentoId() {
        return pagamentoId;
    }

    public void setPagamentoId(UUID pagamentoId) {
        this.pagamentoId = pagamentoId;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getComprovanteRef() {
        return comprovanteRef;
    }

    public void setComprovanteRef(String comprovanteRef) {
        this.comprovanteRef = comprovanteRef;
    }

    public Instant getPagoEm() {
        return pagoEm;
    }

    public void setPagoEm(Instant pagoEm) {
        this.pagoEm = pagoEm;
    }

    public UUID getConfirmadoPorId() {
        return confirmadoPorId;
    }

    public void setConfirmadoPorId(UUID confirmadoPorId) {
        this.confirmadoPorId = confirmadoPorId;
    }

    public Instant getConfirmadoEm() {
        return confirmadoEm;
    }

    public void setConfirmadoEm(Instant confirmadoEm) {
        this.confirmadoEm = confirmadoEm;
    }

    public boolean isConfirmada() {
        return confirmadoEm != null;
    }
}
