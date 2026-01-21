
package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.Coordinate;
import dev.ikm.server.cosmos.api.coordinate.CoordinateService;
import dev.ikm.server.cosmos.api.coordinate.Language;
import dev.ikm.server.cosmos.api.coordinate.Navigation;
import dev.ikm.server.cosmos.api.coordinate.Stamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ScopeService {

	private final ScopeRepository scopeRepository;
	private final CoordinateService coordinateService;

	@Autowired
	public ScopeService(ScopeRepository scopeRepository, CoordinateService coordinateService) {
		this.scopeRepository = scopeRepository;
		this.coordinateService = coordinateService;
	}

	public Scope saveNewScope(String name,
							  List<UUID> stampId,
							  List<UUID> languageId,
							  List<UUID> navigationId) {
		UUID id = UUID.randomUUID();
		Stamp stamp = coordinateService.stampCoordinate(stampId).get();
		Language language = coordinateService.languageCoordinate(languageId).get();
		Navigation navigation = coordinateService.navigationCoordinate(navigationId).get();

		scopeRepository.createScope(new ScopeEntity(id, Instant.now(), name, stamp, language, navigation));

		Coordinate stampCoordinate = coordinateService.stampCoordinate(stamp);
		Coordinate languageCoordinate = coordinateService.languageCoordinate(language);
		Coordinate navigationCoordinate = coordinateService.navigationCoordinate(navigation);

		return new Scope(id, name, stampCoordinate, languageCoordinate, navigationCoordinate);
	}

	public Scope retrieveScope(UUID id) {
		ScopeEntity scopeEntity = scopeRepository.readScope(id);
		Coordinate stampCoordinate = coordinateService.stampCoordinate(scopeEntity.stamp());
		Coordinate languageCoordinate = coordinateService.languageCoordinate(scopeEntity.language());
		Coordinate navigationCoordinate = coordinateService.navigationCoordinate(scopeEntity.navigation());
		return new Scope(scopeEntity.id(), scopeEntity.name(), stampCoordinate, languageCoordinate, navigationCoordinate);
	}

	public List<Scope> retrieveAllScopes() {
		return scopeRepository.readAll().stream()
				.sorted(Comparator.comparing(ScopeEntity::modified).reversed())
				.map(scopeEntity ->
						new Scope(scopeEntity.id(),
								scopeEntity.name(),
								coordinateService.stampCoordinate(scopeEntity.stamp()),
								coordinateService.languageCoordinate(scopeEntity.language()),
								coordinateService.navigationCoordinate(scopeEntity.navigation()))
				)
				.toList();
	}

	public void removeScope(UUID id) {
		scopeRepository.deleteScope(id);
	}

	public void updateScope(Scope scope) {
		scopeRepository.updateScope(scope.id(), new ScopeEntity(
				scope.id(),
				Instant.now(),
				scope.name(),
				coordinateService.stampCoordinate(scope.stampCoordinate().id()).get(),
				coordinateService.languageCoordinate(scope.languageCoordinate().id()).get(),
				coordinateService.navigationCoordinate(scope.navigationCoordinate().id()).get()
		));
	}
}
