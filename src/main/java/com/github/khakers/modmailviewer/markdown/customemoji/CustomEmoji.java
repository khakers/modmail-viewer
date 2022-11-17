package com.github.khakers.modmailviewer.markdown.customemoji;

import com.vladsch.flexmark.util.ast.DoNotDecorate;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class CustomEmoji extends Node implements DoNotDecorate {

    protected BasedSequence openingMarker = BasedSequence.NULL;
    protected BasedSequence name = BasedSequence.NULL;
    protected BasedSequence id = BasedSequence.NULL;
    protected BasedSequence closingMarker = BasedSequence.NULL;

    public CustomEmoji() {

    }

    public CustomEmoji(BasedSequence chars) {
        super(chars);
    }

    public CustomEmoji(BasedSequence openingMarker, BasedSequence name, BasedSequence id, BasedSequence closingMarker) {
        super(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()));
        this.openingMarker = openingMarker;
        this.name = name;
        this.id = id;
        this.closingMarker = closingMarker;

    }

    public BasedSequence getOpeningMarker() {
        return openingMarker;
    }

    public void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }

    public BasedSequence getName() {
        return name;
    }

    public void setName(BasedSequence name) {
        this.name = name;
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
        return new BasedSequence[]{openingMarker, name, id, closingMarker};
    }

    @Override
    public void getAstExtra(@NotNull StringBuilder out) {
        delimitedSegmentSpanChars(out, openingMarker, name, closingMarker, "text");
    }

    @Override
    public String toString() {
        return "CustomEmoji{" +
                "openingMarker=" + openingMarker +
                ", name=" + name +
                ", id=" + id +
                ", closingMarker='" + closingMarker + "'" +
                "} " + super.toString();
    }

    public String toOriginalMarkdown() {
        return this.openingMarker + ":" + this.name + ":" + this.id + this.closingMarker;
    }
}
