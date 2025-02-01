package com.tasoftware.spring_ai.model;

import lombok.Data;

@Data
public class ChatMessage {

  private String role;
  private String content;

  public ChatMessage(String role, String content) {
    this.role = role;
    this.content = content;
  }
}
