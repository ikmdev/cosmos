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
		model.addAttribute("explorerSearchForm", new ExplorerSearchForm("", false, 25));
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
	public ExplorerData search(
			@ModelAttribute("activeScopeId") UUID activeScopeId,
			@ModelAttribute("explorerSearchForm") ExplorerSearchForm explorerSearchForm,
			Model model) {
		if (activeScopeId != null) {
			calculatorService.setScope(activeScopeId);
		}
		return discoveryService.buildExplorationVisualization(explorerSearchForm);
	}

	@GetMapping("/discovery/explore")
	@ResponseBody
	public ExplorerData explore(@ModelAttribute("activeScopeId") UUID activeScopeId,
								@ModelAttribute("explorerSearchForm") ExplorerSearchForm explorerSearchForm,
								@RequestParam("nodeId") int nodeId,
								Model model) {
		if (activeScopeId != null) {
			calculatorService.setScope(activeScopeId);
		}
		return discoveryService.explore(nodeId, explorerSearchForm);
	}
}
