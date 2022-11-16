package com.github.khakers.modmailviewer;

import org.junit.jupiter.api.Test;

class MarkdownTest {


    @Test
    void MarkdownStyleTest() {
        String s = """
                Italics	*italics* or _italics_
                Underline italics	__*underline italics*__
                
                Bold	**bold**
                Underline bold	__**underline bold**__
                
                Bold Italics	***bold italics***
                underline bold italics	__***underline bold italics***__
                
                Underline	__underline__
                Strikethrough	 ~~Strikethrough~~
                """;
        var rendered= Main.RENDERER.render(Main.PARSER.parse(s));
        System.out.println(rendered);
    }

    @Test
    void testSpoilerRendering() {
        String s = """
                |asf||
                ||Some text inside a spoiler||
                ||Some text inside a spoiler||||spoiler||
                ||asdf|
                |asdf|
                """;
        var rendered= Main.RENDERER.render(Main.PARSER.parse(s));
        System.out.println("Spoiler text:");
        System.out.println(rendered);
    }

    @Test
    void testEmojiRendering() {
        String s = """
                text
                <:java:1041989576920678460>
                text <:java:1041989576920678460> text
                """;
        var rendered= Main.RENDERER.render(Main.PARSER.parse(s));
        System.out.println("Custom Emoji:");
        System.out.println(rendered);
    }

    @Test
    void testTimestampRendering() {
        String s = """
                Style	        Input	            Output (12-hour clock)	                Output (24-hour clock)
                Default	        <t:1543392060>	    November 28, 2018 9:01 AM	            28 November 2018 09:01
                Short Time	    <t:1543392060:t>	9:01 AM	                                09:01
                Long Time	    <t:1543392060:T>	9:01:00 AM	                            09:01:00
                Short Date	    <t:1543392060:d>	11/28/2018	                            28/11/2018
                Long Date	    <t:1543392060:D>	November 28, 2018	                    28 November 2018
                Short Date/Time	<t:1543392060:f>	November 28, 2018 9:01 AM	            28 November 2018 09:01
                Long Date/Time	<t:1543392060:F>	Wednesday, November 28, 2018 9:01 AM	Wednesday, 28 November 2018 09:01
                Relative Time	<t:1543392060:R>	3 years ago	3 years ago
                """;
        var rendered= Main.RENDERER.render(Main.PARSER.parse(s));
        System.out.println("Custom Emoji:");
        System.out.println(rendered);
    }

}