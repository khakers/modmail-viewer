package com.github.khakers.modmailviewer.markdown.usermention.internal;

import com.github.khakers.modmailviewer.markdown.usermention.UserMention;
import com.github.khakers.modmailviewer.markdown.usermention.UserMentionExtension;
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

public class UserMentionNodeRenderer implements NodeRenderer {

    private static final Logger logger = LogManager.getLogger();

    private final String customMentionStyleHtmlOpen;
    private final String customMentionStyleHtmlClose;

    private final Attributes extraAttributes;


    public UserMentionNodeRenderer(DataHolder options) {
        this.customMentionStyleHtmlOpen = UserMentionExtension.MENTION_STYLE_HTML_OPEN.get(options);
        this.customMentionStyleHtmlClose = UserMentionExtension.MENTION_STYLE_HTML_CLOSE.get(options);
        this.extraAttributes = UserMentionExtension.EXTRA_MENTION_ATTRIBUTES.get(options);
    }

    /**
     * @return the mapping of nodes this renderer handles to rendering function
     */
    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(UserMention.class, this::render));
        return set;
    }

    private void render(UserMention node, NodeRendererContext context, HtmlWriter html) {
        if (customMentionStyleHtmlOpen == null || customMentionStyleHtmlClose == null) {
            logger.trace(node.toString());
            html
                    .attr("class", "userMention")
//                    .attr("data-bs-toggle", "tooltip")
//                    .attr("data-bs-html", "true")
//                    .attr("data-bs-title", "<strong>:"+node.getName()+":</strong>"+"<br />Custom Server Emoji")
                    .attr(extraAttributes)
                    .withAttr()
                    .tag("span");
            context.render(new Text(node.toOriginalMarkdown()));
            context.renderChildren(node);
            html.tag("/span");
        } else {
            html.raw(customMentionStyleHtmlOpen);
            context.renderChildren(node);
            html.raw(customMentionStyleHtmlClose);
        }
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new UserMentionNodeRenderer(options);
        }
    }
}
