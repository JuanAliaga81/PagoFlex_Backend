package cl.duoc.pagoflex.soporte_service.service;

import cl.duoc.pagoflex.soporte_service.dto.CrearNotificacionRequest;
import cl.duoc.pagoflex.soporte_service.dto.NotificacionResponse;
import cl.duoc.pagoflex.soporte_service.model.Notificacion;
import cl.duoc.pagoflex.soporte_service.model.TipoNotificacion;
import cl.duoc.pagoflex.soporte_service.repository.NotificacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;

@Service
public class NotificacionService {
    private final NotificacionRepository repositorio;

    public NotificacionService(NotificacionRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public NotificacionResponse crear(CrearNotificacionRequest datos) {
        if (datos == null || datos.usuarioId() == null || datos.usuarioId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario inválido");
        }
        if (datos.tipo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo obligatorio");
        }
        TipoNotificacion tipo;
        try {
            tipo = TipoNotificacion.valueOf(datos.tipo().trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo inválido");
        }
        return crearInterna(datos.usuarioId(), tipo, datos.titulo(), datos.mensaje(), datos.referencia());
    }

    @Transactional
    public NotificacionResponse crearInterna(Long usuarioId, TipoNotificacion tipo,
                                              String titulo, String mensaje, String referencia) {
        if (usuarioId == null || usuarioId <= 0 || tipo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos de notificación inválidos");
        }
        String t = titulo == null ? "" : titulo.trim();
        String m = mensaje == null ? "" : mensaje.trim();
        if (t.isBlank() || t.length() > 120 || m.isBlank() || m.length() > 500 ||
            (referencia != null && referencia.length() > 80)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título o mensaje inválido");
        }
        Notificacion n = new Notificacion();
        n.setUsuarioId(usuarioId);
        n.setTipo(tipo);
        n.setTitulo(t);
        n.setMensaje(m);
        n.setReferencia(referencia);
        n.setLeida(false);
        n.setFecha(Instant.now());
        return respuesta(repositorio.save(n));
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listar(Long usuarioId) {
        validarUsuarioId(usuarioId);
        return repositorio.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId)
            .stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public long noLeidas(Long usuarioId) {
        validarUsuarioId(usuarioId);
        return repositorio.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public NotificacionResponse marcarLeida(Long usuarioId, Long id) {
        validarUsuarioId(usuarioId);
        Notificacion n = repositorio.findByIdAndUsuarioId(id, usuarioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada"));
        n.setLeida(true);
        return respuesta(repositorio.save(n));
    }

    @Transactional
    public int marcarTodas(Long usuarioId) {
        validarUsuarioId(usuarioId);
        List<Notificacion> nuevas = repositorio.findByUsuarioIdAndLeidaFalse(usuarioId);
        for (Notificacion n : nuevas) { n.setLeida(true); }
        repositorio.saveAll(nuevas);
        return nuevas.size();
    }

    private void validarUsuarioId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario inválido");
        }
    }

    private NotificacionResponse respuesta(Notificacion n) {
        return new NotificacionResponse(n.getId(), n.getUsuarioId(), n.getTipo().name(),
            n.getTitulo(), n.getMensaje(), n.getReferencia(), n.getLeida(), n.getFecha());
    }
}
