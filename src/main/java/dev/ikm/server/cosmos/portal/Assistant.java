package dev.ikm.server.cosmos.portal;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class Assistant {

	@Value("${google.gemini.key}")
	String apiKey;

	private final Parser markdownParser = Parser.builder().build();
	private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

	public String chat(String message, MultipartFile file) throws IOException {
		ChatModel model = GoogleAiGeminiChatModel.builder()
				.apiKey(apiKey)
				.modelName("gemini-2.5-flash") // Using Flash for speed
				.logRequestsAndResponses(true) // Helpful to see the exact URL being called
				.build();

		List<Content> contents = new ArrayList<>();
		if (message != null && !message.isBlank()) {
			contents.add(TextContent.from(message));
		}

		if (file != null && !file.isEmpty()) {
			// For this example, we'll treat the file as plain text.
			// For more advanced use cases, you could use libraries like Apache Tika to parse different file types.
			String fileContent = new String(file.getBytes());
			contents.add(TextContent.from("The user has also uploaded a file named '" + file.getOriginalFilename() + "' with the following content:\n\n---\n" + fileContent + "\n---"));
		}

		UserMessage userMessage = new UserMessage(contents);
		ChatResponse chatResponse = model.chat(userMessage);
		String markdownResponse = chatResponse.aiMessage().text();
		Node document = markdownParser.parse(markdownResponse);
		return htmlRenderer.render(document);
	}
}
