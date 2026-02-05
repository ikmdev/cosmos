package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
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
						Node versionNode = nodeBuilder.buildSemanticVersion(semanticEntityVersion);
						nodes.add(versionNode);
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
		int chronologyNid = nodeBuilder.parseChronologyNid(nodeId);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(chronologyNid);

		if (optionalEntity.isPresent()) {
			Entity<? extends EntityVersion> entity = optionalEntity.get();
			ConceptEntity<? extends ConceptEntityVersion> conceptEntity = (ConceptEntity<? extends ConceptEntityVersion>) entity;

			//build out associated Semantic Chronologies
			ikeRepository.findAssociatedSemanticIds(conceptEntity.publicId().asUuidList().toList()).stream()
					.map(PublicIds::of)
					.map(Entity::nid)
					.map(Entity::get)
					.filter(Optional::isPresent)
					.map(optEntity -> {
						Entity<? extends EntityVersion> entityChronology = optEntity.get();
						SemanticEntity<? extends SemanticEntityVersion> semanticChronology = (SemanticEntity<? extends SemanticEntityVersion>) entityChronology;
						return semanticChronology;
					})
					.forEach(semanticEntity -> {
						Node semanticChronology = nodeBuilder.buildSemanticChronology(semanticEntity);
						nodes.add(semanticChronology);
						Link chronologyToSemantic = new Link("", nodeId, semanticChronology.id());
						links.add(chronologyToSemantic);
					});


			//build out associated Concept Chronology Versions
			Latest<ConceptEntityVersion> latestConceptEntityVersion = calculatorService.getStampCalculator().latest(conceptEntity.nid());
			if (latestConceptEntityVersion.isPresent()) {
				ConceptEntityVersion conceptEntityVersion = latestConceptEntityVersion.get();
				Node versionNode = nodeBuilder.buildConceptVersion(conceptEntityVersion);
				nodes.add(versionNode);
				Link chronologyToVersion = new Link("", nodeId, versionNode.id());
				links.add(chronologyToVersion);
			}
		}
	}

	private void exploreFromConceptVersion(String nodeId, List<Node> nodes, List<Link> links) {
		EntityVersion entityVersion = triangulateVersion(nodeId);

		if (entityVersion instanceof ConceptEntityVersion conceptEntityVersion) {
			//build out associated STAMP chronologies
			Optional<Entity<EntityVersion>> optionalEntity = Entity.get(conceptEntityVersion.stampNid());

			if (optionalEntity.isPresent()) {
				Entity<? extends EntityVersion> entity = optionalEntity.get();
				StampEntity<? extends StampEntityVersion> stampEntity = (StampEntity<? extends StampEntityVersion>) entity;
				Node stampChronology = nodeBuilder.buildStampChronology(stampEntity);
				nodes.add(stampChronology);
				Link chronologyToStamp = new Link("", nodeId, stampChronology.id());
				links.add(chronologyToStamp);
			}
		}

		//build parent concept chronology
		int chronologyNid = nodeBuilder.parseChronologyNid(nodeId);
		Optional<Entity<EntityVersion>> optionalChronology = Entity.get(chronologyNid);
		if (optionalChronology.isPresent()) {
			Entity<? extends EntityVersion> entity = optionalChronology.get();
			ConceptEntity<? extends ConceptEntityVersion> conceptEntity = (ConceptEntity<? extends ConceptEntityVersion>) entity;
			Node parentConceptChronology = nodeBuilder.buildConceptChronology(conceptEntity);
			nodes.add(parentConceptChronology);
			Link chronologyToParentConcept = new Link("", nodeId, parentConceptChronology.id());
			links.add(chronologyToParentConcept);
		}
	}

	private void exploreFromSemanticChronology(String nodeId, List<Node> nodes, List<Link> links) {
		int chronologyNid = nodeBuilder.parseChronologyNid(nodeId);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(chronologyNid);

		if (optionalEntity.isPresent()) {
			Entity<? extends EntityVersion> entity = optionalEntity.get();
			SemanticEntity<? extends SemanticEntityVersion> semanticEntity = (SemanticEntity<? extends SemanticEntityVersion>) entity;

			//build out reference Concept, Semantic
			int referenceNid = semanticEntity.referencedComponentNid();
			Optional<Entity<EntityVersion>> optionalReferenceEntity = Entity.get(referenceNid);
			if (optionalReferenceEntity.isPresent()) {
				Entity<? extends EntityVersion> referenceEntity = optionalReferenceEntity.get();
				if (referenceEntity instanceof ConceptEntity conceptEntity) {
					Node refConceptChronology = nodeBuilder.buildConceptChronology(conceptEntity);
					nodes.add(refConceptChronology);
					Link chronologyToConcept = new Link("", nodeId, refConceptChronology.id());
					links.add(chronologyToConcept);
				} else if (referenceEntity instanceof SemanticEntity refSemanticEntity) {
					Node refSemanticChronology = nodeBuilder.buildSemanticChronology(refSemanticEntity);
					nodes.add(refSemanticChronology);
					Link chronologyToSemantic = new Link("", nodeId, refSemanticChronology.id());
					links.add(chronologyToSemantic);
				}
			}

			//build out Pattern Chronology
			int patternNid = semanticEntity.patternNid();
			Optional<Entity<EntityVersion>> optionalPatternEntity = Entity.get(patternNid);
			if (optionalPatternEntity.isPresent()) {
				Entity<? extends EntityVersion> patternReferenceEntity = optionalPatternEntity.get();
				if (patternReferenceEntity instanceof PatternEntity patternEntity) {
					Node refPatternChronology = nodeBuilder.buildPatternChronology(patternEntity);
					nodes.add(refPatternChronology);
					Link semanticToPattern = new Link("", nodeId, refPatternChronology.id());
					links.add(semanticToPattern);
				}
			}

			//build out associated Semantic Versions
			Latest<SemanticEntityVersion> latestSemanticEntityVersion = calculatorService.getStampCalculator().latest(semanticEntity.nid());
			if (latestSemanticEntityVersion.isPresent()) {
				SemanticEntityVersion semanticEntityVersion = latestSemanticEntityVersion.get();
				Node versionNode = nodeBuilder.buildSemanticVersion(semanticEntityVersion);
				nodes.add(versionNode);
				Link chronologyToVersion = new Link("", nodeId, versionNode.id());
				links.add(chronologyToVersion);
			}

		}
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
		int chronologyNid = nodeBuilder.parseChronologyNid(nodeId);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(chronologyNid);

		if (optionalEntity.isPresent()) {
			Entity<? extends EntityVersion> entity = optionalEntity.get();
			PatternEntity<? extends PatternEntityVersion> patternEntity = (PatternEntity<? extends PatternEntityVersion>) entity;

			//build out associated Semantic Chronologies
			ikeRepository.findAssociatedSemanticIds(patternEntity.publicId().asUuidList().toList()).stream()
					.map(PublicIds::of)
					.map(Entity::nid)
					.map(Entity::get)
					.filter(Optional::isPresent)
					.map(optEntity -> {
						Entity<? extends EntityVersion> entityChronology = optEntity.get();
						SemanticEntity<? extends SemanticEntityVersion> semanticChronology = (SemanticEntity<? extends SemanticEntityVersion>) entityChronology;
						return semanticChronology;
					})
					.forEach(semanticEntity -> {
						Node semanticChronology = nodeBuilder.buildSemanticChronology(semanticEntity);
						nodes.add(semanticChronology);
						Link chronologyToSemantic = new Link("", nodeId, semanticChronology.id());
						links.add(chronologyToSemantic);
					});

			//Build out Pattern Version
			Latest<PatternEntityVersion> patternEntityVersionLatest = calculatorService.getStampCalculator().latest(patternEntity.nid());
			if (patternEntityVersionLatest.isPresent()) {
				PatternEntityVersion patternEntityVersion = patternEntityVersionLatest.get();
				Node patternVersion = nodeBuilder.buildPatternVersion(patternEntityVersion);
				nodes.add(patternVersion);
				Link chronologyToVersion = new Link("", nodeId, patternVersion.id());
				links.add(chronologyToVersion);
			}
		}
	}

	private void exploreFromPatternVersion(String nodeId, List<Node> nodes, List<Link> links) {
		EntityVersion entityVersion = triangulateVersion(nodeId);
		if (entityVersion instanceof PatternEntityVersion patternEntityVersion) {
			patternEntityVersion.fieldDefinitions()
					.forEach(fieldDefinition -> {
						Node fieldDefinitionNode = nodeBuilder.buildPatternVersionField(fieldDefinition);
						nodes.add(fieldDefinitionNode);
						Link versionToFieldDefinition = new Link("", nodeId, fieldDefinitionNode.id());
						links.add(versionToFieldDefinition);
					});
		}

		//Connect back to Pattern Chronology
		int chronologyNid = nodeBuilder.parseChronologyNid(nodeId);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(chronologyNid);

		if (optionalEntity.isPresent()) {
			Entity<? extends EntityVersion> entity = optionalEntity.get();
			PatternEntity<? extends PatternEntityVersion> patternEntity = (PatternEntity<? extends PatternEntityVersion>) entity;
			Node patternChronology = nodeBuilder.buildPatternChronology(patternEntity);
			nodes.add(patternChronology);
			Link chronologyToPattern = new Link("", nodeId, patternChronology.id());
			links.add(chronologyToPattern);
		}
	}

	private void exploreFromStampChronology(String nodeId, List<Node> nodes, List<Link> links) {
		int chronologyNid = nodeBuilder.parseChronologyNid(nodeId);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(chronologyNid);

		if (optionalEntity.isPresent()) {
			Entity<? extends EntityVersion> entity = optionalEntity.get();
			StampEntity<? extends StampEntityVersion> stampEntity = (StampEntity<? extends StampEntityVersion>) entity;
			Latest<StampEntityVersion> latestStampEntityVersion = calculatorService.getStampCalculator().latest(stampEntity.nid());
			if (latestStampEntityVersion.isPresent()) {
				StampEntityVersion stampEntityVersion = latestStampEntityVersion.get();
				Node versionNode = nodeBuilder.buildStampVersion(stampEntityVersion);
				nodes.add(versionNode);
				Link chronologyToVersion = new Link("", nodeId, versionNode.id());
				links.add(chronologyToVersion);
			}
				
		}

	}

	private void exploreFromStampVersion(String nodeId, List<Node> nodes, List<Link> links) {
		EntityVersion entityVersion = triangulateVersion(nodeId);
		if (entityVersion instanceof StampEntityVersion stampEntityVersion) {

			//State/Status Chronology
			Optional<Entity<EntityVersion>> optionalStatus = Entity.get(stampEntityVersion.stateNid());
			if (optionalStatus.isPresent()) {
				Entity<? extends EntityVersion> entity = optionalStatus.get();
				ConceptEntity<? extends ConceptEntityVersion> conceptEntity = (ConceptEntity<? extends ConceptEntityVersion>) entity;
				Node stateChronology = nodeBuilder.buildConceptChronology(conceptEntity);
				nodes.add(stateChronology);
				Link chronologyToState = new Link("", nodeId, stateChronology.id());
				links.add(chronologyToState);
			}

			//Author Chronology
			Optional<Entity<EntityVersion>> optionalAuthor = Entity.get(stampEntityVersion.authorNid());
			if (optionalAuthor.isPresent()) {
				Entity<? extends EntityVersion> entity = optionalAuthor.get();
				ConceptEntity<? extends ConceptEntityVersion> authorEntity = (ConceptEntity<? extends ConceptEntityVersion>) entity;
				Node authorChronology = nodeBuilder.buildConceptChronology(authorEntity);
				nodes.add(authorChronology);
				Link chronologyToAuthor = new Link("", nodeId, authorChronology.id());
				links.add(chronologyToAuthor);
			}

			//Module Chronology
			Optional<Entity<EntityVersion>> optionalModule = Entity.get(stampEntityVersion.moduleNid());
			if (optionalModule.isPresent()) {
				Entity<? extends EntityVersion> entity = optionalModule.get();
				ConceptEntity<? extends ConceptEntityVersion> moduleEntity = (ConceptEntity<? extends ConceptEntityVersion>) entity;
				Node moduleChronology = nodeBuilder.buildConceptChronology(moduleEntity);
				nodes.add(moduleChronology);
				Link chronologyToModule = new Link("", nodeId, moduleChronology.id());
				links.add(chronologyToModule);
			}

			//Path Chronology
			Optional<Entity<EntityVersion>> optionalPath = Entity.get(stampEntityVersion.pathNid());
			if (optionalPath.isPresent()) {
				Entity<? extends EntityVersion> entity = optionalPath.get();
				ConceptEntity<? extends ConceptEntityVersion> pathEntity = (ConceptEntity<? extends ConceptEntityVersion>) entity;
				Node pathChronology = nodeBuilder.buildConceptChronology(pathEntity);
				nodes.add(pathChronology);
				Link chronologyToPath = new Link("", nodeId, pathChronology.id());
				links.add(chronologyToPath);
			}

			//Link back to Stamp Chronology
			int chronologyNid = nodeBuilder.parseChronologyNid(nodeId);
			Optional<Entity<EntityVersion>> optionalChronology = Entity.get(chronologyNid);
			if (optionalChronology.isPresent()) {
				Entity<? extends EntityVersion> entity = optionalChronology.get();
				StampEntity<? extends StampEntityVersion> stampEntity = (StampEntity<? extends StampEntityVersion>) entity;
				Node stampChronology = nodeBuilder.buildStampChronology(stampEntity);
				nodes.add(stampChronology);
				Link chronologyToStamp = new Link("", nodeId, stampChronology.id());
				links.add(chronologyToStamp);
			}
		}
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
