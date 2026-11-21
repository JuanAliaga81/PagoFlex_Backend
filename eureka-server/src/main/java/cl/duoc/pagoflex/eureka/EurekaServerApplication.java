package cl.duoc.pagoflex.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(
			EurekaServerApplication.class, 
			args);
			System.out.println("Servidor Eureka iniciado en el puerto 8761");
	}
}
