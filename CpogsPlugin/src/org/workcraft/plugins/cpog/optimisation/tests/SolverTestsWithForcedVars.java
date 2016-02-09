/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.workcraft.plugins.cpog.optimisation.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.CpogEncoding;
import org.workcraft.plugins.cpog.optimisation.LegacyCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LegacyDefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.OneHotNumberProvider;
import org.workcraft.plugins.cpog.optimisation.Optimiser;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;


public class SolverTestsWithForcedVars
{
    String [] smallTest1 = new String []
                                       {
               "a",
};
    String [] smallTest2 = new String []
                                       {
               "a",
               "b",
};

    String [] smallTest3 = new String []
                                       {
               "a",
               "1",
};

    String [] smallTest4 = new String []
                                       {
               "0",
               "a",
};

    String [] smallTest5 = new String []
                                       {
               "0",
               "1",
               "a",
};
    String [] smallTest6 = new String []
                                       {
               "0",
               "1",
               "a",
               "A",
};
    String [] processor = new String[]
                                     {

            "1---00001100",
            "1--000111110",
            "1---01000100",
            "1---00111100",
            "1---1--01000",
            "1-0-0000110A",
            "A01-0101111A",
            "A111010111aA",
                                     };



    @Test
    public void solveProcessor_3_4_3_3_2_solvable()
    {
        testSolve(processor, 3, new int[]{4,3,3,2}, true);
    }

    @Test
    public void solveProcessor_3_4_3_3_1_solvable()
    {
        testSolve(processor, 3, new int[]{4,3,3,1}, true);
    }

    @Test
    public void solveProcessor_3_5_3_3_solvable()
    {
        testSolve(processor, 3, new int[]{5,3,3}, true);
    }

    @Test
    public void solveProcessor_3_11_solvable()
    {
        testSolve(processor, 3, 11, true);
    }

    @Test
    public void solveProcessor_3_10_unsolvable()
    {
        testSolve(processor, 3, 10, false);
    }

    @Test
    public void solveProcessor_4_4_4_solvable()
    {
        testSolve(processor, 4, new int[]{4,4}, true);
    }

    @Test
    public void solveProcessor_4_7_unsolvable()
    {
        testSolve(processor, 4, 7, false);
    }

    @Test
    public void solveProcessor_4_8_solvable()
    {
        testSolve(processor, 4, 8, true);
    }

    @Test
    public void solveProcessor_3_4_4_3_solvable()
    {
        testSolve(processor, 3, new int[]{4,4,3}, true);
    }

    @Test
    public void solveSmall1()
    {
        testSolve(smallTest1, 0, 0, true);
    }
    @Test
    public void solveSmall2_0_0_unsolvable()
    {
        testSolve(smallTest2, 0, 0, false);
    }

    @Test
    public void solveSmall2_1_2_unsolvable()
    {
        testSolve(smallTest2, 1, 2, false);
    }

    @Test
    public void solveSmall2_1_3_solvable()
    {
        testSolve(smallTest2, 1, 3, true);
    }

    @Test
    public void solveSmall3_1_1_solvable()
    {
        testSolve(smallTest3, 1, 1, true);
    }

    @Test
    public void solveSmall3_1_0_unsolvable()
    {
        testSolve(smallTest3, 1, 0, false);
    }

    @Test
    public void solveSmall4_1_0_unsolvable()
    {
        testSolve(smallTest4, 1, 0, false);
    }

    @Test
    public void solveSmall4_1_1_solvable()
    {
        testSolve(smallTest4, 1, 1, true);
    }

    @Test
    public void solveSmall5_1_4_unsolvable()
    {
        testSolve(smallTest5, 1, 4, false);
    }

    @Test
    public void solveSmall5_2_1_unsolvable()
    {
        testSolve(smallTest5, 2, 1, false);
    }


    @Test
    public void solveSmall5_2_2_solvable()
    {
        testSolve(smallTest5, 2, 2, true);
    }

    @Test
    public void solveSmall6_2_2_unsolvable()
    {
        testSolve(smallTest6, 2, 2, false);
    }
    @Test
    public void solveSmall6_2_3_solvable()
    {
        testSolve(smallTest6, 2, 3, true);
    }

