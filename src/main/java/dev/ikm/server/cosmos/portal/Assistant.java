package dev.ikm.server.cosmos.portal;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Assistant {

	@Value("${google.gemini.key}")
	String apiKey;

	public String chat(String message) {
		ChatModel model = GoogleAiGeminiChatModel.builder()
				.apiKey(apiKey)
				.modelName("gemini-1.5-flash") // Try Flash first for the demo!
				.logRequestsAndResponses(true) // Helpful to see the exact URL being called
				.build();

		return model.chat(message);
	}

}
