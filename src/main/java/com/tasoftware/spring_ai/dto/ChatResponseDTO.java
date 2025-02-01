package com.tasoftware.spring_ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponseDTO {

  private String response;
  private int tokenCount;
}
