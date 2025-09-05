package dev.ikm.server.database.entity;

import java.util.List;

public record SemanticChronologyView(
		int nid,
		PublicIdView publicIdView,
		int patternNid,
		PublicIdView patternPublicIdView,
		String patternDescription,
		int referenceNid,
		PublicIdView referencePublicIdView,
		String referenceDescription,
		List<SemanticVersionView> versions) {
}
