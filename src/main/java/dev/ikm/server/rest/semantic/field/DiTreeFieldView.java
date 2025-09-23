package dev.ikm.server.rest.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.map.primitive.ImmutableIntIntMap;
import org.eclipse.collections.api.map.primitive.ImmutableIntObjectMap;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DiTreeFieldView(
		List<VertexFieldView> vertices,
		int rootIndex,
		ImmutableIntIntMap predecessors,
		ImmutableIntObjectMap<ImmutableIntList> successors) {
}
