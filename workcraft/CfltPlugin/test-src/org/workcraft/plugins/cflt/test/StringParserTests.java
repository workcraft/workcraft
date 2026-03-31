package org.workcraft.plugins.cflt.test;

import java.io.Reader;
import java.io.StringReader;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.workcraft.plugins.cflt.Model;
import org.workcraft.plugins.cflt.jj.ParseException;
import org.workcraft.plugins.cflt.jj.StringParser;
import org.workcraft.plugins.cflt.jj.TokenMgrError;

class StringParserTests {

    @ParameterizedTest(name = "{1} ParseException for: {0}")
    @MethodSource("invalidExpressions")
    void shouldThrowParseException(String input, Model model) {
        assertThrows(ParseException.class, () -> parseExpression(input, model));
    }

    static Stream<Arguments> invalidExpressions() {
        return Stream.of(
                "(a#b",
                "((a#b) | (c#d)",
                "{a#b",
                "{{a#b} | {c#d}",
                "{a#b)",
                "a#b)",
                "(a#b))",
                "a #",
                "a #| b",
                "a ;| b",
                "a ;; b"
        ).flatMap(input ->
                Stream.of(Model.values()).map(model -> Arguments.of(input, model))
        );
    }

    @ParameterizedTest(name = "{1} TokenMgrError for: {0}")
    @MethodSource("invalidTokens")
    void shouldThrowTokenMgrErrorCommon(String input, Model model) {
        assertThrows(TokenMgrError.class, () -> parseExpression(input, model));
    }

    static Stream<Arguments> invalidTokens() {
        return Stream.of(
                "a$#b",
                "a # b&"
        ).flatMap(input ->
                Stream.of(Model.values()).map(model -> Arguments.of(input, model))
        );
    }

    @ParameterizedTest(name = "{1} Valid parse: {0}")
    @MethodSource("validExpressions")
    void shouldParseSuccessfullyCommon(String input, Model model) {
        assertDoesNotThrow(() -> parseExpression(input, model));
    }

    static Stream<Arguments> validExpressions() {
        return Stream.of(
                "a # b",
                "a | b",
                "a ; b",
                "a b",
                "(a)",
                "((a))",
                "{a}",
                "{{a}}",
                "a # b // comment",
                "a # b\n# c"
        ).flatMap(input ->
                Stream.of(Model.values()).map(model -> Arguments.of(input, model))
        );
    }

    @Test
    void shouldThrowTokenMgrErrorForRepeatedUnaryOperatorsInSTG() {
        assertThrows(TokenMgrError.class, () -> parseExpression("a++ # b-- # c~~", Model.STG));
    }

    @Test
    void shouldParseValidUnaryOperatorsInSTG() {
        assertDoesNotThrow(() -> parseExpression("a+ # b- # c~", Model.STG));
    }

    @Test
    void shouldRejectUnaryOperatorsInPetri() {
        assertThrows(TokenMgrError.class, () -> parseExpression("a+ # b- # c~", Model.PETRI_NET));
    }

    private void parseExpression(String input, Model model) throws ParseException {
        Reader reader = new StringReader(input);
        StringParser parser = new StringParser(reader, model);
        parser.parse();
    }
}