package com.agaramtech.qualis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is the main class from which the application starts.
 */
@SpringBootApplication
@EnableScheduling
public class QualisApplication{

	public static void main(String[] args) {
		SpringApplication.run(QualisApplication.class, args);
	}

}
