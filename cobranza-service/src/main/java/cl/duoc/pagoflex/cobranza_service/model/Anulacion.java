
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
@Table(name = "anulaciones")
public class Anulacion {

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

    @Column(name = "ejecutivo_id", nullable = false)
    private Long ejecutivoId;

    @Column(nullable = false, length = 60)
    private String motivo;

    @Column(nullable = false, length = 300)
    private String comentario = "";

    @Column(nullable = false)
    private Instant fecha;

    public Anulacion() {
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

    public void setCompromiso(Compromiso compromiso) {
        this.compromiso = compromiso;
    }

    public Long getEjecutivoId() {
        return ejecutivoId;
    }

    public void setEjecutivoId(Long ejecutivoId) {
        this.ejecutivoId = ejecutivoId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }
}
