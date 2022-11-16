package com.github.khakers.modmailviewer.markdown.timestamp;

import com.vladsch.flexmark.util.ast.DoNotDecorate;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class Timestamp extends Node implements DoNotDecorate {

    protected BasedSequence openingMarker = BasedSequence.NULL;
    protected BasedSequence timestamp = BasedSequence.NULL;
    protected Instant instant;
    protected BasedSequence style = BasedSequence.NULL;
    protected TimestampType type = TimestampType.SHORT_DATE_TIME;
    protected BasedSequence closingMarker = BasedSequence.NULL;

    public Timestamp() {

    }

    public Timestamp(BasedSequence chars) {
        super(chars);
    }

    public Timestamp(BasedSequence openingMarker, BasedSequence timestamp, @Nullable BasedSequence style, BasedSequence closingMarker) {
        super(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()));
        this.openingMarker = openingMarker;
        this.timestamp = timestamp;
        this.instant = Instant.ofEpochSecond(Long.parseLong(timestamp.toString()));
        this.style = style;
        type = TimestampUtil.parseTimestampStyle(style);
        this.closingMarker = closingMarker;

    }

    public BasedSequence getOpeningMarker() {
        return openingMarker;
    }

    public void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }

    public BasedSequence getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BasedSequence timestamp) {
        this.timestamp = timestamp;
    }

    public BasedSequence getStyle() {
        return style;
    }

    public void setStyle(BasedSequence style) {
        this.style = style;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public TimestampType getType() {
        return type;
    }

    public void setType(TimestampType type) {
        this.type = type;
    }

    public BasedSequence getClosingMarker() {
        return this.closingMarker;
    }

    public void setClosingMarker(BasedSequence closingMarker) {
        this.closingMarker = closingMarker;
    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return new BasedSequence[]{openingMarker, timestamp, style, closingMarker};
    }

    @Override
    public void getAstExtra(@NotNull StringBuilder out) {
        delimitedSegmentSpanChars(out, openingMarker, timestamp, closingMarker, "text");
    }

    @Override
    public String toString() {
        return "Timestamp{" +
                "openingMarker=" + openingMarker +
                ", timestamp=" + timestamp +
                ", instant=" + instant +
                ", style=" + style +
                ", type=" + type +
                ", closingMarker=" + closingMarker +
                "} " + super.toString();
    }

    //    public String toOriginalMarkdown() {
//        return this.openingMarker + ":" + this.timestamp + ":" + this.style + this.closingMarker;
//    }
}
