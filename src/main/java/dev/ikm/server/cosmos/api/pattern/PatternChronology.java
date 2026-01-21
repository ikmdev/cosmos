package dev.ikm.server.cosmos.api.pattern;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatternChronology(
		List<UUID> publicId,
		PatternVersion latestVersion,
		List<PatternVersion> versions) {
}
