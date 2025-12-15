
package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.CoordinateDTO;
import dev.ikm.server.cosmos.api.coordinate.CoordinateService;
import dev.ikm.server.cosmos.api.coordinate.Stamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
							CoordinateDTO stampCoordinate,
							CoordinateDTO languageCoordinate,
							CoordinateDTO navigationCoordinate) {


//		scopeRepository.createScope(new ScopeEntity(name, scopeDTO.stamp(), scopeDTO.lang(), scopeDTO.nav()));
	}

	public List<ScopeDTO> retrieveAllScopes() {
		return null;
//		return scopeRepository.readAll().stream()
//				.map(scopeEntity ->
//						new ScopeDTO(scopeEntity.name(),
//								scopeEntity.stamp(),
//								scopeEntity.language(),
//								scopeEntity.navigation()))
//				.toList();
	}

	public void removeScope(ScopeDTO scopeDTO) {

	}

	public void updateScope(ScopeDTO scopeDTO) {

	}


}
