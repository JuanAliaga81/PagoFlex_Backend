
package cl.duoc.pagoflex.cobranza_service.repo;

import cl.duoc.pagoflex.cobranza_service.model.Anulacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnulacionRepository
        extends JpaRepository<Anulacion, Long> {

    boolean existsByCompromisoId(
        Long compromisoId
    );

    @Query("""
        SELECT a
        FROM Anulacion a
        JOIN FETCH a.compromiso c
        WHERE c.usuarioId = :usuarioId
          AND c.folio = :folio
        """)
    Optional<Anulacion> buscarPorCompromiso(
        @Param("usuarioId") Long usuarioId,
        @Param("folio") String folio
    );
}
