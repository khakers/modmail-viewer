package com.github.khakers.modmailviewer.markdown.customemoji;

import com.github.khakers.modmailviewer.markdown.customemoji.internal.CustomEmojiInlineParserExtension;
import com.github.khakers.modmailviewer.markdown.customemoji.internal.CustomEmojiNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.NullableDataKey;
import org.jetbrains.annotations.NotNull;

public class CustomEmojiExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    final public static NullableDataKey<String> EMOJI_STYLE_HTML_OPEN = new NullableDataKey<>("EMOJI_STYLE_HTML_OPEN");
    final public static NullableDataKey<String> EMOJI_STYLE_HTML_CLOSE = new NullableDataKey<>("EMOJI_STYLE_HTML_CLOSE");

    public static final DataKey<String> EMOJI_URL_TEMPLATE = new DataKey<>("EMOJI_URL_TEMPLATE", "https://cdn.discordapp.com/emojis/%s.png");

    private CustomEmojiExtension() {

    }

    public static CustomEmojiExtension create() {
        return new CustomEmojiExtension();
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
            htmlRendererBuilder.nodeRendererFactory(new CustomEmojiNodeRenderer.Factory());
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
        parserBuilder.customInlineParserExtensionFactory(new CustomEmojiInlineParserExtension.Factory());
    }

    @Override
    public String toString() {
        return "CustomEmojiExtension{}";
    }
}
