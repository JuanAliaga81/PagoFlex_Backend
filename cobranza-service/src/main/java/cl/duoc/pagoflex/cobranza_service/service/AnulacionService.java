
package cl.duoc.pagoflex.cobranza_service.service;

import cl.duoc.pagoflex.cobranza_service.dto.AnulacionResponse;
import cl.duoc.pagoflex.cobranza_service.dto.AnularCompromisoRequest;
import cl.duoc.pagoflex.cobranza_service.dto.UsuarioRemotoResponse;

import cl.duoc.pagoflex.cobranza_service.model.Anulacion;
import cl.duoc.pagoflex.cobranza_service.model.Compromiso;
import cl.duoc.pagoflex.cobranza_service.model.EjecutivoEmpresa;
import cl.duoc.pagoflex.cobranza_service.model.EstadoCompromiso;

import cl.duoc.pagoflex.cobranza_service.repo.AnulacionRepository;
import cl.duoc.pagoflex.cobranza_service.repo.CompromisoRepository;
import cl.duoc.pagoflex.cobranza_service.repo.EjecutivoEmpresaRepository;
import cl.duoc.pagoflex.cobranza_service.repo.PagoRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class AnulacionService {

    private final AnulacionRepository anulacionRepository;
    private final CompromisoRepository compromisoRepository;
    private final EjecutivoEmpresaRepository ejecutivoEmpresaRepository;
    private final PagoRepository pagoRepository;
    private final RestClient usuariosClient;

    private static final ZoneId ZONA_CHILE =
        ZoneId.of("America/Santiago");

    private static final Set<String> MOTIVOS_VALIDOS =
        Set.of(
            "Repactación",
            "Error de registro",
            "Pagado directamente a la empresa",
            "Otro"
        );

    public AnulacionService(
        AnulacionRepository anulacionRepository,
        CompromisoRepository compromisoRepository,
        EjecutivoEmpresaRepository ejecutivoEmpresaRepository,
        PagoRepository pagoRepository,
        @Value("${usuarios.service.url}")
        String urlUsuarios
    ) {

        this.anulacionRepository =
            anulacionRepository;

        this.compromisoRepository =
            compromisoRepository;

        this.ejecutivoEmpresaRepository =
            ejecutivoEmpresaRepository;

        this.pagoRepository =
            pagoRepository;

        this.usuariosClient = RestClient.builder()
            .baseUrl(urlUsuarios)
            .build();
    }

    @Transactional
    public AnulacionResponse anularCompromiso(
        AnularCompromisoRequest datos
    ) {

        validarSolicitud(datos);

        Long ejecutivoId = datos.ejecutivoId();
        Long usuarioId = datos.usuarioId();

        String folio = datos.folio().trim();
        String motivo = datos.motivo().trim();

        String comentario = datos.comentario() == null
            ? ""
            : datos.comentario().trim();

        validarEjecutivo(ejecutivoId);

        EjecutivoEmpresa asignacion =
            buscarEmpresaAsignada(ejecutivoId);

        Compromiso compromiso =
            compromisoRepository.bloquearParaPago(
                usuarioId,
                folio
            ).orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Compromiso no encontrado"
                )
            );

        validarEmpresa(
            asignacion,
            compromiso
        );

        EstadoCompromiso estadoActual =
            compromiso.getEstado();

        if (
            estadoActual != EstadoCompromiso.PENDIENTE &&
            estadoActual != EstadoCompromiso.VENCIDO &&
            estadoActual != EstadoCompromiso.EN_REVISION
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El estado del compromiso no permite anular"
            );
        }

        if (
            pagoRepository.existsByCompromisoId(
                compromiso.getId()
            )
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede anular un compromiso pagado"
            );
        }

        if (
            anulacionRepository.existsByCompromisoId(
                compromiso.getId()
            )
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El compromiso ya tiene una anulación registrada"
            );
        }

        Anulacion anulacion = new Anulacion();

        anulacion.setCompromiso(compromiso);
        anulacion.setEjecutivoId(ejecutivoId);
        anulacion.setMotivo(motivo);
        anulacion.setComentario(comentario);
        anulacion.setFecha(Instant.now());

        compromiso.setEstado(
            EstadoCompromiso.ANULADO
        );

        compromisoRepository.saveAndFlush(
            compromiso
        );

        Anulacion guardada =
            anulacionRepository.saveAndFlush(
                anulacion
            );

        return convertirRespuesta(guardada);
    }

    @Transactional(readOnly = true)
    public AnulacionResponse obtenerAnulacion(
        Long ejecutivoId,
        Long usuarioId,
        String folio
    ) {

        if (
            ejecutivoId == null ||
            ejecutivoId <= 0L ||
            usuarioId == null ||
            usuarioId <= 0L ||
            folio == null ||
            folio.isBlank()
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Datos de consulta inválidos"
            );
        }

        validarEjecutivo(ejecutivoId);

        EjecutivoEmpresa asignacion =
            buscarEmpresaAsignada(ejecutivoId);

        Anulacion anulacion = anulacionRepository
            .buscarPorCompromiso(
                usuarioId,
                folio.trim()
            )
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Anulación no encontrada"
                )
            );

        validarEmpresa(
            asignacion,
            anulacion.getCompromiso()
        );

        return convertirRespuesta(anulacion);
    }

    private void validarSolicitud(
        AnularCompromisoRequest datos
    ) {

        if (datos == null) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar los datos de la anulación"
            );
        }

        if (
            datos.ejecutivoId() == null ||
            datos.ejecutivoId() <= 0L ||
            datos.usuarioId() == null ||
            datos.usuarioId() <= 0L
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Los identificadores no son válidos"
            );
        }

        if (
            datos.folio() == null ||
            datos.folio().isBlank() ||
            datos.folio().trim().length() > 30
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El folio no es válido"
            );
        }

        String motivo = datos.motivo() == null
            ? ""
            : datos.motivo().trim();

        if (!MOTIVOS_VALIDOS.contains(motivo)) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El motivo de anulación no es válido"
            );
        }

        String comentario = datos.comentario() == null
            ? ""
            : datos.comentario().trim();

        if (comentario.length() > 300) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El comentario no puede superar los 300 caracteres"
            );
        }

        if (
            motivo.equals("Otro") &&
            comentario.length() < 10
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El motivo Otro requiere un comentario de al menos 10 caracteres"
            );
        }
    }

    private void validarEjecutivo(
        Long ejecutivoId
    ) {

        UsuarioRemotoResponse usuario;

        try {

            usuario = usuariosClient
                .get()
                .uri(
                    "/api/usuarios/{id}",
                    ejecutivoId
                )
                .retrieve()
                .body(UsuarioRemotoResponse.class);

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 404) {

                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El ejecutivo no está registrado"
                );
            }

            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible validar el ejecutivo"
            );

        } catch (ResourceAccessException e) {

            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Usuarios Service no está disponible"
            );
        }

        if (
            usuario == null ||
            !ejecutivoId.equals(usuario.id()) ||
            !Boolean.TRUE.equals(usuario.activo()) ||
            !"ADMINISTRADOR".equals(usuario.rol())
        ) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "La cuenta no tiene permisos de ejecutivo"
            );
        }
    }

    private EjecutivoEmpresa buscarEmpresaAsignada(
        Long ejecutivoId
    ) {

        return ejecutivoEmpresaRepository
            .buscarEmpresaAsignada(ejecutivoId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El ejecutivo no tiene una empresa asignada"
                )
            );
    }

    private void validarEmpresa(
        EjecutivoEmpresa asignacion,
        Compromiso compromiso
    ) {

        String empresaEjecutivo =
            asignacion.getEmpresa().getNombre();

        String empresaCompromiso =
            compromiso.getEmpresa();

        if (
            empresaEjecutivo == null ||
            empresaCompromiso == null ||
            !empresaEjecutivo.equalsIgnoreCase(
                empresaCompromiso
            )
        ) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "El compromiso pertenece a otra empresa"
            );
        }
    }

    private AnulacionResponse convertirRespuesta(
        Anulacion anulacion
    ) {

        Compromiso compromiso =
            anulacion.getCompromiso();

        String fecha = anulacion.getFecha()
            .atZone(ZONA_CHILE)
            .format(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            );

        return new AnulacionResponse(
            anulacion.getId(),
            compromiso.getId(),
            compromiso.getUsuarioId(),
            compromiso.getFolio(),
            compromiso.getEmpresa(),
            compromiso.getEstado().name(),
            anulacion.getEjecutivoId(),
            anulacion.getMotivo(),
            anulacion.getComentario(),
            fecha
        );
    }
}
