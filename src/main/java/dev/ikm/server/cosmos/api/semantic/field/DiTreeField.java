package dev.ikm.server.cosmos.api.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DiTreeField(
		List<VertexFieldDTO> vertices,
		int rootIndex,
		Map<Integer, Integer> predecessors,
		Map<Integer, List<Integer>> successors) {
}
