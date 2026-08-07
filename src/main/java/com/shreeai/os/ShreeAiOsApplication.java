package com.shreeai.os;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShreeAiOsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShreeAiOsApplication.class, args);
	}

}
