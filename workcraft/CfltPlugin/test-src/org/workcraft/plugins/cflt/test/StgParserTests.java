package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.jj.stg.ParseException;
import org.workcraft.plugins.cflt.jj.stg.StgStringParser;
import org.workcraft.plugins.cflt.jj.stg.TokenMgrError;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class StgParserTests {

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
    void invalidExpressionParsing() {
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
            parseExpression("a+ # b- # c~");
        } catch (ParseException e) {
            thrown = true;
        }
        Assertions.assertFalse(thrown);

        thrown = false;
        try {
            parseExpression("a++ # b-- # c~~");
        } catch (TokenMgrError e) {
            thrown = true;
        } catch (ParseException ignored) {
        }
        Assertions.assertTrue(thrown);
    }

    @Test
    void lexicalError() {
        boolean thrown = false;
        try {
            parseExpression("a$#b");
        } catch (TokenMgrError e) {
            thrown = true;
        } catch (ParseException ignored) {
        }
        Assertions.assertTrue(thrown);

        thrown = false;
        try {
            parseExpression("a # b&");
        } catch (TokenMgrError e) {
            thrown = true;
        } catch (ParseException ignored) {
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
            parseExpression("a # b             //((((a # b $ % }}\n        #     c\n//%$%$}}}");
        } catch (ParseException e) {
            Assertions.assertEquals(e.getClass(), ParseException.class);
            thrown = true;
        }
        Assertions.assertFalse(thrown);
    }

    private void parseExpression(String expressionText) throws ParseException {
        InputStream is = new ByteArrayInputStream(expressionText.getBytes(StandardCharsets.UTF_8));
        StgStringParser parser = new StgStringParser(is);
        parser.parse();
    }

}
