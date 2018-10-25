package com.lennoxpro.document.controllers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocumentSolrApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentSolrApplication.class, args);
		//mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
	}
}
