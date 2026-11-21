package cl.duoc.pagoflex.soporte_service.repository;

import cl.duoc.pagoflex.soporte_service.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);
    Optional<Notificacion> findByIdAndUsuarioId(Long id, Long usuarioId);
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);
    List<Notificacion> findByUsuarioIdAndLeidaFalse(Long usuarioId);
}
