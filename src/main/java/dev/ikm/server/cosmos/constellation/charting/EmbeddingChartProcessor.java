package dev.ikm.server.cosmos.constellation.charting;

import org.springframework.stereotype.Component;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

@Component
public class EmbeddingChartProcessor implements ChartProcessor {

    protected EmbeddingStore<TextSegment> embeddingStore;
	protected EmbeddingModel embeddingModel;

    @Override
    public String getProcessorName() {
        return "Embedding Chart Processor";
    }

    @Override
    public void process(ChartingContext chartingContext) {
        

        // final List<TextSegment> names = batch.stream().map(this::generateEmbeddingName).map(TextSegment::from).toList();
		// 			final List<Metadata> metadata = batch.stream().map(row -> Map.of(
		// 							"id", String.valueOf(row.get("id")),
		// 							"constellationId", String.valueOf(row.get("constellationId"))))
		// 					.map(Metadata::from)
		// 					.toList();
		// 			final List<Embedding> embeddings = embeddingModel.embedAll(names).content();
		// 			final List<TextSegment> segments = new ArrayList<>();

		// 			//Create Segments for vector store
		// 			for (int i = 0; i < names.size(); i++) {
		// 				segments.add(TextSegment.from(names.get(i).text(), metadata.get(i)));
		// 			}
		// 			embeddingStore.addAll(embeddings, segments);

        
        chartingContext.progressUpdate().accept(50); // Update progress to 50% as an example
    }

}
