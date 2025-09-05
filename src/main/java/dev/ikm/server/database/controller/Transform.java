package dev.ikm.server.database.controller;

import dev.ikm.server.database.entity.ConceptChronologyView;
import dev.ikm.server.database.entity.ConceptVersionView;
import dev.ikm.server.database.entity.FieldDefinitionView;
import dev.ikm.server.database.entity.PatternChronologyView;
import dev.ikm.server.database.entity.PatternVersionView;
import dev.ikm.server.database.entity.PublicIdView;
import dev.ikm.server.database.entity.SemanticChronologyView;
import dev.ikm.server.database.entity.SemanticVersionView;
import dev.ikm.server.database.entity.StampChronologyView;
import dev.ikm.server.database.entity.StampVersionView;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdList;
import dev.ikm.tinkar.common.id.PublicIdSet;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Transform {

	private final Function<Integer, String> descriptionFunction;

	public Transform(Function<Integer, String> descriptionFunction) {
		this.descriptionFunction = descriptionFunction;
	}

	public ConceptChronologyView conceptChronology(ConceptEntity<? extends ConceptEntityVersion> conceptEntity) {
		return new ConceptChronologyView(
				conceptEntity.nid(),
				publicId(conceptEntity.publicId()),
				descriptionFunction.apply(conceptEntity.nid()),
				conceptEntity.versions().stream()
						.map(conceptEntityVersion ->
								new ConceptVersionView(stampChronology(conceptEntityVersion.stamp())))
						.toList());
	}

	public SemanticChronologyView semanticChronology(SemanticEntity<? extends SemanticEntityVersion> semanticEntity) {
		return new SemanticChronologyView(
				semanticEntity.nid(),
				publicId(semanticEntity.publicId()),
				semanticEntity.patternNid(),
				publicId(semanticEntity.pattern().publicId()),
				descriptionFunction.apply(semanticEntity.patternNid()),
				semanticEntity.referencedComponentNid(),
				publicId(semanticEntity.referencedComponent().publicId()),
				descriptionFunction.apply(semanticEntity.referencedComponentNid()),
				semanticEntity.versions().stream()
						.map(this::semanticVersion)
						.toList());
	}

	public SemanticVersionView semanticVersion(SemanticEntityVersion semanticEntityVersion) {
		return new SemanticVersionView(
				stampChronology(semanticEntityVersion.stamp()),
				semanticEntityVersion.fieldValues().stream()
						.map(this::field)
						.toList());
	}

	/*





            case VertexId vertexUUID -> toVertexUUID(vertexUUID);
            case DiTree diTree -> toPBDiTree(diTree);
            case DiGraph diGraph -> toPBDiGraph(diGraph);
            case Vertex vertex -> toVertex(vertex);
            case PlanarPoint planarPoint -> toPlanarPoint(planarPoint);
            case SpatialPoint spatialPoint -> toSpatialPoint(spatialPoint);
//            case dev.ikm.tinkar.component.graph.Graph graph -> createGraph
	 */
//TODO - look into how tinkar schema handles some of these cases
	public Object field(Object value) {
		return switch (value) {
			case Integer i -> i;
			case Long l -> l;
			case Float f -> f;
			case Double d -> d;
			case String s -> s;
			case Boolean b -> b;
			case byte[] bytes -> Objects.requireNonNullElse(bytes, new byte[0]);
			case PublicId publicId -> publicId(publicId);
			case PublicIdList publicIdList -> publicIdList.stream()
					.map(publicIdObj -> publicId((PublicId) publicIdObj))
					.toList();
			case PublicIdSet publicIdSet -> publicIdSet.stream()
					.map(publicIdObj -> publicId((PublicId) publicIdObj))
					.collect(Collectors.toSet());
			case IntIdList intIdList -> intIdList.map(this::field).toList();
			case IntIdSet intIdSet -> intIdSet.map(this::field).toSet();
			case Instant instant -> DateTimeUtil.format(instant, DateTimeUtil.SEC_FORMATTER);
			case BigDecimal bigDecimal -> bigDecimal.toPlainString();
			case Component component ->
				switch (component) {
					case ConceptEntity conceptEntity -> conceptChronology(conceptEntity);
					case PatternEntity patternEntity -> patternChronology(patternEntity);
					case SemanticEntity semanticEntity -> semanticChronology(semanticEntity);
					case StampEntity stampEntity -> stampChronology(stampEntity);
					default -> throw new IllegalStateException("Unexpected value: " + component);
				};
			default -> value;
		};
	}

	public PatternChronologyView patternChronology(PatternEntity<? extends PatternEntityVersion> patternEntity) {
		return new PatternChronologyView(
				patternEntity.nid(),
				publicId(patternEntity.publicId()),
				patternEntity.versions().stream()
						.map(this::patternVersion)
						.toList());
	}

	public PatternVersionView patternVersion(PatternEntityVersion patternEntityVersion) {
		return new PatternVersionView(
				stampChronology(patternEntityVersion.stamp()),
				patternEntityVersion.semanticMeaningNid(),
				publicId(patternEntityVersion.semanticMeaning().publicId()),
				descriptionFunction.apply(patternEntityVersion.semanticMeaningNid()),
				patternEntityVersion.semanticPurposeNid(),
				publicId(patternEntityVersion.semanticPurpose().publicId()),
				descriptionFunction.apply(patternEntityVersion.semanticPurposeNid()),
				fieldDefinitions(patternEntityVersion));
	}

	public List<FieldDefinitionView> fieldDefinitions(PatternEntityVersion patternEntityVersion) {
		return patternEntityVersion.fieldDefinitions().stream()
				.map(fieldDefinitionForEntity -> {
					return new FieldDefinitionView(
							fieldDefinitionForEntity.dataTypeNid(),
							publicId(fieldDefinitionForEntity.dataType().publicId()),
							descriptionFunction.apply(fieldDefinitionForEntity.dataTypeNid()),
							fieldDefinitionForEntity.meaningNid(),
							publicId(fieldDefinitionForEntity.meaning().publicId()),
							descriptionFunction.apply(fieldDefinitionForEntity.meaningNid()),
							fieldDefinitionForEntity.purposeNid(),
							publicId(fieldDefinitionForEntity.purpose()),
							descriptionFunction.apply(fieldDefinitionForEntity.purposeNid()),
							fieldDefinitionForEntity.indexInPattern());
				})
				.toList();
	}

	public PublicIdView publicId(PublicId publicId) {
		List<UUID> uuids = publicId.asUuidList().stream().toList();
		return new PublicIdView(uuids);
	}

	public StampChronologyView stampChronology(StampEntity<? extends StampEntityVersion> stampEntity) {
		return new StampChronologyView(
				stampEntity.nid(),
				publicId(stampEntity.publicId()),
				stampEntity.versions().stream()
						.map(stampEntityVersion ->
								new StampVersionView(
										stampEntityVersion.state().nid(),
										publicId(stampEntityVersion.state().publicId()),
										descriptionFunction.apply(stampEntityVersion.state().nid()),
										stampEntityVersion.time(),
										DateTimeUtil.format(stampEntityVersion.time(), DateTimeUtil.SEC_FORMATTER),
										stampEntityVersion.authorNid(),
										publicId(stampEntityVersion.author().publicId()),
										descriptionFunction.apply(stampEntityVersion.authorNid()),
										stampEntityVersion.moduleNid(),
										publicId(stampEntityVersion.module().publicId()),
										descriptionFunction.apply(stampEntityVersion.moduleNid()),
										stampEntityVersion.pathNid(),
										publicId(stampEntityVersion.path().publicId()),
										descriptionFunction.apply(stampEntityVersion.pathNid())))
						.toList());
	}

}
