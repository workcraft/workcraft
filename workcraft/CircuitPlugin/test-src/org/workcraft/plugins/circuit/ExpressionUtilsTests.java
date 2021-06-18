package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.circuit.utils.ExpressionUtils;

class ExpressionUtilsTests {

    @Test
    void extractSetTest() {
        Assertions.assertEquals("", ExpressionUtils.extactSetExpression("", "x"));
        Assertions.assertEquals("", ExpressionUtils.extactSetExpression("x", "x"));
        Assertions.assertEquals("SET", ExpressionUtils.extactSetExpression("SET + x * !RESET", "x"));
        Assertions.assertEquals("SET", ExpressionUtils.extactSetExpression("SET + x * RESET'", "x"));
        Assertions.assertEquals("SET", ExpressionUtils.extactSetExpression("SET + !RESET * x", "x"));
        Assertions.assertEquals("SET", ExpressionUtils.extactSetExpression("SET + RESET' * x", "x"));
        Assertions.assertEquals("a*b", ExpressionUtils.extactSetExpression("a * b + c * x * d", "x"));
        Assertions.assertEquals("(a*b)", ExpressionUtils.extactSetExpression("(a * b) + (c * x * d)", "x"));
        Assertions.assertEquals("(a*!b)'", ExpressionUtils.extactSetExpression("(a * !b)' + (!c * x * d)", "x"));
        Assertions.assertEquals("a+b", ExpressionUtils.extactSetExpression("a + b + c * x + x * d", "x"));
        Assertions.assertEquals("!a+b'", ExpressionUtils.extactSetExpression("!a + b' + !c * x + x * d'", "x"));
    }

    @Test
    void extractResetTest() {
        Assertions.assertEquals("", ExpressionUtils.extactResetExpression("", "x"));
        Assertions.assertEquals("", ExpressionUtils.extactResetExpression("x", "x"));
        Assertions.assertEquals("RESET", ExpressionUtils.extactResetExpression("SET + x * !RESET", "x"));
        Assertions.assertEquals("RESET", ExpressionUtils.extactResetExpression("SET + x * RESET'", "x"));
        Assertions.assertEquals("RESET", ExpressionUtils.extactResetExpression("SET + !RESET * x", "x"));
        Assertions.assertEquals("RESET", ExpressionUtils.extactResetExpression("SET + RESET' * x", "x"));
        Assertions.assertEquals("!(c*d)", ExpressionUtils.extactResetExpression("a * b + c * x * d", "x"));
        Assertions.assertEquals("!((c'*d))", ExpressionUtils.extactResetExpression("(a * !b)' + (c' * x * d)", "x"));
        Assertions.assertEquals("!(c+d)", ExpressionUtils.extactResetExpression("a + b + c * x + x * d", "x"));
        Assertions.assertEquals("!(!c+d')", ExpressionUtils.extactResetExpression("!a + b' + !c * x + x * d'", "x"));
    }

}
