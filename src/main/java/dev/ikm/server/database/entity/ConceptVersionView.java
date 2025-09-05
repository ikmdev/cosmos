package dev.ikm.server.database.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConceptVersionView(
		StampChronologyView stampChronologyView) {
}
