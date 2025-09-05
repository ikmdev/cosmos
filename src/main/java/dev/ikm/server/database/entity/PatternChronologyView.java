package dev.ikm.server.database.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatternChronologyView(
		int nid,
		PublicIdView publicIdView,
		List<PatternVersionView> versions) {
}
