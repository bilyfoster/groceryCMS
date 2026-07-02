package com.brochure.cms.shared.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Iterator;
import java.util.Map;

/**
 * Render a BlockNote JSON document to sanitized HTML.
 *
 * <p>BlockNote stores documents as a JSON array of blocks. Each block has a type,
 * props, an optional content array of inline nodes, and optional children blocks.
 * This renderer converts the structured JSON into plain HTML so it can be served
 * on public pages and sanitized by the caller.
 */
public final class BlockNoteRenderer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BlockNoteRenderer() {}

    public static String render(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        try {
            JsonNode root = MAPPER.readTree(json);
            if (!root.isArray()) {
                return "";
            }
            StringBuilder html = new StringBuilder();
            renderBlocks((ArrayNode) root, html, false);
            return html.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static void renderBlocks(ArrayNode blocks, StringBuilder out, boolean nested) {
        if (blocks == null) {
            return;
        }

        String listTag = null;
        for (JsonNode block : blocks) {
            String type = textValue(block.path("type"));
            if (listTag != null && !isListItem(type)) {
                out.append("</").append(listTag).append(">");
                listTag = null;
            }

            switch (type) {
                case "paragraph" -> renderParagraph(block, out);
                case "heading" -> renderHeading(block, out);
                case "bulletListItem" -> {
                    if (listTag == null) {
                        listTag = "ul";
                        out.append("<ul>");
                    }
                    renderListItem(block, out);
                }
                case "numberedListItem" -> {
                    if (listTag == null) {
                        listTag = "ol";
                        out.append("<ol>");
                    }
                    renderListItem(block, out);
                }
                case "checkListItem" -> {
                    if (listTag == null) {
                        listTag = "ul";
                        out.append("<ul>");
                    }
                    renderCheckListItem(block, out);
                }
                case "image" -> renderImage(block, out);
                case "table" -> renderTable(block, out);
                case "code" -> renderCode(block, out);
                case "quote" -> renderQuote(block, out);
                case "horizontalRule" -> out.append("<hr />");
                default -> {
                    // Unknown block: try rendering children and content.
                    renderBlockContent(block, out);
                    renderChildren(block, out);
                }
            }
        }
        if (listTag != null) {
            out.append("</").append(listTag).append(">");
        }
    }

    private static boolean isListItem(String type) {
        return "bulletListItem".equals(type)
                || "numberedListItem".equals(type)
                || "checkListItem".equals(type);
    }

    private static void renderParagraph(JsonNode block, StringBuilder out) {
        String align = alignment(block);
        out.append("<p").append(align).append(">");
        renderBlockContent(block, out);
        out.append("</p>");
        renderChildren(block, out);
    }

    private static void renderHeading(JsonNode block, StringBuilder out) {
        int level = block.path("props").path("level").asInt(2);
        level = Math.max(1, Math.min(6, level));
        String tag = "h" + level;
        String align = alignment(block);
        out.append("<").append(tag).append(align).append(">");
        renderBlockContent(block, out);
        out.append("</").append(tag).append(">");
        renderChildren(block, out);
    }

    private static void renderListItem(JsonNode block, StringBuilder out) {
        out.append("<li>");
        renderBlockContent(block, out);
        out.append("</li>");
        renderChildren(block, out);
    }

    private static void renderCheckListItem(JsonNode block, StringBuilder out) {
        boolean checked = block.path("props").path("checked").asBoolean(false);
        out.append("<li><input type=\"checkbox\" disabled").append(checked ? " checked" : "").append(" /> ");
        renderBlockContent(block, out);
        out.append("</li>");
        renderChildren(block, out);
    }

    private static void renderImage(JsonNode block, StringBuilder out) {
        JsonNode props = block.path("props");
        String url = textValue(props.path("url"));
        if (url == null || url.isBlank()) {
            return;
        }
        String caption = textValue(props.path("caption"));
        String width = textValue(props.path("width"));
        String style = width != null && !width.isBlank() ? " style=\"width:" + escapeAttribute(width) + "\"" : "";
        out.append("<figure").append(style).append("><img src=\"").append(escapeAttribute(url)).append("\" alt=\"");
        out.append(caption != null ? escapeAttribute(caption) : "").append("\" />");
        if (caption != null && !caption.isBlank()) {
            out.append("<figcaption>").append(escapeHtml(caption)).append("</figcaption>");
        }
        out.append("</figure>");
        renderChildren(block, out);
    }

    private static void renderTable(JsonNode block, StringBuilder out) {
        out.append("<table><tbody>");
        JsonNode rows = block.path("content");
        if (rows.isArray()) {
            for (JsonNode row : rows) {
                out.append("<tr>");
                JsonNode cells = row.path("content");
                if (cells.isArray()) {
                    for (JsonNode cell : cells) {
                        out.append("<td>");
                        renderBlockContent(cell, out);
                        out.append("</td>");
                    }
                }
                out.append("</tr>");
            }
        }
        out.append("</tbody></table>");
        renderChildren(block, out);
    }

    private static void renderCode(JsonNode block, StringBuilder out) {
        String language = textValue(block.path("props").path("language"));
        out.append("<pre").append(language != null ? " class=\"language-" + escapeAttribute(language) + "\"" : "").append("><code>");
        renderBlockContent(block, out);
        out.append("</code></pre>");
        renderChildren(block, out);
    }

    private static void renderQuote(JsonNode block, StringBuilder out) {
        out.append("<blockquote>");
        renderBlockContent(block, out);
        out.append("</blockquote>");
        renderChildren(block, out);
    }

    private static void renderBlockContent(JsonNode block, StringBuilder out) {
        JsonNode content = block.path("content");
        if (content.isArray()) {
            renderInline(content, out);
        }
    }

    private static void renderChildren(JsonNode block, StringBuilder out) {
        JsonNode children = block.path("children");
        if (children.isArray() && children.size() > 0) {
            renderBlocks((ArrayNode) children, out, true);
        }
    }

    private static void renderInline(JsonNode nodes, StringBuilder out) {
        for (JsonNode node : nodes) {
            String type = textValue(node.path("type"));
            if ("link".equals(type)) {
                String href = textValue(node.path("href"));
                out.append("<a href=\"").append(href != null ? escapeAttribute(href) : "#").append("\">");
                renderInline(node.path("content"), out);
                out.append("</a>");
            } else {
                String text = textValue(node.path("text"));
                if (text == null) {
                    text = "";
                }
                JsonNode styles = node.path("styles");
                text = applyStyle(text, styles, "bold", "strong");
                text = applyStyle(text, styles, "italic", "em");
                text = applyStyle(text, styles, "underline", "u");
                text = applyStyle(text, styles, "strike", "s");
                text = applyStyle(text, styles, "code", "code");
                out.append(text);
            }
        }
    }

    private static String applyStyle(String text, JsonNode styles, String style, String tag) {
        if (styles != null && styles.path(style).asBoolean(false)) {
            return "<" + tag + ">" + text + "</" + tag + ">";
        }
        return text;
    }

    private static String alignment(JsonNode block) {
        String align = textValue(block.path("props").path("textAlignment"));
        if (align == null || align.isBlank() || "left".equals(align)) {
            return "";
        }
        return " style=\"text-align:" + escapeAttribute(align) + "\"";
    }

    private static String textValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node.toString();
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String escapeAttribute(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
