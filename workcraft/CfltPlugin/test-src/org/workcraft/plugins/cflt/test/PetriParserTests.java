package org.workcraft.plugins.cflt.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.javaccPetri.ParseException;
import org.workcraft.plugins.cflt.javaccPetri.PetriStringParser;

class PetriParserTests {

    @Test
    void missingBracketEng() {

        boolean thrown = false;
        try {
            parseExpression("(a#b");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("((a#b) | (c#d)");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("{a#b");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("{{a#b} | {c#d}");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("{a#b)");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }
    @Test
    void missingBracketBeg() {

        boolean thrown = false;
        try {
            parseExpression("a#b)");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("(a#b))");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }
    @Test
    void invaidExpressionParsing() {

        boolean thrown = false;
        try {
            parseExpression("a #");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a #| b");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a ;| b");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a ;; b");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a+ # b");
        } catch (Error e) {
            thrown = true;
        } catch (ParseException e) {

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
        } catch (ParseException e) {

        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a # b&");
        } catch (Error e) {
            thrown = true;
        } catch (ParseException e) {

        }
        Assertions.assertTrue(thrown);
    }
    @Test
    void commentIgnoring() {

        boolean thrown = false;
        try {
            parseExpression("a # b //((((a # b $ % }}");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertFalse(thrown);
    }
    @Test
    void tabSpaceMultipleLinesHandling() {

        boolean thrown = false;
        try {
            parseExpression("a # b             //((((a # b $ % }}" + "\n" + "        #     c" + "\n" + "//%$%$}}}");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertFalse(thrown);
    }

    private void parseExpression(String expressionText) throws ParseException {
        InputStream is = new ByteArrayInputStream(expressionText.getBytes(StandardCharsets.UTF_8));
        PetriStringParser parser = new PetriStringParser(is);
        parser.parse(expressionText);

    }
}