package dev.ikm.server.cosmos.portal;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

@Service
public class FormattingService {

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public FormattingService() {
        this.markdownParser = Parser.builder().build();
		this.htmlRenderer = HtmlRenderer.builder().build();
    }

    public String formatAIResponse(String aiResponse) {
        Node markdownDocument = markdownParser.parse(aiResponse);
		return htmlRenderer.render(markdownDocument);
    }
}
