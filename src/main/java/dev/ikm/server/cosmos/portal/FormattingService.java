package dev.ikm.server.cosmos.portal;

import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

@Service
public class FormattingService {

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public FormattingService() {
        List<Extension> extensions = List.of(TablesExtension.create());
        this.markdownParser = Parser.builder().extensions(extensions).build();
        this.htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
    }

    public String formatAIResponse(String aiResponse) {
        Node markdownDocument = markdownParser.parse(aiResponse);
        return htmlRenderer.render(markdownDocument);
    }
}
