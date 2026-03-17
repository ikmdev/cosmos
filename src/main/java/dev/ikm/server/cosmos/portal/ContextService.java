package dev.ikm.server.cosmos.portal;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;

@Service
public class ContextService {

	private final EmbeddingStore<TextSegment> embeddingStore;
	private final EmbeddingModel embeddingModel;
	private final ContextGenerator contextGenerator;

	public ContextService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel,
			ContextGenerator contextGenerator) {
		this.embeddingStore = embeddingStore;
		this.embeddingModel = embeddingModel;
		this.contextGenerator = contextGenerator;
	}

	public String buildAttachmentContext(MultipartFile attachedFile) {
		String fileContext = "";
		// If a file was attached, extract the text
		if (attachedFile != null && !attachedFile.isEmpty()) {
			try {
				fileContext = new String(attachedFile.getBytes(), StandardCharsets.UTF_8);
				// 2. Safely truncate if it exceeds the limit
				int maxLength = 50000;
				if (fileContext.length() > maxLength) {
					fileContext = fileContext.substring(0, maxLength)
							+ "\n\n... [SYSTEM NOTE: File truncated due to context size limits.]";
				}
			} catch (Exception e) {
				return "System Error: Could not read the uploaded file.";
			}
		}
		return fileContext;
	}

	public String buildRAGContext(String userMessage, UUID constellationId) {
		String ragContext = "No relevant clinical concepts found.";
		try {

			// Embed the user's typed question
			Embedding queryEmbedding = embeddingModel.embed(userMessage).content();

			// Filter by the specific Constellation
			Filter filter = metadataKey("constellationId").isEqualTo(constellationId.toString());

			// Execute search
			EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
					.queryEmbedding(queryEmbedding)
					.filter(filter)
					.maxResults(3) // Keep it tight for the MVP
					.minScore(0.7) // Only return high-confidence matches
					.build();
			EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);

			// Identify applicable concepts based on embeddings
			List<Integer> nids = result.matches().stream()
					.map(EmbeddingMatch::embedded)
					.map(textSegment -> textSegment.metadata().getInteger("nid"))
					.toList();

			// Generate Context based on semantically applicable nids (concepts)
			if (!nids.isEmpty()) {
				ragContext = contextGenerator.generate(nids);
			}
		} catch (Exception e) {
			System.err.println("Vector search failed: " + e.getMessage());
		}
		return ragContext;
	}
}
