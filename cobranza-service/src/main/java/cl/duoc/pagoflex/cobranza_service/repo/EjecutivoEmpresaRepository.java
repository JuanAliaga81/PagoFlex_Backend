
package cl.duoc.pagoflex.cobranza_service.repo;

import cl.duoc.pagoflex.cobranza_service.model.EjecutivoEmpresa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EjecutivoEmpresaRepository
        extends JpaRepository<EjecutivoEmpresa, Long> {

    @Query("""
        SELECT ee
        FROM EjecutivoEmpresa ee
        JOIN FETCH ee.empresa
        WHERE ee.ejecutivoId = :ejecutivoId
        """)
    Optional<EjecutivoEmpresa> buscarEmpresaAsignada(
        @Param("ejecutivoId") Long ejecutivoId
    );
}
