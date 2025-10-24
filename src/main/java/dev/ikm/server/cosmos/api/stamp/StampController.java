package dev.ikm.server.cosmos.api.stamp;

import dev.ikm.server.cosmos.database.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stamp")
public class StampController {

	private final StampService stampService;
	private final Context context;

	@Autowired
	public StampController(StampService stampService, Context context) {
		this.stampService = stampService;
		this.context = context;
	}

	@GetMapping("/{uuid}")
	public StampChronologyDTO getStampWithALlVersions(
			@PathVariable("uuid") UUID uuid) {
		return stampService.retrieveStampWithAllVersions(uuid);
	}

	@GetMapping("/latest/{uuid}")
	public StampChronologyDTO getStampWithLatestVersion(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		context.setViewContext(stampId, langId, navId);
		return stampService.retrieveStampWithLatestVersion(uuid);
	}

	@GetMapping("/{uuid}/semantics")
	public List<List<UUID>> getSemanticsForStampChronology(
			@PathVariable("uuid") UUID uuid) {
		return stampService.retrieveSemantics(uuid);
	}
}
