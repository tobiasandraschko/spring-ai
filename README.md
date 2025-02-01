# AI Chat API with Context Memory

A Spring Boot REST API providing chat capabilities with AI models, featuring conversation memory, automatic summarization based on token limits, and detailed debug logging for troubleshooting.

## Prerequisites

- **JDK 17 or higher**
- **Ollama** installed on your system:
  ```bash
  # macOS/Linux
  curl -fsSL https://ollama.com/install.sh | sh
  # Windows: Download from https://ollama.com/download
  ```
- **Pull the AI model:**
  ```bash
  # DeepSeek Coder is trained from scratch on both 87% code and 13% natural language
  ollama run deepseek-coder:6.7b
  # DeepSeeks first-generation of reasoning models with comparable performance to OpenAI-o1
  ollama run deepseek-r1:7b
  ```

## Configuration

In your `application.yaml` file, configure the AI client and enable debug logging for the application packages:

```yaml
spring:
  application:
    name: spring-ai
  ai:
    ollama:
      base-url: http://localhost:11434 # default ollama port
      model: deepseek-r1:latest

springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
  show-actuator: false
  packages-to-scan: com.tasoftware.spring_ai.controller

logging:
  level:
    com.tasoftware.spring_ai.controller: DEBUG
    com.tasoftware.spring_ai.service: DEBUG
```

## Running the Application

Start the application with Gradle:

```bash
./gradlew bootRun
```

Access Swagger UI at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Using the API via Swagger UI

### Starting a New Conversation

1. In Swagger UI, navigate to the endpoint `/api/v1/chat/{conversationId}`
2. Click on **"Try it out"**
3. Enter:
   - **conversationId**: any desired string (example: `"123e4567-e89b-12d3-a456-426614174000"`)
   - **message**: `"Hello, who are you?"`
4. Click **"Execute"**

### Testing Context Memory and Summarization

Try the following sequence in the same conversation:

1. First message:
   ```
   conversationId: test-1
   message: What is Python?
   ```
2. Follow-up message:
   ```
   conversationId: test-1
   message: Show me an example
   ```

The AI maintains context between messages. When the overall token count in the conversation exceeds **8000 tokens**, the API automatically triggers a summarization that groups older messages in chunks of 5. The resulting summaries replace the original messages except for the last 5 messages, which are preserved to maintain the immediate context for accurate responses.

## How It Works

- **Unique Conversation Threads:**  
  Each `conversationId` creates a unique conversation maintained in memory.
- **In-Memory Storage:**  
  Messages and responses are stored in memory and reset when the server restarts.
- **Automatic Summarization:**  
  When the cumulative token count of a conversation exceeds **8000 tokens**, older messages are summarized in groups of 5. The summaries are used as context going forward while the last 5 messages remain unchanged. This ensures efficient token usage without losing immediate context.

- **Debug Logging:**  
  Detailed debug logs are enabled for the controller and service packages to help trace requests, responses, and internal state changes during processing.

## Limitations

- **In-Memory Storage:**  
  Conversation history is stored in memory, so it resets when the server is restarted.
- **Summarization Threshold:**  
  Summarization occurs only when the cumulative token count exceeds **8000 tokens**.
- **Single Model Support:**  
  The current configuration supports a single AI model instance per application.

## Common Issues

- **Ollama Not Running:**  
  Ensure Ollama is running (`ollama serve`) before starting the application.
- **Model Availability:**  
  Verify that the specified model is downloaded.
- **Configuration Issues:**  
  Double-check the base URL and other settings in your `application.yaml`.

## API Endpoints

| Method | Endpoint                        | Description                                                            |
| ------ | ------------------------------- | ---------------------------------------------------------------------- |
| GET    | `/api/v1/chat/{conversationId}` | Send a message to and receive a response from the AI in a conversation |

---
