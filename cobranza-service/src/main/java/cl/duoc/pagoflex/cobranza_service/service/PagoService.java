
package cl.duoc.pagoflex.cobranza_service.service;

import cl.duoc.pagoflex.cobranza_service.dto.ComprobanteResponse;
import cl.duoc.pagoflex.cobranza_service.dto.RegistrarPagoRequest;
import cl.duoc.pagoflex.cobranza_service.model.Compromiso;
import cl.duoc.pagoflex.cobranza_service.model.EstadoCompromiso;
import cl.duoc.pagoflex.cobranza_service.model.Pago;
import cl.duoc.pagoflex.cobranza_service.repo.CompromisoRepository;
import cl.duoc.pagoflex.cobranza_service.repo.PagoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final CompromisoRepository compromisoRepository;

    private static final ZoneId ZONA_CHILE =
        ZoneId.of("America/Santiago");

    private static final DateTimeFormatter FORMATO_FECHA =
        DateTimeFormatter.ofPattern(
            "dd-MM-yyyy",
            Locale.forLanguageTag("es-CL")
        );

    public PagoService(
        PagoRepository pagoRepository,
        CompromisoRepository compromisoRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.compromisoRepository =
            compromisoRepository;
    }

    @Transactional
    public ComprobanteResponse registrarPago(
        RegistrarPagoRequest datos
    ) {

        validarSolicitud(datos);

        Long usuarioId = datos.usuarioId();

        String folio = datos.folio().trim();
        String medioPago = datos.medioPago().trim();

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

        if (
            compromiso.getEstado() ==
                EstadoCompromiso.PAGADO ||
            pagoRepository.existsByCompromisoId(
                compromiso.getId()
            )
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Este compromiso ya fue pagado"
            );
        }

        if (
            compromiso.getEstado() ==
                EstadoCompromiso.ANULADO ||
            compromiso.getEstado() ==
                EstadoCompromiso.EN_REVISION
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El compromiso no está disponible para pagar"
            );
        }

        if (
            compromiso.getEstado() !=
                EstadoCompromiso.PENDIENTE &&
            compromiso.getEstado() !=
                EstadoCompromiso.VENCIDO
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El estado del compromiso no permite pagar"
            );
        }

        validarFechaYEstado(compromiso);

        validarCuotas(compromiso);

        List<EstadoCompromiso> estadosPendientes =
            List.of(
                EstadoCompromiso.PENDIENTE,
                EstadoCompromiso.VENCIDO,
                EstadoCompromiso.EN_REVISION
            );

        boolean existeAnterior =
            compromisoRepository.existeCompromisoAnterior(
                usuarioId,
                compromiso.getEmpresa(),
                compromiso.getId(),
                compromiso.getFechaVencimiento(),
                estadosPendientes
            );

        if (existeAnterior) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Debes pagar primero el compromiso más antiguo de esta empresa"
            );
        }

        Long monto = compromiso.getMonto();
        Long recargo = compromiso.getRecargo();

        if (
            monto == null ||
            recargo == null ||
            monto <= 0L ||
            recargo < 0L ||
            monto > Long.MAX_VALUE - recargo
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El monto del compromiso no es válido"
            );
        }

        long total = monto + recargo;

        if (
            total <= 0L ||
            total > Integer.MAX_VALUE
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El total está fuera del rango permitido"
            );
        }

        if (datos.montoEsperado() != total) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El monto cambió. Debes revisar y confirmar el pago nuevamente"
            );
        }

        Pago pago = new Pago();

        String numeroComprobante =
            "CPR-" +
            UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);

        pago.setCompromiso(compromiso);

        pago.setNumeroComprobante(
            numeroComprobante
        );

        pago.setFolioCompromiso(
            compromiso.getFolio()
        );

        pago.setEmpresa(
            compromiso.getEmpresa()
        );

        pago.setConcepto(
            compromiso.getConcepto()
        );

        pago.setMontoPagado(total);

        pago.setFechaHora(Instant.now());

        pago.setCanal("APP");

        pago.setMedioPago(medioPago);

        pago.setNumeroCuota(
            compromiso.getNumeroCuota()
        );

        pago.setTotalCuotas(
            compromiso.getTotalCuotas()
        );

        pagoRepository.saveAndFlush(pago);

        compromiso.setEstado(
            EstadoCompromiso.PAGADO
        );

        compromisoRepository.saveAndFlush(
            compromiso
        );

        return convertirRespuesta(pago);
    }

    @Transactional(readOnly = true)
    public List<ComprobanteResponse> obtenerHistorial(
        Long usuarioId
    ) {

        validarUsuarioId(usuarioId);

        return pagoRepository
            .buscarHistorial(usuarioId)
            .stream()
            .map(this::convertirRespuesta)
            .toList();
    }

    @Transactional(readOnly = true)
    public ComprobanteResponse obtenerComprobante(
        Long usuarioId,
        String codigo
    ) {

        validarUsuarioId(usuarioId);

        if (
            codigo == null ||
            codigo.isBlank()
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar el código del comprobante"
            );
        }

        Pago pago = pagoRepository
            .buscarComprobante(
                usuarioId,
                codigo.trim()
            )
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Comprobante no encontrado"
                )
            );

        return convertirRespuesta(pago);
    }

    private void validarSolicitud(
        RegistrarPagoRequest datos
    ) {

        if (datos == null) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar los datos del pago"
            );
        }

        validarUsuarioId(datos.usuarioId());

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

        if (
            datos.medioPago() == null ||
            datos.medioPago().isBlank() ||
            datos.medioPago().trim().length() > 60
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes indicar un medio de pago válido"
            );
        }

        if (
            datos.montoEsperado() == null ||
            datos.montoEsperado() <= 0L ||
            datos.montoEsperado() > Integer.MAX_VALUE
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes confirmar un monto válido"
            );
        }
    }

    private void validarUsuarioId(
        Long usuarioId
    ) {

        if (
            usuarioId == null ||
            usuarioId <= 0L
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El identificador de usuario no es válido"
            );
        }
    }

    private void validarFechaYEstado(
        Compromiso compromiso
    ) {

        if (compromiso.getFechaVencimiento() == null) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El compromiso no tiene una fecha válida"
            );
        }

        LocalDate hoy = LocalDate.now(
            ZONA_CHILE
        );

        boolean vencio =
            compromiso.getFechaVencimiento()
                .isBefore(hoy);

        if (
            compromiso.getEstado() ==
                EstadoCompromiso.PENDIENTE &&
            vencio
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El compromiso está vencido y requiere actualizar su estado y recargo"
            );
        }

        if (
            compromiso.getEstado() ==
                EstadoCompromiso.VENCIDO &&
            !vencio
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La fecha y el estado del compromiso no coinciden"
            );
        }

        if (
            compromiso.getEstado() ==
                EstadoCompromiso.PENDIENTE &&
            compromiso.getRecargo() != null &&
            compromiso.getRecargo() != 0L
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El compromiso pendiente tiene un recargo inesperado"
            );
        }
    }

    private void validarCuotas(
        Compromiso compromiso
    ) {

        Integer numero =
            compromiso.getNumeroCuota();

        Integer total =
            compromiso.getTotalCuotas();

        String plan =
            compromiso.getReferenciaPlan();

        if (
            numero == null &&
            total == null &&
            plan == null
        ) {
            return;
        }

        if (
            numero == null ||
            total == null ||
            plan == null ||
            plan.isBlank() ||
            numero < 1 ||
            total < numero
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Los datos de las cuotas del compromiso no son válidos"
            );
        }
    }

    private ComprobanteResponse convertirRespuesta(
        Pago pago
    ) {

        String fechaPago = pago.getFechaHora()
            .atZone(ZONA_CHILE)
            .format(FORMATO_FECHA);

        return new ComprobanteResponse(
            pago.getNumeroComprobante(),
            pago.getFolioCompromiso(),
            pago.getEmpresa(),
            pago.getConcepto(),
            pago.getMontoPagado(),
            fechaPago,
            pago.getNumeroCuota(),
            pago.getTotalCuotas()
        );
    }
}
