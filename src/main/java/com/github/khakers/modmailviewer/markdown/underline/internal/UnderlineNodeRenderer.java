package com.github.khakers.modmailviewer.markdown.underline.internal;

import com.github.khakers.modmailviewer.markdown.underline.Underline;
import com.github.khakers.modmailviewer.markdown.underline.UnderlineExtension;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class UnderlineNodeRenderer implements NodeRenderer {
    final private String underlineStyleHtmlOpen;
    final private String underlineStyleHtmlClose;

    public UnderlineNodeRenderer(DataHolder options) {
        this.underlineStyleHtmlOpen = UnderlineExtension.UNDERLINE_STYLE_HTML_OPEN.get(options);
        this.underlineStyleHtmlClose = UnderlineExtension.UNDERLINE_STYLE_HTML_CLOSE.get(options);
    }


    /**
     * @return the mapping of nodes this renderer handles to rendering function
     */
    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(Underline.class, this::render));
        return set;
    }

    private void render(Underline node, NodeRendererContext context, HtmlWriter html) {
        if (underlineStyleHtmlOpen == null || underlineStyleHtmlClose == null) {
            if (context.getHtmlOptions().sourcePositionParagraphLines) {
                html.attr("class","text-decoration-underline").withAttr().tag("span");
            } else {
                html.srcPos(node.getText())
                        .attr("class","text-decoration-underline").withAttr().tag("span");
            }
            context.renderChildren(node);
            html.tag("/span");
        } else {
            html.raw(underlineStyleHtmlOpen);
            context.renderChildren(node);
            html.raw(underlineStyleHtmlClose);
        }
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new UnderlineNodeRenderer(options);
        }
    }

}
