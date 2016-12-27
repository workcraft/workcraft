package org.workcraft.testing.formula.sat;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.Encoding;
import org.workcraft.formula.sat.LegacySolver;
import org.workcraft.formula.utils.FormulaToString;

public abstract class SolverTests {

    private static final boolean DEBUG = false;

    static String[] smallscenarios = {"110", "101", "011"};

    static String[] xorscenarios = {"000", "011", "101", "110"};

    static String[] processor = {
            "1---000000011",
            "1--0-00011111",
            "1---000100001",
            "1---000001111",
            "1---001-0-010",
            "1-0-010000011",
            "001--10110111",
            "0111110100111",
            };

    static String[] processorIncorrect = {
            "11111100",
            "------01",
            "-----011",
            "-0-----1",
            "0-0000-1",
            "00000111",
            "00001000",
            "0010-011",
            "01000010",
            "0101-000",
            "01010011",
            "11011111",
            "11110111",
            };

    protected abstract LegacySolver<?> createSolver();

    protected LegacySolver<?> createSolver(int[] levels) {
        return null;
    }

    private void testSolve(String[] scenarios, int free, int derived, boolean solutionExists) {
        Encoding solution = createSolver().solve(scenarios, free, derived);
        printSolution(solution);
        if (solutionExists) {
            assertNotNull(solution);
        } else {
            assertNull(solution);
        }
    }

    private void testSolve(String[] scenarios, int free, int[] levels, boolean solutionExists) {
        Encoding solution = createSolver(levels).solve(scenarios, free, 0);
        printSolution(solution);
        if (solutionExists) {
            assertNotNull(solution);
        } else {
            assertNull(solution);
        }
    }

    private void printSolution(Encoding solution) {
        if (!DEBUG) return;
        if (solution == null) {
            System.out.println("No solution.");
        } else {
            boolean[][] encoding = solution.getEncoding();
            for (int i = 0; i < encoding.length; i++) {
                for (int j = 0; j < encoding[i].length; j++) {
                    System.out.print(encoding[i][j] ? 1 : 0);
                }
                System.out.println();
            }
            System.out.println("Functions:");
            BooleanFormula[] functions = solution.getFunctions();
            for (int i = 0; i < functions.length; i++) {
                System.out.println(FormulaToString.toString(functions[i]));
            }
        }
    }

    @Test
    public void testProcessor5Levels3() {
        testSolve(processor, 5, new int[] {3}, true);
    }

    @Test
    public void testProcessor3Levels44() {
        testSolve(processor, 3, new int[] {4, 4}, true);
    }

    @Test
    public void testProcessor3Levels61() {
        testSolve(processor, 3, new int[] {6, 1}, true);
    }

    @Test
    public void testProcessor3Levels52() {
        testSolve(processor, 3, new int[] {5, 2}, true);
    }

    @Test
    public void testProcessor4Levels1() {
        testSolve(processor, 3, 8, true);
    }

    @Test
    public void testProcessor4Levels5() {
        testSolve(processor, 4, new int[] {5}, true);
    }

    @Test
    public void testProcessor36() {
        testSolve(processor, 3, 6, true);
    }

    @Test
    public void testProcessor37() {
        testSolve(processor, 3, 7, true);
    }

    @Test
    public void testProcessor38() {
        testSolve(processor, 3, 8, true);
    }

    @Test
    public void testProcessor39() {
        testSolve(processor, 3, 9, true);
    }

    @Test
    public void testProcessor310() {
        testSolve(processor, 3, 10, true);
    }

    @Test
    public void testProcessor41() {
        testSolve(processor, 4, 1, false);
    }

    @Test
    public void testProcessor520() {
        testSolve(processor, 5, 20, true);
    }

    @Test
    public void testProcessor43() {
        testSolve(processor, 4, 3, false);
    }

    @Test
    public void testProcessor44() {
        testSolve(processor, 4, 4, false);
    }

    @Test
    public void testProcessor45() {
        testSolve(processor, 4, 5, true);
    }

    @Test
    public void testSmall10() {
        Encoding solution = createSolver().solve(smallscenarios, 1, 0);
        assertNull(solution);
    }

    @Test
    public void testSmall20() {
        Encoding solution = createSolver().solve(smallscenarios, 2, 0);
        assertNull(solution);
    }

    @Test
    public void testSmall21() {
        Encoding solution = createSolver().solve(smallscenarios, 2, 1);
        assertNotNull(solution);
    }

    @Test
    public void testSmall22() {
        Encoding solution = createSolver().solve(smallscenarios, 2, 2);
        assertNotNull(solution);
    }

    @Test
    public void testSmall23() {
        Encoding solution = createSolver().solve(smallscenarios, 2, 3);
        assertNotNull(solution);
    }

    @Test
    public void testSmall30() {
        Encoding solution = createSolver().solve(smallscenarios, 3, 0);
        assertNotNull(solution);
    }

    @Test
    public void testProcessor80() {
        testSolve(processor, 8, 0, true);
    }

    @Test
    public void testProcessor70() {
        testSolve(processor, 7, 0, false);
    }

    @Test
    public void testProcessor71() {
        testSolve(processor, 7, 1, true);
    }

    @Test
    public void testProcessor61() {
        testSolve(processor, 6, 1, false);
    }

    @Test
    public void testProcessor63() {
        testSolve(processor, 6, 3, true);
    }

    @Test
    public void testProcessor62() {
        testSolve(processor, 6, 2, true);
    }

    @Test
    public void testProcessor55() {
        testSolve(processor, 5, 5, true);
    }

    @Test
    public void testProcessor56() {
        testSolve(processor, 5, 6, true);
    }

    @Test
    public void testProcessor52() {
        testSolve(processor, 5, 2, false);
    }

    @Test
    public void testProcessor53() {
        testSolve(processor, 5, 3, true);
    }

    @Test
    public void testProcessor54() {
        testSolve(processor, 5, 4, false);
    }

    @Test
    public void testProcessor46() {
        testSolve(processor, 4, 6, false);
    }

    @Test
    public void testProcessor49() {
        testSolve(processor, 4, 9, true);
    }

    @Test
    public void testProcessor48() {
        testSolve(processor, 4, 8, false);
    }

    @Test
    public void testXor() {
        testSolve(xorscenarios, 2, 1, false);
    }

}
