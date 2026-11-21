
package cl.duoc.pagoflex.cobranza_service.repo;

import cl.duoc.pagoflex.cobranza_service.model.Compromiso;
import cl.duoc.pagoflex.cobranza_service.model.EstadoCompromiso;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompromisoRepository
        extends JpaRepository<Compromiso, Long> {

    List<Compromiso>
    findByUsuarioIdOrderByFechaVencimientoAscIdAsc(
        Long usuarioId
    );

    Optional<Compromiso> findByUsuarioIdAndFolio(
        Long usuarioId,
        String folio
    );

    boolean existsByUsuarioIdAndEmpresaIgnoreCaseAndConceptoIgnoreCaseAndNumeroCuota(
        Long usuarioId,
        String empresa,
        String concepto,
        Integer numeroCuota
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c
        FROM Compromiso c
        WHERE c.usuarioId = :usuarioId
          AND c.folio = :folio
        """)
    Optional<Compromiso> bloquearParaPago(
        @Param("usuarioId") Long usuarioId,
        @Param("folio") String folio
    );

    @Query("""
        SELECT CASE
            WHEN COUNT(c) > 0 THEN true
            ELSE false
        END
        FROM Compromiso c
        WHERE c.usuarioId = :usuarioId
          AND LOWER(c.empresa) = LOWER(:empresa)
          AND c.id <> :compromisoId
          AND c.fechaVencimiento < :fechaVencimiento
          AND c.estado IN :estados
        """)
    boolean existeCompromisoAnterior(
        @Param("usuarioId") Long usuarioId,
        @Param("empresa") String empresa,
        @Param("compromisoId") Long compromisoId,
        @Param("fechaVencimiento")
        LocalDate fechaVencimiento,
        @Param("estados")
        Collection<EstadoCompromiso> estados
    );

    List<Compromiso>
    findByUsuarioIdAndEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAscIdAsc(
        Long usuarioId,
        EstadoCompromiso estado,
        LocalDate fechaActual
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c
        FROM Compromiso c
        WHERE c.usuarioId = :usuarioId
          AND c.estado = :estado
          AND c.fechaVencimiento < :fechaActual
        ORDER BY c.fechaVencimiento ASC, c.id ASC
        """)
    List<Compromiso> bloquearVencidos(
        @Param("usuarioId") Long usuarioId,
        @Param("estado") EstadoCompromiso estado,
        @Param("fechaActual") LocalDate fechaActual
    );
}
