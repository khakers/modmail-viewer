package com.github.khakers.modmailviewer.markdown.spoiler;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class SpoilerVisitorExt {
    public static <V extends SpoilerVisitor> VisitHandler<?>[] VISIT_HANDLERS(V visitor) {
        return new VisitHandler<?>[] {
                new VisitHandler<>(Spoiler.class, visitor::visit),
        };
    }
}
