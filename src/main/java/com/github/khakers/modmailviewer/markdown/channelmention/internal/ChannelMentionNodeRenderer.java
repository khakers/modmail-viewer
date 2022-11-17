package com.github.khakers.modmailviewer.markdown.channelmention.internal;

import com.github.khakers.modmailviewer.markdown.channelmention.ChannelMention;
import com.github.khakers.modmailviewer.markdown.channelmention.ChannelMentionExtension;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.html.Attributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ChannelMentionNodeRenderer implements NodeRenderer {

    private static final Logger logger = LogManager.getLogger();

    private final String customStyleHtmlOpen;
    private final String customStyleHtmlClose;

    private final Attributes extraAttributes;


    public ChannelMentionNodeRenderer(DataHolder options) {
        this.customStyleHtmlOpen = ChannelMentionExtension.CHANNEL_STYLE_HTML_OPEN.get(options);
        this.customStyleHtmlClose = ChannelMentionExtension.CHANNEL_STYLE_HTML_CLOSE.get(options);
        this.extraAttributes = ChannelMentionExtension.EXTRA_CHANNEL_ATTRIBUTES.get(options);
    }

    /**
     * @return the mapping of nodes this renderer handles to rendering function
     */
    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(ChannelMention.class, this::render));
        return set;
    }

    private void render(ChannelMention node, NodeRendererContext context, HtmlWriter html) {
        if (customStyleHtmlOpen == null || customStyleHtmlClose == null) {
            logger.trace(node.toString());
            html
                    .attr("class", "channelMention")
//                    .attr("data-bs-toggle", "tooltip")
//                    .attr("data-bs-html", "true")
//                    .attr("data-bs-title", "<strong>:"+node.getName()+":</strong>"+"<br />Custom Server Emoji")
                    .attr(extraAttributes)
                    .withAttr()
                    .tag("span");
            context.render(new Text(node.toMentionText()));
            context.renderChildren(node);
            html.tag("/span");
        } else {
            html.raw(customStyleHtmlOpen);
            context.renderChildren(node);
            html.raw(customStyleHtmlClose);
        }
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new ChannelMentionNodeRenderer(options);
        }
    }
}
