package com.idp.idpapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IdpapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdpapiApplication.class, args);
	}

}
