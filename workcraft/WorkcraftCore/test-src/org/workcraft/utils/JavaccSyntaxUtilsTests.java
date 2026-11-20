package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JavaccSyntaxUtilsTests {

    @Test
    void testLexerError() {
        JavaccSyntaxUtils.SyntaxError error = JavaccSyntaxUtils.parseSyntaxError(
                "Lexical error at line 5, column 2.  Encountered: ' ' (32), after prefix \"#\"");

        Assertions.assertEquals(new JavaccSyntaxUtils.SyntaxError("unexpected \" \" after \"#\"", 5, 2, 1), error);

        error = JavaccSyntaxUtils.parseSyntaxError(
                "Lexical error at line 3, column 7.  Encountered: '@' (64), after : \"\"");

        Assertions.assertEquals(new JavaccSyntaxUtils.SyntaxError("unexpected \"@\"", 3, 7, 1), error);

        error = JavaccSyntaxUtils.parseSyntaxError(
                "Lexical error at line 4, column 9.  Encountered: <EOF> after : \"\\\"abc\"");

        Assertions.assertEquals(new JavaccSyntaxUtils.SyntaxError("unexpected end of file after \"\\\"abc\"", 4, 9, 1),
                error);
    }

    @Test
    void testParserError() {
        String eol = System.lineSeparator();

        JavaccSyntaxUtils.SyntaxError error = JavaccSyntaxUtils.parseSyntaxError(
                "Encountered \" <NAME> \"baz \"\" at line 5, column 13." + eol
                        + "Was expecting:" + eol
                        + "    \"(\" ..." + eol
                        + "    " + eol);

        Assertions.assertEquals(new JavaccSyntaxUtils.SyntaxError(
                "unexpected \"baz\", expected one of \"(\"", 5, 13, 3), error);

        error = JavaccSyntaxUtils.parseSyntaxError(
                "Encountered \" \"module\" \"module \"\" at line 2, column 1." + eol
                        + "Was expecting one of:" + eol
                        + "    \"0\" ..." + eol
                        + "    <NAME> ..." + eol
                        + "    \"!\" \"(\" ..." + eol
                        + "    " + eol);

        Assertions.assertEquals(new JavaccSyntaxUtils.SyntaxError(
                "unexpected \"module\", expected one of \"0\", <NAME>, \"!\" \"(\"", 2, 1, 6), error);

        error = JavaccSyntaxUtils.parseSyntaxError(
                "Encountered \"<EOF>\" at line 0, column 0." + eol
                        + "Was expecting one of:" + eol
                        + "    \"endmodule\" ..." + eol
                        + "    <NAME> ..." + eol);

        Assertions.assertEquals(new JavaccSyntaxUtils.SyntaxError(
                "unexpected end of file, expected one of \"endmodule\", <NAME>", 0, 0, 0), error);
    }

    @Test
    void testUnrecognisedError() {
        Assertions.assertNull(JavaccSyntaxUtils.parseSyntaxError(null));
        Assertions.assertNull(JavaccSyntaxUtils.parseSyntaxError(""));
        Assertions.assertNull(JavaccSyntaxUtils.parseSyntaxError("Multiple declarations of net o in module a"));
    }

}
