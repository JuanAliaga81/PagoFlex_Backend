
package cl.duoc.pagoflex.usuarios_service.repo;

import cl.duoc.pagoflex.usuarios_service.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByNombreAsc();

    List<Usuario>
    findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(
        String nombre,
        String correo
    );

    Optional<Usuario> findByCorreoIgnoreCase(
        String correo
    );

    boolean existsByCorreoIgnoreCase(
        String correo
    );

    boolean existsByCorreoIgnoreCaseAndIdNot(
        String correo,
        Long id
    );

    boolean existsByRut(String rut);
}
