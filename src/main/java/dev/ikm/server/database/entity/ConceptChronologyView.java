package dev.ikm.server.database.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConceptChronologyView(
		int nid,
		PublicIdView publicIdView,
		String description,
		List<ConceptVersionView> versions) {
}
