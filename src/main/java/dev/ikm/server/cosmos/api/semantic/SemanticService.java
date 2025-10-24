package dev.ikm.server.cosmos.api.semantic;

import dev.ikm.server.cosmos.database.Context;
import dev.ikm.server.cosmos.database.IkeRepository;
import dev.ikm.server.cosmos.api.semantic.field.DiGraphFieldDTO;
import dev.ikm.server.cosmos.api.semantic.field.DiTreeFieldDTO;
import dev.ikm.server.cosmos.api.semantic.field.PlanarPointFieldDTO;
import dev.ikm.server.cosmos.api.semantic.field.PropertyFieldDTO;
import dev.ikm.server.cosmos.api.semantic.field.SpatialPointFieldDTO;
import dev.ikm.server.cosmos.api.semantic.field.VertexFieldDTO;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdList;
import dev.ikm.tinkar.common.id.PublicIdSet;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.id.VertexId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.location.PlanarPoint;
import dev.ikm.tinkar.component.location.SpatialPoint;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiGraphEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.TinkarTermV2;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntIntMap;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SemanticService {

	private final IkeRepository ikeRepository;
	private final Context context;

	@Autowired
	public SemanticService(IkeRepository ikeRepository, Context context) {
		this.ikeRepository = ikeRepository;
		this.context = context;
	}

	public SemanticChronologyDTO retrieveSemanticWithAllVersions(UUID uuid) {
		Optional<SemanticEntity<? extends SemanticEntityVersion>> optionalSemanticEntity = ikeRepository.findSemanticById(PublicIds.of(uuid));
		if (optionalSemanticEntity.isPresent()) {
			SemanticEntity<? extends SemanticEntityVersion> semanticEntity = optionalSemanticEntity.get();
			return new SemanticChronologyDTO(
					PublicIds.of(uuid).asUuidList().toList(),
					semanticEntity.pattern().publicId().asUuidList().toList(),
					semanticEntity.referencedComponent().publicId().asUuidList().toList(),
					null,
					semanticEntity.versions().stream()
							.map(semanticEntityVersion ->
									new SemanticVersionDTO(
											semanticEntityVersion.stamp().publicId().asUuidList().toList(),
											semanticEntityVersion.fieldValues().stream()
													.map(this::field)
													.toList()
									))
							.toList());
		} else {
			return new SemanticChronologyDTO(
					PublicIds.of(uuid).asUuidList().toList(),
					null,
					null,
					null,
					Collections.emptyList());
		}
	}

	public SemanticChronologyDTO retrieveSemanticWithLatestVersion(UUID uuid) {
		Latest<SemanticEntityVersion> latestSemanticEntityVersion = ikeRepository.findLatestSemanticById(PublicIds.of(uuid), context.getStampCalculator());
		if (latestSemanticEntityVersion.isPresent()) {
			SemanticEntityVersion semanticEntityVersion = latestSemanticEntityVersion.get();
			return new SemanticChronologyDTO(
					semanticEntityVersion.publicId().asUuidList().toList(),
					semanticEntityVersion.pattern().publicId().asUuidList().toList(),
					semanticEntityVersion.referencedComponent().publicId().asUuidList().toList(),
					new SemanticVersionDTO(
							semanticEntityVersion.stamp().publicId().asUuidList().toList(),
							semanticEntityVersion.fieldValues().stream()
									.map(this::field)
									.toList()),
					null);
		} else {
			return new SemanticChronologyDTO(
					PublicIds.of(uuid).asUuidList().toList(),
					null,
					null,
					null,
					null);
		}
	}

	public Object field(Object value) {
		return switch (value) {
			case Integer i -> i;
			case Long l -> l;
			case Float f -> f;
			case Double d -> d;
			case String s -> s;
			case Boolean b -> b;
			case byte[] bytes -> Objects.requireNonNullElse(bytes, new byte[0]);
			case EntityVertex entityVertex -> {
				int meaningNid = Entity.nid(entityVertex.meaning());
				Optional<Entity<EntityVersion>> optionalEntity = Entity.get(meaningNid);
				if (optionalEntity.isEmpty()) {
					yield null;
				}
				Entity<? extends EntityVersion> entity = optionalEntity.get();
				yield new VertexFieldDTO(
						entityVertex.asUuid(),
						entityVertex.vertexIndex(),
						entity.publicId().asUuidList().stream().toList(),
						entityVertex.properties().stream()
								.map(object -> new PropertyFieldDTO(field(object)))
								.toList()
				);
			}
			case VertexId vertexId -> vertexId.asUuid();
			case PublicId publicId -> publicId.asUuidList().stream().toList();
			case PublicIdList publicIdList -> publicIdList.stream()                                //object
					.map(publicIdObj -> {
						PublicId publicId = (PublicId) publicIdObj;
						return publicId.asUuidList().stream().toList();
					})
					.toList();
			case PublicIdSet publicIdSet -> publicIdSet.stream()
					.map(publicIdObj -> {
						PublicId publicId = (PublicId) publicIdObj;
						return publicId.asUuidList().stream().toList();
					})
					.collect(Collectors.toSet());
			case IntIdList intIdList -> intIdList
					.map(intIdValue -> {
						Entity<? extends EntityVersion> entity = Entity.getFast(intIdValue);
						return entity.publicId().asUuidList().stream().toList();
					})
					.toList();
			case IntIdSet intIdSet -> intIdSet
					.map(intIdValue -> {
						Entity<? extends EntityVersion> entity = Entity.getFast(intIdValue);
						return entity.publicId().asUuidList().stream().toList();
					})
					.toSet();
			case Instant instant -> DateTimeUtil.format(instant, DateTimeUtil.SEC_FORMATTER);
			case BigDecimal bigDecimal -> bigDecimal.toPlainString();
			case Component component -> component.publicId().asUuidList().stream().toList();
			case DiTreeEntity diTreeEntity ->
					new DiTreeFieldDTO(
					diTreeEntity.vertexMap().stream()
							.map(entityVertex -> (VertexFieldDTO) field(entityVertex))
							.toList(),
					diTreeEntity.root().vertexIndex(),
					transformIntIntList(diTreeEntity.predecessorMap()),
					transformIntObjectIntListMap(diTreeEntity.successorMap()));
			case DiGraphEntity<? extends EntityVertex> diGraphEntity -> new DiGraphFieldDTO(
					diGraphEntity.vertexMap().stream()
							.map(entityVertex -> (VertexFieldDTO) field(entityVertex))
							.toList(),
					diGraphEntity.roots().stream()
							.map(entityVertex -> entityVertex.vertexIndex())
							.toList(),
					transformIntObjectIntListMap(diGraphEntity.successorMap()),
					transformIntObjectIntListMap(diGraphEntity.predecessorMap()));
			case PlanarPoint planarPoint -> new PlanarPointFieldDTO(planarPoint.x(), planarPoint.y());
			case SpatialPoint spatialPoint ->
					new SpatialPointFieldDTO(spatialPoint.x(), spatialPoint.y(), spatialPoint.z());
			default -> value;
		};
	}

	private Map<Integer, Integer> transformIntIntList(ImmutableIntIntMap immutableIntIntMap) {
		Map<Integer, Integer> map = new HashMap<>();
		immutableIntIntMap.forEachKeyValue(map::put);
		return map;
	}

	private Map<Integer, List<Integer>> transformIntObjectIntListMap(ImmutableIntObjectMap<ImmutableIntList> immutableIntObjectMap) {
		Map<Integer, List<Integer>> map = new HashMap<>();
		immutableIntObjectMap.keyValuesView()
				.forEach(pair -> {
					map.put(pair.getOne(), pair.getTwo().primitiveStream().boxed().collect(Collectors.toList()));
				});
		return map;
	}

	public List<List<UUID>> retrieveSemantics(UUID uuid) {
		List<List<UUID>> semanticIds = new ArrayList<>();
		PublicId conceptPublicId = PublicIds.of(uuid);
		PrimitiveData.get().forEachSemanticNidForComponent(
				Entity.nid(conceptPublicId),
				semanticNid -> {
					semanticIds.add(PrimitiveData.publicId(semanticNid).asUuidList().toList());
				});
		return semanticIds;
	}

	public List<UUID> calculateUSDialect(UUID uuid) {
		List<UUID> acceptabilityId = new ArrayList<>();
		PublicId semanticPublicId = PublicIds.of(uuid);
		PrimitiveData.get().forEachSemanticNidForComponentOfPattern(
				Entity.nid(semanticPublicId),
				TinkarTermV2.US_DIALECT_PATTERN.nid(),
				nid -> {
					Latest<Field<PublicId>> acceptability = context.getStampCalculator()
							.getFieldForSemanticWithMeaning(nid, TinkarTermV2.UNITED_STATES_OF_AMERICA_ENGLISH_DIALECT);
					if (acceptability.isPresent()) {
						acceptabilityId.addAll(acceptability.get().value().asUuidList().stream().toList());
					}
				}
		);
		return acceptabilityId;
	}

	public List<UUID> calculateGBDialect(UUID uuid) {
		List<UUID> acceptabilityId = new ArrayList<>();
		PublicId semanticPublicId = PublicIds.of(uuid);
		PrimitiveData.get().forEachSemanticNidForComponentOfPattern(
				Entity.nid(semanticPublicId),
				TinkarTermV2.GB_DIALECT_PATTERN.nid(),
				nid -> {
					Latest<Field<PublicId>> acceptability = context.getStampCalculator()
							.getFieldForSemanticWithMeaning(nid, TinkarTermV2.GREAT_BRITAIN_ENGLISH_DIALECT);
					if (acceptability.isPresent()) {
						acceptabilityId.addAll(acceptability.get().value().asUuidList().stream().toList());
					}
				}
		);
		return acceptabilityId;
	}


}
