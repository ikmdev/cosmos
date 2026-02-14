package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.api.coordinate.CoordinateService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

import java.util.UUID;

@Controller
public class ObservatoryController {

	Logger LOG = LoggerFactory.getLogger(ObservatoryController.class);

	private final CoordinateService coordinateService;
	private final ObservatoryService observatoryService;


	@Autowired
	public ObservatoryController(CoordinateService coordinateService, ObservatoryService observatoryService) {
		this.coordinateService = coordinateService;
		this.observatoryService = observatoryService;
	}

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "observatory");
		model.addAttribute("titleDisplayName", "Observatory");
		model.addAttribute("footerText", "Configuring your cosmic viewpoint");
	}

	@GetMapping("/observatory")
	public String getObservatory(Model model) {
		addSharedModelAttributes(model);

		//Add to the model the enumerations for Stamp, Language, and Navigation coordinates
		ObservatoryForm observatoryForm = new ObservatoryForm(
				"",
				coordinateService.stampCoordinates(),
				coordinateService.languageCoordinates(),
				coordinateService.navigationCoordinates(),
				null,
				null,
				null);
		model.addAttribute("observatoryForm", observatoryForm);
		return "observatory";
	}

	@HxRequest
	@GetMapping("/observatory")
	public FragmentsRendering getObservatoryWithFragments(Model model) {
		addSharedModelAttributes(model);

		//Add to the model the enumerations for Stamp, Language, and Navigation coordinates
		ObservatoryForm observatoryForm = new ObservatoryForm(
				"",
				coordinateService.stampCoordinates(),
				coordinateService.languageCoordinates(),
				coordinateService.navigationCoordinates(),
				null,
				null,
				null);
		model.addAttribute("observatoryForm", observatoryForm);
		return FragmentsRendering
				.with("observatory :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}

	@HxRequest
	@PostMapping("/observatory")
	public FragmentsRendering postObservatory(@ModelAttribute("observatoryForm") ObservatoryForm observatoryForm, Model model) {
		//Create a new Observatory
		Observatory newObservatory = observatoryService.saveNewObservatory(
				observatoryForm.name(),
				observatoryForm.selectedStampCoordinateId(),
				observatoryForm.selectedLanguageCoordinateId(),
				observatoryForm.selectedNavigationCoordinateId());
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
		model.addAttribute("observatories", observatoryService.retrieveAllObservatories());
		return FragmentsRendering
				.with("fragments/layout/navigation :: observatorySelector")
				.build();
	}

	@HxRequest
	@DeleteMapping("/observatory/{id}")
	public FragmentsRendering deleteObservatory(@PathVariable("id") UUID id, Model model) {
		observatoryService.removeObservatory(id);
		model.addAttribute("observatories", observatoryService.retrieveAllObservatories());
		return FragmentsRendering
				.with("fragments/layout/navigation :: observatorySelector")
				.build();
	}
}
