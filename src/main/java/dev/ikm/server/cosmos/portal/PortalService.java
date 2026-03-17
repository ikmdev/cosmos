package dev.ikm.server.cosmos.portal;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.ikm.server.cosmos.constellation.ConstellationEntity;

@Service
public class PortalService {

	private final CosmosAgent cosmosAgent;
	private final ConcurrentMap<UUID, ConstellationEntity> constellationDB;

	private final Parser markdownParser;
	private final HtmlRenderer htmlRenderer;

	public PortalService(ConcurrentMap<UUID, ConstellationEntity> constellationDB, CosmosAgent cosmosAgent) {
		this.constellationDB = constellationDB;
		this.cosmosAgent = cosmosAgent;
		this.markdownParser = Parser.builder().build();
		this.htmlRenderer = HtmlRenderer.builder().build();
	}

	public String converse(String sessionId, String userMessage, String attachedData, UUID constellationId) {
		ConstellationEntity constellationEntity = constellationDB.get(constellationId);

		String aiResponse = cosmosAgent.chat(sessionId, constellationEntity.portalPrompt(), attachedData, userMessage);
		Node markdownDocument = markdownParser.parse(aiResponse);
		return htmlRenderer.render(markdownDocument);
	}

}