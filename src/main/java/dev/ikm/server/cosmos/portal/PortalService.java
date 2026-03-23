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
	private final CosmosOptimizer cosmosOptimizer;
	private final ConcurrentMap<UUID, ConstellationEntity> constellationDB;
	private final ContextService contextService;
	private final FormattingService formattingService;

	public PortalService(ConcurrentMap<UUID, ConstellationEntity> constellationDB, CosmosAgent cosmosAgent,
			CosmosOptimizer cosmosOptimizer, ContextService contextService, FormattingService formattedService) {
		this.constellationDB = constellationDB;
		this.cosmosAgent = cosmosAgent;
		this.cosmosOptimizer = cosmosOptimizer;
		this.contextService = contextService;
		this.formattingService = formattedService;
	}

	public Optional<String> converse(String sessionId, String userMessage, MultipartFile uploadedFile,
			UUID constellationId) {
		ConstellationEntity constellationEntity = constellationDB.get(constellationId);
		String optimizedUserMessage = cosmosOptimizer.optimizeForSearch(userMessage);
		String ragContext = contextService.buildRAGContext(optimizedUserMessage, constellationId);
		String attachmentContext = contextService.buildAttachmentContext(uploadedFile);
		String aiResponse = cosmosAgent.chat(sessionId, constellationEntity.portalPrompt(), ragContext,
				attachmentContext, userMessage);
		LOG.info("\n" + "SessionId: {}\n\n" + "System Prompt: {}\n\n" + "RAG Context: {}\n\n"
				+ "Attachment Context: {}\n\n" + "User Prompt: {}\n\n" + "Optimized Prompt: {}\n\n"
				+ "AI Response: {}\n",
				sessionId, constellationEntity.portalPrompt(), ragContext, attachmentContext,
				userMessage, optimizedUserMessage, aiResponse);
		return Optional.of(formattingService.formatAIResponse(aiResponse));
	}
}