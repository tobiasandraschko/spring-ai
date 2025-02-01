package com.tasoftware.spring_ai.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

  @Bean
  public ChatClient chatClient() {
    OllamaApi ollamaApi = new OllamaApi("http://localhost:11434");

    OllamaChatModel chatModel = OllamaChatModel
      .builder()
      .ollamaApi(ollamaApi)
      .defaultOptions(OllamaOptions.builder().model("deepseek-r1").build())
      .build();

    return ChatClient.create(chatModel);
  }
}
