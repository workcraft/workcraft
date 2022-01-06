package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.circuit.utils.ExpressionUtils;

class ExpressionUtilsTests {

    @Test
    void extractSetTest() {
        Assertions.assertEquals("", ExpressionUtils.extractSetExpression("", "x"));
        Assertions.assertEquals("", ExpressionUtils.extractSetExpression("x", "x"));
        Assertions.assertEquals("SET", ExpressionUtils.extractSetExpression("SET + x * !RESET", "x"));
        Assertions.assertEquals("SET", ExpressionUtils.extractSetExpression("SET + x * RESET'", "x"));
        Assertions.assertEquals("SET", ExpressionUtils.extractSetExpression("SET + !RESET * x", "x"));
        Assertions.assertEquals("SET", ExpressionUtils.extractSetExpression("SET + RESET' * x", "x"));
        Assertions.assertEquals("a*b", ExpressionUtils.extractSetExpression("a * b + c * x * d", "x"));
        Assertions.assertEquals("(a*b)", ExpressionUtils.extractSetExpression("(a * b) + (c * x * d)", "x"));
        Assertions.assertEquals("(a*!b)'", ExpressionUtils.extractSetExpression("(a * !b)' + (!c * x * d)", "x"));
        Assertions.assertEquals("a+b", ExpressionUtils.extractSetExpression("a + b + c * x + x * d", "x"));
        Assertions.assertEquals("!a+b'", ExpressionUtils.extractSetExpression("!a + b' + !c * x + x * d'", "x"));
    }

    @Test
    void extractResetTest() {
        Assertions.assertEquals("", ExpressionUtils.extractResetExpression("", "x"));
        Assertions.assertEquals("", ExpressionUtils.extractResetExpression("x", "x"));
        Assertions.assertEquals("RESET", ExpressionUtils.extractResetExpression("SET + x * !RESET", "x"));
        Assertions.assertEquals("RESET", ExpressionUtils.extractResetExpression("SET + x * RESET'", "x"));
        Assertions.assertEquals("RESET", ExpressionUtils.extractResetExpression("SET + !RESET * x", "x"));
        Assertions.assertEquals("RESET", ExpressionUtils.extractResetExpression("SET + RESET' * x", "x"));
        Assertions.assertEquals("!(c*d)", ExpressionUtils.extractResetExpression("a * b + c * x * d", "x"));
        Assertions.assertEquals("!((c'*d))", ExpressionUtils.extractResetExpression("(a * !b)' + (c' * x * d)", "x"));
        Assertions.assertEquals("!(c+d)", ExpressionUtils.extractResetExpression("a + b + c * x + x * d", "x"));
        Assertions.assertEquals("!(!c+d')", ExpressionUtils.extractResetExpression("!a + b' + !c * x + x * d'", "x"));
    }

}
