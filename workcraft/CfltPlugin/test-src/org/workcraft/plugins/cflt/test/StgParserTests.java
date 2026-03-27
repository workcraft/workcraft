package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.jj.stg.ParseException;
import org.workcraft.plugins.cflt.jj.stg.StgStringParser;
import org.workcraft.plugins.cflt.jj.stg.TokenMgrError;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StgParserTests extends AbstractParserTests {

    @Override
    protected void parseExpression(String expressionText) throws ParseException {
        InputStream is = new ByteArrayInputStream(expressionText.getBytes(StandardCharsets.UTF_8));
        StgStringParser parser = new StgStringParser(is);
        parser.parse();
    }

    @Override
    protected Class<? extends Throwable> parseExceptionClass() {
        return ParseException.class;
    }

    @Override
    protected Class<? extends Throwable> tokenErrorClass() {
        return TokenMgrError.class;
    }

    @Test
    void shouldThrowTokenMgrErrorForRepeatedUnaryOperators() {
        assertThrows(TokenMgrError.class, () -> parseExpression("a++ # b-- # c~~"));
    }

    @Test
    void shouldParseValidUnaryOperators() {
        assertDoesNotThrow(() -> parseExpression("a+ # b- # c~"));
    }
}