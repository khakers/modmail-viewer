package com.github.khakers.modmailviewer.markdown.channelmention;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class ChannelMentionVisitorExt {
    public static <V extends ChannelMentionVisitor> VisitHandler<?>[] VISIT_HANDLERS(V visitor) {
        return new VisitHandler<?>[] {
                new VisitHandler<>(ChannelMention.class, visitor::visit),
        };
    }
}
