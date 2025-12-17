
package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.CoordinateDTO;
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

	public ScopeDTO saveNewScope(String name,
							 List<UUID> stampId,
							 List<UUID> languageId,
							 List<UUID> navigationId) {
		UUID id = UUID.randomUUID();
		Stamp stamp = coordinateService.stampCoordinate(stampId).get();
		Language language = coordinateService.languageCoordinate(languageId).get();
		Navigation navigation = coordinateService.navigationCoordinate(navigationId).get();

		scopeRepository.createScope(new ScopeEntity(id, Instant.now(), name, stamp, language, navigation));

		CoordinateDTO stampCoordinate = coordinateService.stampCoordinate(stamp);
		CoordinateDTO languageCoordinate = coordinateService.languageCoordinate(language);
		CoordinateDTO navigationCoordinate = coordinateService.navigationCoordinate(navigation);

		return new ScopeDTO(id, name, stampCoordinate, languageCoordinate, navigationCoordinate);
	}

	public List<ScopeDTO> retrieveAllScopes() {
		return scopeRepository.readAll().stream()
				.sorted(Comparator.comparing(ScopeEntity::modified))
				.map(scopeEntity ->
						new ScopeDTO(scopeEntity.id(),
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

	public void updateScope(ScopeDTO scopeDTO) {
		scopeRepository.updateScope(scopeDTO.id(), new ScopeEntity(
				scopeDTO.id(),
				Instant.now(),
				scopeDTO.name(),
				coordinateService.stampCoordinate(scopeDTO.stampCoordinate().id()).get(),
				coordinateService.languageCoordinate(scopeDTO.languageCoordinate().id()).get(),
				coordinateService.navigationCoordinate(scopeDTO.navigationCoordinate().id()).get()
		));
	}
}
