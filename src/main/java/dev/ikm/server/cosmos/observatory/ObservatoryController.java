package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.List;
import java.util.UUID;

@Controller
public class ObservatoryController {

	Logger LOG = LoggerFactory.getLogger(ObservatoryController.class);

	private final ObservatoryService observatoryService;


	@Autowired
	public ObservatoryController(ObservatoryService observatoryService) {
		this.observatoryService = observatoryService;
	}

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "observatory");
		model.addAttribute("titleDisplayName", "Observatory");
		model.addAttribute("footerText", "Configuring your cosmic viewpoint");

		// Add form backing object with default/empty values
		model.addAttribute("observatoryForm", new ObservatoryForm(
				"",
				null,
				null,
				null,
				List.of(),
				List.of(),
				List.of()));

		// Add data needed to render the form's select options
		observatoryService.retrieveStamps().ifPresent(stampCoordinates -> model.addAttribute("stampCoordinates", stampCoordinates));
		observatoryService.retrieveLanguages().ifPresent(languageCoordinates -> model.addAttribute("languageCoordinates", languageCoordinates));
		observatoryService.retrieveNavigations().ifPresent(navigationCoordinates -> model.addAttribute("navigationCoordinates", navigationCoordinates));
		observatoryService.retrieveModules().ifPresent(modules -> model.addAttribute("modules", modules));

		// Add data for the initial scope tree view
		observatoryService.retrieveRootScope().ifPresent(scope -> {
			model.addAttribute("scope", scope);
			observatoryService.retrieveChildren(scope.facade()).ifPresent(children -> model.addAttribute("children", children));
		});
	}

	@GetMapping("/observatory")
	public String getObservatory(
			Model model) {
		addSharedModelAttributes(model);
		return "observatory";
	}

	@HxRequest
	@GetMapping("/observatory")
	public FragmentsRendering getObservatoryWithFragments(
			Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("observatory :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}

	@HxRequest
	@PostMapping("/observatory")
	public FragmentsRendering postObservatory(
			@ModelAttribute("observatoryForm") ObservatoryForm observatoryForm,
			Model model) {
		//Create a new Observatory
		Observatory newObservatory = observatoryService.saveNewObservatory(observatoryForm);
		model.addAttribute("newObservatory", newObservatory);
		model.addAttribute("observatories", observatoryService.retrieveAllObservatories());
		return FragmentsRendering
				.with("fragments/observatory/observatory-table-row :: new-observatory-row")
				.fragment("fragments/layout/navigation :: observatorySelector")
				.build();
	}

	@HxRequest
	@PostMapping("/observatory/selected")
	public FragmentsRendering postSelectedObservatory(@RequestParam("observatoryId") UUID id, HttpServletResponse response, Model model) {
		Cookie cookie = new Cookie("cosmos-observatory-id", id.toString());
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		model.addAttribute("activeObservatoryId", id);
		observatoryService.retrieveAllObservatories().ifPresent(observatories -> model.addAttribute("observatories", observatories));
		return FragmentsRendering
				.with("fragments/layout/navigation :: observatorySelector")
				.build();
	}

	@HxRequest
	@DeleteMapping("/observatory/{id}")
	public FragmentsRendering deleteObservatory(@PathVariable("id") UUID id, Model model) {
		observatoryService.removeObservatory(id);
		observatoryService.retrieveAllObservatories().ifPresent(observatories -> model.addAttribute("observatories", observatories));
		return FragmentsRendering
				.with("fragments/layout/navigation :: observatorySelector")
				.build();
	}


	@HxRequest
	@GetMapping("/observatory/scope/search")
	public FragmentsRendering getSearchResults(
			@RequestParam(value = "query", required = false, defaultValue = "") String query,
			@PageableDefault(size = 10, page = 0) Pageable pageable,
			Model model) {
		observatoryService.search(query, pageable).ifPresent(searchResults -> model.addAttribute("scopeSearchResultsPage", searchResults));
		return FragmentsRendering.with("fragments/observatory/observatory-scope-search :: search-results-list").build();
	}


	@HxRequest
	@GetMapping("/observatory/scope/traverse")
	public FragmentsRendering getTraverse(
			@RequestParam("nid") @StringToFacade Facade scope,
			Model model) {
		observatoryService.buildScopeNode(scope).ifPresent(scopeNode -> model.addAttribute("scope", scopeNode));
		observatoryService.retrieveChildren(scope).ifPresent(children -> model.addAttribute("children", children));
		observatoryService.retrieveParents(scope).ifPresent(parents -> model.addAttribute("parents", parents));
		return FragmentsRendering.with("fragments/observatory/observatory-scope-tree :: scope-tree").build();
	}

	@HxRequest
	@PostMapping("/observatory/scope/add")
	public FragmentsRendering includeScope(
			@RequestParam("nid") @StringToFacade Facade scope,
			Model model) {
		model.addAttribute("item", scope);
		model.addAttribute("fieldName", "includedScopes");
		return FragmentsRendering.with("fragments/observatory/observatory-scope-list-item :: scope-list-item").build();
	}

}
