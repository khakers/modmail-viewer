package com.github.khakers.modmailviewer.markdown.timestamp.internal;

import com.github.khakers.modmailviewer.markdown.timestamp.Timestamp;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

public class TimestampInlineParserExtension implements InlineParserExtension {

    private static final Logger logger = LogManager.getLogger();

    public static final Pattern TIMESTAMP = Pattern.compile("(<)t:(\\d+)(?::([tTdDfFR]))?(>)", Pattern.CASE_INSENSITIVE);

    public TimestampInlineParserExtension(LightInlineParser lightInlineParser) {

    }

    @Override
    public void finalizeDocument(@NotNull InlineParser inlineParser) {

    }

    @Override
    public void finalizeBlock(@NotNull InlineParser inlineParser) {

    }

    /**
     * Parse input
     *
     * @param inlineParser
     * @return true if character input was processed
     */
    @Override
    public boolean parse(@NotNull LightInlineParser inlineParser) {
        BasedSequence[] matches = inlineParser.matchWithGroups(TIMESTAMP);
        if (matches != null) {
            inlineParser.flushTextNode();
            logger.debug(Arrays.toString(matches));
            BasedSequence openMarker = matches[1];
            BasedSequence timestamp = matches[2];
            BasedSequence style = matches[3];
            BasedSequence closeMarker = matches[4];

            inlineParser.getBlock().appendChild(new Timestamp(openMarker, timestamp, style, closeMarker));
            return true;
        }
        return false;
    }

    public static class Factory implements InlineParserExtensionFactory {
        @Nullable
        @Override
        public Set<Class<?>> getAfterDependents() {
            return null;
        }

        @NotNull
        @Override
        public CharSequence getCharacters() {
            return "<";
        }

        @Nullable
        @Override
        public Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @NotNull
        @Override
        public InlineParserExtension apply(@NotNull LightInlineParser lightInlineParser) {
            return new TimestampInlineParserExtension(lightInlineParser);
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }
    }

}
