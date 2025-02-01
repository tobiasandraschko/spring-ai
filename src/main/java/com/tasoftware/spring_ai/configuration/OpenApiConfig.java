package com.tasoftware.spring_ai.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
      .info(
        new Info()
          .title("Spring AI Chat API")
          .description("API for interacting with AI chat functionality")
          .version("1.0.0")
      );
  }
}
