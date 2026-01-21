package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.List;
import java.util.UUID;

@Controller
public class DiscoveryController {

	Logger LOG = LoggerFactory.getLogger(DiscoveryController.class);

	private final DiscoveryService discoveryService;
	private final SearchService searchService;
	private final CalculatorService calculatorService;

	@Autowired
	public DiscoveryController(DiscoveryService discoveryService,
							   SearchService searchService,
							   CalculatorService calculatorService) {
		this.discoveryService = discoveryService;
		this.searchService = searchService;
		this.calculatorService = calculatorService;
	}

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "discovery");
		model.addAttribute("titleDisplayName", "Discovery");
		model.addAttribute("footerText", "Navigate your knowledge universe");
		model.addAttribute("graphData", quickViz());
		model.addAttribute("graphId", "discovery-graph");
	}

	@GetMapping("/discovery")
	public String getDiscovery(Model model) {
		addSharedModelAttributes(model);
		return "discovery";
	}

	@HxRequest
	@GetMapping("/discovery")
	public FragmentsRendering getDiscoveryWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("discovery :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}

	@HxRequest
	@GetMapping("/discovery/search")
	public String search(
			@ModelAttribute("activeScopeId") UUID activeScopeId,
			@ModelAttribute("scopeForm") DiscoverySearchForm discoverySearchForm,
			Model model) {
		calculatorService.setScope(activeScopeId);
		List<SearchResult> results = searchService.search(discoverySearchForm.query());
		model.addAttribute("graphData", quickViz());
		return "discovery-search";
	}

	private GraphViz quickViz() {
		Node node = new Node("Andrew", 1);
		Node node2 = new Node("Bob", 2);
		Link link = new Link(node.id(), node2.id(), 1);
		return new GraphViz(List.of(node, node2), List.of(link));
	}
}
