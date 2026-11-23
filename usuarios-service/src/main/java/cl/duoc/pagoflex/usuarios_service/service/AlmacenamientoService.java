package cl.duoc.pagoflex.usuarios_service.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class AlmacenamientoService {

    private static final long TAMANO_MAXIMO = 2_000_000L;

    private final Path carpeta;

    public AlmacenamientoService() {

        carpeta = Path.of(
            System.getProperty("user.home"),
            "PagoFlex",
            "avatares"
        ).toAbsolutePath().normalize();

        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new IllegalStateException(
                "No se pudo crear la carpeta de fotografías",
                e
            );
        }
    }

    public String guardar(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes seleccionar una fotografía"
            );
        }

        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fotografía supera el tamaño permitido"
            );
        }

        if (!"image/jpeg".equals(archivo.getContentType())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fotografía debe estar en formato JPEG"
            );
        }

        try {
            byte[] contenido = archivo.getBytes();

            if (
                contenido.length < 4 ||
                (contenido[0] & 0xFF) != 0xFF ||
                (contenido[1] & 0xFF) != 0xD8 ||
                (contenido[contenido.length - 2] & 0xFF) != 0xFF ||
                (contenido[contenido.length - 1] & 0xFF) != 0xD9
            ) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El archivo no es una imagen JPEG válida"
                );
            }

            String nombre = UUID.randomUUID() + ".jpg";

            Path destino = carpeta.resolve(nombre);

            Files.write(
                destino,
                contenido
            );

            return nombre;

        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo guardar la fotografía"
            );
        }
    }

    public byte[] leer(String nombre) {

        Path archivo = rutaSegura(nombre);

        try {
            return Files.readAllBytes(archivo);
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "La fotografía no está disponible"
            );
        }
    }

    public void eliminar(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return;
        }

        Path archivo = rutaSegura(nombre);

        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            // La fotografía anterior no pudo eliminarse.
        }
    }

    private Path rutaSegura(String nombre) {

        if (
            nombre == null ||
            !nombre.matches(
                "[0-9a-fA-F-]{36}\\.jpg"
            )
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nombre de fotografía inválido"
            );
        }

        Path ruta = carpeta.resolve(nombre).normalize();

        if (!ruta.startsWith(carpeta)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ruta de fotografía inválida"
            );
        }

        return ruta;
    }
}