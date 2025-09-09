package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Collections;
import java.util.List;

class TextUtilsTests {

    // Bullet symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    private static final String BULLET_SYMBOL = Character.toString((char) 0x2022);
    private static final String BULLET_PREFIX = "  " + BULLET_SYMBOL + ' ';

    @Test
    void splitWordsTest() {
        Assertions.assertEquals(List.of(), TextUtils.splitWords(null));

        Assertions.assertEquals(List.of(), TextUtils.splitWords("    "));

        Assertions.assertEquals(List.of("abc", "def"), TextUtils.splitWords("    abc   def   "));

        String text = "1 22  333 \n 4444 \n\n 55555 \r\n666666 \r\n7777777\r\n \n 88888888\n \r\n999999999";
        Assertions.assertEquals(List.of("1", "22", "333", "4444", "55555", "666666", "7777777", "88888888", "999999999"),
                TextUtils.splitWords(text));
    }

    @Test
    void splitLinesTest() {
        Assertions.assertEquals(List.of(), TextUtils.splitLines(null));

        Assertions.assertEquals(List.of(""), TextUtils.splitLines(""));

        Assertions.assertEquals(List.of("", ""), TextUtils.splitLines("\n"));

        Assertions.assertEquals(List.of("", "", ""), TextUtils.splitLines("\n\n"));

        Assertions.assertEquals(List.of(" ", " "), TextUtils.splitLines(" \n "));

        String text = "1 22  333 \n 4444 \n\n 55555 \r\n666666 \r\n7777777\r\n \n 88888888\n \r\n999999999";
        Assertions.assertEquals(List.of("1 22  333 ", " 4444 ", "", " 55555 ", "666666 ", "7777777", " ", " 88888888", " ", "999999999"),
                TextUtils.splitLines(text));
    }

    @Test
    void truncateLineTest() {
        String line = "1 22 333 4444 55555 666666 7777777 88888888";
        Assertions.assertEquals("1 22\u2026",
                TextUtils.truncateLine(line, 5));

        Assertions.assertEquals("1 22 333 4444\u2026",
                TextUtils.truncateLine(line, 12));
    }

    @Test
    void truncateTextTest() {
        String text = "1 22 333 \r\n  4444 55555 666666 7777777 88888888\n  999999999";
        Assertions.assertEquals("1 22 333 \n  4444 55555\u2026\n  999999999",
                TextUtils.truncateLines(text, 10));
    }

    @Test
    void wrapLineTest() {
        String line = "1 22 333 4444 55555 666666 7777777 88888888";
        Assertions.assertEquals("1 22\n333\n4444\n55555\n666666\n7777777\n88888888",
                TextUtils.wrapLine(line, 5));

        Assertions.assertEquals("1 22 333 4444\n55555 666666\n7777777\n88888888",
                TextUtils.wrapLine(line, 14));

        int n = TextUtils.DEFAULT_WRAP_LENGTH / 2;
        String aLine = TextUtils.repeat("a ", n);
        String bLine = TextUtils.repeat("b ", n);
        String cLine = TextUtils.repeat("c ", n);
        Assertions.assertEquals(aLine.trim() + '\n' + bLine.trim() + '\n' + cLine.trim(),
                TextUtils.wrapLine(aLine + bLine + cLine));
    }

    @Test
    void wrapTextTest() {
        String text = "1 22 333 \r\n 4444 55555 666666 7777777 88888888\n999999999";
        Assertions.assertEquals("1 22 333 \n4444 55555\n666666\n7777777\n88888888\n999999999",
                TextUtils.wrapText(text, 10));

        int n = TextUtils.DEFAULT_WRAP_LENGTH / 2;
        String aLine = TextUtils.repeat("a ", n);
        String a2Line = TextUtils.repeat("a ", n / 2);
        String bLine = TextUtils.repeat("b ", n);
        String cLine = TextUtils.repeat("c ", n);
        Assertions.assertEquals(aLine.trim() + '\n' + a2Line.trim() + '\n' + bLine.trim() + '\n' + cLine.trim(),
                TextUtils.wrapText(aLine + a2Line + '\n' + bLine + cLine));
    }

    @Test
    void wrapItemsTest() {
        Assertions.assertEquals("",
                TextUtils.wrapItems(List.of()));

        Assertions.assertEquals("A, B, C,\nD, E, F",
                TextUtils.wrapItems(List.of("A", "B", "C", "D", "E", "F"), 9));
    }

