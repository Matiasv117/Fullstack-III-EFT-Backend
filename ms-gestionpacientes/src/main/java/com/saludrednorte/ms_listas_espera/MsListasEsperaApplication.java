package com.saludrednorte.ms_listas_espera;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "Patient Management Service", version = "1.0", description = "Gestión de pacientes y listas de espera"))
@SpringBootApplication
@EnableFeignClients
public class MsListasEsperaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsListasEsperaApplication.class, args);
	}

}
