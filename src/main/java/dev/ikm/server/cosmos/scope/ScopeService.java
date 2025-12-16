
package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.CoordinateDTO;
import dev.ikm.server.cosmos.api.coordinate.CoordinateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	public void createScope(String name,
							List<UUID> stampId,
							List<UUID> languageId,
							List<UUID> navigationId) {

		scopeRepository.createScope(
				new ScopeEntity(
						UUID.randomUUID(),
						name,
						coordinateService.stampCoordinate(stampId).get(),
						coordinateService.languageCoordinate(languageId).get(),
						coordinateService.navigationCoordinate(navigationId).get()
				));
	}

	public List<ScopeDTO> retrieveAllScopes() {
		return scopeRepository.readAll().stream()
				.map(scopeEntity ->
						new ScopeDTO(scopeEntity.id(),
								scopeEntity.name(),
								coordinateService.stampCoordinate(scopeEntity.stamp()),
								coordinateService.languageCoordinate(scopeEntity.language()),
								coordinateService.navigationCoordinate(scopeEntity.navigation()))
				)
				.toList();
	}

	public void removeScope(ScopeDTO scopeDTO) {
		scopeRepository.deleteScope(scopeDTO.id());
	}

	public void updateScope(ScopeDTO scopeDTO) {
		scopeRepository.updateScope(scopeDTO.id(), new ScopeEntity(
				scopeDTO.id(),
				scopeDTO.name(),
				coordinateService.stampCoordinate(scopeDTO.stampCoordinate().id()).get(),
				coordinateService.languageCoordinate(scopeDTO.languageCoordinate().id()).get(),
				coordinateService.navigationCoordinate(scopeDTO.navigationCoordinate().id()).get()
		));
	}
}
