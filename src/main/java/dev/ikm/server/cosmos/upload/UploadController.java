package dev.ikm.server.cosmos.upload;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

	@PostMapping("/single")
	public UploadReportDTO singlePBZipUpload(@RequestBody UploadReportDTO uploadReportDTO) {
		return uploadReportDTO;
	}

	@PostMapping("/multiple")
	public UploadReportDTO multiplePBZipUpload(@RequestBody UploadReportDTO uploadReportDTO) {
		return uploadReportDTO;
	}
}
