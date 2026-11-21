package com.example.order_client.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${order.cors-allowed-origins}")
  private String[] allowedOrigins;

  /** Em producao o front e servido pelo mesmo host (nginx); o CORS serve ao dev com Vite. */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**").allowedOrigins(allowedOrigins).allowedMethods("*");
  }

  @Bean
  RestClient workerRestClient(@Value("${order.worker-url}") String workerUrl) {
    return RestClient.builder().baseUrl(workerUrl).build();
  }

  /** Virtual threads (Java 21) para disparar rajadas de pedidos sem bloquear requisicoes. */
  @Bean(destroyMethod = "close")
  ExecutorService simulationExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
