package dev.ikm.server.cosmos.api.calculator;

import dev.ikm.server.cosmos.api.coordinate.Context;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CalculatorService {

	private final Context context;

	@Autowired
	public CalculatorService(Context context) {
		this.context = context;
	}

	public StampCalculator getStampCalculator() {
		return context.getStampCalculator();
	}

	public String calculateFQN(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return context.getLanguageCalculator()
				.getFullyQualifiedNameText(Entity.nid(conceptPublicId))
				.orElse("");
	}

	public String calculateSYN(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return context.getLanguageCalculator()
				.getRegularDescriptionText(Entity.nid(conceptPublicId))
				.orElse("");
	}

	public String calculateDEF(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return context.getLanguageCalculator()
				.getDefinitionDescriptionText(Entity.nid(conceptPublicId))
				.orElse("");
	}

	public List<List<UUID>> calculateChildren(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return context.getNavigationCalculator()
				.childrenOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateParents(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return context.getNavigationCalculator()
				.parentsOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateDescendants(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return context.getNavigationCalculator()
				.descendentsOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateAncestors(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return context.getNavigationCalculator()
				.ancestorsOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateKinds(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return context.getNavigationCalculator()
				.kindOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}
}
