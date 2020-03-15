package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.plugins.circuit.utils.ExpressionUtils;

public class ExpressionUtilsTests {

    @Test
    public void extractSetTest() {
        Assert.assertEquals("", ExpressionUtils.extactSetExpression("", "x"));
        Assert.assertEquals("", ExpressionUtils.extactSetExpression("x", "x"));
        Assert.assertEquals("SET", ExpressionUtils.extactSetExpression("SET + x * !RESET", "x"));
        Assert.assertEquals("SET", ExpressionUtils.extactSetExpression("SET + x * RESET'", "x"));
        Assert.assertEquals("SET", ExpressionUtils.extactSetExpression("SET + !RESET * x", "x"));
        Assert.assertEquals("SET", ExpressionUtils.extactSetExpression("SET + RESET' * x", "x"));
        Assert.assertEquals("a*b", ExpressionUtils.extactSetExpression("a * b + c * x * d", "x"));
        Assert.assertEquals("(a*b)", ExpressionUtils.extactSetExpression("(a * b) + (c * x * d)", "x"));
        Assert.assertEquals("(a*!b)'", ExpressionUtils.extactSetExpression("(a * !b)' + (!c * x * d)", "x"));
        Assert.assertEquals("a+b", ExpressionUtils.extactSetExpression("a + b + c * x + x * d", "x"));
        Assert.assertEquals("!a+b'", ExpressionUtils.extactSetExpression("!a + b' + !c * x + x * d'", "x"));
    }

    @Test
    public void extractResetTest() {
        Assert.assertEquals("", ExpressionUtils.extactResetExpression("", "x"));
        Assert.assertEquals("", ExpressionUtils.extactResetExpression("x", "x"));
        Assert.assertEquals("RESET", ExpressionUtils.extactResetExpression("SET + x * !RESET", "x"));
        Assert.assertEquals("RESET", ExpressionUtils.extactResetExpression("SET + x * RESET'", "x"));
        Assert.assertEquals("RESET", ExpressionUtils.extactResetExpression("SET + !RESET * x", "x"));
        Assert.assertEquals("RESET", ExpressionUtils.extactResetExpression("SET + RESET' * x", "x"));
        Assert.assertEquals("!(c*d)", ExpressionUtils.extactResetExpression("a * b + c * x * d", "x"));
        Assert.assertEquals("!((c'*d))", ExpressionUtils.extactResetExpression("(a * !b)' + (c' * x * d)", "x"));
        Assert.assertEquals("!(c+d)", ExpressionUtils.extactResetExpression("a + b + c * x + x * d", "x"));
        Assert.assertEquals("!(!c+d')", ExpressionUtils.extactResetExpression("!a + b' + !c * x + x * d'", "x"));
    }

}
