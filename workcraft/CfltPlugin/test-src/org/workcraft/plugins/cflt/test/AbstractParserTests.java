package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractParserTests {

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected abstract void parseExpression(String input) throws Exception;
    protected abstract Class<? extends Throwable> parseExceptionClass();
    protected abstract Class<? extends Throwable> tokenErrorClass();

    @ParameterizedTest(name = "Should throw ParseException for: {0}")
    @ValueSource(strings = {
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
    })
    void shouldThrowParseException(String input) {
        assertThrows(parseExceptionClass(), () -> parseExpression(input));
    }

    @ParameterizedTest(name = "Should throw TokenMgrError for: {0}")
    @ValueSource(strings = {
        "a$#b",
        "a # b&"
    })
    void shouldThrowTokenMgrErrorCommon(String input) {
        assertThrows(tokenErrorClass(), () -> parseExpression(input));
    }

    @ParameterizedTest(name = "Should parse successfully: {0}")
    @ValueSource(strings = {
        // Valid cases
        "a # b",
        "a | b",
        "a ; b",
        "a b",
        "(a)",
        "((a))",
        "{a}",
        "{{a}}",

        // Comments, spaces and new lines
        "a # b //((((a # b $ % }}",
        "a # b             //((((a # b $ % }}\n        #     c\n//%$%$}}}"
    })
    void shouldParseSuccessfullyCommon(String input) {
        assertDoesNotThrow(() -> parseExpression(input));
    }
}