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
                TextUtils.wrapLine(line, 12));
    }

    @Test
    public void wrapTextTest() {
        String text = "1 22 333 \r\n 4444 55555 666666 7777777 88888888\n999999999";
        Assert.assertEquals("1 22 333 \n4444 55555\n666666\n7777777\n88888888\n999999999",
                TextUtils.wrapText(text, 10));
    }

    @Test
    public void getHeadAndTailTest() {
        String text = "1\n22\n333\n4444\n55555\n666666\n7777777\n88888888\n999999999";
        Assert.assertEquals("1\n22\n333\n\u2026\n88888888\n999999999",
                TextUtils.getHeadAndTail(text, 3, 2));
    }

}
