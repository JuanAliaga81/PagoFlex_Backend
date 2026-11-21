package cl.duoc.pagoflex.soporte_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reportes")
public class Reporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 30)
    private String folio;

    @Column(nullable = false, length = 120)
    private String empresa;

    @Column(nullable = false, length = 60)
    private String motivo;

    @Column(nullable = false, length = 500)
    private String comentario = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @Column(name = "agente_id")
    private Long agenteId;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_tomado")
    private Instant fechaTomado;

    @Column(length = 1000)
    private String respuesta;

    @Column(name = "resultado_compromiso", length = 20)
    private String resultadoCompromiso;

    @Column(name = "fecha_resolucion")
    private Instant fechaResolucion;

    @Column(name = "clave_activa", unique = true, length = 100)
    private String claveActiva;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }
    public Long getAgenteId() { return agenteId; }
    public void setAgenteId(Long agenteId) { this.agenteId = agenteId; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Instant getFechaTomado() { return fechaTomado; }
    public void setFechaTomado(Instant fechaTomado) { this.fechaTomado = fechaTomado; }
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    public String getResultadoCompromiso() { return resultadoCompromiso; }
    public void setResultadoCompromiso(String resultadoCompromiso) { this.resultadoCompromiso = resultadoCompromiso; }
    public Instant getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Instant fechaResolucion) { this.fechaResolucion = fechaResolucion; }
    public String getClaveActiva() { return claveActiva; }
    public void setClaveActiva(String claveActiva) { this.claveActiva = claveActiva; }
}
