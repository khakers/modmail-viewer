package com.github.khakers.modmailviewer.markdown.underline;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class UnderlineVisitorExt {
    public static <V extends UnderlineVisitor> VisitHandler<?>[] VISIT_HANDLERS(V visitor) {
        return new VisitHandler<?>[] {
                new VisitHandler<>(Underline.class, visitor::visit),
        };
    }
}
