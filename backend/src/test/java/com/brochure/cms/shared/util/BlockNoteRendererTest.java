package com.brochure.cms.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BlockNoteRendererTest {

    @Test
    void emptyInput_returnsEmptyString() {
        assertThat(BlockNoteRenderer.render(null)).isEmpty();
        assertThat(BlockNoteRenderer.render("")).isEmpty();
        assertThat(BlockNoteRenderer.render("   ")).isEmpty();
    }

    @Test
    void paragraphWithInlineStyles_rendersHtml() {
        String json = """
            [
              {
                "type": "paragraph",
                "props": {"textAlignment": "left"},
                "content": [
                  {"type": "text", "text": "Hello ", "styles": {}},
                  {"type": "text", "text": "world", "styles": {"bold": true}}
                ],
                "children": []
              }
            ]
            """;
        assertThat(BlockNoteRenderer.render(json)).contains("<p>Hello <strong>world</strong></p>");
    }

    @Test
    void headingWithAlignment_rendersAlignedHeading() {
        String json = """
            [
              {
                "type": "heading",
                "props": {"level": 2, "textAlignment": "center"},
                "content": [{"type": "text", "text": "Title"}],
                "children": []
              }
            ]
            """;
        assertThat(BlockNoteRenderer.render(json)).contains("<h2 style=\"text-align:center\">Title</h2>");
    }

    @Test
    void bulletList_rendersUnorderedList() {
        String json = """
            [
              {"type": "bulletListItem", "props": {}, "content": [{"type": "text", "text": "One"}]},
              {"type": "bulletListItem", "props": {}, "content": [{"type": "text", "text": "Two"}]}
            ]
            """;
        String html = BlockNoteRenderer.render(json);
        assertThat(html).contains("<ul>");
        assertThat(html).contains("<li>One</li>");
        assertThat(html).contains("<li>Two</li>");
        assertThat(html).contains("</ul>");
    }

    @Test
    void imageBlock_rendersFigureWithEscapedAttributes() {
        String json = """
            [
              {
                "type": "image",
                "props": {"url": "/images/test.jpg", "caption": "A <nice> photo", "width": "512"},
                "content": [],
                "children": []
              }
            ]
            """;
        String html = BlockNoteRenderer.render(json);
        assertThat(html).contains("<figure style=\"width:512\">");
        assertThat(html).contains("src=\"/images/test.jpg\"");
        assertThat(html).contains("alt=\"A &lt;nice&gt; photo\"");
        assertThat(html).contains("<figcaption>A &lt;nice&gt; photo</figcaption>");
    }

    @Test
    void link_rendersAnchor() {
        String json = """
            [
              {
                "type": "paragraph",
                "props": {},
                "content": [
                  {
                    "type": "link",
                    "href": "https://example.com",
                    "content": [{"type": "text", "text": "click"}]
                  }
                ],
                "children": []
              }
            ]
            """;
        assertThat(BlockNoteRenderer.render(json))
                .contains("<a href=\"https://example.com\">click</a>");
    }
}
