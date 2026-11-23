
package cl.duoc.pagoflex.usuarios_service.repo;

import cl.duoc.pagoflex.usuarios_service.model.CodigoRecuperacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoRecuperacionRepository
        extends JpaRepository<CodigoRecuperacion, Long> {

    Optional<CodigoRecuperacion> findByUsuarioId(
        Long usuarioId
    );
}
