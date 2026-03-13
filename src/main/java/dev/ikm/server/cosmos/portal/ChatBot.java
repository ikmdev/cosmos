package dev.ikm.server.cosmos.portal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.markdown.MarkdownDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;

@Component
public class ChatBot {

	private final ChatModel chatModel;
	private final EmbeddingModel embeddingModel;
	private final EmbeddingStore embeddingStore;

	private final MarkdownDocumentParser markdownDocumentParser;
	private final Parser markdownParser;
	private final HtmlRenderer htmlRenderer;

	private static final CopyOnWriteArrayList<ChatMessage> savedChatMessages = new CopyOnWriteArrayList<>();
	private static final Deque<AiMessage> previousAIMessage = new ArrayDeque<>();
	private final List<ChatMessage> currentChatMessages;

	public ChatBot(ChatModel chatModel, EmbeddingModel embeddingModel, EmbeddingStore embeddingStore) {
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
		this.embeddingStore = embeddingStore;
		this.currentChatMessages = new ArrayList<>();
		this.markdownDocumentParser = new MarkdownDocumentParser();
		this.markdownParser = Parser.builder().build();
		this.htmlRenderer = HtmlRenderer.builder().build();
	}

	public void init(File systemPromptMarkdown) {
		if (systemPromptMarkdown != null && systemPromptMarkdown.getName().endsWith(".md")) {
			try {
				InputStream inputStream = new FileInputStream(systemPromptMarkdown);
				Document systemPromptDocument = markdownDocumentParser.parse(inputStream);
				SystemMessage systemPromptMessage = SystemMessage.from(systemPromptDocument.text());
				currentChatMessages.add(systemPromptMessage);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error initializing chat bot.");
			}
		}
		currentChatMessages.addAll(savedChatMessages);
		if (!previousAIMessage.isEmpty()) {
			currentChatMessages.add(previousAIMessage.pop());
		}
	}

	public void prepareConversation(MultipartFile attachment, boolean keepPreparation) {
		if (attachment != null && Objects.requireNonNull(attachment.getOriginalFilename()).endsWith(".md")) {
			try {
				Document attachmentDocument = markdownDocumentParser.parse(attachment.getInputStream());
				UserMessage attachmentMessage = UserMessage.from(attachmentDocument.text());
				currentChatMessages.add(attachmentMessage);

				if (keepPreparation) {
					savedChatMessages.add(attachmentMessage);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error preparing conversation.");
			}
		} else {
			throw new IllegalStateException("No attachment provided.");
		}
	}

	public String performConversation(String userPrompt, boolean isInformed) {
		if (userPrompt == null || userPrompt.isBlank()) {
			return "Enter a prompt for the LLM.";
		}
		ChatResponse chatResponse = isInformed ? conductInformedFlow(userPrompt) : conductUninformedFlow(userPrompt);
		if (chatResponse != null) {
			AiMessage aiMessage = chatResponse.aiMessage();
			previousAIMessage.push(aiMessage);
			String markdownResponse = aiMessage.text();
			Node document = markdownParser.parse(markdownResponse);
			return htmlRenderer.render(document);
		} else {
			return "No response from LLM.";
		}
	}

	private ChatResponse conductInformedFlow(String userPrompt) {

		return null;
	}

	private ChatResponse conductUninformedFlow(String userPrompt) {

		Embedding queryEmbedding = embeddingModel.embed(userPrompt).content();
		List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(
				EmbeddingSearchRequest.builder()
						.queryEmbedding(queryEmbedding)
						.maxResults(5) // Get the single best entry point
						.build()
		).matches();

//		if (matches.isEmpty()) return "No relevant knowledge found.";

		// Extract all IDs from the results
		List<String> targetIds = matches.stream()
				.map(m -> m.embedded().metadata().getString("id"))
				.toList();


		// Convert your Neo4j Map result into a Markdown string for the LLM
// 		String markdownContext = results.all().stream()
// 				.map(result -> (Map<String, Object>) result.get("graphContext"))
// 				.map(record -> {
// 					StringBuilder sb = new StringBuilder();
// 					sb.append("### Source Node\n");
// //					((Map<String, Object>) record.get("seed_node"))
// 					//TODO - figure out this parsing
// 					sb.append("- Labels: ").append(record.get("labels")).append("\n");
// 					sb.append("- Data: ").append(record.get("data")).append("\n\n");
// 					sb.append("### 2-Hop Connections\n");
// 					// Iterate through dynamic neighbors
// 					List<Map<String, Object>> neighbors = (List<Map<String, Object>>) record.get("connections");
// 					for (Map<String, Object> n : neighbors) {
// 						sb.append("- ").append(n.get("labels")).append(": ").append(n.get("data")).append("\n");
// 					}
// 					return sb.toString();
// 				})
// 				.collect(Collectors.joining("\n---\n"));

		UserMessage userMessage = UserMessage.from(userPrompt);
		currentChatMessages.add(userMessage);
		return chatModel.chat(currentChatMessages);
	}
}
