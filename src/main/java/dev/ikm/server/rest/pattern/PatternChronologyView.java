package dev.ikm.server.rest.pattern;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatternChronologyView(
		List<UUID> publicId,
		String description,
		PatternVersionView latestVersion,
		List<PatternVersionView> history) {
}
