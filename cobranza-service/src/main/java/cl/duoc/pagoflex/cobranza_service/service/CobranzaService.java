
package cl.duoc.pagoflex.cobranza_service.service;

import cl.duoc.pagoflex.cobranza_service.dto.CompromisoResponse;
import cl.duoc.pagoflex.cobranza_service.dto.RegistrarCompromisoRequest;
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
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CobranzaService {

    private final CompromisoRepository compromisoRepository;
    private final EmpresaRepository empresaRepository;

    private static final DateTimeFormatter FORMATO_FECHA =
        DateTimeFormatter
            .ofPattern(
                "dd-MM-uuuu",
                Locale.forLanguageTag("es-CL")
            )
            .withResolverStyle(ResolverStyle.STRICT);

    private static final ZoneId ZONA_CHILE =
        ZoneId.of("America/Santiago");

    public CobranzaService(
        CompromisoRepository compromisoRepository,
        EmpresaRepository empresaRepository
    ) {
        this.compromisoRepository =
            compromisoRepository;

        this.empresaRepository =
            empresaRepository;
    }

    @Transactional
    public CompromisoResponse registrarCompromiso(
        RegistrarCompromisoRequest datos
    ) {

        if (datos == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar los datos del compromiso"
            );
        }

        if (
            datos.usuarioId() == null ||
            datos.usuarioId() <= 0L
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes seleccionar un usuario válido"
            );
        }

        String empresa = limpiar(datos.empresa());
        String concepto = limpiar(datos.concepto());

        if (
            empresa.isBlank() ||
            empresa.length() > 120
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar una empresa válida"
            );
        }

        if (
            concepto.length() < 3 ||
            concepto.length() > 150
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El concepto debe tener entre 3 y 150 caracteres"
            );
        }

        if (
            datos.monto() == null ||
            datos.monto() <= 0L ||
            datos.monto() > Integer.MAX_VALUE
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El monto ingresado no es válido"
            );
        }

        LocalDate fecha = convertirFecha(
            datos.fechaVencimiento()
        );

        if (fecha.isBefore(
            LocalDate.now(ZONA_CHILE)
        )) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El vencimiento no puede ser anterior a hoy"
            );
        }

        validarCuotas(datos);

        if (datos.numeroCuota() != null) {

            boolean existeCuota =
                compromisoRepository
                    .existsByUsuarioIdAndEmpresaIgnoreCaseAndConceptoIgnoreCaseAndNumeroCuota(
                        datos.usuarioId(),
                        empresa,
                        concepto,
                        datos.numeroCuota()
                    );

            if (existeCuota) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe esa cuota para el usuario"
                );
            }
        }

        Empresa empresaGuardada = empresaRepository
            .findByNombreIgnoreCase(empresa)
            .orElse(null);

        if (empresaGuardada == null) {

            Empresa nuevaEmpresa = new Empresa();

            nuevaEmpresa.setNombre(empresa);

            empresaGuardada =
                empresaRepository.save(nuevaEmpresa);
        }

        Compromiso compromiso = new Compromiso();

        String folio = "CP-" +
            UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);

        compromiso.setUsuarioId(
            datos.usuarioId()
        );

        compromiso.setFolio(folio);

        compromiso.setEmpresa(
            empresaGuardada.getNombre()
        );

        compromiso.setConcepto(concepto);

        compromiso.setMonto(datos.monto());

        compromiso.setRecargo(0L);

        compromiso.setFechaVencimiento(fecha);

        compromiso.setEstado(
            EstadoCompromiso.PENDIENTE
        );

        compromiso.setNumeroCuota(
            datos.numeroCuota()
        );

        compromiso.setTotalCuotas(
            datos.totalCuotas()
        );

        compromiso.setReferenciaPlan(
            datos.referenciaPlan() == null
                ? null
                : datos.referenciaPlan().trim()
        );

        Compromiso guardado =
            compromisoRepository.save(compromiso);

        return aRespuesta(guardado);
    }

    @Transactional(readOnly = true)
    public List<CompromisoResponse> listarCompromisos(
        Long usuarioId
    ) {

        validarUsuarioId(usuarioId);

        return compromisoRepository
            .findByUsuarioIdOrderByFechaVencimientoAscIdAsc(
                usuarioId
            )
            .stream()
            .map(this::aRespuesta)
            .toList();
    }

    @Transactional(readOnly = true)
    public CompromisoResponse obtenerCompromiso(
        Long usuarioId,
        String folio
    ) {

        validarUsuarioId(usuarioId);

        if (folio == null || folio.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El folio no es válido"
            );
        }

        Compromiso compromiso = compromisoRepository
            .findByUsuarioIdAndFolio(
                usuarioId,
                folio.trim()
            )
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Compromiso no encontrado"
                )
            );

        return aRespuesta(compromiso);
    }

    @Transactional(readOnly = true)
    public List<String> listarEmpresas() {

        return empresaRepository
            .findAllByOrderByNombreAsc()
            .stream()
            .map(Empresa::getNombre)
            .toList();
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

    private void validarCuotas(
        RegistrarCompromisoRequest datos
    ) {

        Integer numero = datos.numeroCuota();
        Integer total = datos.totalCuotas();
        String plan = datos.referenciaPlan();

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
            plan.isBlank()
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes completar todos los datos de las cuotas"
            );
        }

        if (
            numero < 1 ||
            total < 1 ||
            numero > total ||
            plan.trim().length() > 80
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Los datos de las cuotas no son válidos"
            );
        }
    }

    private LocalDate convertirFecha(
        String texto
    ) {

        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar una fecha de vencimiento"
            );
        }

        try {

            return LocalDate.parse(
                texto.trim(),
                FORMATO_FECHA
            );

        } catch (DateTimeParseException e) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha debe tener el formato dd-MM-yyyy"
            );
        }
    }

    private String limpiar(String texto) {

        return texto == null
            ? ""
            : texto.trim();
    }

    private CompromisoResponse aRespuesta(
        Compromiso compromiso
    ) {

        return new CompromisoResponse(
            compromiso.getId(),
            compromiso.getUsuarioId(),
            compromiso.getFolio(),
            compromiso.getEmpresa(),
            compromiso.getConcepto(),
            compromiso.getMonto(),
            compromiso.getRecargo(),
            compromiso.getFechaVencimiento()
                .format(FORMATO_FECHA),
            compromiso.getEstado().name(),
            compromiso.getNumeroCuota(),
            compromiso.getTotalCuotas(),
            compromiso.getReferenciaPlan()
        );
    }
}
