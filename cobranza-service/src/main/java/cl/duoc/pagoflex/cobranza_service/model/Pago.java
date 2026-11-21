
package cl.duoc.pagoflex.cobranza_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "compromiso_id",
        nullable = false,
        unique = true
    )
    private Compromiso compromiso;

    @Column(
        name = "numero_comprobante",
        nullable = false,
        unique = true,
        length = 20
    )
    private String numeroComprobante;

    @Column(
        name = "folio_compromiso",
        nullable = false,
        length = 30
    )
    private String folioCompromiso;

    @Column(nullable = false, length = 120)
    private String empresa;

    @Column(nullable = false, length = 150)
    private String concepto;

    @Column(
        name = "monto_pagado",
        nullable = false
    )
    private Long montoPagado;

    @Column(
        name = "fecha_hora",
        nullable = false
    )
    private Instant fechaHora;

    @Column(nullable = false, length = 20)
    private String canal = "APP";

    @Column(
        name = "medio_pago",
        nullable = false,
        length = 60
    )
    private String medioPago;

    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    @Column(name = "total_cuotas")
    private Integer totalCuotas;

    public Pago() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Compromiso getCompromiso() {
        return compromiso;
    }

    public void setCompromiso(
        Compromiso compromiso
    ) {
        this.compromiso = compromiso;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(
        String numeroComprobante
    ) {
        this.numeroComprobante =
            numeroComprobante;
    }

    public String getFolioCompromiso() {
        return folioCompromiso;
    }

    public void setFolioCompromiso(
        String folioCompromiso
    ) {
        this.folioCompromiso =
            folioCompromiso;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Long getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(
        Long montoPagado
    ) {
        this.montoPagado = montoPagado;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(
        Instant fechaHora
    ) {
        this.fechaHora = fechaHora;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(
        String medioPago
    ) {
        this.medioPago = medioPago;
    }

    public Integer getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(
        Integer numeroCuota
    ) {
        this.numeroCuota = numeroCuota;
    }

    public Integer getTotalCuotas() {
        return totalCuotas;
    }

    public void setTotalCuotas(
        Integer totalCuotas
    ) {
        this.totalCuotas = totalCuotas;
    }
}
