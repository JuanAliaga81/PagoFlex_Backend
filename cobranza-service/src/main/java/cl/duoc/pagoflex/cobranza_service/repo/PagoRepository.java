
package cl.duoc.pagoflex.cobranza_service.repo;

import cl.duoc.pagoflex.cobranza_service.model.Pago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PagoRepository
        extends JpaRepository<Pago, Long> {

    boolean existsByCompromisoId(
        Long compromisoId
    );

    @Query("""
        SELECT p
        FROM Pago p
        JOIN FETCH p.compromiso c
        WHERE c.usuarioId = :usuarioId
        ORDER BY p.fechaHora DESC, p.id DESC
        """)
    List<Pago> buscarHistorial(
        @Param("usuarioId") Long usuarioId
    );

    @Query("""
        SELECT p
        FROM Pago p
        JOIN FETCH p.compromiso c
        WHERE c.usuarioId = :usuarioId
          AND p.numeroComprobante = :codigo
        """)
    Optional<Pago> buscarComprobante(
        @Param("usuarioId") Long usuarioId,
        @Param("codigo") String codigo
    );
}
