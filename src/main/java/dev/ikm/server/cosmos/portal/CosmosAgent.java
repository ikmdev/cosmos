package dev.ikm.server.cosmos.portal;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CosmosAgent {

  @SystemMessage({
      "{{custom_system_prompt}}",
      "",
      "=== RETRIEVED CLINICAL KNOWLEDGE ===",
      "Use the following definitions to understand clinical concepts in the user's query or data:",
      "{{rag_context}}",
      "",
      "=== UPLOADED USER DATA ===",
      "If data is provided below, use it to answer the question.",
      "{{file_data}}"
  })
  String chat(
      @MemoryId String sessionId,
      @V("custom_system_prompt") String systemPrompt,
      @V("rag_context") String ragContext,
      @V("file_data") String fileData,
      @UserMessage String userMessage);

}
