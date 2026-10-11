package com.tramtruyen.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sanitizes chapter HTML before it is persisted or rendered.
 */
@Component
public class RichTextSanitizer {

    private static final Safelist ALLOWED_HTML = Safelist.none()
            .addTags("p", "br", "strong", "b", "em", "i", "u", "s",
                    "h1", "h2", "h3", "ul", "ol", "li", "blockquote",
                    "div", "span", "font")
            .addAttributes("div", "align")
            .addAttributes("span", "class", "style")
            .addAttributes("font", "face", "size", "color");
    private static final Set<String> ALLOWED_CLASSES = Set.of(
            "font-serif", "font-sans", "font-mono",
            "text-small", "text-large", "text-xlarge"
    );
    private static final Set<String> ALLOWED_FONT_FACES = Set.of(
            "Arial", "Georgia", "Tahoma", "Times New Roman", "Verdana"
    );
    private static final Set<String> ALLOWED_FONT_SIZES = Set.of("1", "2", "3", "4", "5", "6", "7");
    private static final Pattern HEX_COLOR = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final Pattern FONT_SIZE = Pattern.compile("^(8|9|[1-8][0-9]|96)px$", Pattern.CASE_INSENSITIVE);

    /**
     * Removes tags and attributes that are not supported by the chapter editor.
     *
     * @param html untrusted editor content
     * @return safe HTML fragment
     */
    public String sanitize(String html) {
        Document dirty = Jsoup.parseBodyFragment(html == null ? "" : html);
        Document clean = new Cleaner(ALLOWED_HTML).clean(dirty);

        for (Element element : clean.body().getAllElements()) {
            sanitizeClasses(element);
            sanitizeFontAttributes(element);
            sanitizeFontSize(element);
            sanitizeAlignment(element);
        }
        return clean.body().html().trim();
    }

    private void sanitizeClasses(Element element) {
        if (!element.hasAttr("class")) {
            return;
        }
        String classes = Stream.of(element.attr("class").split("\\s+"))
                .filter(ALLOWED_CLASSES::contains)
                .collect(Collectors.joining(" "));
        if (classes.isBlank()) {
            element.removeAttr("class");
        } else {
            element.attr("class", classes);
        }
    }

    private void sanitizeFontAttributes(Element element) {
        if (!"font".equals(element.tagName())) {
            return;
        }
        if (!ALLOWED_FONT_FACES.contains(element.attr("face"))) {
            element.removeAttr("face");
        }
        if (!ALLOWED_FONT_SIZES.contains(element.attr("size"))) {
            element.removeAttr("size");
        }
        if (!HEX_COLOR.matcher(element.attr("color")).matches()) {
            element.removeAttr("color");
        }
    }

    private void sanitizeAlignment(Element element) {
        if ("div".equals(element.tagName())
                && !Set.of("left", "center", "right", "justify").contains(element.attr("align"))) {
            element.removeAttr("align");
        }
    }

    private void sanitizeFontSize(Element element) {
        if (!element.hasAttr("style")) {
            return;
        }

        String sanitizedStyle = Stream.of(element.attr("style").split(";"))
                .map(String::trim)
                .map(this::sanitizeStyleDeclaration)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.joining("; "));
        if (sanitizedStyle.isBlank()) {
            element.removeAttr("style");
            return;
        }
        element.attr("style", sanitizedStyle);
    }

    private String sanitizeStyleDeclaration(String declaration) {
        String[] parts = declaration.split(":", 2);
        if (parts.length != 2) {
            return null;
        }

        String property = parts[0].trim().toLowerCase();
        String value = parts[1].trim().toLowerCase();
        if ("font-size".equals(property) && FONT_SIZE.matcher(value).matches()) {
            return property + ": " + value;
        }
        if ("line-height".equals(property) && "inherit".equals(value)) {
            return property + ": " + value;
        }
        return null;
    }
}
