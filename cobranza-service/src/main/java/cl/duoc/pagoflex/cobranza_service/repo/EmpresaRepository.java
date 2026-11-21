package cl.duoc.pagoflex.cobranza_service.repo;

import cl.duoc.pagoflex.cobranza_service.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmpresaRepository
        extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByNombreIgnoreCase(
        String nombre
    );

    List<Empresa> findAllByOrderByNombreAsc();
}