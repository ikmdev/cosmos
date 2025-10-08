package dev.ikm.server.cosmos.semantic;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SemanticChronologyDTO(
		List<UUID> publicId,
		List<UUID> patternPublicId,
		List<UUID> referencedComponentPublicId,
		SemanticVersionDTO latestVersion,
		List<SemanticVersionDTO> versions) {
}