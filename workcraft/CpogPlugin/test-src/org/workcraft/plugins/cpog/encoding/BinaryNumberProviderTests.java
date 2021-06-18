package org.workcraft.plugins.cpog.encoding;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.FreeVariable;
import org.workcraft.formula.visitors.StringGenerator;

class BinaryNumberProviderTests {

    @Test
    void testBigConstraint() {
        BinaryNumberProvider p = new BinaryNumberProvider();
        p.generate("x", 25);
        BooleanFormula formula = p.getConstraints();
        String str = StringGenerator.toString(formula);
        Assertions.assertEquals("(xb4 * xb3 * (xb2' * xb1' * xb0')')'", str);
    }

    @Test
    void testValuesCount() {
        BinaryNumberProvider p = new BinaryNumberProvider();
        BinaryIntBooleanFormula num = p.generate("", 9);
        Assertions.assertEquals(9, num.getValuesCount());
    }

    @Test
    void testBigSelect() {
        BinaryNumberProvider p = new BinaryNumberProvider();
        BinaryIntBooleanFormula num = p.generate("", 9);
        BooleanFormula[] f = new BooleanFormula[9];
        f[0] = new FreeVariable("a");
        f[1] = new FreeVariable("b");
        f[2] = new FreeVariable("c");
        f[3] = new FreeVariable("d");
        f[4] = new FreeVariable("e");
        f[5] = new FreeVariable("f");
        f[6] = new FreeVariable("g");
        f[7] = new FreeVariable("h");
        f[8] = new FreeVariable("i");
        BooleanFormula formula = p.select(f, num);
        String str = formula.accept(new StringGenerator());
        Assertions.assertEquals("((b3 * b2' * b1' * b0' * i)' * (b3' * ((b2 * ((b1 * ((b0 * h)' * (b0' * g)')')' * (b1' * ((b0 * f)' * (b0' * e)')')')')' * (b2' * ((b1 * ((b0 * d)' * (b0' * c)')')' * (b1' * ((b0 * b)' * (b0' * a)')')')')')')')'", str);
    }

    @Test
    void testEmptyConstraint() {
        BinaryNumberProvider p = new BinaryNumberProvider();
        p.generate("x", 2);
        BooleanFormula formula = p.getConstraints();
        String str = formula.accept(new StringGenerator());
        Assertions.assertEquals("1", str);
    }

    @Test
    void testZeroBitEmptyConstraint() {
        BinaryNumberProvider p = new BinaryNumberProvider();
        p.generate("", 1);
        Assertions.assertEquals("1", p.getConstraints().accept(new StringGenerator()));
    }

    @Test
    void testSelectZeroBit() {
        BinaryNumberProvider p = new BinaryNumberProvider();
        BinaryIntBooleanFormula num = p.generate("", 1);
        BooleanFormula[] f = new BooleanFormula[1];
        f[0] = new FreeVariable("x");
        BooleanFormula result = p.select(f, num);
        Assertions.assertEquals("x", result.accept(new StringGenerator()));
    }

    @Test
    void testSelectOneBit() {
        BinaryNumberProvider p = new BinaryNumberProvider();
        BinaryIntBooleanFormula num = p.generate("", 2);
        BooleanFormula[] f = new BooleanFormula[2];
        f[0] = new FreeVariable("x");
        f[1] = new FreeVariable("y");
        BooleanFormula result = p.select(f, num);
        String str = result.accept(new StringGenerator());
        Assertions.assertEquals("((b0 * y)' * (b0' * x)')'", str);
    }

    @Test
    void testSelectThreeValues() {
        BinaryNumberProvider p = new BinaryNumberProvider();
        BinaryIntBooleanFormula num = p.generate("", 3);
        BooleanFormula[] f = new BooleanFormula[3];
        f[0] = new FreeVariable("x");
        f[1] = new FreeVariable("y");
        f[2] = new FreeVariable("z");
        BooleanFormula result = p.select(f, num);
        String str = result.accept(new StringGenerator());
        Assertions.assertEquals("((b1 * b0' * z)' * (b1' * ((b0 * y)' * (b0' * x)')')')'", str);
    }

}
