package dev.ikm.server.cosmos.api.upload;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

	@PostMapping("/single")
	public UploadReport singlePBZipUpload(@RequestBody UploadReport uploadReport) {
		return uploadReport;
	}

	@PostMapping("/multiple")
	public UploadReport multiplePBZipUpload(@RequestBody UploadReport uploadReport) {
		return uploadReport;
	}
}
