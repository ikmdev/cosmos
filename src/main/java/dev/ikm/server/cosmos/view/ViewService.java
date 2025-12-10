package dev.ikm.server.cosmos.view;

import org.springframework.stereotype.Service;

@Service
public class ViewService {

	private final ViewRepository viewRepository;

	public ViewService(ViewRepository viewRepository) {
		this.viewRepository = viewRepository;
	}


}
