package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DiscoveryService {

	private final CalculatorService calculatorService;
	private final SearchService searchService;
	private final IkeRepository ikeRepository;
	private final NodeBuilder nodeBuilder;

	@Autowired
	public DiscoveryService(CalculatorService calculatorService, SearchService searchService, IkeRepository ikeRepository, NodeBuilder nodeBuilder) {
		this.calculatorService = calculatorService;
		this.searchService = searchService;
		this.ikeRepository = ikeRepository;
		this.nodeBuilder = nodeBuilder;
	}

	public ExplorerData buildExplorationVisualization(ExplorerSearchForm explorerSearchForm) {
		List<Node> nodes = new ArrayList<>();
		List<Link> links = new ArrayList<>();

		searchService.tinkarDataSearch(explorerSearchForm.query(), explorerSearchForm.maxResults(), SearchService.SortType.SEMANTIC_SCORE)
				.forEach(searchResult -> {
					Latest<SemanticEntityVersion> latest = ikeRepository.findLatestSemanticById(searchResult.id());
					if (latest.isPresent()) {
						SemanticEntityVersion semanticEntityVersion = latest.get();

//						Node chronologyNode = nodeBuilder.buildSemanticChronology(semanticEntityVersion.chronology());
						Node versionNode = nodeBuilder.buildSemanticVersion(semanticEntityVersion);
//						nodes.add(chronologyNode);
						nodes.add(versionNode);

//						Link chronologyToVersion = new Link("", chronologyNode.id(), versionNode.id());
//						links.add(chronologyToVersion);
					}
				});

		return new ExplorerData(nodes, links);
	}

	public ExplorerData explore(String nodeId, ExplorerSearchForm explorerSearchForm) {
		List<Node> nodes = new ArrayList<>();
		List<Link> links = new ArrayList<>();

		boolean isVersion = nodeBuilder.isVersion(nodeId);
		int entityNid = nodeBuilder.parseChronologyNid(nodeId);
		Entity<? extends EntityVersion> entity = Entity.getFast(entityNid);

		if (entity instanceof ConceptEntity conceptEntity) {
			if (isVersion) {
				exploreFromConceptVersion(nodeId, nodes, links);
			} else {
				exploreFromConceptChronology(nodeId, nodes, links);
			}
		} else if (entity instanceof SemanticEntity semanticEntity) {
			if (isVersion) {
				exploreFromSemanticVersion(nodeId, nodes, links);
			} else {
				exploreFromSemanticChronology(nodeId, nodes, links);
			}
		} else if (entity instanceof PatternEntity patternEntity) {
			if (isVersion) {
				exploreFromPatternVersion(nodeId, nodes, links);
			} else {
				exploreFromPatternChronology(nodeId, nodes, links);
			}
		} else if (entity instanceof StampEntity stampEntity) {
			if (isVersion) {
				exploreFromStampVersion(nodeId, nodes, links);
			} else {
				exploreFromStampChronology(nodeId, nodes, links);
			}
		}

		return new ExplorerData(nodes, links);
	}

	private void exploreFromConceptChronology(String nodeId, List<Node> nodes, List<Link> links) {
		//build out associated Semantic Chronologies

		//build out associated Concept Chronology Versions
	}

	private void exploreFromConceptVersion(String nodeId, List<Node> nodes, List<Link> links) {
		//build out associated STAMP chronologies
	}

	private void exploreFromSemanticChronology(String nodeId, List<Node> nodes, List<Link> links) {
		int chronologyNid = nodeBuilder.parseChronologyNid(nodeId);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(chronologyNid);

		if (optionalEntity.isPresent()) {
			Entity<? extends EntityVersion> entity = optionalEntity.get();
			SemanticEntity<? extends SemanticEntityVersion> semanticEntity = (SemanticEntity<? extends SemanticEntityVersion>) entity;

		}


		//build out reference Concept, Semantic


		//build out associated Semantic Versions
	}

	private void exploreFromSemanticVersion(String nodeId, List<Node> nodes, List<Link> links) {
		EntityVersion entityVersion = triangulateVersion(nodeId);
		if (entityVersion instanceof SemanticEntityVersion semanticEntityVersion) {
			//Semantic Chronology
			Node semanticChronology = nodeBuilder.buildSemanticChronology(semanticEntityVersion.chronology());
			nodes.add(semanticChronology);

			//STAMP Chronology
			Node stampChronology = nodeBuilder.buildStampChronology(semanticEntityVersion.stamp());
			nodes.add(stampChronology);

			//Version Concepts
			List<Node> associatedValueNodes = new ArrayList<>();
			for (Object obj : semanticEntityVersion.fieldValues()) {
				if (obj instanceof EntityFacade entityFacade) {
					if (entityFacade instanceof EntityProxy.Concept proxyConcept) {
						Optional<Entity<EntityVersion>> optionalEntity = Entity.get(entityFacade.nid());
						if (optionalEntity.isPresent()) {
							Entity<? extends EntityVersion> entity = optionalEntity.get();
							if (entity instanceof ConceptEntity conceptEntity) {
								Node conceptChronology = nodeBuilder.buildConceptChronology(conceptEntity);
								associatedValueNodes.add(conceptChronology);
								nodes.add(conceptChronology);
							}
						}
					}
				}
			}

			//Link Nodes that are Concept values from the Semantic
			for (Node associatedValueNode : associatedValueNodes) {
				Link chronologyToVersion = new Link("", nodeId, associatedValueNode.id());
				links.add(chronologyToVersion);
			}

			//Link the Semantic and STAMP Chronology to the Semantic Version
			Link semVersionToSemanticChronology = new Link("", nodeId, semanticChronology.id());
			links.add(semVersionToSemanticChronology);
			Link semanticVersionToSTAMPChronology = new Link("", nodeId, stampChronology.id());
			links.add(semanticVersionToSTAMPChronology);
		}
	}

	private void exploreFromPatternChronology(String nodeId, List<Node> nodes, List<Link> links) {

	}

	private void exploreFromPatternVersion(String nodeId, List<Node> nodes, List<Link> links) {

	}

	private void exploreFromStampChronology(String nodeId, List<Node> nodes, List<Link> links) {

	}

	private void exploreFromStampVersion(String nodeId, List<Node> nodes, List<Link> links) {

	}

	private EntityVersion triangulateVersion(String nodeId) {
		int chronologyNid = nodeBuilder.parseChronologyNid(nodeId);
		int stampNid = nodeBuilder.parseVersionSTAMPNid(nodeId);

		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(chronologyNid);

		if (optionalEntity.isPresent()) {
			Entity<? extends EntityVersion> entity = optionalEntity.get();
			Optional<? extends EntityVersion> entityVersion = entity.getVersion(stampNid);
			if (entityVersion.isPresent()) {
				return entityVersion.get();
			}
		}

		throw new RuntimeException("Unable to triangulate version");
	}


}
