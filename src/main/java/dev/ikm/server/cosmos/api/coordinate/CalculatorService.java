package dev.ikm.server.cosmos.api.coordinate;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.UUID;

@Service
@RequestScope
public class CalculatorService {

	private StampCoordinateRecord stampCoordinateRecord;
	private LanguageCoordinateRecord languageCoordinateRecord;
	private NavigationCoordinateRecord navigationCoordinateRecord;

	public void setViewContext(UUID stampId, UUID languageId, UUID navigationId) {
		this.stampCoordinateRecord = Stamp.toRecord(stampId);
		this.languageCoordinateRecord = Language.toRecord(languageId);
		this.navigationCoordinateRecord = Navigation.toRecord(navigationId);
	}

	public StampCalculator getStampCalculator() {
		return StampCalculatorWithCache.getCalculator(stampCoordinateRecord);
	}

	public LanguageCalculator getLanguageCalculator() {
		return LanguageCalculatorWithCache.getCalculator(stampCoordinateRecord, Lists.immutable.of(languageCoordinateRecord));
	}

	public NavigationCalculator getNavigationCalculator() {
		return NavigationCalculatorWithCache.getCalculator(stampCoordinateRecord, Lists.immutable.of(languageCoordinateRecord), navigationCoordinateRecord);
	}


	public String calculateFQN(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return getLanguageCalculator()
				.getFullyQualifiedNameText(Entity.nid(conceptPublicId))
				.orElse("");
	}

	public String calculateSYN(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return getLanguageCalculator()
				.getRegularDescriptionText(Entity.nid(conceptPublicId))
				.orElse("");
	}

	public String calculateDEF(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return getLanguageCalculator()
				.getDefinitionDescriptionText(Entity.nid(conceptPublicId))
				.orElse("");
	}

	public List<List<UUID>> calculateChildren(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return getNavigationCalculator()
				.childrenOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateParents(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return getNavigationCalculator()
				.parentsOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateDescendants(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return getNavigationCalculator()
				.descendentsOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateAncestors(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return getNavigationCalculator()
				.ancestorsOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateKinds(UUID id) {
		PublicId conceptPublicId = PublicIds.of(id);
		return getNavigationCalculator()
				.kindOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}
}
