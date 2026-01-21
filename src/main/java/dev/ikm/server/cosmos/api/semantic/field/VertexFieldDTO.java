package dev.ikm.server.cosmos.api.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VertexFieldDTO(
		UUID uuid,
		int index,
		List<UUID> conceptPublicId,
		List<PropertyField> properties) {
}
