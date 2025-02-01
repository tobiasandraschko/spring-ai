package com.tasoftware.spring_ai.controller;

import com.tasoftware.spring_ai.dto.ChatResponseDTO;
import com.tasoftware.spring_ai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat API with conversation memory")
@Slf4j
public class ChatController {

  private final ChatService chatService;

  @Operation(
    summary = "Chat with AI",
    description = "Send a message and get a response (with token count) while maintaining conversation context"
  )
  @ApiResponse(
    responseCode = "200",
    description = "AI response with token count",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(
        implementation = ChatResponseDTO.class,
        example = "{\"response\": \"This is an AI response\", \"tokenCount\": 6}"
      )
    )
  )
  @GetMapping("/chat/{conversationId}")
  public ChatResponseDTO chat(
    @Parameter(
      description = "Conversation ID to maintain context",
      example = "123e4567-e89b-12d3-a456-426614174000"
    ) @PathVariable("conversationId") String conversationId,
    @Parameter(
      description = "Message to send to the AI",
      example = "What is the capital of France?"
    ) @RequestParam(name = "message") String message
  ) {
    log.debug(
      "Received chat request for conversationId: {} with message: {}",
      conversationId,
      message
    );
    ChatResponseDTO response = chatService.processMessage(
      conversationId,
      message
    );
    log.debug("Sending response: {}", response);
    return response;
  }
}
