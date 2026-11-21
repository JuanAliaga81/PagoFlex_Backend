package cl.duoc.pagoflex.soporte_service.repository;

import cl.duoc.pagoflex.soporte_service.model.EstadoReporte;
import cl.duoc.pagoflex.soporte_service.model.Reporte;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Reporte> findAllByOrderByFechaCreacionDesc();
    List<Reporte> findByEstadoOrderByFechaCreacionAsc(EstadoReporte estado);
    Optional<Reporte> findByCodigoAndUsuarioId(String codigo, Long usuarioId);
    boolean existsByClaveActiva(String claveActiva);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reporte r WHERE r.id = :id")
    Optional<Reporte> bloquear(@Param("id") Long id);
}
