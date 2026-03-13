package dev.ikm.server.cosmos.portal;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssistantService {

	private final ChatBot chatBot;

	public AssistantService(ChatBot chatBot) {
		this.chatBot = chatBot;
	}


	public String chat(String userPrompt, MultipartFile attachedFile, boolean constellationIntegrated) {
		// chatBot.init(systemPromptMarkdown);
		if (attachedFile != null) {
			chatBot.prepareConversation(attachedFile, constellationIntegrated);
		}
		return chatBot.performConversation(userPrompt, constellationIntegrated);
	}

	public String chat(String userPrompt, boolean constellationIntegrated) {
		return chat(userPrompt, null, constellationIntegrated);
	}
}
