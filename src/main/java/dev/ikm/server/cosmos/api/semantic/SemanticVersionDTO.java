package dev.ikm.server.cosmos.api.semantic;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SemanticVersionDTO(
		List<UUID> stampPublicId,
		List<Object> values) {
}
