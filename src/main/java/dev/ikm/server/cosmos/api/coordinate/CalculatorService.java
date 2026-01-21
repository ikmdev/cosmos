package dev.ikm.server.cosmos.api.coordinate;

import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.server.cosmos.scope.Scope;
import dev.ikm.server.cosmos.scope.ScopeService;
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
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.UUID;

@Service
@RequestScope
public class CalculatorService {

	private final ScopeService scopeService;
	private final IkeRepository ikeRepository;

	private StampCoordinateRecord stampCoordinateRecord;
	private LanguageCoordinateRecord languageCoordinateRecord;
	private NavigationCoordinateRecord navigationCoordinateRecord;

	public CalculatorService(ScopeService scopeService, IkeRepository ikeRepository) {
		this.scopeService = scopeService;
		this.ikeRepository = ikeRepository;
	}

	public void setScope(UUID scopeId) {
		Scope scope = scopeService.retrieveScope(scopeId);
		setScope(scope.stampCoordinate().id().getFirst(), scope.languageCoordinate().id().getFirst(), scope.navigationCoordinate().id().getFirst());
	}

	public void setScope(UUID stampId, UUID languageId, UUID navigationId) {
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

	public String calculateFQN(PublicId publicId) {
		return getLanguageCalculator()
				.getFullyQualifiedNameText(Entity.nid(publicId))
				.orElse("");
	}

	public String calculateText(PublicId publicId) {
		EntityFacade facade = ikeRepository.getEntityFacade(publicId);
		return getLanguageCalculator()
				.getDescriptionText(facade)
				.orElse("");
	}

	public String calculateText(UUID id) {
		return calculateText(PublicIds.of(id));
	}

	public String calculateFQN(UUID id) {
		return calculateFQN(PublicIds.of(id));
	}

	public String calculateSYN(PublicId publicId) {
		return getLanguageCalculator()
				.getRegularDescriptionText(Entity.nid(publicId))
				.orElse("");
	}

	public String calculateSYN(UUID id) {
		return calculateSYN(PublicIds.of(id));
	}

	public String calculateDEF(PublicId publicId) {
		return getLanguageCalculator()
				.getDefinitionDescriptionText(Entity.nid(publicId))
				.orElse("");
	}

	public String calculateDEF(UUID id) {
		return calculateDEF(PublicIds.of(id));
	}

	public List<List<UUID>> calculateChildren(PublicId publicId) {
		return getNavigationCalculator()
				.childrenOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> pId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateChildren(UUID id) {
		return calculateChildren(PublicIds.of(id));
	}

	public List<List<UUID>> calculateParents(PublicId publicId) {
		return getNavigationCalculator()
				.parentsOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> pId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateParents(UUID id) {
		return calculateParents(PublicIds.of(id));
	}

	public List<List<UUID>> calculateDescendants(PublicId publicId) {
		return getNavigationCalculator()
				.descendentsOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> pId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateDescendants(UUID id) {
		return calculateDescendants(PublicIds.of(id));
	}

	public List<List<UUID>> calculateAncestors(PublicId publicId) {
		return getNavigationCalculator()
				.ancestorsOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> pId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateAncestors(UUID id) {
		return calculateAncestors(PublicIds.of(id));
	}

	public List<List<UUID>> calculateKinds(PublicId publicId) {
		return getNavigationCalculator()
				.kindOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> pId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateKinds(UUID id) {
		return calculateKinds(PublicIds.of(id));
	}

}
