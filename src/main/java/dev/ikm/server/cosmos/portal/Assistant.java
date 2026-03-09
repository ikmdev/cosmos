package dev.ikm.server.cosmos.portal;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.markdown.MarkdownDocumentParser;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class Assistant {

	private final Neo4jClient neo4jClient;
	private final ChatModel chatModel;
	private final List<ChatMessage> chatHistory;


	@Autowired
	public Assistant(Neo4jClient neo4jClient,
					 @Value("${ollama.url}") String ollamaUrl) {
		this.neo4jClient = neo4jClient;
		this.chatModel = OllamaChatModel.builder()
				.baseUrl(ollamaUrl)
				.modelName("command-r")
				.temperature(0.0) // Critical for clinical accuracy
				.timeout(Duration.ofMinutes(5))
				.build();
		this.chatHistory = new ArrayList<>();
	}

	private final Parser markdownParser = Parser.builder().build();
	private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

	public String informedChat(String userPrompt, MultipartFile file) throws IOException {
		List<ChatMessage> chatMessages = new ArrayList<>();

		//Check for a real user prompt
		if (userPrompt != null && !userPrompt.isBlank()) {
			chatMessages.addAll(chatHistory);
			chatMessages.add(UserMessage.from(TextContent.from(userPrompt)));
		} else {
			return "Please provide a prompt.";
		}

		MarkdownDocumentParser parser = new MarkdownDocumentParser();
		//Handle uploaded files
		if (file != null && !file.isEmpty()) {
			if (file.getOriginalFilename().endsWith(".md")) {
				return "Please upload a markdown file.";
			}
			Document document = parser.parse(file.getInputStream());
			UserMessage userProvidedFileMessage =
					UserMessage.from(TextContent.from("The user has also uploaded a file named '"
							+ file.getOriginalFilename()
							+ "' with the following content:\n\n---\n" + document.text() + "\n---"));
			chatHistory.add(userProvidedFileMessage);
		}





		ObjectMapper mapper = new ObjectMapper();
		String schemaJson = mapper.writeValueAsString(getFullSchema());

		String prompt = "Given this JSON schema of a Clinical Lab database: " + schemaJson +
				"\nWrite a Cypher query to find all analyzers with 'Critical' status.";

		chatMessages.add(UserMessage.from(prompt));




//		UserMessage userMessage = new UserMessage(contents);
		ChatResponse chatResponse = chatModel.chat(chatMessages);
		String markdownResponse = chatResponse.aiMessage().text();
		Node document = markdownParser.parse(markdownResponse);
		return htmlRenderer.render(document);
	}

	public String uninformedChat(String userPrompt, MultipartFile file) throws IOException {
		return "uninformed response";
	}

	private Map<String, Object> getFullSchema() {
		return neo4jClient.query("CALL apoc.meta.schema()")
				.fetch()
				.one() // Returns a single Map representing the graph structure
				.orElseThrow(() -> new RuntimeException("Could not fetch schema"));
	}
}
