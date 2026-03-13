package dev.ikm.server.cosmos.observatory;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.FragmentsRendering;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ObservatoryController {

	Logger LOG = LoggerFactory.getLogger(ObservatoryController.class);

	private final ObservatoryService observatoryService;

	public ObservatoryController(ObservatoryService observatoryService) {
		this.observatoryService = observatoryService;
	}

	private void addNewObservatoryModelAttributes(Model model) {
		// Add form backing object with default/empty values
		model.addAttribute("observatoryForm", new ObservatoryForm(
				"",
				null,
				null,
				null,
				Set.of(),
				Set.of()));

		// Add data needed to render the form's select options
		observatoryService.retrieveStamps().ifPresent(stampCoordinates -> model.addAttribute("stampCoordinates", stampCoordinates));
		observatoryService.retrieveLanguages().ifPresent(languageCoordinates -> model.addAttribute("languageCoordinates", languageCoordinates));
		observatoryService.retrieveNavigations().ifPresent(navigationCoordinates -> model.addAttribute("navigationCoordinates", navigationCoordinates));
		observatoryService.retrieveModules().ifPresent(modules -> model.addAttribute("modules", modules));
	}

	@HxRequest
	@GetMapping("/observatory/manage")
	public FragmentsRendering getManageObservatories(
			Model model) {
		observatoryService.retrieveAllObservatories().ifPresent(observatories -> model.addAttribute("observatories", observatories));
		return FragmentsRendering
				.with("fragments/observatory/observatory-manage :: observatory-manage-table")
				.build();
	}

	@HxRequest
	@GetMapping("/observatory/new")
	public FragmentsRendering getNewObservatory(
			Model model) {
		addNewObservatoryModelAttributes(model);
		return FragmentsRendering
				.with("fragments/observatory/observatory-form :: create-observatory-form")
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
		observatoryService.retrieveAllObservatories().ifPresent(observatories -> model.addAttribute("observatories", observatories));
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

}
