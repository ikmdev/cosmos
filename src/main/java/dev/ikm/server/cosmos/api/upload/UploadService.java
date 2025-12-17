package dev.ikm.server.cosmos.api.upload;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UploadService {

	private final IkeRepository ikeRepository;
	private final CalculatorService calculatorService;

	@Autowired
	public UploadService(IkeRepository ikeRepository, CalculatorService calculatorService) {
		this.ikeRepository = ikeRepository;
		this.calculatorService = calculatorService;
	}


}
