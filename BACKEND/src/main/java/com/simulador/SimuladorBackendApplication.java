package com.simulador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada que inicia la aplicación Spring Boot. */
@SpringBootApplication
public class SimuladorBackendApplication {

	/** Inicia el servidor y carga el contexto de Spring Boot. */
	public static void main(String[] args) {
		SpringApplication.run(SimuladorBackendApplication.class, args);
	}

}
