package cl.duoc.pagoflex.usuarios_service.service;

import cl.duoc.pagoflex.usuarios_service.dto.UsuarioResponse;
import cl.duoc.pagoflex.usuarios_service.model.Usuario;
import cl.duoc.pagoflex.usuarios_service.repo.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FotoPerfilService {

    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoService almacenamiento;

    public FotoPerfilService(
        UsuarioRepository usuarioRepository,
        AlmacenamientoService almacenamiento
    ) {
        this.usuarioRepository = usuarioRepository;
        this.almacenamiento = almacenamiento;
    }

    public UsuarioResponse guardar(
        Long usuarioId,
        MultipartFile archivo
    ) {

        Usuario usuario = buscarUsuario(usuarioId);

        String fotoAnterior = usuario.getFotoPerfil();

        String fotoNueva = almacenamiento.guardar(
            archivo
        );

        try {

            usuario.setFotoPerfil(fotoNueva);

            Usuario actualizado =
                usuarioRepository.saveAndFlush(usuario);

            almacenamiento.eliminar(fotoAnterior);

            return respuesta(actualizado);

        } catch (RuntimeException e) {

            almacenamiento.eliminar(fotoNueva);

            throw e;
        }
    }

    public byte[] obtener(Long usuarioId) {

        Usuario usuario = buscarUsuario(usuarioId);

        String foto = usuario.getFotoPerfil();

        if (foto == null || foto.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "El usuario todavía no tiene fotografía"
            );
        }

        return almacenamiento.leer(foto);
    }

    private Usuario buscarUsuario(Long usuarioId) {

        if (usuarioId == null || usuarioId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Identificador inválido"
            );
        }

        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuario no encontrado"
                )
            );
    }

    private UsuarioResponse respuesta(Usuario usuario) {

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
}