package dev.ikm.server.cosmos.api.semantic.field;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdList;
import dev.ikm.tinkar.common.id.PublicIdSet;
import dev.ikm.tinkar.common.id.VertexId;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.location.PlanarPoint;
import dev.ikm.tinkar.component.location.SpatialPoint;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.graph.DiGraphEntity;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntIntMap;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FieldTransformationService {


	public Object transformField(Object value) {
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
								.map(object -> new PropertyField(transformField(object)))
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
			case DiTreeEntity diTreeEntity -> new DiTreeField(
					diTreeEntity.vertexMap().stream()
							.map(entityVertex -> (VertexFieldDTO) transformField(entityVertex))
							.toList(),
					diTreeEntity.root().vertexIndex(),
					transformIntIntList(diTreeEntity.predecessorMap()),
					transformIntObjectIntListMap(diTreeEntity.successorMap()));
			case DiGraphEntity<? extends EntityVertex> diGraphEntity -> new DiGraphField(
					diGraphEntity.vertexMap().stream()
							.map(entityVertex -> (VertexFieldDTO) transformField(entityVertex))
							.toList(),
					diGraphEntity.roots().stream()
							.map(entityVertex -> entityVertex.vertexIndex())
							.toList(),
					transformIntObjectIntListMap(diGraphEntity.successorMap()),
					transformIntObjectIntListMap(diGraphEntity.predecessorMap()));
			case PlanarPoint planarPoint -> new PlanarPointField(planarPoint.x(), planarPoint.y());
			case SpatialPoint spatialPoint ->
					new SpatialPointField(spatialPoint.x(), spatialPoint.y(), spatialPoint.z());
			default -> value;
		};
	}

	public Map<Integer, Integer> transformIntIntList(ImmutableIntIntMap immutableIntIntMap) {
		Map<Integer, Integer> map = new HashMap<>();
		immutableIntIntMap.forEachKeyValue(map::put);
		return map;
	}

	public Map<Integer, List<Integer>> transformIntObjectIntListMap(ImmutableIntObjectMap<ImmutableIntList> immutableIntObjectMap) {
		Map<Integer, List<Integer>> map = new HashMap<>();
		immutableIntObjectMap.keyValuesView()
				.forEach(pair -> {
					map.put(pair.getOne(), pair.getTwo().primitiveStream().boxed().collect(Collectors.toList()));
				});
		return map;
	}

}
