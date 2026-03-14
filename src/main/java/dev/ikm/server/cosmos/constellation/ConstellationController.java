package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.observatory.StringToFacade;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.Set;
import java.util.UUID;

@Controller
public class ConstellationController {

	private final Logger LOG = LoggerFactory.getLogger(ConstellationController.class);

	private final ConstellationService constellationService;
	private final CalculatorService calculatorService;

	@Autowired
	public ConstellationController(ConstellationService constellationService, CalculatorService calculatorService) {
		this.constellationService = constellationService;
		this.calculatorService = calculatorService;
	}

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "constellation");
		model.addAttribute("titleDisplayName", "Constellation");
		model.addAttribute("footerText", "Mapping the stars of knowledge");
		model.addAttribute("constellationForm", new ConstellationForm("", Set.of(), ""));
		constellationService.retrieveAllConstellations(calculatorService.getObservatoryId())
				.ifPresent(constellations -> model.addAttribute("constellations", constellations));

		// Add data for the initial scope tree view
		constellationService.retrieveRootScope().ifPresent(scope -> {
			model.addAttribute("scope", scope);
			constellationService.retrieveChildren(scope.facade()).ifPresent(children -> model.addAttribute("children", children));
		});
	}

	@GetMapping("/constellation")
	public String getConstellation(Model model) {
		addSharedModelAttributes(model);
		return "constellation";
	}

	@HxRequest
	@GetMapping("/constellation")
	public FragmentsRendering getConstellationWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("constellation :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}

	@HxRequest
	@PostMapping("/constellation")
	public FragmentsRendering postConstellation(
			@ModelAttribute("constellationForm") ConstellationForm constellationForm,
			Model model) {
		constellationService.createConstellation(calculatorService.getObservatoryId(), constellationForm)
				.ifPresent(constellation -> model.addAttribute("newConstellation", constellation));
		return FragmentsRendering
				.with("fragments/constellation/constellation-table-row :: new-constellation-row")
				.build();
	}

	@HxRequest
	@DeleteMapping("/constellation/{id}")
	@ResponseBody
	public String deleteConstellation(@PathVariable("id") UUID id, Model model) {
		constellationService.removeConstellation(id);
		return "";
	}

	@HxRequest
	@GetMapping("/constellation/scope/search")
	public FragmentsRendering getSearchResults(
			@RequestParam(value = "query", required = false, defaultValue = "") String query,
			@PageableDefault(size = 10, page = 0) Pageable pageable,
			Model model) {
		constellationService.search(query, pageable)
				.ifPresent(searchResults -> model.addAttribute("scopeSearchResultsPage", searchResults));
		return FragmentsRendering.with("fragments/constellation/constellation-scope-search :: search-results-list").build();
	}

	@HxRequest
	@GetMapping("/constellation/scope/traverse")
	public FragmentsRendering getTraverse(
			@RequestParam("nid") @StringToFacade Facade scope,
			Model model) {
		constellationService.buildScopeNode(scope).ifPresent(scopeNode -> model.addAttribute("scope", scopeNode));
		constellationService.retrieveChildren(scope).ifPresent(children -> model.addAttribute("children", children));
		constellationService.retrieveParents(scope).ifPresent(parents -> model.addAttribute("parents", parents));
		return FragmentsRendering.with("fragments/constellation/constellation-scope-tree :: scope-tree").build();
	}

	@HxRequest
	@PostMapping("/constellation/scope/add")
	public FragmentsRendering addScope(
			@RequestParam("nid") @StringToFacade Facade scope,
			Model model) {
		model.addAttribute("item", scope);
		constellationService.retrieveDescendantCount(scope)
				.ifPresent(count -> model.addAttribute("descendantCount", count));
		return FragmentsRendering.with("fragments/constellation/constellation-scope-list-item :: scope-list-item").build();
	}

	@HxRequest
	@GetMapping("/constellations/status/{id}")
	public FragmentsRendering getConstellationStatus(@PathVariable("id") UUID id, Model model) {
		constellationService.getConstellationStatus(id)
				.ifPresent(constellation -> model.addAttribute("constellation", constellation));
		return FragmentsRendering
				.with("fragments/constellation/constellation-table-row :: constellation-row")
				.build();
	}

	@HxRequest
	@GetMapping("/constellation/table")
	public FragmentsRendering getConstellationDataForTable(Model model) {
		constellationService.retrieveAllConstellations(calculatorService.getObservatoryId())
				.ifPresent(constellations -> model.addAttribute("constellations", constellations));
		return FragmentsRendering
				.with("constellation :: constellation-table-body")
				.build();
	}
}
