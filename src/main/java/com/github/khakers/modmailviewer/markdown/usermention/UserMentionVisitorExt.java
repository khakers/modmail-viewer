package com.github.khakers.modmailviewer.markdown.usermention;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class UserMentionVisitorExt {
    public static <V extends UserMentionVisitor> VisitHandler<?>[] VISIT_HANDLERS(V visitor) {
        return new VisitHandler<?>[] {
                new VisitHandler<>(UserMention.class, visitor::visit),
        };
    }
}
