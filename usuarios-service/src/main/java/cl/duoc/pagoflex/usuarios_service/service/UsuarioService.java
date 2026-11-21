
package cl.duoc.pagoflex.usuarios_service.service;

import cl.duoc.pagoflex.usuarios_service.dto.ActualizarUsuarioRequest;
import cl.duoc.pagoflex.usuarios_service.dto.CambiarContrasenaRequest;
import cl.duoc.pagoflex.usuarios_service.dto.LoginRequest;
import cl.duoc.pagoflex.usuarios_service.dto.RegistroRequest;
import cl.duoc.pagoflex.usuarios_service.dto.UsuarioResponse;
import cl.duoc.pagoflex.usuarios_service.model.Usuario;
import cl.duoc.pagoflex.usuarios_service.repo.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(
        UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponse> listar(String buscar) {

        List<Usuario> usuarios;

        if (buscar == null || buscar.isBlank()) {

            usuarios =
                usuarioRepository.findAllByOrderByNombreAsc();

        } else {

            usuarios = usuarioRepository
                .findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(
                    buscar.trim(),
                    buscar.trim()
                );
        }

        return usuarios.stream()
            .map(this::aRespuesta)
            .toList();
    }

    public UsuarioResponse obtener(Long id) {

        Usuario usuario = buscarUsuario(id);

        return aRespuesta(usuario);
    }

    public UsuarioResponse registrar(
        RegistroRequest datos
    ) {

        if (datos == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar los datos del usuario"
            );
        }

        String nombre = limpiar(datos.nombre());
        String correo = normalizarCorreo(datos.correo());
        String rut = normalizarRut(datos.rut());

        validarNombre(nombre);
        validarCorreo(correo);

        if (!rutValido(rut)) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El RUT ingresado no es válido"
            );
        }

        validarContrasena(datos.contrasena());

        if (!datos.contrasena().equals(
            datos.confirmacion()
        )) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las contraseñas no coinciden"
            );
        }

        if (
            usuarioRepository.existsByCorreoIgnoreCase(
                correo
            )
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El correo ya está registrado"
            );
        }

        if (usuarioRepository.existsByRut(rut)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El RUT ya está registrado"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setRut(rut);
        usuario.setRol("USUARIO_FINAL");
        usuario.setActivo(true);

        establecerContrasena(
            usuario,
            datos.contrasena()
        );

        Usuario guardado = usuarioRepository.save(usuario);

        return aRespuesta(guardado);
    }

    public UsuarioResponse iniciarSesion(
        LoginRequest datos
    ) {

        if (
            datos == null ||
            datos.correo() == null ||
            datos.contrasena() == null
        ) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos"
            );
        }

        String correo = normalizarCorreo(
            datos.correo()
        );

        Usuario usuario = usuarioRepository
            .findByCorreoIgnoreCase(correo)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Correo o contraseña incorrectos"
                )
            );

        if (!verificarContrasena(
            datos.contrasena(),
            usuario
        )) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos"
            );
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "La cuenta se encuentra inactiva"
            );
        }

        return aRespuesta(usuario);
    }

    public UsuarioResponse actualizar(
        Long id,
        ActualizarUsuarioRequest datos
    ) {

        Usuario usuario = buscarUsuario(id);

        if (datos == null) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar los datos"
            );
        }

        String nombre = limpiar(datos.nombre());
        String correo = normalizarCorreo(datos.correo());

        validarNombre(nombre);
        validarCorreo(correo);

        if (
            usuarioRepository
                .existsByCorreoIgnoreCaseAndIdNot(
                    correo,
                    id
                )
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El correo ya pertenece a otra cuenta"
            );
        }

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);

        Usuario guardado = usuarioRepository.save(usuario);

        return aRespuesta(guardado);
    }

    public void cambiarContrasena(
        Long id,
        CambiarContrasenaRequest datos
    ) {

        Usuario usuario = buscarUsuario(id);

        if (
            datos == null ||
            datos.actual() == null
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar la contraseña actual"
            );
        }

        if (!verificarContrasena(
            datos.actual(),
            usuario
        )) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "La contraseña actual es incorrecta"
            );
        }

        validarContrasena(datos.nueva());

        establecerContrasena(
            usuario,
            datos.nueva()
        );

        usuarioRepository.save(usuario);
    }

    private Usuario buscarUsuario(Long id) {

        if (id == null || id <= 0L) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Identificador inválido"
            );
        }

        return usuarioRepository.findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuario no encontrado"
                )
            );
    }

    private UsuarioResponse aRespuesta(
        Usuario usuario
    ) {

        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getRut(),
            usuario.getRol(),
            usuario.getFotoPerfil(),
            usuario.getActivo()
        );
    }

    private String limpiar(String texto) {

        return texto == null
            ? ""
            : texto.trim();
    }

    private String normalizarCorreo(String correo) {

        return limpiar(correo)
            .toLowerCase(Locale.ROOT);
    }

    private String normalizarRut(String rut) {

        return limpiar(rut)
            .replace(".", "")
            .replace("-", "")
            .toUpperCase(Locale.ROOT);
    }

    private void validarNombre(String nombre) {

        if (
            nombre.length() < 2 ||
            nombre.length() > 80
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El nombre debe tener entre 2 y 80 caracteres"
            );
        }
    }

    private void validarCorreo(String correo) {

        boolean formatoValido = correo.matches(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );

        if (
            !formatoValido ||
            correo.length() > 120
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes ingresar un correo válido"
            );
        }
    }

    private void validarContrasena(
        String contrasena
    ) {

        if (
            contrasena == null ||
            contrasena.length() < 8
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La contraseña debe tener al menos 8 caracteres"
            );
        }
    }

    private boolean rutValido(String rut) {

        if (!rut.matches("[0-9]{7,8}[0-9K]")) {
            return false;
        }

        String numero = rut.substring(
            0,
            rut.length() - 1
        );

        char digito = rut.charAt(
            rut.length() - 1
        );

        int suma = 0;
        int multiplicador = 2;

        for (
            int i = numero.length() - 1;
            i >= 0;
            i--
        ) {

            int valor = Character.digit(
                numero.charAt(i),
                10
            );

            suma += valor * multiplicador;

            multiplicador++;

            if (multiplicador > 7) {
                multiplicador = 2;
            }
        }

        int resultado = 11 - (suma % 11);

        char esperado;

        if (resultado == 11) {

            esperado = '0';

        } else if (resultado == 10) {

            esperado = 'K';

        } else {

            esperado = Character.forDigit(
                resultado,
                10
            );
        }

        return digito == esperado;
    }

    private void establecerContrasena(
        Usuario usuario,
        String contrasena
    ) {

        byte[] salBytes = new byte[16];

        new SecureRandom().nextBytes(salBytes);

        byte[] hashBytes = generarHash(
            contrasena,
            salBytes
        );

        usuario.setContrasenaHash(
            Base64.getEncoder()
                .encodeToString(hashBytes)
        );

        usuario.setSal(
            Base64.getEncoder()
                .encodeToString(salBytes)
        );
    }

    private boolean verificarContrasena(
        String contrasena,
        Usuario usuario
    ) {

        if (
            contrasena == null ||
            usuario.getSal() == null ||
            usuario.getContrasenaHash() == null
        ) {

            return false;
        }

        try {

            byte[] sal = Base64.getDecoder()
                .decode(usuario.getSal());

            byte[] hashGuardado = Base64.getDecoder()
                .decode(usuario.getContrasenaHash());

            byte[] hashIngresado = generarHash(
                contrasena,
                sal
            );

            return MessageDigest.isEqual(
                hashGuardado,
                hashIngresado
            );

        } catch (IllegalArgumentException e) {

            return false;
        }
    }

    private byte[] generarHash(
        String contrasena,
        byte[] sal
    ) {

        PBEKeySpec especificacion = new PBEKeySpec(
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
}
