package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.ike.Facade;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Gatherers;

public abstract class BaseChartProcessor implements ChartProcessor {

	protected EmbeddingStore<TextSegment> embeddingStore;
	protected EmbeddingModel embeddingModel;

	@Autowired
	protected void setEmbeddingStore(EmbeddingStore<TextSegment> embeddingStore) {
		this.embeddingStore = embeddingStore;
	}

	@Autowired
	protected void setEmbeddingModel(EmbeddingModel embeddingModel) {
		this.embeddingModel = embeddingModel;
	}

	private void writeEmbedding(List<Map<String, Object>> data, int batchSize) {
		data.stream()
				.gather(Gatherers.windowFixed(batchSize))
				.forEach(batch -> {
					final List<TextSegment> names = batch.stream().map(this::generateEmbeddingName).map(TextSegment::from).toList();
					final List<Metadata> metadata = batch.stream().map(row -> Map.of(
									"id", String.valueOf(row.get("id")),
									"constellationId", String.valueOf(row.get("constellationId"))))
							.map(Metadata::from)
							.toList();
					final List<Embedding> embeddings = embeddingModel.embedAll(names).content();
					final List<TextSegment> segments = new ArrayList<>();

					//Create Segments for vector store
					for (int i = 0; i < names.size(); i++) {
						segments.add(TextSegment.from(names.get(i).text(), metadata.get(i)));
					}
					embeddingStore.addAll(embeddings, segments);
				});

	}

	private String generateEmbeddingName(Map<String, Object> row) {
		StringBuilder sb = new StringBuilder();
		row.forEach((key, value) -> {
			if (key.equals("id") || key.equals("constellationId")) {
				return;
			}
			String formattedKey = key.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
			sb.append(formattedKey).append(": ").append(value).append(", ");
		});
		return sb.substring(0, sb.length() - 2);
	}

	String findLabel(String nid, Map<Facade, Set<Integer>> scopedConcepts) {
		int conceptNid = Integer.parseInt(nid);
		for (Map.Entry<Facade, Set<Integer>> entry : scopedConcepts.entrySet()) {
			if (entry.getValue().contains(conceptNid)) {
				return entry.getKey().name().replaceAll("[^a-zA-Z0-9]", "");
			}
		}
		return "Concept";
	}

	boolean isScope(int nid, Set<Facade> scopeFacades) {
		return scopeFacades.stream()
				.anyMatch(scopeFacade -> scopeFacade.id().nid() == nid);
	}


}
