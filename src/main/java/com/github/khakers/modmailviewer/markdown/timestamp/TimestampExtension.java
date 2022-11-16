package com.github.khakers.modmailviewer.markdown.timestamp;

import com.github.khakers.modmailviewer.markdown.timestamp.internal.TimestampInlineParserExtension;
import com.github.khakers.modmailviewer.markdown.timestamp.internal.TimestampNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.NullableDataKey;
import com.vladsch.flexmark.util.html.Attributes;
import org.jetbrains.annotations.NotNull;

public class TimestampExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    final public static NullableDataKey<String> EMOJI_STYLE_HTML_OPEN = new NullableDataKey<>("EMOJI_STYLE_HTML_OPEN");
    final public static NullableDataKey<String> EMOJI_STYLE_HTML_CLOSE = new NullableDataKey<>("EMOJI_STYLE_HTML_CLOSE");

    final public static DataKey<Attributes> EXTRA_IMG_ATTRIBUTES =  new DataKey<>("EMOJI_URL_TEMPLATE", Attributes.EMPTY);

    public static final DataKey<String> EMOJI_URL_TEMPLATE = new DataKey<>("EMOJI_URL_TEMPLATE", "https://cdn.discordapp.com/emojis/%s.png");

    private TimestampExtension() {

    }

    public static TimestampExtension create() {
        return new TimestampExtension();
    }

    /**
     * This method is called first on all extensions so that they can adjust the options that must be
     * common to all extensions.
     *
     * @param options option set that will be used for the builder
     */
    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    /**
     * This method is called first on all extensions so that they can adjust the options that must be common to all extensions.
     *
     * @param options option set that will be used for the builder
     */
    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    /**
     * Called to give each extension to register extension points that it contains
     *
     * @param htmlRendererBuilder builder to call back for extension point registration
     * @param rendererType        type of rendering being performed. For now "HTML", "JIRA" or "YOUTRACK"
     */
    @Override
    public void extend(HtmlRenderer.@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new TimestampNodeRenderer.Factory());
        }
    }

    /**
     * This method is called on all extensions so that they can register their custom processors
     *
     * @param parserBuilder parser builder with which to register extensions
     */
    @Override
    public void extend(Parser.Builder parserBuilder) {
//        parserBuilder.customDelimiterProcessor(new CustomEmojiDelimiterProcessor());
        parserBuilder.customInlineParserExtensionFactory(new TimestampInlineParserExtension.Factory());
    }

    @Override
    public String toString() {
        return "CustomEmojiExtension{}";
    }
}