    @Test
    void wrapMessageWithItemsTest() {
        Assertions.assertEquals("Nothing",
                TextUtils.wrapMessageWithItems("Nothing", List.of()));

        Assertions.assertEquals("Vegetable carrot",
                TextUtils.wrapMessageWithItems("Vegetable", List.of("carrot")));

        Assertions.assertEquals("Toys: ball, car",
                TextUtils.wrapMessageWithItems("Toy", List.of("ball", "car")));

        Assertions.assertEquals("Boxes: small, large",
                TextUtils.wrapMessageWithItems("Box", List.of("small", "large")));

        Assertions.assertEquals("Bodies:\nA, B, C",
                TextUtils.wrapMessageWithItems("Body", List.of("A", "B", "C"), 10));
    }

    @Test
    void getHeadAndTailTest() {
        String text = "1\n22\n333\n4444\n55555\n666666\n7777777\n88888888\n999999999";
        Assertions.assertEquals("1\n22\n333\n\u2026\n88888888\n999999999",
                TextUtils.getHeadAndTail(text, 3, 2));
    }

    @Test
    void removeHtmlSimpleTagsTest() {
        Assertions.assertEquals("abc 123",
                TextUtils.removeHtmlSimpleTags("<HTML><i>abc</i><br> <b>123</b><br></HTML>"));

        String nonHtmlText = "Place '<a+,b-/>' is redundant";
        Assertions.assertEquals(nonHtmlText,
                TextUtils.removeHtmlSimpleTags(nonHtmlText));
    }

    @Test
    void escapeHtmlTest() {
        String text = "(a < b) & (c > d) = \"true\"";
        Assertions.assertEquals("(a &lt; b) &amp; (c &gt; d) = &quot;true&quot;",
                TextUtils.escapeHtml(text));
    }

    @Test
    void getHtmlSpan() {
        String text = "abc123";
        Color foregroundColor = new Color(0x12, 0x34, 0x56);
        Color backgroundColor = new Color(0xab, 0xcd, 0xef);

        Assertions.assertEquals("<span style=\"color: #123456; background-color: #abcdef\">" + text + "</span>",
                TextUtils.getHtmlSpan(text, foregroundColor, backgroundColor));

        Assertions.assertEquals(TextUtils.getHtmlSpanColor(text, foregroundColor),
                TextUtils.getHtmlSpan(text, foregroundColor, null));

        Assertions.assertEquals(TextUtils.getHtmlSpanHighlight(text, backgroundColor),
                TextUtils.getHtmlSpan(text, null, backgroundColor));
    }

    @Test
    void replaceLinebreaksTest() {
        //noinspection ConstantValue
        Assertions.assertNull(TextUtils.replaceLinebreaks(null, " "));
        Assertions.assertEquals("aaa bbb ccc",
                TextUtils.replaceLinebreaks("aaa\nbbb\r\nccc", " "));
    }

    @Test
    void removeLinebreaksTest() {
        //noinspection ConstantValue
        Assertions.assertNull(TextUtils.removeLinebreaks(null));
        Assertions.assertEquals("aaabbbccc",
                TextUtils.removeLinebreaks("aaa\nbbb\r\nccc"));
    }

    @Test
    void abbreviateTest() {
        Assertions.assertEquals("", TextUtils.abbreviate(null));
        Assertions.assertEquals("abc", TextUtils.abbreviate("aaa bbb ccc"));
        Assertions.assertEquals("ABC", TextUtils.abbreviate("Aaa Bbb Ccc"));
        Assertions.assertEquals("ABC", TextUtils.abbreviate("AaaBbbCcc111"));
    }

    @Test
    void isXmlElementTest() {
        Assertions.assertFalse(TextUtils.isXmlElement(null));
        Assertions.assertFalse(TextUtils.isXmlElement(""));
        Assertions.assertFalse(TextUtils.isXmlElement("abc"));
        Assertions.assertFalse(TextUtils.isXmlElement(" a b c "));
        Assertions.assertTrue(TextUtils.isXmlElement("<tag attr=val>text</tag>"));
        Assertions.assertFalse(TextUtils.isXmlElement("<tag attr=val>text</gat>"));
        Assertions.assertTrue(TextUtils.isXmlElement(" <tag attr=val> text </tag > "));
        Assertions.assertTrue(TextUtils.isXmlElement(" <t-a.g attr=val>\nline1\nline2\n</t-a.g > "));
        Assertions.assertTrue(TextUtils.isXmlElement("<tag attr=val/>"));
    }

