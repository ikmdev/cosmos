package dev.ikm.server.database.entity;

import java.util.List;

public record StampChronologyView(
		int stampNid,
		PublicIdView publicIdView,
		List<StampVersionView> stampVersionViews) {
}
