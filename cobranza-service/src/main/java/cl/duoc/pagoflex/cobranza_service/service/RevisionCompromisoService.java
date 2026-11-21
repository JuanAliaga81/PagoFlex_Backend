package cl.duoc.pagoflex.cobranza_service.service;

import cl.duoc.pagoflex.cobranza_service.dto.RevisionCompromisoRequest;
import cl.duoc.pagoflex.cobranza_service.model.Compromiso;
import cl.duoc.pagoflex.cobranza_service.model.Empresa;
import cl.duoc.pagoflex.cobranza_service.model.EstadoCompromiso;
import cl.duoc.pagoflex.cobranza_service.repo.CompromisoRepository;
import cl.duoc.pagoflex.cobranza_service.repo.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class RevisionCompromisoService {
    private final CompromisoRepository compromisos;
    private final EmpresaRepository empresas;
    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    public RevisionCompromisoService(CompromisoRepository compromisos, EmpresaRepository empresas) {
        this.compromisos = compromisos;
        this.empresas = empresas;
    }

    @Transactional
    public Compromiso abrir(RevisionCompromisoRequest datos) {
        Compromiso c = buscar(datos);
        if (c.getEstado() != EstadoCompromiso.PENDIENTE && c.getEstado() != EstadoCompromiso.VENCIDO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este compromiso no admite revisión");
        }
        c.setEstado(EstadoCompromiso.EN_REVISION);
        return compromisos.saveAndFlush(c);
    }

    @Transactional
    public Compromiso resolver(RevisionCompromisoRequest datos) {
        Compromiso c = buscar(datos);
        if (c.getEstado() == EstadoCompromiso.ANULADO) {
            return c;
        }
        if (c.getEstado() != EstadoCompromiso.EN_REVISION) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El compromiso no está en revisión");
        }
        boolean vencido = c.getFechaVencimiento().isBefore(LocalDate.now(ZONA));
        if (vencido) {
            Long recargo = c.getRecargo();
            if (recargo == null || recargo == 0L) {
                Empresa empresa = empresas.findByNombreIgnoreCase(c.getEmpresa())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Empresa no registrada"));
                recargo = empresa.getRecargoFijo();
                if (recargo == null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Falta configurar recargo de la empresa");
                }
            }
            if (recargo < 0 || c.getMonto() == null || c.getMonto() < 0 ||
                c.getMonto() > Integer.MAX_VALUE - recargo) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Recargo inválido");
            }
            c.setRecargo(recargo);
            c.setEstado(EstadoCompromiso.VENCIDO);
        } else {
            c.setRecargo(0L);
            c.setEstado(EstadoCompromiso.PENDIENTE);
        }
        return compromisos.saveAndFlush(c);
    }

    private Compromiso buscar(RevisionCompromisoRequest datos) {
        if (datos == null || datos.usuarioId() == null || datos.usuarioId() <= 0 ||
            datos.folio() == null || datos.folio().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud de revisión inválida");
        }
        return compromisos.bloquearParaPago(datos.usuarioId(), datos.folio().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compromiso no encontrado"));
    }
}
