package dev.ikm.server.cosmos.portal;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.ikm.server.cosmos.constellation.ConstellationEntity;
import dev.langchain4j.data.document.parser.markdown.MarkdownDocumentParser;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

@Service
public class PortalService {

	private final CosmosAgent cosmosAgent;
	private final ConcurrentMap<UUID, ConstellationEntity> constellationDB;
	
	private final MarkdownDocumentParser markdownDocumentParser;
	private final Parser markdownParser;
	private final HtmlRenderer htmlRenderer;

	public PortalService(ConcurrentMap<UUID, ConstellationEntity> constellationDB, CosmosAgent cosmosAgent) {
		this.constellationDB = constellationDB;
		this.cosmosAgent = cosmosAgent;
		this.markdownDocumentParser = new MarkdownDocumentParser();
		this.markdownParser = Parser.builder().build();
		this.htmlRenderer = HtmlRenderer.builder().build();
	}

	public String converse(String sessionId, String userMessage, MultipartFile userAttachment, UUID constellationId) {
		ConstellationEntity constellationEntity = constellationDB.get(constellationId);
		String servicePrompt = constellationEntity.portalPrompt();
		return cosmosAgent.chat(sessionId, servicePrompt, userMessage);
	}

}
