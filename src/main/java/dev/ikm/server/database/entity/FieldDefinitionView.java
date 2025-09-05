package dev.ikm.server.database.entity;

public record FieldDefinitionView(
		int dataTypeNid,
		PublicIdView dataTypePublicIdView,
		String dataTypeDescription,
		int meaningNid,
		PublicIdView meaningPublicIdView,
		String meaningDescription,
		int purposeNid,
		PublicIdView purposePublicIdView,
		String purposeDescription,
		int index) {
}
