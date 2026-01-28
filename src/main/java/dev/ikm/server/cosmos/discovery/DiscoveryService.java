package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DiscoveryService {

	private final CalculatorService calculatorService;
	private final SearchService searchService;
	private final IkeRepository ikeRepository;

	@Autowired
	public DiscoveryService(CalculatorService calculatorService, SearchService searchService, IkeRepository ikeRepository) {
		this.calculatorService = calculatorService;
		this.searchService = searchService;
		this.ikeRepository = ikeRepository;
	}

	public ExplorerData buildExplorationVisualization(ExplorerSearchForm explorerSearchForm) {
		List<Node> nodes = new ArrayList<>();
		List<Link> links = new ArrayList<>();

		searchService.tinkarDataSearch(explorerSearchForm.query(), explorerSearchForm.maxResults(), SearchService.SortType.SEMANTIC_SCORE)
				.forEach(searchResult -> {
					 Latest<SemanticEntityVersion> latest = ikeRepository.findLatestSemanticById(searchResult.id());
					 if (latest.isPresent()) {
						 SemanticEntityVersion semanticEntityVersion = latest.get();

						 int nodeId = semanticEntityVersion.entity().publicIdHash();
						 String label = semanticEntityVersion.entity().idString();
						 int group = 10;
						 int size = 1;
						 nodes.add(new Node(
										 nodeId,
										 NodeType.SEMANTIC,
										 label,
										 group,
										 size,
										 buildNodeValues(semanticEntityVersion))
						 );
					 }
				});

		return new ExplorerData(nodes, links);
	}

	private List<String> buildNodeValues(SemanticEntityVersion semanticEntityVersion) {
		List<String> values = new ArrayList<>();

		for (int i = 0; i < semanticEntityVersion.fieldValues().size(); i++) {
			Object object = semanticEntityVersion.fieldValues().get(i);
			if (object instanceof EntityFacade entityFacade) {
				values.add("Entity: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entityFacade));
			} else {
				switch (semanticEntityVersion.fieldDataType(i)) {
					case LONG -> values.add("Long: " + semanticEntityVersion.fieldAsLong(i));
					case FLOAT -> values.add("Float: " + semanticEntityVersion.fieldAsFloat(i));
					case BOOLEAN -> values.add("Boolean: " + semanticEntityVersion.fieldAsBoolean(i));
					case INTEGER -> values.add("Integer: " + semanticEntityVersion.fieldAsInt(i));
					case STRING -> values.add("String: " + semanticEntityVersion.fieldAsString(i));
					case DECIMAL -> values.add("Decimal: " + semanticEntityVersion.fieldAsDouble(i));
					case COMPONENT_ID_LIST -> {

						values.add("Component: " + object);
					}
					case COMPONENT_ID_SET -> {
						values.add("Component: " + object);
					}
					case DITREE -> values.add("DITree: " + object);
					default -> throw new RuntimeException("Unsupported data type:" + semanticEntityVersion.fieldDataType(i));
				};
			}
		}

		semanticEntityVersion.fieldValues()
				.forEach(o -> {

				});
		return values;
	}

	public ExplorerData exploreNeighbors(UUID nodeId, ExplorerSearchForm explorerSearchForm) {
		List<Node> nodes = new ArrayList<>();
		List<Link> links = new ArrayList<>();

		return new ExplorerData(nodes, links);
	}





}
