package com.example.order_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OrderClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderClientApplication.class, args);
	}
}
