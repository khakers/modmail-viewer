package com.github.khakers.modmailviewer.markdown.spoiler;

import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class Spoiler extends Node implements DelimitedNode {
    protected BasedSequence openingMarker = BasedSequence.NULL;
    protected BasedSequence text = BasedSequence.NULL;
    protected BasedSequence closingMarker = BasedSequence.NULL;

    public Spoiler() {

    }

    public Spoiler(BasedSequence chars) {
        super(chars);
    }

    public Spoiler(BasedSequence openingMarker, BasedSequence text, BasedSequence closingMarker) {
        super(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()));
        this.openingMarker = openingMarker;
        this.text = text;
        this.closingMarker = closingMarker;

    }

    @Override
    public BasedSequence getOpeningMarker() {
        return openingMarker;
    }

    @Override
    public void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }

    @Override
    public BasedSequence getText() {
        return text;
    }

    @Override
    public void setText(BasedSequence text) {
        this.text = text;
    }

    @Override
    public BasedSequence getClosingMarker() {
        return this.closingMarker;
    }

    @Override
    public void setClosingMarker(BasedSequence closingMarker) {
        this.closingMarker = closingMarker;
    }

//    @Override
//    public boolean collectText(ISequenceBuilder<? extends ISequenceBuilder<?, BasedSequence>, BasedSequence> out, int flags, NodeVisitor nodeVisitor) {
//        return DelimitedNode.super.collectText(out, flags, nodeVisitor);
//    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return new BasedSequence[]{openingMarker, text, closingMarker};
    }

    @Override
    public void getAstExtra(@NotNull StringBuilder out) {
        delimitedSegmentSpanChars(out, openingMarker, text, closingMarker, "text");
    }
}