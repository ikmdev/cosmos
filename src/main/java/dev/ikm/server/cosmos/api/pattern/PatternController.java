package dev.ikm.server.cosmos.api.pattern;

import dev.ikm.server.cosmos.api.coordinate.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pattern")
public class PatternController {

	private final PatternService patternService;
	private final Context context;

	@Autowired
	public PatternController(PatternService patternService, Context context) {
		this.patternService = patternService;
		this.context = context;
	}

	@GetMapping("/{uuid}")
	public PatternChronologyDTO getPatternWithAllVersions(
			@PathVariable("uuid") UUID uuid) {
		return patternService.retrievePatternWithAllVersions(uuid);
	}

	@GetMapping("/latest/{uuid}")
	public PatternChronologyDTO getPatternLatestVersion(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		context.setViewContext(stampId, langId, navId);
		return patternService.retrievePatternWithLatestVersion(uuid);
	}

	@GetMapping("/{uuid}/fqn")
	public String getPatternFQN(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		context.setViewContext(stampId, langId, navId);
		return patternService.calculateFQN(uuid);
	}

	@GetMapping("/{uuid}/syn")
	public String getPatternSYN(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		context.setViewContext(stampId, langId, navId);
		return patternService.calculateSYN(uuid);
	}

	@GetMapping("/{uuid}/def")
	public String getPatternDEF(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		context.setViewContext(stampId, langId, navId);
		return patternService.calculateDEF(uuid);
	}

	@GetMapping("/{uuid}/semantics")
	public List<List<UUID>> getSemanticsForPatternChronology(
			@PathVariable("uuid") UUID uuid) {
		return patternService.retrieveSemantics(uuid);
	}
}
