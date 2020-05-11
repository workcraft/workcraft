package org.workcraft.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TextUtilsTests {

    @Test
    public void splitWordsTest() {
        Assert.assertEquals(Arrays.asList(), TextUtils.splitWords(null));

        Assert.assertEquals(Arrays.asList(), TextUtils.splitWords("    "));

        Assert.assertEquals(Arrays.asList("abc", "def"), TextUtils.splitWords("    abc   def   "));

        String text = "1 22  333 \n 4444 \n\n 55555 \r\n666666 \r\n7777777\r\n \n 88888888\n \r\n999999999";
        Assert.assertEquals(Arrays.asList("1", "22", "333", "4444", "55555", "666666", "7777777", "88888888", "999999999"),
                TextUtils.splitWords(text));
    }

    @Test
    public void splitLinesTest() {
        Assert.assertEquals(Arrays.asList(), TextUtils.splitLines(null));

        Assert.assertEquals(Arrays.asList(""), TextUtils.splitLines(""));

        Assert.assertEquals(Arrays.asList("", ""), TextUtils.splitLines("\n"));

        Assert.assertEquals(Arrays.asList("", "", ""), TextUtils.splitLines("\n\n"));

        Assert.assertEquals(Arrays.asList(" ", " "), TextUtils.splitLines(" \n "));

        String text = "1 22  333 \n 4444 \n\n 55555 \r\n666666 \r\n7777777\r\n \n 88888888\n \r\n999999999";
        Assert.assertEquals(Arrays.asList("1 22  333 ", " 4444 ", "", " 55555 ", "666666 ", "7777777", " ", " 88888888", " ", "999999999"),
                TextUtils.splitLines(text));
    }

    @Test
    public void truncateLineTest() {
        String line = "1 22 333 4444 55555 666666 7777777 88888888";
        Assert.assertEquals("1 22\u2026",
                TextUtils.truncateLine(line, 5));

        Assert.assertEquals("1 22 333 4444\u2026",
                TextUtils.truncateLine(line, 12));
    }

    @Test
    public void truncateTextTest() {
        String text = "1 22 333 \r\n 4444 55555 666666 7777777 88888888\n999999999";
        Assert.assertEquals("1 22 333 \n4444 55555\u2026\n999999999",
                TextUtils.truncateText(text, 10));
    }

    @Test
    public void wrapLineTest() {
        String line = "1 22 333 4444 55555 666666 7777777 88888888";
        Assert.assertEquals("1 22\n333\n4444\n55555\n666666\n7777777\n88888888",
                TextUtils.wrapLine(line, 5));

        Assert.assertEquals("1 22 333 4444\n55555 666666\n7777777\n88888888",
                TextUtils.wrapLine(line, 14));

        int n = TextUtils.DEFAULT_WRAP_LENGTH / 2;
        String aLine = TextUtils.repeat("a ", n);
        String bLine = TextUtils.repeat("b ", n);
        String cLine = TextUtils.repeat("c ", n);
        Assert.assertEquals(aLine.trim() + "\n" + bLine.trim() + "\n" + cLine.trim(),
                TextUtils.wrapLine(aLine + bLine + cLine));
    }

    @Test
    public void wrapTextTest() {
        String text = "1 22 333 \r\n 4444 55555 666666 7777777 88888888\n999999999";
        Assert.assertEquals("1 22 333 \n4444 55555\n666666\n7777777\n88888888\n999999999",
                TextUtils.wrapText(text, 10));

        int n = TextUtils.DEFAULT_WRAP_LENGTH / 2;
        String aLine = TextUtils.repeat("a ", n);
        String a2Line = TextUtils.repeat("a ", n / 2);
        String bLine = TextUtils.repeat("b ", n);
        String cLine = TextUtils.repeat("c ", n);
        Assert.assertEquals(aLine.trim() + "\n" + a2Line.trim() + "\n" + bLine.trim() + "\n" + cLine.trim(),
                TextUtils.wrapText(aLine + a2Line + "\n" + bLine + cLine));
    }

    @Test
    public void wrapItemsTest() {
        Assert.assertEquals("",
                TextUtils.wrapItems(Arrays.asList()));

        Assert.assertEquals("A, B, C,\nD, E, F",
                TextUtils.wrapItems(Arrays.asList("A", "B", "C", "D", "E", "F"), 9));
    }

    @Test
    public void wrapMessageWithItemsTest() {
        Assert.assertEquals("Nothing",
                TextUtils.wrapMessageWithItems("Nothing", Arrays.asList()));

        Assert.assertEquals("Vegetable 'carrot'",
                TextUtils.wrapMessageWithItems("Vegetable", Arrays.asList("carrot")));

        Assert.assertEquals("Toys: ball, car",
                TextUtils.wrapMessageWithItems("Toy", Arrays.asList("ball", "car")));

        Assert.assertEquals("Boxes: small, large",
                TextUtils.wrapMessageWithItems("Box", Arrays.asList("small", "large")));

        Assert.assertEquals("Bodies:\nA, B, C",
                TextUtils.wrapMessageWithItems("Body", Arrays.asList("A", "B", "C"), 10));
    }

    @Test
    public void getHeadAndTailTest() {
        String text = "1\n22\n333\n4444\n55555\n666666\n7777777\n88888888\n999999999";
        Assert.assertEquals("1\n22\n333\n\u2026\n88888888\n999999999",
                TextUtils.getHeadAndTail(text, 3, 2));
    }

    @Test
    public void escapeHtmlTest() {
        String text = "(a < b) & (c > d) = \"true\"";
        Assert.assertEquals("(a &lt; b) &amp; (c &gt; d) = &quot;true&quot;",
                TextUtils.escapeHtml(text));
    }

}
