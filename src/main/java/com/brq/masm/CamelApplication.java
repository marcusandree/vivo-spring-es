package com.brq.masm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
public class CamelApplication {

	private static Logger LOGGER = LogManager.getLogger(CamelApplication.class);

	public static void main(String[] args) {
		LOGGER.info("Inicializando aplicacao");
		SpringApplication.run(CamelApplication.class, args);
	}
}
