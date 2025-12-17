package dev.ikm.server.cosmos.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ScopeRepository {

	private static final Logger LOG = LoggerFactory.getLogger(ScopeRepository.class);

	private final ScopeDatabaseConfig scopeDatabaseConfig;

	@Autowired
	public ScopeRepository(ScopeDatabaseConfig scopeDatabaseConfig) {
		this.scopeDatabaseConfig = scopeDatabaseConfig;
	}

	public void createScope(ScopeEntity scopeEntity) {
			scopeDatabaseConfig.getScopeDB().put(scopeEntity.id(), scopeEntity);
	}

	public ScopeEntity readScope(UUID id) {
		return scopeDatabaseConfig.getScopeDB().get(id);
	}

	public List<ScopeEntity> readAll() {
		return scopeDatabaseConfig.getScopeDB().values().stream().toList();
	}

	public void updateScope(UUID id, ScopeEntity scopeEntity) {
		scopeDatabaseConfig.getScopeDB().put(id, scopeEntity);
	}

	public void deleteScope(UUID id) {
		scopeDatabaseConfig.getScopeDB().remove(id);
	}

}
