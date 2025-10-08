package dev.ikm.server.cosmos.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlanarPointFieldDTO(
		float x,
		float y) {
}
