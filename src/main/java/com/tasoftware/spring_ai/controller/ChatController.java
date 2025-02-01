package com.tasoftware.spring_ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat API endpoints")
public class ChatController {

  private final ChatClient chatClient;

  @Operation(
    summary = "Get LLM response",
    description = "Sends a message to the LLM and returns its response"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successful response",
    content = @Content(
      mediaType = "text/plain",
      schema = @Schema(type = "string")
    )
  )
  @GetMapping("/prompt")
  public String prompt(
    @Parameter(
      description = "The message to send to the LLM",
      required = true
    ) @RequestParam(name = "message", required = true) String message
  ) {
    return chatClient.prompt(message).call().content();
  }
}
