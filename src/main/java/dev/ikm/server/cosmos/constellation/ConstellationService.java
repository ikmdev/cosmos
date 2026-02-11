package dev.ikm.server.cosmos.constellation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
public class ConstellationService {

	private final Neo4jClient neo4jClient;

	@Autowired
	public ConstellationService(Neo4jClient neo4jClient) {
		this.neo4jClient = neo4jClient;
	}
}