    @Test
    public void solveProcessor_8_1_unsolvable()
    {
        testSolve(processor, 8, 1, false);
    }

    @Test
    public void solveProcessor_8_2_unsolvable()
    {
        testSolve(processor, 8, 2, false);
    }

    @Test
    public void solveProcessor_8_3_solvable()
    {
        testSolve(processor, 8, new int[]{2,1}, true);
    }

    @Test
    public void solveProcessor_8_4_solvable()
    {
        testSolve(processor, 8, 4, true);
    }

    @Test
    public void solveProcessor_8_5_solvable()
    {
        testSolve(processor, 8, 5, true);
    }

    @Test
    public void solveProcessor_8_6_solvable()
    {
        testSolve(processor, 8, 6, true);
    }

    @Test
    public void solveProcessor_8_7_solvable()
    {
        testSolve(processor, 8, 7, true);
    }

    @Test
    public void solveProcessor_8_8_solvable()
    {
        testSolve(processor, 8, 8, true);
    }

    @Test
    public void solveProcessor_5_5_unsolvable()
    {
        testSolve(processor, 5, 5, false);
    }

    @Test
    public void solveProcessor_5_6_solvable()
    {
        testSolve(processor, 5, new int[]{2,3,1}, true);
    }

    @Test
    public void solveProcessor_5_9_solvable()
    {
        testSolve(processor, 5, 9, true);
    }

    @Test
    public void solveProcessor_6_5_solvable()
    {
        testSolve(processor, 6, 5, true);
    }

    @Test
    public void solveProcessor_6_7_solvable()
    {
        testSolve(processor, 6, 7, true);
    }

    @Test
    public void solveProcessor_6_8_solvable()
    {
        testSolve(processor, 6, 8, true);
    }

    @Test
    public void solveProcessor_6_9_solvable()
    {
        testSolve(processor, 6, 9, true);
    }

    @Test
    public void solveProcessor_6_4_unsolvable()
    {
        testSolve(processor, 6, 4, false);
    }

    @Test
    public void solveProcessor_7_4_solvable()
    {
        testSolve(processor, 7, 4, true);
    }

    @Test
    public void solveProcessor_7_6_solvable()
    {
        testSolve(processor, 7, 6, true);
    }

    @Test
    public void solveProcessor_7_7_solvable()
    {
        testSolve(processor, 7, 7, true);
    }

    @Test
    public void solveProcessor_7_3_unsolvable()
    {
        testSolve(processor, 7, 3, false);
    }

    private void testSolve(String[] scenarios, int free, int derived, boolean solutionExists) {
        CpogEncoding solution = createSolver().solve(scenarios,free,derived);
        printSolution(solution);
        if(solutionExists)
            assertNotNull(solution);
        else
            assertNull(solution);
    }

    private void testSolve(String[] scenarios, int free, int [] levels, boolean solutionExists) {
        CpogEncoding solution = createSolver(levels).solve(scenarios,free,0);
        printSolution(solution);
        if(solutionExists)
            assertNotNull(solution);
        else
            assertNull(solution);
    }

    private LegacyCpogSolver createSolver(int[] levels) {
        return new LegacyDefaultCpogSolver<BooleanFormula>(new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider(), levels), new CleverCnfGenerator());
    }

    private LegacyCpogSolver createSolver() {
        return new LegacyDefaultCpogSolver<BooleanFormula>(new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider()), new CleverCnfGenerator());
    }

    private void printSolution(CpogEncoding solution) {
        if(solution == null)
            System.out.println("No solution.");
        else
        {
            boolean[][] encoding = solution.getEncoding();
            for(int i=0;i<encoding.length;i++)
            {
                for(int j=0;j<encoding[i].length;j++)
                    System.out.print(encoding[i][j]?1:0);
                System.out.println();
            }

            System.out.println("Functions:");
            BooleanFormula[] functions = solution.getFunctions();
            for(int i=0;i<functions.length;i++)
            {
                System.out.println(FormulaToString.toString(functions[i]));
            }
        }
    }
}
