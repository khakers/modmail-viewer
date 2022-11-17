package com.github.khakers.modmailviewer.markdown.usermention;

import com.vladsch.flexmark.util.ast.DoNotDecorate;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class UserMention extends Node implements DoNotDecorate {

    protected BasedSequence openingMarker = BasedSequence.NULL;
    protected BasedSequence id = BasedSequence.NULL;
    protected BasedSequence closingMarker = BasedSequence.NULL;

    public UserMention() {

    }

    public UserMention(BasedSequence chars) {
        super(chars);
    }

    public UserMention(BasedSequence openingMarker, BasedSequence id, BasedSequence closingMarker) {
        super(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()));
        this.openingMarker = openingMarker;
        this.id = id;
        this.closingMarker = closingMarker;

    }

    public BasedSequence getOpeningMarker() {
        return openingMarker;
    }

    public void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }



    public BasedSequence getId() {
        return id;
    }

    public void setId(BasedSequence id) {
        this.id = id;
    }

    public BasedSequence getClosingMarker() {
        return this.closingMarker;
    }

    public void setClosingMarker(BasedSequence closingMarker) {
        this.closingMarker = closingMarker;
    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return new BasedSequence[]{openingMarker, id, closingMarker};
    }

    @Override
    public void getAstExtra(@NotNull StringBuilder out) {
        delimitedSegmentSpanChars(out, openingMarker, id, closingMarker, "text");
    }

    @Override
    public String toString() {
        return "UserMention{" +
                "openingMarker=" + openingMarker +
                ", id=" + id +
                ", closingMarker=" + closingMarker +
                "} " + super.toString();
    }

    public String toOriginalMarkdown() {
        return this.openingMarker + "@" + this.id + this.closingMarker;
    }

    public String toMentionText() {
        return "@"+this.id;
    }
}
