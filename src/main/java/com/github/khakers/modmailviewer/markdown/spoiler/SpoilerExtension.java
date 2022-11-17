package com.github.khakers.modmailviewer.markdown.spoiler;

import com.github.khakers.modmailviewer.markdown.spoiler.internal.SpoilerDelimiterProcessor;
import com.github.khakers.modmailviewer.markdown.spoiler.internal.SpoilerNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.NullableDataKey;
import org.jetbrains.annotations.NotNull;

public class SpoilerExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    final public static NullableDataKey<String> SPOILER_STYLE_HTML_OPEN = new NullableDataKey<>("SPOILER_STYLE_HTML_OPEN");
    final public static NullableDataKey<String> SPOILER_STYLE_HTML_CLOSE = new NullableDataKey<>("SPOILER_STYLE_HTML_CLOSE");

    private SpoilerExtension() {

    }

    public static SpoilerExtension create() {
        return new SpoilerExtension();
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
     * @see Builder#attributeProviderFactory(AttributeProviderFactory)
     * @see Builder#nodeRendererFactory(NodeRendererFactory)
     * @see Builder#linkResolverFactory(LinkResolverFactory)
     * @see Builder#htmlIdGeneratorFactory(HeaderIdGeneratorFactory)
     */
    @Override
    public void extend(HtmlRenderer.@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new SpoilerNodeRenderer.Factory());
        }
    }

    /**
     * This method is called on all extensions so that they can register their custom processors
     *
     * @param parserBuilder parser builder with which to register extensions
     * @see Builder#customBlockParserFactory(CustomBlockParserFactory)
     * @see Builder#customInlineParserExtensionFactory(InlineParserExtensionFactory)
     * @see Builder#customInlineParserFactory(InlineParserFactory)
     * @see Builder#customDelimiterProcessor(DelimiterProcessor)
     * @see Builder#postProcessorFactory(PostProcessorFactory)
     * @see Builder#paragraphPreProcessorFactory(ParagraphPreProcessorFactory)
     * @see Builder#blockPreProcessorFactory(BlockPreProcessorFactory)
     * @see Builder#linkRefProcessorFactory(LinkRefProcessorFactory)
     * @see Builder#specialLeadInHandler(SpecialLeadInHandler)
     */
    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new SpoilerDelimiterProcessor());
    }
}
