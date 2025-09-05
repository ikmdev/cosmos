package dev.ikm.server.database.entity;

import java.util.List;

public record PatternVersionView(
		StampChronologyView stampChronologyView,
		int meaningNid,
		PublicIdView meaningPublicIdView,
		String meaningDescription,
		int purposeNid,
		PublicIdView purposePublicIdView,
		String purposeDescription,
		List<FieldDefinitionView> fieldDefinitions) {
}
