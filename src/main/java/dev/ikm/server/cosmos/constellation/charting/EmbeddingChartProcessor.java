package dev.ikm.server.cosmos.constellation.charting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.TinkarTermV2;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

/***
 * Concept: [Primary Name].
 * Category: [Immediate Parent/Is-A].
 * Synonyms: [Comma-separated aliases].
 * Description: [Primary Definition Semantic].
 * Available Semantic Features: [Semantic Name 1] (Provides [Brief
 * Purpose/Meaning]), [Semantic Name 2] (Provides [Brief Purpose/Meaning]).
 */

@Component
public class EmbeddingChartProcessor implements ChartProcessor {

	private record EmbeddingData(TextSegment name, Metadata metadata) {
	}

	private final EmbeddingStore<TextSegment> embeddingStore;
	private final EmbeddingModel embeddingModel;

	public EmbeddingChartProcessor(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
		this.embeddingStore = embeddingStore;
		this.embeddingModel = embeddingModel;
	}

	@Override
	public String getProcessorName() {
		return "Embedding Chart Processor";
	}

	@Override
	public void process(ChartingContext chartingContext) {
		List<EmbeddingData> embeddingBatch = new ArrayList<>();

		chartingContext.chart().scopes().stream()
				.map(scope -> chartingContext.chart().navigationCalculator().kindOf(scope.id().nid()))
				.flatMap(intIdSet -> intIdSet.intStream().boxed())
				.forEach(nid -> {
					EmbeddingData embeddingData = generateEmbeddingName(nid, chartingContext.chart().constellationId(),
							chartingContext);
					embeddingBatch.add(embeddingData);
					if (embeddingBatch.size() == chartingContext.batchSize()) { // Process in batches of 100
						processBatch(embeddingBatch, chartingContext);
						embeddingBatch.clear();
					}
				});

		// Process any remaining items in the batch
		if (!embeddingBatch.isEmpty()) {
			processBatch(embeddingBatch, chartingContext);
		}
	}

	private EmbeddingData generateEmbeddingName(int nid, UUID constelationId, ChartingContext chartingContext) {
		// Generate a descriptive name for the embedding based on the concept's
		// attributes
		StringBuilder nameBuilder = new StringBuilder();
		nameBuilder.append("Concept: " + generateConcept(nid, chartingContext)).append(". ");
		nameBuilder.append("Category: " + generateCategory(nid, chartingContext)).append(". ");
		nameBuilder.append("Synonyms: " + generateSynonyms(nid, chartingContext)).append(". ");
		nameBuilder.append("Description: " + generateDescription(nid, chartingContext)).append(". ");
		nameBuilder.append("Available Semantic Features: " + generateSemanticFeatures(nid, chartingContext))
				.append(". ");
		// Create metadata for the embedding - this will help to filter based on
		// constellation used in prompts
		Metadata metadata = Metadata.from(Map.of(
				"id", nid,
				"constellationId", constelationId.toString()));
		return new EmbeddingData(TextSegment.from(nameBuilder.toString()), metadata);
	}

	private String generateConcept(int nid, ChartingContext chartingContext) {
		return chartingContext.chart().languageCalculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(nid);
	}

	private String generateCategory(int nid, ChartingContext chartingContext) {
		StringBuilder categoryBuilder = new StringBuilder();
		IntIdSet intIdSet = chartingContext.chart().navigationCalculator().ancestorsOf(nid);
		int[] nids = intIdSet.toArray();

		for (int i = 0; i < nids.length && i < 4; i++) {
			if (i != TinkarTermV2.INTEGRATED_KNOWLEDGE_MANAGEMENT.nid()) {
				categoryBuilder.append(
						chartingContext.chart().languageCalculator().getDescriptionTextOrNid(nids[i]))
						.append(", ");
			}
		}

		return categoryBuilder.toString().substring(0, categoryBuilder.length() - 2); // Remove trailing ", "
	}

	private String generateSynonyms(int nid, ChartingContext chartingContext) {
		Optional<String> regularName = chartingContext.chart().languageCalculator().getRegularDescriptionText(nid);
		if (regularName.isPresent()) {
			return regularName.get();
		}
		return "";
	}

	private String generateDescription(int nid, ChartingContext chartingContext) {
		Optional<String> descriptionName = chartingContext.chart().languageCalculator().getDescriptionText(nid);
		if (descriptionName.isPresent()) {
			return descriptionName.get();
		}
		return "";
	}

	private String generateSemanticFeatures(int nid, ChartingContext chartingContext) {
		StringBuilder semanticFeaturesBuilder = new StringBuilder();
		PrimitiveData.get().forEachSemanticNidForComponent(nid, semanticNid -> {
			Latest<SemanticEntityVersion> latestSemanticEntityVersion = chartingContext.chart().stampCalculator()
					.latest(semanticNid);
			if (latestSemanticEntityVersion.isPresent()) {
				SemanticEntityVersion semanticEntityVersion = latestSemanticEntityVersion.get();
				Latest<PatternEntityVersion> latestPatternEntityVersion = chartingContext.chart().stampCalculator()
						.latest(semanticEntityVersion.patternNid());
				if (latestPatternEntityVersion.isPresent()) {
					PatternEntityVersion patternEntityVersion = latestPatternEntityVersion.get();
					String semanticName = chartingContext.chart().languageCalculator()
							.getDescriptionTextOrNid(patternEntityVersion.nid());
					String purpose = chartingContext.chart().languageCalculator()
							.getDescriptionTextOrNid(patternEntityVersion.semanticPurposeNid());
					semanticFeaturesBuilder.append(semanticName + " (Provides " + purpose + "), ");
				}
			}
		});
		return semanticFeaturesBuilder.toString().substring(0, semanticFeaturesBuilder.length() - 2);
	}

	private void processBatch(List<EmbeddingData> embeddingBatch, ChartingContext chartingContext) {
		// Generate embeddings values from string names
		List<TextSegment> names = embeddingBatch.stream().map(EmbeddingData::name).toList();
		List<Embedding> embeddings = embeddingModel.embedAll(names).content();
		// Create Segments for vector store with embedding values and metadata
		List<TextSegment> segmentsWithMetaData = embeddingBatch.stream()
				.map(embeddingData -> TextSegment.from(embeddingData.name().text(), embeddingData.metadata()))
				.toList();
		embeddingStore.addAll(embeddings, segmentsWithMetaData);
		chartingContext.progressUpdate().accept(chartingContext.batchSize());
	}
}
