package org.workcraft.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

public class EquationUtilsTests {

    @Test
    public void solveLinearEquationTest() {
        Assert.assertEquals(new HashSet<>(),
                EquationUtils.solveLinearEquation(0.0, 0.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(1.0)),
                EquationUtils.solveLinearEquation(1.0, -1.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(0.5)),
                EquationUtils.solveLinearEquation(-2.0, 1.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(-4.0)),
                EquationUtils.solveLinearEquation(-1.2, -4.8));
    }

    @Test
    public void solveQuadraticEquationTest() {
        Assert.assertEquals(new HashSet<>(),
                EquationUtils.solveQuadraticEquation(0.0, 0.0, 0.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(1.0)),
                EquationUtils.solveQuadraticEquation(0.0, 1.0, -1.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(1.0, -1.0)),
                EquationUtils.solveQuadraticEquation(1.0, 0.0, -1.0));

        Assert.assertEquals(new HashSet<>(),
                EquationUtils.solveQuadraticEquation(1.0, 2.0, 3.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(2.0, 5.0)),
                EquationUtils.solveQuadraticEquation(1.0, -7.0, 10.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(1.5)),
                EquationUtils.solveQuadraticEquation(-4.0, 12.0, -9.0));
    }

    @Test
    public void solveCubicEquationTest() {
        Assert.assertEquals(new HashSet<>(),
                EquationUtils.solveCubicEquation(0.0, 0.0, 0.0, 0.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(1.0)),
                EquationUtils.solveCubicEquation(0.0, 0.0, 1.0, -1.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(-0.7293231430114582)),
                EquationUtils.solveCubicEquation(5.0, 4.0, 3.0, 2.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(-0.605829586188268)),
                EquationUtils.solveCubicEquation(4.0, 3.0, 2.0, 1.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(-4.0, 1.0)),
                EquationUtils.solveCubicEquation(1.0, 2.0, -7.0, 4.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(2.0000000000000004, 0.9999999999999991)),
                EquationUtils.solveCubicEquation(1.0, -5.0, 8.0, -4.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(-0.6403882032022075, 0.3903882032022076, 1.0)),
                EquationUtils.solveCubicEquation(4.0, -3.0, -2.0, 1.0));

        Assert.assertEquals(new HashSet<>(Arrays.asList(-2.0000000000000004, -0.9999999999999997, 3.0000000000000004)),
                EquationUtils.solveCubicEquation(1.0, 0.0, -7.0, -6.0));
    }

}
