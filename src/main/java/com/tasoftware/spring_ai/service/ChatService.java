package com.tasoftware.spring_ai.service;

import com.tasoftware.spring_ai.dto.ChatResponseDTO;
import com.tasoftware.spring_ai.model.ChatMessage;
import com.tasoftware.spring_ai.model.Conversation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

  private final ChatClient chatClient;
  private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
  private static final int TOKEN_LIMIT = 8000;
  private static final int CHUNK_SIZE = 5;

  public ChatResponseDTO processMessage(
    String conversationId,
    String userMessage
  ) {
    log.debug(
      "Processing message for conversationId: {} - userMessage: {}",
      conversationId,
      userMessage
    );

    // Retrieve or create conversation.
    Conversation conversation = conversations.computeIfAbsent(
      conversationId,
      Conversation::new
    );

    // Add the user's message.
    conversation.getMessages().add(new ChatMessage("user", userMessage));
    log.debug(
      "Added user message. Total messages: {}",
      conversation.getMessages().size()
    );

    // Check for summarization conditions.
    int totalTokens = countTokens(conversation);
    log.debug("Total tokens in conversation: {}", totalTokens);
    if (
      conversation.getMessages().size() > CHUNK_SIZE &&
      totalTokens > TOKEN_LIMIT
    ) {
      log.debug("Token limit exceeded, summarizing conversation...");
      conditionallySummarizeConversation(conversation);
      log.debug(
        "Summarization complete. New total messages: {}",
        conversation.getMessages().size()
      );
    }

    // Build prompt with conversation context (including summary and unsummarized messages).
    String promptText = buildPrompt(conversation, userMessage);
    log.debug("Built prompt text:\n{}", promptText);

    // Get AI response.
    String rawResponse = chatClient.prompt(promptText).call().content();
    log.debug("Raw response received: {}", rawResponse);

    // Remove reasoning enclosed in <think>...</think> tags.
    String cleanedResponse = stripReasoning(rawResponse);
    log.debug("Cleaned response: {}", cleanedResponse);

    // Add the assistant's response to the conversation.
    conversation
      .getMessages()
      .add(new ChatMessage("assistant", cleanedResponse));
    log.debug(
      "Added assistant message. Total messages: {}",
      conversation.getMessages().size()
    );

    // The token count is computed from the prompt input.
    int tokenCount = countTokens(promptText);
    log.debug("Token count of prompt: {}", tokenCount);
    return new ChatResponseDTO(cleanedResponse, tokenCount);
  }

  /**
   * Strips out any content between <think> and </think> tags including the tags.
   * The (?s) flag (DOTALL) makes the dot match across line breaks.
   */
  private String stripReasoning(String text) {
    if (text == null) {
      return null;
    }
    String cleanedText = text.replaceAll("(?s)<think>.*?</think>", "").trim();
    log.debug("stripReasoning - before: [{}] after: [{}]", text, cleanedText);
    return cleanedText;
  }

  /**
   * If the overall token count exceeds TOKEN_LIMIT, summarize groups of CHUNK_SIZE messages
   * from the beginning of the conversation (keeping the last CHUNK_SIZE messages intact).
   * The summary is stored in the conversation object.
   */
  private void conditionallySummarizeConversation(Conversation conversation) {
    List<ChatMessage> originalMessages = conversation.getMessages();
    int totalMessages = originalMessages.size();
    log.debug(
      "Conditionally summarizing conversation. Total messages: {}",
      totalMessages
    );

    // Preserve the last CHUNK_SIZE messages.
    int preserveStartIndex = Math.max(CHUNK_SIZE, totalMessages - CHUNK_SIZE);
    if (preserveStartIndex <= 0) {
      log.debug("Nothing to summarize.");
      return;
    }

    StringBuilder summaryBuilder = new StringBuilder();

    // Summarize older messages in chunks.
    for (int i = 0; i < preserveStartIndex; i += CHUNK_SIZE) {
      int endIndex = Math.min(i + CHUNK_SIZE, preserveStartIndex);
      StringBuilder chunkContent = new StringBuilder();
      chunkContent.append("Summarize the following conversation:\n");
      for (int j = i; j < endIndex; j++) {
        ChatMessage msg = originalMessages.get(j);
        chunkContent
          .append(msg.getRole())
          .append(": ")
          .append(msg.getContent())
          .append("\n");
      }
      String summarizationPrompt = chunkContent.toString();
      log.debug(
        "Summarizing chunk from index {} to {}:\n{}",
        i,
        endIndex,
        summarizationPrompt
      );
      String chunkSummary = chatClient
        .prompt(summarizationPrompt)
        .call()
        .content();
      log.debug("Chunk summary: {}", chunkSummary);
      summaryBuilder.append(chunkSummary).append("\n");
    }

    // Store the accumulated summary.
    conversation.setSummary(summaryBuilder.toString().trim());

    // Preserve only the latest unsummarized messages.
    List<ChatMessage> newMessages = new ArrayList<>(
      originalMessages.subList(preserveStartIndex, totalMessages)
    );
    conversation.setMessages(newMessages);
    log.debug(
      "Stored summary and preserved last {} messages.",
      newMessages.size()
    );
  }

  /**
   * Builds the conversation prompt including any previous summary and messages.
   */
  private String buildPrompt(Conversation conversation, String currentMessage) {
    StringBuilder prompt = new StringBuilder();
    if (
      conversation.getSummary() != null && !conversation.getSummary().isEmpty()
    ) {
      prompt
        .append("Previous conversation summary:\n")
        .append(conversation.getSummary())
        .append("\n\n");
    }

    prompt.append("Current conversation:\n");
    conversation
      .getMessages()
      .forEach(msg ->
        prompt
          .append(msg.getRole())
          .append(": ")
          .append(msg.getContent())
          .append("\n")
      );
    String builtPrompt = prompt.toString();
    log.debug("Final prompt built:\n{}", builtPrompt);
    return builtPrompt;
  }

  /**
   * Counts total tokens in a given text.
   */
  private int countTokens(String text) {
    if (text == null || text.trim().isEmpty()) {
      return 0;
    }
    return text.trim().split("\\s+").length;
  }

  /**
   * Counts the cumulative tokens of all messages in the conversation.
   */
  private int countTokens(Conversation conversation) {
    int sum = conversation
      .getMessages()
      .stream()
      .mapToInt(msg -> countTokens(msg.getContent()))
      .sum();
    log.debug("Calculated cumulative token count for conversation: {}", sum);
    return sum;
  }
}
