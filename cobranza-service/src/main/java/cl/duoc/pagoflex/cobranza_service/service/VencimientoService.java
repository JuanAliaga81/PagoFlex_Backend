
package cl.duoc.pagoflex.cobranza_service.service;

import cl.duoc.pagoflex.cobranza_service.dto.ConfigurarRecargoRequest;
import cl.duoc.pagoflex.cobranza_service.dto.EmpresaRecargoResponse;
import cl.duoc.pagoflex.cobranza_service.dto.ResultadoActualizacionVencimientosResponse;
import cl.duoc.pagoflex.cobranza_service.dto.VencimientoDetectadoResponse;

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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class VencimientoService {

    private final CompromisoRepository compromisoRepository;
    private final EmpresaRepository empresaRepository;

    private static final ZoneId ZONA_CHILE =
        ZoneId.of("America/Santiago");

    private static final DateTimeFormatter FORMATO_FECHA =
        DateTimeFormatter.ofPattern(
            "dd-MM-yyyy",
            Locale.forLanguageTag("es-CL")
        );

    public VencimientoService(
        CompromisoRepository compromisoRepository,
        EmpresaRepository empresaRepository
    ) {

        this.compromisoRepository =
            compromisoRepository;

        this.empresaRepository =
            empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<VencimientoDetectadoResponse>
    detectarVencidos(Long usuarioId) {

        validarUsuarioId(usuarioId);

        LocalDate hoy = LocalDate.now(ZONA_CHILE);

        List<Compromiso> compromisos =
            compromisoRepository
                .findByUsuarioIdAndEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAscIdAsc(
                    usuarioId,
                    EstadoCompromiso.PENDIENTE,
                    hoy
                );

        List<VencimientoDetectadoResponse> resultado =
            new ArrayList<>();

        for (Compromiso compromiso : compromisos) {

            long dias = ChronoUnit.DAYS.between(
                compromiso.getFechaVencimiento(),
                hoy
            );

            int diasAtraso = (int) Math.min(
                Math.max(dias, 0L),
                Integer.MAX_VALUE
            );

            resultado.add(
                new VencimientoDetectadoResponse(
                    compromiso.getId(),
                    compromiso.getUsuarioId(),
                    compromiso.getFolio(),
                    compromiso.getEmpresa(),
                    compromiso.getConcepto(),
                    compromiso.getFechaVencimiento()
                        .format(FORMATO_FECHA),
                    diasAtraso
                )
            );
        }

        return resultado;
    }

    @Transactional
    public ResultadoActualizacionVencimientosResponse
    actualizarVencimientos(Long usuarioId) {

        validarUsuarioId(usuarioId);

        LocalDate hoy = LocalDate.now(ZONA_CHILE);

        List<Compromiso> pendientes =
            compromisoRepository.bloquearVencidos(
                usuarioId,
                EstadoCompromiso.PENDIENTE,
                hoy
            );

        int cantidadActualizada = 0;

        List<String> sinConfigurar =
            new ArrayList<>();

        for (Compromiso compromiso : pendientes) {

            Empresa empresa = empresaRepository
                .findByNombreIgnoreCase(
                    compromiso.getEmpresa().trim()
                )
                .orElse(null);

            Long tarifa = empresa == null
                ? null
                : empresa.getRecargoFijo();

            if (tarifa == null) {

                sinConfigurar.add(
                    compromiso.getEmpresa()
                        + ": "
                        + compromiso.getFolio()
                );

                continue;
            }

            if (
                tarifa < 0L ||
                tarifa > Integer.MAX_VALUE
            ) {

                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La empresa tiene un recargo fuera de rango"
                );
            }

            Long recargoActual =
                compromiso.getRecargo();

            if (
                recargoActual == null ||
                recargoActual != 0L
            ) {

                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El compromiso tiene un recargo previo inesperado: "
                        + compromiso.getFolio()
                );
            }

            Long monto = compromiso.getMonto();

            if (
                monto == null ||
                monto < 0L ||
                monto > Integer.MAX_VALUE - tarifa
            ) {

                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El total del compromiso está fuera de rango: "
                        + compromiso.getFolio()
                );
            }

            compromiso.setRecargo(tarifa);

            compromiso.setEstado(
                EstadoCompromiso.VENCIDO
            );

            compromisoRepository.save(
                compromiso
            );

            cantidadActualizada++;
        }

        return new ResultadoActualizacionVencimientosResponse(
            cantidadActualizada,
            sinConfigurar,
            true
        );
    }

    @Transactional(readOnly = true)
    public EmpresaRecargoResponse obtenerRecargoEmpresa(
        Long empresaId
    ) {

        Empresa empresa = buscarEmpresa(
            empresaId
        );

        return convertirEmpresa(empresa);
    }

    @Transactional
    public EmpresaRecargoResponse configurarRecargo(
        Long empresaId,
        ConfigurarRecargoRequest datos
    ) {

        if (
            datos == null ||
            datos.recargoFijo() == null
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar el recargo fijo"
            );
        }

        Long recargo = datos.recargoFijo();

        if (
            recargo < 0L ||
            recargo > Integer.MAX_VALUE
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El recargo debe estar entre 0 y 2147483647"
            );
        }

        Empresa empresa = buscarEmpresa(
            empresaId
        );

        empresa.setRecargoFijo(
            recargo
        );

        Empresa guardada =
            empresaRepository.save(empresa);

        return convertirEmpresa(guardada);
    }

    private Empresa buscarEmpresa(
        Long empresaId
    ) {

        if (
            empresaId == null ||
            empresaId <= 0L
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Identificador de empresa inválido"
            );
        }

        return empresaRepository
            .findById(empresaId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa no encontrada"
                )
            );
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
                "Identificador de usuario inválido"
            );
        }
    }

    private EmpresaRecargoResponse convertirEmpresa(
        Empresa empresa
    ) {

        return new EmpresaRecargoResponse(
            empresa.getId(),
            empresa.getNombre(),
            empresa.getRecargoFijo()
        );
    }
}
