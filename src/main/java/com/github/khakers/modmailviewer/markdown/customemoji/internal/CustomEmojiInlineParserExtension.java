package com.github.khakers.modmailviewer.markdown.customemoji.internal;

import com.github.khakers.modmailviewer.markdown.customemoji.CustomEmoji;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

public class CustomEmojiInlineParserExtension implements InlineParserExtension {

    public static final Pattern CUSTOM_EMOJI = Pattern.compile("(<):(\\w+):(\\d+)(>)", Pattern.CASE_INSENSITIVE);

    public CustomEmojiInlineParserExtension(LightInlineParser lightInlineParser) {

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
        BasedSequence[] matches = inlineParser.matchWithGroups(CUSTOM_EMOJI);
        if (matches != null) {
            inlineParser.flushTextNode();
            System.out.println(Arrays.toString(matches));
            BasedSequence openMarker = matches[1];
            BasedSequence name = matches[2];
            BasedSequence id = matches[3];
            BasedSequence closeMarker = matches[4];

            inlineParser.getBlock().appendChild(new CustomEmoji(openMarker, name, id, closeMarker));
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
            return new CustomEmojiInlineParserExtension(lightInlineParser);
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }
    }

}
