package dev.ikm.server.cosmos.portal;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.ikm.server.cosmos.constellation.ConstellationEntity;

@Service
public class PortalService {

	private final static Logger LOG = LoggerFactory.getLogger(PortalService.class.getName());

	private final CosmosAgent cosmosAgent;
	private final ConcurrentMap<UUID, ConstellationEntity> constellationDB;
	private final ContextService contextService;
	private final FormattingService formattingService;

	public PortalService(ConcurrentMap<UUID, ConstellationEntity> constellationDB, CosmosAgent cosmosAgent,
			ContextService contextService, FormattingService formattedService) {
		this.constellationDB = constellationDB;
		this.cosmosAgent = cosmosAgent;
		this.contextService = contextService;
		this.formattingService = formattedService;
	}

	public Optional<String> converse(String sessionId, String userMessage, MultipartFile uploadedFile,
			UUID constellationId) {
		ConstellationEntity constellationEntity = constellationDB.get(constellationId);
		String ragContext = contextService.buildRAGContext(userMessage, constellationId);
		String attachmentContext = contextService.buildAttachmentContext(uploadedFile);

		LOG.info("\n" + "SessionId: {}\n\n" + "System Prompt: {}\n\n" + "RAG Context: {}\n\n"
				+ "Attachment Context: {}\n\n" + "User Prompt: {}\n",
				sessionId, constellationEntity.portalPrompt(), ragContext, attachmentContext,
				userMessage);

		String aiResponse = cosmosAgent.chat(sessionId, constellationEntity.portalPrompt(), ragContext,
				attachmentContext,
				userMessage);
		return Optional.of(formattingService.formatAIResponse(aiResponse));
	}
}