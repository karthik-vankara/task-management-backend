package com.karthik.task_management_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskManagementBackendApplication {

	public static void main(String[] args) {
		// Load environment variables from .env.local
		Dotenv dotenv = Dotenv.configure()
				.filename(".env.local")
				.ignoreIfMissing()
				.load();
		
		// Set as system properties
		dotenv.entries().forEach(entry -> {
			String key = entry.getKey();
			String value = entry.getValue();
			if (System.getProperty(key) == null) {
				System.setProperty(key, value);
			}
		});
		
		SpringApplication.run(TaskManagementBackendApplication.class, args);
	}

}