    @Test
    void codeToStringTest() {
        Assertions.assertEquals("a", TextUtils.codeToString(0));
        Assertions.assertEquals("b", TextUtils.codeToString(1));
        Assertions.assertEquals("c", TextUtils.codeToString(2));
        Assertions.assertEquals("z", TextUtils.codeToString(25));
        Assertions.assertEquals("ba", TextUtils.codeToString(26)); // 26 = 1 * 26 + 0
        Assertions.assertEquals("bb", TextUtils.codeToString(27)); // 27 = 1 * 26 + 1
        Assertions.assertEquals("bc", TextUtils.codeToString(28)); // 28 = 1 * 26 + 2
        Assertions.assertEquals("zz", TextUtils.codeToString(25 * 26 + 25));
        Assertions.assertEquals("`", TextUtils.codeToString(-1));
    }

    @Test
    void getBulletTest() {
        Assertions.assertEquals(BULLET_PREFIX,
                TextUtils.getBullet(null));

        Assertions.assertEquals(BULLET_PREFIX,
                TextUtils.getBullet(""));

        Assertions.assertEquals(BULLET_PREFIX + "item",
                TextUtils.getBullet("item"));
    }

    @Test
    void getBulletpointTest() {
        Assertions.assertEquals('\n' + BULLET_PREFIX,
                TextUtils.getBulletpoint(null));

        Assertions.assertEquals('\n' + BULLET_PREFIX,
                TextUtils.getBulletpoint(""));

        Assertions.assertEquals('\n' + BULLET_PREFIX + "item",
                TextUtils.getBulletpoint("item"));
    }

    @Test
    void getBulletpointPairTest() {
        Assertions.assertEquals('\n' + BULLET_PREFIX + "key: <empty>",
                TextUtils.getBulletpointPair("key", ""));

        Assertions.assertEquals('\n' + BULLET_PREFIX + "key: value",
                TextUtils.getBulletpointPair("key", "value"));

        Assertions.assertEquals('\n' + BULLET_PREFIX + "key: value",
                TextUtils.getBulletpointPair("key", List.of("value")));

        Assertions.assertEquals('\n' + BULLET_PREFIX + "keys: value1, value2",
                TextUtils.getBulletpointPair("key", List.of("value1", "value2")));
    }

    @Test
    void getTextWithBulletpointsTest() {
        Assertions.assertEquals("", TextUtils.getTextWithBulletpoints(null, null));
        Assertions.assertEquals("", TextUtils.getTextWithBulletpoints(null, Collections.emptyList()));
        Assertions.assertEquals("", TextUtils.getTextWithBulletpoints("", Collections.emptyList()));
        Assertions.assertEquals("text", TextUtils.getTextWithBulletpoints("text", Collections.emptyList()));
        Assertions.assertEquals("single:"
                + '\n' + BULLET_PREFIX + "item",
                TextUtils.getTextWithBulletpoints("single", List.of("item")));

        Assertions.assertEquals("intro:"
                + '\n' + BULLET_PREFIX +  "item1"
                + '\n' + BULLET_PREFIX +  "item2",
                TextUtils.getTextWithBulletpoints("intro", List.of("item1", "item2")));

        Assertions.assertEquals("single item",
                TextUtils.getTextWithBulletpoints("single", List.of("item"), true));
    }

    @Test
    void replaceBulletsTest() {
        Assertions.assertEquals("text", TextUtils.replaceBullets("text", "*"));
        Assertions.assertEquals("  * item", TextUtils.replaceBullets(BULLET_PREFIX +  "item", "*"));
        Assertions.assertEquals("intro:\n  * item1\n  * item2",
                TextUtils.replaceBullets("intro:"
                + '\n' + BULLET_PREFIX +  "item1"
                + '\n' + BULLET_PREFIX +  "item2", "*"));
    }

    @Test
    void getTextWithOptionalDetailsTest() {
        Assertions.assertEquals("text", TextUtils.getTextWithOptionalDetails("text", null));
        Assertions.assertEquals("text", TextUtils.getTextWithOptionalDetails("text", ""));
        Assertions.assertEquals("text (details)", TextUtils.getTextWithOptionalDetails("text", "details"));
    }

}
