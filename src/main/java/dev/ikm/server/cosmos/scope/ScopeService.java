package dev.ikm.server.cosmos.scope;

import org.springframework.stereotype.Service;

@Service
public class ScopeService {

	private final ScopeRepository scopeRepository;

	public ScopeService(ScopeRepository scopeRepository) {
		this.scopeRepository = scopeRepository;
	}


}
