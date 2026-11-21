
package cl.duoc.pagoflex.cobranza_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ejecutivos_empresas")
public class EjecutivoEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "ejecutivo_id",
        nullable = false,
        unique = true
    )
    private Long ejecutivoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "empresa_id",
        nullable = false
    )
    private Empresa empresa;

    public EjecutivoEmpresa() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEjecutivoId() {
        return ejecutivoId;
    }

    public void setEjecutivoId(Long ejecutivoId) {
        this.ejecutivoId = ejecutivoId;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
}
