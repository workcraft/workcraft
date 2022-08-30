package org.workcraft.plugins.cflt.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.javaccStg.ParseException;
import org.workcraft.plugins.cflt.javaccStg.StgStringParser;

class StgParserTests {

    @Test
    void missingBracketEng() {

        boolean thrown = false;
        try {
            parseExpression("(a#b");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("((a#b) | (c#d)");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("{a#b");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("{{a#b} | {c#d}");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("{a#b)");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }
    @Test
    void missingBracketBeg() {

        boolean thrown = false;
        try {
            parseExpression("a#b)");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("(a#b))");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }
    @Test
    void invaidExpressionParsing() {

        boolean thrown = false;
        try {
            parseExpression("a #");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a #| b");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a ;| b");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a ;; b");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a+ # b- # c~");
        } catch (Error | ParseException e) {
            thrown = true;
        }
        Assertions.assertFalse(thrown);

        thrown = false;
        try {
            parseExpression("a++ # b-- # c~~");
        } catch (Error | ParseException e) {
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }
    @Test
    void lexicalError() {

        boolean thrown = false;
        try {
            parseExpression("a$#b");
        } catch (Error e) {
            thrown = true;
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {

        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a # b&");
        } catch (Error e) {
            thrown = true;
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {

        }
        Assertions.assertTrue(thrown);
    }
    @Test
    void commentIgnoring() {

        boolean thrown = false;
        try {
            parseExpression("a # b //((((a # b $ % }}");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertFalse(thrown);
    }
    @Test
    void tabSpaceMultipleLinesHandling() {

        boolean thrown = false;
        try {
            parseExpression("a # b             //((((a # b $ % }}" + "\n" + "        #     c" + "\n" + "//%$%$}}}");
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            Assertions.assertEquals(e.getClass(), org.workcraft.plugins.cflt.javaccStg.ParseException.class);
            thrown = true;
        }
        Assertions.assertFalse(thrown);
    }

    private void parseExpression(String expressionText) throws org.workcraft.plugins.cflt.javaccStg.ParseException {
        InputStream is = new ByteArrayInputStream(expressionText.getBytes(StandardCharsets.UTF_8));
        StgStringParser parser = new StgStringParser(is);
        parser.parse(expressionText);

    }
}