package dev.ikm.server.cosmos.agent;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.ikm.server.cosmos.portal.CosmosAgent;
import dev.ikm.server.cosmos.portal.CosmosOptimizer;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

@Configuration
public class AgentConfig {

	@Bean
	public ChatModel chatModel(
			@Value("${ollama.url}") String ollamaUrl,
			@Value("${ollama.llm.model}") String ollamaModel) {
		return OllamaChatModel.builder()
				.baseUrl(ollamaUrl)
				.modelName(ollamaModel)
				.temperature(0.0) // Critical for clinical accuracy
				.timeout(Duration.ofMinutes(5))
				.build();
	}

	@Bean
	public ChatMemoryProvider chatMemoryProvider(
			@Value("${ollama.chat.memory.max.message}") int maxMessage) {
		return memoryId -> MessageWindowChatMemory.builder()
				.id(memoryId)
				.maxMessages(maxMessage)
				.build();
	}

	@Bean
	public CosmosAgent cosmosAgent(ChatModel chatModel, ChatMemoryProvider chatMemoryProvider) {
		return AiServices.builder(CosmosAgent.class)
				.chatModel(chatModel)
				.chatMemoryProvider(chatMemoryProvider)
				.build();
	}

	@Bean
    public CosmosOptimizer queryOptimizer(ChatModel chatModel) {
        return AiServices.builder(CosmosOptimizer.class)
                .chatModel(chatModel)
                .build();
    }
}
