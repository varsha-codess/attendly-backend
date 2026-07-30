package com.attendly.attendly_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AttendlyBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(AttendlyBackendApplication.class, args);
	}
}