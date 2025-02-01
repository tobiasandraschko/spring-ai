package com.tasoftware.spring_ai.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Conversation {

  private String id;
  private List<ChatMessage> messages;
  private String summary;

  public Conversation(String id) {
    this.id = id;
    this.messages = new ArrayList<>();
  }
}
