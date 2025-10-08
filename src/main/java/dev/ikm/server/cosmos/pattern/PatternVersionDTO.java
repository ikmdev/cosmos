package dev.ikm.server.cosmos.pattern;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatternVersionDTO(
		List<UUID> stampPublicId,
		List<UUID> meaningPublicId,
		List<UUID> purposePublicId,
		List<FieldDefinitionDTO> fieldDefinitions) {
}
