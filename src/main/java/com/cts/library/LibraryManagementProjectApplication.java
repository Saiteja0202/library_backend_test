package com.cts.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryManagementProjectApplication extends ServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(LibraryManagementProjectApplication.class, args);
	}
	
}
