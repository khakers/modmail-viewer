package com.github.khakers.modmailviewer.markdown.spoiler.internal;

import com.github.khakers.modmailviewer.markdown.spoiler.Spoiler;
import com.github.khakers.modmailviewer.markdown.spoiler.SpoilerExtension;
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

public class SpoilerNodeRenderer implements NodeRenderer {
    final private String spoilerStyleHtmlOpen;
    final private String spoilerStyleHtmlClose;

    public SpoilerNodeRenderer(DataHolder options) {
        this.spoilerStyleHtmlOpen = SpoilerExtension.SPOILER_STYLE_HTML_OPEN.get(options);
        this.spoilerStyleHtmlClose = SpoilerExtension.SPOILER_STYLE_HTML_CLOSE.get(options);
    }

    /**
     * @return the mapping of nodes this renderer handles to rendering function
     */
    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(Spoiler.class, this::render));
        return set;
    }

    private void render(Spoiler node, NodeRendererContext context, HtmlWriter html) {
        if (spoilerStyleHtmlOpen == null || spoilerStyleHtmlClose == null) {
            html.tag("text-spoiler");
            context.renderChildren(node);
            html.tag("/text-spoiler");
        } else {
            html.raw(spoilerStyleHtmlOpen);
            context.renderChildren(node);
            html.raw(spoilerStyleHtmlClose);
        }
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new SpoilerNodeRenderer(options);
        }
    }
}
