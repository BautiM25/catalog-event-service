package ar.edu.unnoba.pdyc2026.catalog_event_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class CatalogEventServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogEventServiceApplication.class, args);
	}

}
