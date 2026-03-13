package dev.ikm.server.cosmos.ai;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.community.rag.content.retriever.lucene.LuceneEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

@Configuration
public class LangChainConfig {

	private final String vectorDBName = "vector-db";

	@Bean
	public EmbeddingStore<TextSegment> embeddingStore(
			@Value("${cosmos.directory}") File directory) throws IOException {
		Directory vectorDBDirectory = FSDirectory.open(directory.toPath().resolve(vectorDBName));
		return LuceneEmbeddingStore.builder()
				.directory(vectorDBDirectory)
				.build();
	}

	@Bean
	public EmbeddingModel embeddingModel(
			@Value("${ollama.url}") String ollamaUrl,
			@Value("${ollama.embedding.model}") String embeddingModel) {
		return OllamaEmbeddingModel.builder()
				.baseUrl(ollamaUrl)
				.modelName(embeddingModel)
				.timeout(Duration.ofSeconds(60))
				.logRequests(true)
				.logResponses(true)
				.build();
	}

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

}
