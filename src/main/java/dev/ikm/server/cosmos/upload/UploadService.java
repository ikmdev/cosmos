package dev.ikm.server.cosmos.upload;

import dev.ikm.server.cosmos.database.Context;
import dev.ikm.server.cosmos.database.IkeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UploadService {

	private final IkeRepository ikeRepository;
	private final Context context;

	@Autowired
	public UploadService(IkeRepository ikeRepository, Context context) {
		this.ikeRepository = ikeRepository;
		this.context = context;
	}


}
