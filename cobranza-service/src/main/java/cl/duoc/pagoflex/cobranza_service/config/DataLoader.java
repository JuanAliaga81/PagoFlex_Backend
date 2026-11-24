
package cl.duoc.pagoflex.cobranza_service.config;

import java.time.LocalDate;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import cl.duoc.pagoflex.cobranza_service.dto.UsuarioRemotoResponse;

import cl.duoc.pagoflex.cobranza_service.model.Empresa;
import cl.duoc.pagoflex.cobranza_service.model.EjecutivoEmpresa;
import cl.duoc.pagoflex.cobranza_service.model.Compromiso;
import cl.duoc.pagoflex.cobranza_service.model.EstadoCompromiso;

import cl.duoc.pagoflex.cobranza_service.repo.EmpresaRepository;
import cl.duoc.pagoflex.cobranza_service.repo.EjecutivoEmpresaRepository;
import cl.duoc.pagoflex.cobranza_service.repo.CompromisoRepository;

@Configuration
public class DataLoader {

    @Value("${usuarios.service.url}")
    private String urlUsuarios;

    @Bean
    public CommandLineRunner initData(
            EmpresaRepository empresaRepo,
            EjecutivoEmpresaRepository ejecutivoRepo,
            CompromisoRepository compromisoRepo) {

        return args -> {

            if (empresaRepo.count() == 0) {

                empresaRepo.save(
                        crearEmpresa("Conecta Telecom", 2500L));

                empresaRepo.save(
                        crearEmpresa("ElectroSur", 3000L));

                empresaRepo.save(
                        crearEmpresa("Aguas del Norte", 1500L));

                System.out.println("Datos cargados: empresas");

            } else {
                System.out.println("No se insertaron datos: empresas");
            }


            if (ejecutivoRepo.count() == 0) {

                Long ejecutivoId = obtenerUsuarioId(
                        "ejecutivo@pagoflex.com");

                Empresa empresa = empresaRepo
                        .findByNombreIgnoreCase("Conecta Telecom")
                        .orElseThrow();

                EjecutivoEmpresa asignacion = new EjecutivoEmpresa();

                asignacion.setEjecutivoId(ejecutivoId);
                asignacion.setEmpresa(empresa);

                ejecutivoRepo.save(asignacion);

                System.out.println("Datos cargados: ejecutivos_empresas");

            } else {
                System.out.println("No se insertaron datos: ejecutivos_empresas");
            }


            if (compromisoRepo.count() == 0) {

                Long usuarioId = obtenerUsuarioId(
                        "juanaliaga.cu@pagoflex.com");

                LocalDate hoy = LocalDate.now();

                compromisoRepo.save(
                        crearCompromiso(
                                usuarioId,
                                "Conecta Telecom",
                                "CT-10001",
                                "Plan internet hogar fibra óptica",
                                24990L,
                                0L,
                                hoy.plusDays(10),
                                EstadoCompromiso.PENDIENTE));

                compromisoRepo.save(
                        crearCompromiso(
                                usuarioId,
                                "ElectroSur",
                                "ES-20001",
                                "Consumo mensual de electricidad",
                                38560L,
                                0L,
                                hoy.plusDays(15),
                                EstadoCompromiso.PENDIENTE));

                compromisoRepo.save(
                        crearCompromiso(
                                usuarioId,
                                "Aguas del Norte",
                                "AN-30001",
                                "Consumo mensual de agua potable",
                                18750L,
                                1500L,
                                hoy.minusDays(5),
                                EstadoCompromiso.VENCIDO));

                System.out.println("Datos cargados: compromisos");

            } else {
                System.out.println("No se insertaron datos: compromisos");
            }

        };
    }

    private Empresa crearEmpresa(
            String nombre,
            Long recargo) {

        Empresa empresa = new Empresa();

        empresa.setNombre(nombre);
        empresa.setRecargoFijo(recargo);

        return empresa;
    }

    private Compromiso crearCompromiso(
            Long usuarioId,
            String empresa,
            String folio,
            String concepto,
            Long monto,
            Long recargo,
            LocalDate fecha,
            EstadoCompromiso estado) {

        Compromiso compromiso = new Compromiso();

        compromiso.setUsuarioId(usuarioId);
        compromiso.setEmpresa(empresa);
        compromiso.setFolio(folio);
        compromiso.setConcepto(concepto);
        compromiso.setMonto(monto);
        compromiso.setRecargo(recargo);
        compromiso.setFechaVencimiento(fecha);
        compromiso.setEstado(estado);

        return compromiso;
    }

    private Long obtenerUsuarioId(String correo) {

        UsuarioRemotoResponse[] usuarios = RestClient.builder()
                .baseUrl(urlUsuarios)
                .build()
                .get()
                .uri("/api/usuarios")
                .retrieve()
                .body(UsuarioRemotoResponse[].class);

        return Arrays.stream(usuarios)
                .filter(usuario ->
                        correo.equalsIgnoreCase(usuario.correo()))
                .map(UsuarioRemotoResponse::id)
                .findFirst()
                .orElseThrow();
    }
}
