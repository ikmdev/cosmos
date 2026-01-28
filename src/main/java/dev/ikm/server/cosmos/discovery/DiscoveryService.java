package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

	public ExplorerVisualization buildVisualization(DiscoverySearchForm discoverySearchForm) {
		List<Node> nodes = new ArrayList<>();
		List<Link> links = new ArrayList<>();

		searchService.searchTinkarData(discoverySearchForm.query())
				.forEach(searchResult -> {
					 Latest<SemanticEntityVersion> latest = ikeRepository.findLatestSemanticById(searchResult.id());
					 if (latest.isPresent()) {
						 SemanticEntityVersion semanticEntityVersion = latest.get();
					 }
				});

		return new ExplorerVisualization(nodes, links);
	}





}
