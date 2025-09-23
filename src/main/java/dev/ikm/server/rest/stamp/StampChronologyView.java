package dev.ikm.server.rest.stamp;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StampChronologyView(
		List<UUID> publicId,
		StampVersionView latestVersion,
		List<StampVersionView> history) {
}
