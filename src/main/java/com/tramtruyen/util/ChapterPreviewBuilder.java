package com.tramtruyen.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Turns stored (already sanitized) chapter content into display blocks.
 * <p>
 * The preview is limited by both a block count and a character budget so a chapter whose HTML has no
 * line breaks (one giant block) can never leak its full text to a reader who has not paid.
 */
@Component
public class ChapterPreviewBuilder {

    static final int PREVIEW_BLOCK_LIMIT = 2;
    static final int PREVIEW_CHAR_LIMIT = 500;
    private static final String ELLIPSIS = "…";
    private static final Set<String> BLOCK_TAGS = Set.of(
            "p", "div", "h1", "h2", "h3", "blockquote", "ul", "ol", "li");

    /**
     * Builds the teaser shown for a locked chapter.
     *
     * @param content sanitized chapter content (HTML fragment or plain text)
     * @return at most {@value #PREVIEW_BLOCK_LIMIT} safe HTML blocks within the character budget
     */
    public List<String> buildPreview(String content) {
        List<String> preview = new ArrayList<>();
        int remaining = PREVIEW_CHAR_LIMIT;
        for (Block block : extractBlocks(content)) {
            if (preview.size() >= PREVIEW_BLOCK_LIMIT || remaining <= 0) {
                break;
            }
            if (block.text().length() <= remaining) {
                preview.add(block.html());
                remaining -= block.text().length();
            } else {
                preview.add(new TextNode(truncate(block.text(), remaining)).outerHtml());
                remaining = 0;
            }
        }
        return preview;
    }

    /**
     * Splits plain-text content into non-blank paragraphs (used when the content has no HTML tags).
     *
     * @param content chapter content
     * @return trimmed non-blank lines
     */
    public List<String> splitParagraphs(String content) {
        List<String> paragraphs = new ArrayList<>();
        if (content == null) {
            return paragraphs;
        }
        for (String line : content.replace("\r", "").split("\n")) {
            if (!line.isBlank()) {
                paragraphs.add(line);
            }
        }
        return paragraphs;
    }

    private List<Block> extractBlocks(String content) {
        List<Block> blocks = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return blocks;
        }
        collect(Jsoup.parseBodyFragment(content).body().childNodes(), blocks);
        return blocks;
    }

    private void collect(List<Node> nodes, List<Block> blocks) {
        for (Node node : nodes) {
            if (node instanceof TextNode textNode) {
                collectPlainText(textNode, blocks);
            } else if (node instanceof Element element) {
                collectElement(element, blocks);
            }
        }
    }

    private void collectPlainText(TextNode node, List<Block> blocks) {
        for (String line : node.getWholeText().replace("\r", "").split("\n")) {
            String text = line.trim();
            if (!text.isEmpty()) {
                blocks.add(new Block(text, new TextNode(text).outerHtml()));
            }
        }
    }

    private void collectElement(Element element, List<Block> blocks) {
        if (hasBlockChild(element)) {
            collect(element.childNodes(), blocks);
            return;
        }
        String text = element.text().trim();
        if (!text.isEmpty()) {
            blocks.add(new Block(text, element.outerHtml()));
        }
    }

    private boolean hasBlockChild(Element element) {
        return element.children().stream().anyMatch(child -> BLOCK_TAGS.contains(child.tagName()));
    }

    private String truncate(String text, int limit) {
        if (text.length() <= limit) {
            return text;
        }
        int cut = text.lastIndexOf(' ', limit);
        int end = cut > limit / 2 ? cut : limit;
        return text.substring(0, end).stripTrailing() + ELLIPSIS;
    }

    private record Block(String text, String html) {
    }
}
