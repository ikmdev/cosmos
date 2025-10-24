package dev.ikm.server.cosmos.api.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PropertyFieldDTO(Object field) {
}
