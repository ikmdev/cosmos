package dev.ikm.server.cosmos.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntIntMap;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DiTreeFieldDTO(
		List<VertexFieldDTO> vertices,
		int rootIndex,
		Map<Integer, Integer> predecessors,
		Map<Integer, List<Integer>> successors) {
}
