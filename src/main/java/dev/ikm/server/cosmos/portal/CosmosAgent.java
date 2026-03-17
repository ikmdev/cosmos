package dev.ikm.server.cosmos.portal;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CosmosAgent {

      // The template variable must match the @V name below
     @SystemMessage({
        "{{custom_system_prompt}}",
        "",
        "If the user has uploaded data, it will be provided below. Use this data to answer their questions accurately. Do not hallucinate data.",
        "=== UPLOADED USER DATA ===",
        "{{file_data}}"
    })
    String chat(
            @MemoryId String sessionId,
            @V("custom_system_prompt") String systemPrompt,
            @V("file_data") String fileData,
            @UserMessage String userMessage);

}
