
package cl.duoc.pagoflex.usuarios_service.service;

import cl.duoc.pagoflex.usuarios_service.dto.ConfirmarRecuperacionRequest;
import cl.duoc.pagoflex.usuarios_service.dto.SolicitarRecuperacionRequest;
import cl.duoc.pagoflex.usuarios_service.dto.VerificarRecuperacionRequest;

import cl.duoc.pagoflex.usuarios_service.model.CodigoRecuperacion;
import cl.duoc.pagoflex.usuarios_service.model.Usuario;

import cl.duoc.pagoflex.usuarios_service.repo.CodigoRecuperacionRepository;
import cl.duoc.pagoflex.usuarios_service.repo.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import java.time.Duration;
import java.time.Instant;

import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class RecuperacionContrasenaService {

    private static final SecureRandom GENERADOR =
        new SecureRandom();

    private static final Duration DURACION =
        Duration.ofMinutes(5);

    private static final Duration ESPERA_REENVIO =
        Duration.ofSeconds(60);

    private static final int MAX_INTENTOS = 3;

    private final UsuarioRepository usuarioRepository;

    private final CodigoRecuperacionRepository
        codigoRepository;

    private final CorreoRecuperacionService
        correoRecuperacionService;

    private final byte[] claveFirma = new byte[32];

    // ========================================
    // CONSTRUCTOR
    // ========================================

    public RecuperacionContrasenaService(
        UsuarioRepository usuarioRepository,
        CodigoRecuperacionRepository codigoRepository,
        CorreoRecuperacionService correoRecuperacionService
    ) {

        this.usuarioRepository =
            usuarioRepository;

        this.codigoRepository =
            codigoRepository;

        this.correoRecuperacionService =
            correoRecuperacionService;

        // Clave para proteger los codigos.
        // Cambia al reiniciar el servidor.

        GENERADOR.nextBytes(claveFirma);
    }

    // ========================================
    // 1. SOLICITAR CODIGO
    // ========================================

    @Transactional
    public void solicitar(
        SolicitarRecuperacionRequest datos
    ) {

        String correo = normalizarCorreo(
            datos == null ? null : datos.correo()
        );

        validarCorreo(correo);

        Usuario usuario = usuarioRepository
            .findByCorreoIgnoreCase(correo)
            .orElse(null);

        // No revelar si el correo existe.
        // La respuesta HTTP sera la misma.

        if (
            usuario == null ||
            !Boolean.TRUE.equals(usuario.getActivo())
        ) {
            return;
        }

        Instant ahora = Instant.now();

        CodigoRecuperacion registro =
            codigoRepository
                .findByUsuarioId(usuario.getId())
                .orElseGet(CodigoRecuperacion::new);

        // Esperar 60 segundos entre envios.

        if (
            registro.getUltimoEnvio() != null &&
            ahora.isBefore(
                registro.getUltimoEnvio()
                    .plus(ESPERA_REENVIO)
            )
        ) {
            return;
        }

        // Codigo aleatorio de seis digitos.

        String codigo = String.format(
            Locale.ROOT,
            "%06d",
            GENERADOR.nextInt(1_000_000)
        );

        registro.setUsuarioId(
            usuario.getId()
        );

        registro.setCodigoHash(
            generarFirma(
                usuario.getId(),
                codigo
            )
        );

        registro.setVenceEn(
            ahora.plus(DURACION)
        );

        registro.setUltimoEnvio(ahora);

        registro.setIntentos(0);

        codigoRepository.save(registro);

        // Enviar al correo real registrado.
        // Si falla el envio, la transaccion
        // revierte el nuevo codigo guardado.

        correoRecuperacionService.enviarCodigo(
            usuario.getCorreo(),
            codigo
        );
    }

    // ========================================
    // 2. VERIFICAR CODIGO
    // ========================================

    @Transactional(
        noRollbackFor = ResponseStatusException.class
    )
    public void verificar(
        VerificarRecuperacionRequest datos
    ) {

        if (datos == null) {
            throw codigoInvalido();
        }

        validarCodigo(
            normalizarCorreo(datos.correo()),
            datos.codigo()
        );
    }

    // ========================================
    // 3. CONFIRMAR CONTRASEÑA
    // ========================================

    @Transactional(
        noRollbackFor = ResponseStatusException.class
    )
    public void confirmar(
        ConfirmarRecuperacionRequest datos
    ) {

        if (datos == null) {
            throw codigoInvalido();
        }

        Usuario usuario = validarCodigo(
            normalizarCorreo(datos.correo()),
            datos.codigo()
        );

        String nueva = datos.nuevaContrasena();

        if (
            nueva == null ||
            nueva.length() < 8 ||
            nueva.length() > 128
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La contraseña debe tener entre 8 y 128 caracteres"
            );
        }

        if (esContrasenaActual(usuario, nueva)) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La nueva contraseña debe ser diferente"
            );
        }

        establecerContrasena(
            usuario,
            nueva
        );

        usuarioRepository.save(usuario);

        // Invalidar el codigo inmediatamente.

        codigoRepository
            .findByUsuarioId(usuario.getId())
            .ifPresent(codigoRepository::delete);
    }

    // ========================================
    // 4. VALIDAR CODIGO EN MYSQL
    // ========================================

    private Usuario validarCodigo(
        String correo,
        String codigo
    ) {

        if (
            codigo == null ||
            !codigo.matches("[0-9]{6}")
        ) {
            throw codigoInvalido();
        }

        Usuario usuario = usuarioRepository
            .findByCorreoIgnoreCase(correo)
            .orElseThrow(this::codigoInvalido);

        if (
            !Boolean.TRUE.equals(usuario.getActivo())
        ) {
            throw codigoInvalido();
        }

        CodigoRecuperacion registro =
            codigoRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow(this::codigoInvalido);

        if (
            registro.getVenceEn() == null ||
            !Instant.now().isBefore(
                registro.getVenceEn()
            )
        ) {

            codigoRepository.delete(registro);

            throw codigoInvalido();
        }

        if (
            registro.getIntentos() >= MAX_INTENTOS
        ) {

            codigoRepository.delete(registro);

            throw codigoInvalido();
        }

        String firmaIngresada = generarFirma(
            usuario.getId(),
            codigo
        );

        boolean coincide = MessageDigest.isEqual(
            firmaIngresada.getBytes(
                StandardCharsets.UTF_8
            ),
            registro.getCodigoHash().getBytes(
                StandardCharsets.UTF_8
            )
        );

        if (!coincide) {

            int intentos =
                registro.getIntentos() + 1;

            registro.setIntentos(intentos);

            if (intentos >= MAX_INTENTOS) {

                codigoRepository.delete(registro);

            } else {

                codigoRepository.save(registro);
            }

            throw codigoInvalido();
        }

        return usuario;
    }

    // ========================================
    // 5. PROTEGER CODIGO CON HMAC
    // ========================================

    private String generarFirma(
        Long usuarioId,
        String codigo
    ) {

        try {

            Mac mac = Mac.getInstance(
                "HmacSHA256"
            );

            mac.init(
                new SecretKeySpec(
                    claveFirma,
                    "HmacSHA256"
                )
            );

            byte[] resultado = mac.doFinal(
                (usuarioId + ":" + codigo).getBytes(
                    StandardCharsets.UTF_8
                )
            );

            return HexFormat.of()
                .formatHex(resultado);

        } catch (Exception e) {

            throw new IllegalStateException(
                "No se pudo proteger el codigo",
                e
            );
        }
    }

    // ========================================
    // 6. COMPARAR CONTRASEÑA ACTUAL
    // ========================================

    private boolean esContrasenaActual(
        Usuario usuario,
        String nueva
    ) {

        if (
            usuario.getSal() == null ||
            usuario.getContrasenaHash() == null
        ) {
            return false;
        }

        try {

            byte[] sal = Base64.getDecoder()
                .decode(usuario.getSal());

            byte[] guardado = Base64.getDecoder()
                .decode(usuario.getContrasenaHash());

            byte[] calculado = generarHash(
                nueva,
                sal
            );

            return MessageDigest.isEqual(
                guardado,
                calculado
            );

        } catch (IllegalArgumentException e) {

            return false;
        }
    }

    // ========================================
    // 7. ACTUALIZAR CONTRASEÑA
    // ========================================

    private void establecerContrasena(
        Usuario usuario,
        String nueva
    ) {

        byte[] sal = new byte[16];

        GENERADOR.nextBytes(sal);

        byte[] hash = generarHash(
            nueva,
            sal
        );

        usuario.setSal(
            Base64.getEncoder()
                .encodeToString(sal)
        );

        usuario.setContrasenaHash(
            Base64.getEncoder()
                .encodeToString(hash)
        );
    }

    // ========================================
    // 8. HASH COMPATIBLE CON LOGIN
    // ========================================

    private byte[] generarHash(
        String contrasena,
        byte[] sal
    ) {

        PBEKeySpec especificacion =
            new PBEKeySpec(
                contrasena.toCharArray(),
                sal,
                600000,
                256
            );

        try {

            SecretKeyFactory fabrica =
                SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA1"
                );

            return fabrica
                .generateSecret(especificacion)
                .getEncoded();

        } catch (Exception e) {

            throw new IllegalStateException(
                "No se pudo procesar la contraseña",
                e
            );

        } finally {

            especificacion.clearPassword();
        }
    }

    // ========================================
    // 9. NORMALIZAR CORREO
    // ========================================

    private String normalizarCorreo(
        String correo
    ) {

        return correo == null
            ? ""
            : correo.trim()
                .toLowerCase(Locale.ROOT);
    }

    // ========================================
    // 10. VALIDAR FORMATO DEL CORREO
    // ========================================

    private void validarCorreo(
        String correo
    ) {

        if (
            correo.isBlank() ||
            correo.length() > 120 ||
            !correo.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar un correo válido"
            );
        }
    }

    // ========================================
    // 11. ERROR DE CODIGO INVALIDO
    // ========================================

    private ResponseStatusException codigoInvalido() {

        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Código incorrecto o vencido"
        );
    }
}
