
package cl.duoc.pagoflex.soporte_service.service;

import cl.duoc.pagoflex.soporte_service.dto.CompromisoRemotoResponse;
import cl.duoc.pagoflex.soporte_service.dto.CrearReporteRequest;
import cl.duoc.pagoflex.soporte_service.dto.ReporteResponse;
import cl.duoc.pagoflex.soporte_service.dto.ResolverReporteRequest;
import cl.duoc.pagoflex.soporte_service.dto.TomarReporteRequest;
import cl.duoc.pagoflex.soporte_service.model.EstadoReporte;
import cl.duoc.pagoflex.soporte_service.model.Reporte;
import cl.duoc.pagoflex.soporte_service.model.TipoNotificacion;
import cl.duoc.pagoflex.soporte_service.repository.ReporteRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ReporteService {

    private final ReporteRepository repositorio;
    private final UsuariosCliente usuarios;
    private final CobranzaCliente cobranza;
    private final NotificacionService notificaciones;

    private static final ZoneId ZONA =
            ZoneId.of("America/Santiago");

    private static final Set<String> MOTIVOS = Set.of(
            "Ya pagué por otro medio",
            "No reconozco este cobro",
            "El monto no corresponde",
            "Otro"
    );

    public ReporteService(
            ReporteRepository repositorio,
            UsuariosCliente usuarios,
            CobranzaCliente cobranza,
            NotificacionService notificaciones
    ) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
        this.cobranza = cobranza;
        this.notificaciones = notificaciones;
    }

    @Transactional
    public ReporteResponse crear(CrearReporteRequest datos) {

        if (datos == null ||
                datos.usuarioId() == null ||
                datos.usuarioId() <= 0 ||
                datos.folio() == null ||
                datos.folio().isBlank() ||
                datos.folio().trim().length() > 30 ||
                datos.motivo() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos del reporte inválidos"
            );
        }

        String folio = datos.folio().trim();
        String motivo = datos.motivo().trim();

        String comentario = datos.comentario() == null
                ? ""
                : datos.comentario().trim();

        if (!MOTIVOS.contains(motivo) ||
                comentario.length() > 500 ||
                (motivo.equals("Otro") && comentario.isBlank())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Motivo o comentario inválido"
            );
        }

        usuarios.validarPagador(datos.usuarioId());

        CompromisoRemotoResponse c =
                cobranza.obtener(datos.usuarioId(), folio);

        if (!datos.usuarioId().equals(c.usuarioId()) ||
                !folio.equals(c.folio()) ||
                !("PENDIENTE".equals(c.estado()) ||
                  "VENCIDO".equals(c.estado()))) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Compromiso no disponible para reportar"
            );
        }

        String clave = datos.usuarioId() + ":" + folio;

        if (repositorio.existsByClaveActiva(clave)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un reporte activo"
            );
        }

        cobranza.abrir(datos.usuarioId(), folio);

        Reporte r = new Reporte();

        r.setCodigo(
                "REP-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase()
        );

        r.setUsuarioId(datos.usuarioId());
        r.setFolio(folio);
        r.setEmpresa(c.empresa());
        r.setMotivo(motivo);
        r.setComentario(comentario);
        r.setEstado(EstadoReporte.PENDIENTE);
        r.setFechaCreacion(Instant.now());
        r.setClaveActiva(clave);

        try {
            repositorio.saveAndFlush(r);

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un reporte para este compromiso"
            );
        }

        notificaciones.crearInterna(
                datos.usuarioId(),
                TipoNotificacion.REPORTE,
                "Reporte recibido",
                "Tu reporte " + r.getCodigo() + " está pendiente.",
                folio
        );

        return respuesta(r);
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listarUsuario(Long usuarioId) {

        validarId(usuarioId);

        return repositorio
                .findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::respuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReporteResponse obtenerUsuario(
            Long usuarioId,
            String codigo
    ) {

        validarId(usuarioId);

        if (codigo == null || codigo.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Código inválido"
            );
        }

        return respuesta(
                repositorio
                        .findByCodigoAndUsuarioId(
                                codigo.trim(),
                                usuarioId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Reporte no encontrado"
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listarGestion(
            Long agenteId,
            EstadoReporte estado
    ) {

        usuarios.validarAgente(agenteId);

        List<Reporte> lista = estado == null
                ? repositorio.findAllByOrderByFechaCreacionDesc()
                : repositorio.findByEstadoOrderByFechaCreacionAsc(
                        estado
                );

        return lista.stream()
                .map(this::respuesta)
                .toList();
    }

    @Transactional
    public ReporteResponse tomar(
            Long id,
            TomarReporteRequest datos
    ) {

        if (datos == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Agente obligatorio"
            );
        }

        usuarios.validarAgente(datos.agenteId());

        Reporte r = buscarBloqueado(id);

        if (r.getEstado() != EstadoReporte.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se toman reportes pendientes"
            );
        }

        r.setEstado(EstadoReporte.EN_PROCESO);
        r.setAgenteId(datos.agenteId());
        r.setFechaTomado(Instant.now());

        return respuesta(repositorio.save(r));
    }

    @Transactional
    public ReporteResponse resolver(
            Long id,
            ResolverReporteRequest datos
    ) {

        if (datos == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datos obligatorios"
            );
        }

        usuarios.validarAgente(datos.agenteId());

        String respuestaTexto = datos.respuesta() == null
                ? ""
                : datos.respuesta().trim();

        if (respuestaTexto.isBlank() ||
                respuestaTexto.length() > 1000) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes ingresar una respuesta (máximo 1000 caracteres)"
            );
        }

        Reporte r = buscarBloqueado(id);

        if (r.getEstado() != EstadoReporte.EN_PROCESO ||
                !datos.agenteId().equals(r.getAgenteId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El reporte no está asignado a este agente"
            );
        }

        CompromisoRemotoResponse c =
                cobranza.resolver(
                        r.getUsuarioId(),
                        r.getFolio()
                );

        r.setRespuesta(respuestaTexto);
        r.setEstado(EstadoReporte.RESUELTO);
        r.setResultadoCompromiso(c.estado());
        r.setFechaResolucion(Instant.now());
        r.setClaveActiva(null);

        repositorio.save(r);

        notificaciones.crearInterna(
                r.getUsuarioId(),
                TipoNotificacion.REPORTE,
                "Reporte resuelto",
                "Tu reporte " + r.getCodigo() + " fue resuelto.",
                r.getFolio()
        );

        return respuesta(r);
    }

    private Reporte buscarBloqueado(Long id) {

        validarId(id);

        return repositorio.bloquear(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reporte no encontrado"
                        )
                );
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Identificador inválido"
            );
        }
    }

    private boolean estaAtrasado(Reporte r) {

        if (r.getEstado() != EstadoReporte.PENDIENTE) {
            return false;
        }

        LocalDate fecha = r.getFechaCreacion()
                .atZone(ZONA)
                .toLocalDate();

        LocalDate hoy = LocalDate.now(ZONA);

        int diasHabiles = 0;

        for (
                LocalDate d = fecha.plusDays(1);
                !d.isAfter(hoy);
                d = d.plusDays(1)
        ) {

            if (d.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    d.getDayOfWeek() != DayOfWeek.SUNDAY) {

                diasHabiles++;
            }
        }

        return diasHabiles > 2;
    }

    private ReporteResponse respuesta(Reporte r) {

        return new ReporteResponse(
                r.getId(),
                r.getCodigo(),
                r.getUsuarioId(),
                r.getFolio(),
                r.getEmpresa(),
                r.getMotivo(),
                r.getComentario(),
                r.getEstado().name(),
                r.getAgenteId(),
                r.getFechaCreacion(),
                r.getFechaTomado(),
                r.getRespuesta(),
                r.getResultadoCompromiso(),
                r.getFechaResolucion(),
                estaAtrasado(r)
        );
    }
}
