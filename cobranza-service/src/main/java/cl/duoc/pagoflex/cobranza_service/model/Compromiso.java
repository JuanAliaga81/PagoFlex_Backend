package cl.duoc.pagoflex.cobranza_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "compromisos")
public class Compromiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(
        nullable = false,
        unique = true,
        length = 30
    )
    private String folio;

    @Column(nullable = false, length = 120)
    private String empresa;

    @Column(nullable = false, length = 150)
    private String concepto;

    @Column(nullable = false)
    private Long monto;

    @Column(nullable = false)
    private Long recargo = 0L;

    @Column(
        name = "fecha_vencimiento",
        nullable = false
    )
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompromiso estado =
        EstadoCompromiso.PENDIENTE;

    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    @Column(name = "total_cuotas")
    private Integer totalCuotas;

    @Column(
        name = "referencia_plan",
        length = 80
    )
    private String referenciaPlan;

    public Compromiso() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
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

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public Long getRecargo() {
        return recargo;
    }

    public void setRecargo(Long recargo) {
        this.recargo = recargo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(
        LocalDate fechaVencimiento
    ) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public EstadoCompromiso getEstado() {
        return estado;
    }

    public void setEstado(
        EstadoCompromiso estado
    ) {
        this.estado = estado;
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

    public String getReferenciaPlan() {
        return referenciaPlan;
    }

    public void setReferenciaPlan(
        String referenciaPlan
    ) {
        this.referenciaPlan = referenciaPlan;
    }
}
