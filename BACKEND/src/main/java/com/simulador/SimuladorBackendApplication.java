package com.simulador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

@SpringBootApplication
public class SimuladorBackendApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(SimuladorBackendApplication.class);
		String mongoUri = System.getenv("MONGODB_URI");

		if (mongoUri != null && !mongoUri.isBlank()) {
			String configuredMongoUri = mongoUri.strip();
			application.addInitializers(context -> context.getEnvironment().getPropertySources().addFirst(
					new MapPropertySource("mongodbUriEnvironmentVariable",
							Map.of("spring.data.mongodb.uri", configuredMongoUri))));
			System.out.println("MongoDB URI loaded from MONGODB_URI environment variable.");
		} else {
			System.out.println("MONGODB_URI is not set; using the configured local MongoDB URI.");
		}

		application.run(args);
	}

}
