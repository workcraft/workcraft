package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.jj.petri.ParseException;
import org.workcraft.plugins.cflt.jj.petri.PetriStringParser;
import org.workcraft.plugins.cflt.jj.petri.TokenMgrError;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PetriParserTests extends AbstractParserTests {

    @Override
    protected void parseExpression(String expressionText) throws ParseException {
        InputStream is = new ByteArrayInputStream(expressionText.getBytes(StandardCharsets.UTF_8));
        PetriStringParser parser = new PetriStringParser(is);
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
    void shouldThrowTokenMgrError() {
        assertThrows(TokenMgrError.class, () -> parseExpression("a+ # b"));
    }
}