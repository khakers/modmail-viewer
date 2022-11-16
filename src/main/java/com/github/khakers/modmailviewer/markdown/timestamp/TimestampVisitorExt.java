package com.github.khakers.modmailviewer.markdown.timestamp;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class TimestampVisitorExt {
    public static <V extends TimestampVisitor> VisitHandler<?>[] VISIT_HANDLERS(V visitor) {
        return new VisitHandler<?>[] {
                new VisitHandler<>(Timestamp.class, visitor::visit),
        };
    }
}
