
package cl.duoc.pagoflex.usuarios_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "codigos_recuperacion")
public class CodigoRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "usuario_id",
        nullable = false,
        unique = true
    )
    private Long usuarioId;

    @Column(
        name = "codigo_hash",
        nullable = false,
        length = 64
    )
    private String codigoHash;

    @Column(name = "vence_en", nullable = false)
    private Instant venceEn;

    @Column(name = "ultimo_envio", nullable = false)
    private Instant ultimoEnvio;

    @Column(nullable = false)
    private int intentos;

    public CodigoRecuperacion() {
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCodigoHash() {
        return codigoHash;
    }

    public void setCodigoHash(String codigoHash) {
        this.codigoHash = codigoHash;
    }

    public Instant getVenceEn() {
        return venceEn;
    }

    public void setVenceEn(Instant venceEn) {
        this.venceEn = venceEn;
    }

    public Instant getUltimoEnvio() {
        return ultimoEnvio;
    }

    public void setUltimoEnvio(Instant ultimoEnvio) {
        this.ultimoEnvio = ultimoEnvio;
    }

    public int getIntentos() {
        return intentos;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }
}
