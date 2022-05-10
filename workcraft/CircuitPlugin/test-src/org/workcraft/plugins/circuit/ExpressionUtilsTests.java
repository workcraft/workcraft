package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.circuit.utils.ExpressionUtils;

class ExpressionUtilsTests {

    @Test
    void trivialExtractSetResetTest() {
        extractSetResetTest(null, null, "", "");
        extractSetResetTest("", "", "", "");
        extractSetResetTest("x", "x", "CONST0", "CONST0");
        extractSetResetTest("x'", "x", "CONST1", "CONST1");
    }

    @Test
    void combinationalExtractSetResetTest() {
        extractSetResetTest("a", null, "a", "!a");
        extractSetResetTest("a", "", "a", "!a");
        extractSetResetTest("a", "x", "a", "!a");
        extractSetResetTest("!a", "x", "!a", "!!a");
        extractSetResetTest("a + b", null, "a + b", "!(a + b)");
        extractSetResetTest("!a * (b + c)", null, "!a * (b + c)", "!(!a * (b + c))");
    }

    @Test
    void latchExtractSetResetTest() {
        extractSetResetTest("S + !R * Q", "Q", "S", "!(S + !R)");
        extractSetResetTest("S + Q * R'", "Q", "S", "!(S + !R)");
        extractSetResetTest("!S + R * Q", "Q", "!S", "!(!S + R)");
        extractSetResetTest("S' + Q * R", "Q", "!S", "!(!S + R)");
        extractSetResetTest("D * C + D * Q + !C * Q", "Q", "D * C", "!(D + !C)");
        extractSetResetTest("D * C + !C * Q", "Q", "D * C", "!(D * C + !C)");
    }

    @Test
    void celementExtractSetResetTest() {
        extractSetResetTest("A * B + (A + B) * Q", "Q", "A * B", "!(A + B)");
        extractSetResetTest("A * B + (A' * B')' * Q", "Q", "A * B", "!A * !B");
        extractSetResetTest("!A * !B + (!A + !B) * Q", "Q", "!A * !B", "!(!A + !B)");
        extractSetResetTest("(A + B)' + (A * B)' * Q", "Q", "!(A + B)", "A * B");
    }

    private void extractSetResetTest(String expression, String seqLiteral,
            String expSetExpression, String expResetExpression) {

        Assertions.assertEquals(expSetExpression, ExpressionUtils.extractSetFunction(expression, seqLiteral));
        Assertions.assertEquals(expResetExpression, ExpressionUtils.extractResetFunction(expression, seqLiteral));
    }

    @Test
    void extractHeuristicSetTest() {
        extractHeuristicSetResetTest("", "", "", "");
        extractHeuristicSetResetTest("x", "x", "", "");
        extractHeuristicSetResetTest("SET + x * !RESET", "x", "SET", "RESET");
        extractHeuristicSetResetTest("SET + x * RESET'", "x", "SET", "RESET");
        extractHeuristicSetResetTest("SET + !RESET * x", "x", "SET", "RESET");
        extractHeuristicSetResetTest("SET + RESET' * x", "x", "SET", "RESET");
        extractHeuristicSetResetTest("a * b + c * x * d", "x", "a*b", "!(c*d)");
        extractHeuristicSetResetTest("(a * b) + (c * x * d)", "x", "(a*b)", "!((c*d))");
        extractHeuristicSetResetTest("(a * !b)' + (!c * x * d)", "x", "(a*!b)'", "!((!c*d))");
        extractHeuristicSetResetTest("a + b + c * x + x * d", "x", "a+b", "!(c+d)");
        extractHeuristicSetResetTest("!a + b' + !c * x + x * d'", "x", "!a+b'", "!(!c+d')");
    }

    private void extractHeuristicSetResetTest(String expression, String seqLiteral,
            String expSetFunction, String expResetFunction) {

        Assertions.assertEquals(expSetFunction, ExpressionUtils.extractHeuristicSetFunction(expression, seqLiteral));
        Assertions.assertEquals(expResetFunction, ExpressionUtils.extractHeuristicResetFunction(expression, seqLiteral));
    }

}
