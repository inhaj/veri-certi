package com.vericerti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VericertiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VericertiApplication.class, args);
	}

}
