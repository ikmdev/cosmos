package dev.ikm.server.rest.semantic;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ikm.server.rest.stamp.StampChronologyView;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SemanticVersionView(
		StampChronologyView stamp,
		List<Object> values) {
}
