
package cl.duoc.pagoflex.usuarios_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.pagoflex.usuarios_service.dto.RegistroRequest;
import cl.duoc.pagoflex.usuarios_service.model.Usuario;
import cl.duoc.pagoflex.usuarios_service.repo.UsuarioRepository;
import cl.duoc.pagoflex.usuarios_service.service.UsuarioService;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initData(
            UsuarioRepository usuarioRepo,
            UsuarioService usuarioService) {

        return args -> {

            if (usuarioRepo.count() == 0) {

                String contrasena = "pagoflex123";

                crearUsuario(
                        usuarioRepo,
                        usuarioService,
                        "Juan Aliaga",
                        "juanaliaga.cu@pagoflex.com",
                        "21851141-4",
                        contrasena,
                        "USUARIO_FINAL");

                crearUsuario(
                        usuarioRepo,
                        usuarioService,
                        "EjecutivoPagoflex",
                        "ejecutivo@pagoflex.com",
                        "900000022",
                        contrasena,
                        "ADMINISTRADOR");

                crearUsuario(
                        usuarioRepo,
                        usuarioService,
                        "AgentePagoflex",
                        "agente@pagoflex.com",
                        "900000030",
                        contrasena,
                        "AGENTE");

                System.out.println("Datos cargados: usuarios");

            } else {
                System.out.println("No se insertaron datos: usuarios");
            }
        };
    }

    private void crearUsuario(
            UsuarioRepository usuarioRepo,
            UsuarioService usuarioService,
            String nombre,
            String correo,
            String rut,
            String contrasena,
            String rol) {

        var respuesta = usuarioService.registrar(
                new RegistroRequest(
                        nombre,
                        correo,
                        rut,
                        contrasena,
                        contrasena));

        Usuario usuario = usuarioRepo.findById(respuesta.id())
                .orElseThrow();

        usuario.setRol(rol);

        usuarioRepo.save(usuario);
    }
}
