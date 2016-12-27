package org.workcraft.testing.formula.sat;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.Encoding;
import org.workcraft.formula.encoding.onehot.OneHotIntBooleanFormula;
import org.workcraft.formula.encoding.onehot.OneHotNumberProvider;
import org.workcraft.formula.sat.CleverCnfGenerator;
import org.workcraft.formula.sat.LegacySolver;
import org.workcraft.formula.sat.Optimiser;
import org.workcraft.formula.utils.FormulaToString;

@Ignore
public class SolverTestsWithForcedVars {
    private static final boolean DEBUG = false;
    String[] smallTest1 = new String[] {"a"};
    String[] smallTest2 = new String[] {"a", "b"};

    String[] smallTest3 = new String[] {"a", "1"};

    String[] smallTest4 = new String[] {"0", "a"};

    String[] smallTest5 = new String[] {"0", "1", "a"};
    String[] smallTest6 = new String[] {"0", "1", "a", "A"};
    String[] processor = new String[] {
            "1---00001100",
            "1--000111110",
            "1---01000100",
            "1---00111100",
            "1---1--01000",
            "1-0-0000110A",
            "A01-0101111A",
            "A111010111aA",
            };

    private LegacySolver<BooleanFormula> createSolver(int[] levels) {
        return new LegacySolver<BooleanFormula>(
                new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider(), levels), new CleverCnfGenerator());
    }

    private LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<BooleanFormula>(
                new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider()), new CleverCnfGenerator());
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
    public void solveProcessor34332Solvable() {
        testSolve(processor, 3, new int[] {4, 3, 3, 2}, true);
    }

    @Test
    public void solveProcessor34331Solvable() {
        testSolve(processor, 3, new int[] {4, 3, 3, 1}, true);
    }

    @Test
    public void solveProcessor3533Solvable() {
        testSolve(processor, 3, new int[] {5, 3, 3}, true);
    }

    @Test
    public void solveProcessor311Solvable() {
        testSolve(processor, 3, 11, true);
    }

    @Test
    public void solveProcessor310Unsolvable() {
        testSolve(processor, 3, 10, false);
    }

    @Test
    public void solveProcessor444Solvable() {
        testSolve(processor, 4, new int[] {4, 4}, true);
    }

    @Test
    public void solveProcessor47Unsolvable() {
        testSolve(processor, 4, 7, false);
    }

    @Test
    public void solveProcessor48Solvable() {
        testSolve(processor, 4, 8, true);
    }

    @Test
    public void solveProcessor3443Solvable() {
        testSolve(processor, 3, new int[] {4, 4, 3}, true);
    }

    @Test
    public void solveSmall1() {
        testSolve(smallTest1, 0, 0, true);
    }

    @Test
    public void solveSmall200Unsolvable() {
        testSolve(smallTest2, 0, 0, false);
    }

    @Test
    public void solveSmall212Unsolvable() {
        testSolve(smallTest2, 1, 2, false);
    }

    @Test
    public void solveSmall213Solvable() {
        testSolve(smallTest2, 1, 3, true);
    }

    @Test
    public void solveSmall311Solvable() {
        testSolve(smallTest3, 1, 1, true);
    }

    @Test
    public void solveSmall310Unsolvable() {
        testSolve(smallTest3, 1, 0, false);
    }

    @Test
    public void solveSmall410Unsolvable() {
        testSolve(smallTest4, 1, 0, false);
    }

    @Test
    public void solveSmall411Solvable() {
        testSolve(smallTest4, 1, 1, true);
    }

    @Test
    public void solveSmall514Unsolvable() {
        testSolve(smallTest5, 1, 4, false);
    }

    @Test
    public void solveSmall521Unsolvable() {
        testSolve(smallTest5, 2, 1, false);
    }

    @Test
    public void solveSmall522Solvable() {
        testSolve(smallTest5, 2, 2, true);
    }

    @Test
    public void solveSmall622Unsolvable() {
        testSolve(smallTest6, 2, 2, false);
    }

    @Test
    public void solveSmall623Solvable() {
        testSolve(smallTest6, 2, 3, true);
    }

    @Test
    public void solveProcessor81Unsolvable() {
        testSolve(processor, 8, 1, false);
    }

    @Test
    public void solveProcessor82Unsolvable() {
        testSolve(processor, 8, 2, false);
    }

    @Test
    public void solveProcessor83Solvable() {
        testSolve(processor, 8, new int[] {2, 1}, true);
    }

    @Test
    public void solveProcessor84Solvable() {
        testSolve(processor, 8, 4, true);
    }

    @Test  // 9sec -- too long
    public void solveProcessor85Solvable() {
        testSolve(processor, 8, 5, true);
    }

    @Test  // 5sec -- too long
    public void solveProcessor86Solvable() {
        testSolve(processor, 8, 6, true);
    }

    @Test
    public void solveProcessor87Solvable() {
        testSolve(processor, 8, 7, true);
    }

    @Test  // 41sec -- too long
    public void solveProcessor88Solvable() {
        testSolve(processor, 8, 8, true);
    }

    @Test
    public void solveProcessor55Unsolvable() {
        testSolve(processor, 5, 5, false);
    }

    @Test
    public void solveProcessor56Solvable() {
        testSolve(processor, 5, new int[] {2, 3, 1}, true);
    }

    @Test
    public void solveProcessor59Solvable() {
        testSolve(processor, 5, 9, true);
    }

    @Test  // 23sec -- too long
    public void solveProcessor65Solvable() {
        testSolve(processor, 6, 5, true);
    }

    @Test
    public void solveProcessor67Solvable() {
        testSolve(processor, 6, 7, true);
    }

    @Test  // 101sec -- too long
    public void solveProcessor68Solvable() {
        testSolve(processor, 6, 8, true);
    }

    @Test
    public void solveProcessor69Solvable() {
        testSolve(processor, 6, 9, true);
    }

    @Test
    public void solveProcessor64Unsolvable() {
        testSolve(processor, 6, 4, false);
    }

    @Test
    public void solveProcessor74Solvable() {
        testSolve(processor, 7, 4, true);
    }

    @Test
    public void solveProcessor76Solvable() {
        testSolve(processor, 7, 6, true);
    }

    @Test
    public void solveProcessor77Solvable() {
        testSolve(processor, 7, 7, true);
    }

    @Test  // 15sec - too long
    public void solveProcessor73Unsolvable() {
        testSolve(processor, 7, 3, false);
    }

}
