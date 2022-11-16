package com.github.khakers.modmailviewer.markdown.underline.internal;

import com.github.khakers.modmailviewer.markdown.underline.Underline;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class UnderlineDelimiterProcessor implements DelimiterProcessor {
    /**
     * @return the character that marks the beginning of a delimited node, must not clash with any built-in special
     * characters
     */
    @Override
    public char getOpeningCharacter() {
        return '_';
    }

    /**
     * @return the character that marks the the ending of a delimited node, must not clash with any built-in special
     * characters. Note that for a symmetric delimiter such as "*", this is the same as the opening.
     */
    @Override
    public char getClosingCharacter() {
        return '_';
    }

    /**
     * @return Minimum number of delimiter characters that are needed to activate this. Must be at least 1.
     */
    @Override
    public int getMinLength() {
        return 1;
    }

    /**
     * Determine how many (if any) of the delimiter characters should be used.
     * <p>
     * This allows implementations to decide how many characters to use based on the properties of the delimiter runs.
     * An implementation can also return 0 when it doesn't want to allow this particular combination of delimiter runs.
     *
     * @param opener the opening delimiter run
     * @param closer the closing delimiter run
     * @return how many delimiters should be used; must not be greater than length of either opener or closer
     */
    @Override
    public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        // Based on code from https://github.com/vsch/flexmark-java/blob/master/flexmark-ext-gfm-strikethrough/src/main/java/com/vladsch/flexmark/ext/gfm/strikethrough/internal/StrikethroughSubscriptDelimiterProcessor.java
        /*
        Copyright (c) 2015-2016, Atlassian Pty Ltd
        All rights reserved.

        Copyright (c) 2016-2018, Vladimir Schneider,
        All rights reserved.

        BSD-2-Clause license
        */

        // "multiple of 3" rule for internal delimiter runs
        if ((opener.canClose() || closer.canOpen()) && (opener.length() + closer.length()) % 3 == 0) {
            return 0;
        }
        // calculate actual number of delimiters used from this closer
        if (opener.length() < 3 || closer.length() < 3) {
            return Math.min(closer.length(), opener.length());
        } else {
            return closer.length() % 2 == 0 ? 2 : 1;
        }
    }

    /**
     * Process the matched delimiters, e.g. by wrapping the nodes between opener and closer in a new node, or appending
     * a new node after the opener.
     * <p>
     * Note that removal of the delimiter from the delimiter nodes and unlinking them is done by the caller.
     *
     * @param opener         the delimiter with text node that contained the opening delimiter
     * @param closer         the delimiter with text node that contained the closing delimiter
     * @param delimitersUsed the number of delimiters that were used
     */
    @Override
    public void process(Delimiter opener, Delimiter closer, int delimitersUsed) {
        // We have to disable standard _ delimiter processing since this conflicts directly with it.
        // We still want _italics_ to work, so we need handle returning an Emphasis when only one delimiter is present
        DelimitedNode emphasis;
        if (delimitersUsed == 1)
            emphasis = new Emphasis(opener.getTailChars(delimitersUsed), BasedSequence.NULL, closer.getLeadChars(delimitersUsed));
        else
            emphasis = new Underline(opener.getTailChars(delimitersUsed), BasedSequence.NULL, closer.getLeadChars(delimitersUsed));

        opener.moveNodesBetweenDelimitersTo(emphasis, closer);

    }

    /**
     * Allow delimiter processor to substitute unmatched delimiters by custom nodes
     *
     * @param inlineParser inline parser instance
     * @param delimiter    delimiter run that was not matched
     * @return node to replace unmatched delimiter, null or delimiter.getNode() to replace with delimiter text
     */
    @Override
    public Node unmatchedDelimiterNode(InlineParser inlineParser, DelimiterRun delimiter) {
        return null;
    }

    /**
     * Decide whether this delimiter can be an open delimiter
     *
     * @param before              string before delimiter or '\n' if none
     * @param after               string after delimiter or '\n' if none
     * @param leftFlanking        is left flanking delimiter
     * @param rightFlanking       is right flanking delimiter
     * @param beforeIsPunctuation is punctuation before
     * @param afterIsPunctuation  is punctuation after
     * @param beforeIsWhitespace  is whitespace before
     * @param afterIsWhiteSpace   is whitespace after
     * @return true if can be open delimiter
     */
    @Override
    public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking, boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace, boolean afterIsWhiteSpace) {
        return leftFlanking;
    }

    /**
     * Decide whether this delimiter can be a close delimiter
     *
     * @param before              string before delimiter or '\n' if none
     * @param after               string after delimiter or '\n' if none
     * @param leftFlanking        is left flanking delimiter
     * @param rightFlanking       is right flanking delimiter
     * @param beforeIsPunctuation is punctuation before
     * @param afterIsPunctuation  is punctuation after
     * @param beforeIsWhitespace  is whitespace before
     * @param afterIsWhiteSpace   is whitespace after
     * @return true if can be open delimiter
     */
    @Override
    public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking, boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace, boolean afterIsWhiteSpace) {
        return rightFlanking;
    }

    /**
     * Whether to skip delimiters that cannot be openers or closers
     *
     * @return true if to skip
     */
    @Override
    public boolean skipNonOpenerCloser() {
        return false;
    }
}
