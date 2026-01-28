package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class DiscoveryController {

	Logger LOG = LoggerFactory.getLogger(DiscoveryController.class);

	private final DiscoveryService discoveryService;
	private final CalculatorService calculatorService;

	@Autowired
	public DiscoveryController(DiscoveryService discoveryService,
							   CalculatorService calculatorService) {
		this.discoveryService = discoveryService;
		this.calculatorService = calculatorService;
	}

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "discovery");
		model.addAttribute("titleDisplayName", "Discovery");
		model.addAttribute("footerText", "Navigate your knowledge universe");
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
	@ResponseBody
	public ExplorerVisualization search(
			@ModelAttribute("activeScopeId") UUID activeScopeId,
			@ModelAttribute("scopeForm") DiscoverySearchForm discoverySearchForm,
			Model model) {
		if (activeScopeId != null) {
			calculatorService.setScope(activeScopeId);
		}
		return discoveryService.buildVisualization(discoverySearchForm);
	}

	@GetMapping("/discovery/expand")
	@ResponseBody
	public ExplorerVisualization expand(@RequestParam("nodeId") String nodeId) {
		// In a real implementation, this would call discoveryService.getNeighbors(nodeId)
		return mockExpansion(nodeId);
	}

	private ExplorerVisualization mockExpansion(String sourceId) {
		List<Node> nodes = new ArrayList<>();
		List<Link> links = new ArrayList<>();

		// Generate 3 mock neighbors
		for (int i = 1; i <= 3; i++) {
			String newId = sourceId + "-" + UUID.randomUUID().toString().substring(0, 4);
			nodes.add(new Node(newId, NodeType.CONCEPT, "New Concept", 2, 1));
			links.add(new Link("is-a", sourceId, newId));
		}
		return new ExplorerVisualization(nodes, links);
	}

	private ExplorerVisualization quickViz() {
		List<Node> nodes = new ArrayList<>();
		List<Link> links = new ArrayList<>();
		Node conceptA = new Node("a9faf540-81b1-43cd-ae87-eef505aef52e", NodeType.CONCEPT, "", 1, 10);
		Node conceptB = new Node("b7925af4-0b38-4f6c-94aa-67049a3fa03b", NodeType.CONCEPT, "", 1, 1);
		nodes.add(conceptA);
		nodes.add(conceptB);

		Node semantic1 = new Node("2ee5edb7-a72b-4866-a96f-71f904c6166a", NodeType.SEMANTIC, "My Semantic 1", 2, 1);
		Node semantic2 = new Node("6b1746fc-c4f5-44ca-a1b2-d75159597a88", NodeType.SEMANTIC, "My Semantic 2", 2, 1);
		nodes.add(semantic1);
		nodes.add(semantic2);


		Node pattern1 = new Node("c09564dd-e415-4fb5-82dc-75a6552d26e6", NodeType.PATTERN, "My Pattern 1", 3, 1);
		Node pattern2 = new Node("242c04fb-153f-4ef0-a873-5e380a940b77", NodeType.PATTERN, "My Pattern 1", 3, 1);
		nodes.add(pattern1);
		nodes.add(pattern2);

		Link link = new Link("", semantic1.id(), conceptA.id());
		Link link2 = new Link("", semantic2.id(), conceptB.id());
		links.add(link);
		links.add(link2);
		return new ExplorerVisualization(nodes, links);
	}
}
