package com.github.khakers.modmailviewer.markdown.timestamp.internal;

import com.github.khakers.modmailviewer.markdown.timestamp.Timestamp;
import com.github.khakers.modmailviewer.markdown.timestamp.TimestampExtension;
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

public class TimestampNodeRenderer implements NodeRenderer {

    private static final Logger logger = LogManager.getLogger();

    private final Attributes extraAttributes;

    public TimestampNodeRenderer(DataHolder options) {
        this.extraAttributes = TimestampExtension.EXTRA_TIMESTAMP_ATTRIBUTES.get(options);
    }

    /**
     * @return the mapping of nodes this renderer handles to rendering function
     */
    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(Timestamp.class, this::render));
        return set;
    }

    private void render(Timestamp node, NodeRendererContext context, HtmlWriter html) {
        logger.trace(node.toString());
        html
                .attr("class", "timestamp")
                .attr("timestamp-type", node.getType().toString())
                .attr("timestamp", node.getInstant().toString())
                .attr("timestamp-title-type", "detailed")
                .attr("data-bs-toggle", "tooltip")
                .attr(extraAttributes)
                .withAttr()
                .tag("span");
//            context.renderChildren(node);
        html.tag("/span");

    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new TimestampNodeRenderer(options);
        }
    }
}
