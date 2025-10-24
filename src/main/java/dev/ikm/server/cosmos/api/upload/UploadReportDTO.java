package dev.ikm.server.cosmos.api.upload;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UploadReportDTO(
		long conceptsCount,
		long semanticsCount,
		long patternsCount,
		long stampsCount) {
}
