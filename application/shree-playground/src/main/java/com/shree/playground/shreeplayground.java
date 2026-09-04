package com.shree.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class shreeplayground {

	public static void main(String[] args) {
		System.out.println("DEBUG_ENV_KEY: " + System.getenv("GEMINI_API_KEY"));
		System.out.println("DEBUG_ENV_CHAIN: " + System.getenv("SHREE_LLM_CHAIN"));
		SpringApplication.run(shreeplayground.class, args);
	}

}
