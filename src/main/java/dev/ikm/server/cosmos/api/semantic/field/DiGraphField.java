package dev.ikm.server.cosmos.api.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DiGraphField(
		List<VertexFieldDTO> vertices,
		List<Integer> roots,
		Map<Integer, List<Integer>> successors,
		Map<Integer, List<Integer>> predecessors) {
}
