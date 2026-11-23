package cl.duoc.pagoflex.usuarios_service.controller;

import cl.duoc.pagoflex.usuarios_service.dto.UsuarioResponse;
import cl.duoc.pagoflex.usuarios_service.service.FotoPerfilService;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/usuarios")
public class FotoPerfilController {

    private final FotoPerfilService fotoPerfilService;

    public FotoPerfilController(
        FotoPerfilService fotoPerfilService
    ) {
        this.fotoPerfilService = fotoPerfilService;
    }

    @PostMapping(
        value = "/{id}/avatar",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UsuarioResponse subirAvatar(
        @PathVariable Long id,
        @RequestParam("archivo") MultipartFile archivo
    ) {

        return fotoPerfilService.guardar(
            id,
            archivo
        );
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> obtenerAvatar(
        @PathVariable Long id
    ) {

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .cacheControl(CacheControl.noStore())
            .body(
                fotoPerfilService.obtener(id)
            );
    }
}