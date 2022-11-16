package com.github.khakers.modmailviewer.markdown.customemoji.internal;

import com.github.khakers.modmailviewer.markdown.customemoji.CustomEmoji;
import com.github.khakers.modmailviewer.markdown.customemoji.CustomEmojiExtension;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.html.Attributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class CustomEmojiNodeRenderer implements NodeRenderer {

    private final String customEmojiStyleHtmlOpen;
    private final String customEmojiStyleHtmlClose;

    private final Attributes extraAttributes;

    private final String customEmojiURI;

    public CustomEmojiNodeRenderer(DataHolder options) {
        this.customEmojiStyleHtmlOpen = CustomEmojiExtension.EMOJI_STYLE_HTML_OPEN.get(options);
        this.customEmojiStyleHtmlClose = CustomEmojiExtension.EMOJI_STYLE_HTML_CLOSE.get(options);
        this.customEmojiURI = CustomEmojiExtension.EMOJI_URL_TEMPLATE.get(options);
        this.extraAttributes = CustomEmojiExtension.EXTRA_IMG_ATTRIBUTES.get(options);
    }

    /**
     * @return the mapping of nodes this renderer handles to rendering function
     */
    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(CustomEmoji.class, this::render));
        return set;
    }

    private void render(CustomEmoji node, NodeRendererContext context, HtmlWriter html) {
        if (customEmojiStyleHtmlOpen == null || customEmojiStyleHtmlClose == null) {
            System.out.println(node.toString());
            html
                    .attr("draggable", "false")
                    .attr("class", "emoji")
                    .attr("src", String.format(customEmojiURI, node.getId()))
                    .attr("alt", node.toOriginalMarkdown())
                    .attr("data-bs-toggle", "tooltip")
                    .attr("data-bs-html", "true")
                    .attr("data-bs-title", "<strong>:"+node.getName()+":</strong>"+"<br />Custom Server Emoji")
                    .attr(extraAttributes)
                    .withAttr()
                    .tag("img");
//            context.renderChildren(node);
            html.tag("/img");
        } else {
            html.raw(customEmojiStyleHtmlOpen);
            context.renderChildren(node);
            html.raw(customEmojiStyleHtmlClose);
        }
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new CustomEmojiNodeRenderer(options);
        }
    }
}
