package com.github.khakers.modmailviewer.markdown.customemoji;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class CustomEmojiVisitorExt {
    public static <V extends CustomEmojiVisitor> VisitHandler<?>[] VISIT_HANDLERS(V visitor) {
        return new VisitHandler<?>[] {
                new VisitHandler<>(CustomEmoji.class, visitor::visit),
        };
    }
}
