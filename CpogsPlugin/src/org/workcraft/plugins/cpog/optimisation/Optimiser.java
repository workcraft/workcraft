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

package org.workcraft.plugins.cpog.optimisation;

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.ONE;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.ZERO;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.and;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.iff;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.not;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.or;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanUtils;

public class Optimiser<BooleanNumber> implements CpogSATProblemGenerator<BooleanFormula>
{
    BooleanFormula generateBinaryFunction(BooleanFormula[] vars, int funcId)
    {
        return generateBinaryFunction(vars, vars, funcId);
    }

    BooleanFormula generateBinaryFunction(BooleanFormula[] arg1, BooleanFormula[] arg2, int funcId)
    {
        BooleanFormula isIff = ZERO;//*/new FV("f"+funcId + "_isIff");
        BooleanNumber var1Number = generateInt("f"+funcId + "_v1_", arg1.length);
        BooleanNumber var2Number = generateInt("f"+funcId + "_v2_", arg2.length);
        //BooleanFormula less = numberProvider.less(var1Number, var2Number);
        //rho.add(less);
        BooleanFormula noNegate1 = new FreeVariable("f"+funcId + "_v1_plain");
        BooleanFormula noNegate2 = new FreeVariable("f"+funcId + "_v2_plain");
        BooleanFormula var1 = numberProvider.select(arg1, var1Number);
        BooleanFormula var2 = numberProvider.select(arg2, var2Number);

        //noNegate1 = ZERO;
        //noNegate1 = ZERO;
        /*if(funcId == 0)
        {
            var1 = arg1[0];
            var2 = arg2[1];
            //noNegate1 = ZERO;
            //noNegate2 = ZERO;
        }

        if(funcId == 1)
        {
            var1 = arg1[1];
            var2 = arg2[2];
            //noNegate1 = ZERO;
        }
        if(funcId == 2)
        {
            var1 = arg1[2];
            var2 = arg2[3];
            //noNegate1 = ZERO;
        }
        if(funcId == 3)
        {
            var1 = arg1[3];
            var2 = arg2[0];
            //noNegate1 = ZERO;
        }
        if(funcId == 4)
        {
            var1 = arg1[2];
            var2 = arg2[0];
            //noNegate1 = ZERO;
        }*/
        //noNegate1 = ZERO;
        //noNegate2 = ZERO;

        BooleanFormula and = and(
                iff(var1, noNegate1),
                iff(var2, noNegate2)
                );

        //if(true)
        //    return not(and(var1,var2));
        BooleanFormula iff = iff(var1, var2);

        return or(
            and(isIff, iff),
            and(not(isIff), and)
        );
    }

    private BooleanNumber generateInt(String varPrefix, int variablesCount) {
        return numberProvider.generate(varPrefix, variablesCount);
    }

    NumberProvider<BooleanNumber> numberProvider;
    private int[] levels;

    public Optimiser(NumberProvider<BooleanNumber> numberProvider)
    {
        this.numberProvider = numberProvider;
    }

    /**
     * @param levels
     * Specifies the number of gates to be found on each depth level. Null means no limit.
     */
    public Optimiser(NumberProvider<BooleanNumber> numberProvider, int [] levels)
    {
        this(numberProvider);
        this.levels = levels;
    }

    @Override
    public CpogOptimisationTask<BooleanFormula> getFormula(String [] scenarios, BooleanVariable [] variables, int derivedVariables)
    {
        Map<Character, BooleanVariable> forcedVariables = new HashMap<Character, BooleanVariable>();

        BooleanFormula[][] parsedMatrix = new BooleanFormula[scenarios.length][];

        for(int i=0;i<scenarios.length;i++)
        {
            String s = scenarios[i];
            parsedMatrix[i] = new BooleanFormula[s.length()];
            for(int j=0;j<s.length();j++)
            {
                Character c = s.charAt(j);
                BooleanFormula cell;
                if(c == '1')
                    cell = ONE;
                else
                    if(c == '0')
                        cell = ZERO;
                    else
                        if(c == '-')
                            cell = null;
                        else
                        {
                            boolean upper = false;
                            if(c>='A' && c<='Z')
                            {
                                upper = true;
                                c = Character.toLowerCase(c);
                            }

                            if(c>='a' && c<='z')
                            {
                                cell = forcedVariables.get(c);
                                if(cell == null)
                                {
                                    BooleanVariable var = new FreeVariable(c.toString());
                                    forcedVariables.put(c, var);
                                    cell = var;
                                }
                                if(upper)
                                    cell = not(cell);
                            }
                            else
                                throw new RuntimeException("unknown character: " + c);
                        }
                parsedMatrix[i][j] = cell;
            }
        }


        CpogOptimisationTask<BooleanFormula> preResult = getFormula(parsedMatrix, new ArrayList<BooleanVariable>(forcedVariables.values()), variables, derivedVariables);

        BooleanFormula[][] vars = preResult.getEncodingVars();
        BooleanFormula[] funcs = preResult.getFunctionVars();
        BooleanFormula result = preResult.getTask();

        for(BooleanVariable v : forcedVariables.values())
            result = eliminateUnrestrictableVar(result, v);

        return new CpogOptimisationTask<BooleanFormula>(funcs, vars, result);
    }

    private BooleanFormula eliminateUnrestrictableVar(BooleanFormula result, BooleanVariable v) {
        //System.out.println("original: " + FormulaToString.toString(result));

        BooleanFormula one = replace(result, v, ONE);
        BooleanFormula zero = replace(result, v, ZERO);
        //System.out.println("one: " + FormulaToString.toString(one));
        //System.out.println("zero: " + FormulaToString.toString(zero));
        return and(one, zero);
    }

    private BooleanFormula replace(BooleanFormula where, BooleanVariable what, BooleanFormula with)
    {
        return BooleanUtils.prettifyReplace(where, what, with);
    }

    /**
     * Produces the SAT problem for finding the optimal CPOG encoding given the scenarios.
     * @param scenarios
     * @param forcedParams
     * List of forced input signals.
     * @param variables
     * Number of input signals used to encode scenarios
     * @param derivedVariables
     * Number of gates in the decoder. Ignored if levels!=null. :(
     * @return
     * Boolean formula to satisfy along with the formulas for all output signals.
     */
    public CpogOptimisationTask<BooleanFormula> getFormula(BooleanFormula [][] scenarios, List<? extends BooleanFormula> forcedParams, BooleanVariable[] variables, int derivedVariables)
    {
        // Generate function parameters

        List<BooleanFormula> parameters = new ArrayList<BooleanFormula>(Arrays.asList(variables));
        parameters.addAll(forcedParams);

        // Generate functions
        List<BooleanFormula> allVariables = generateFunctions(parameters, derivedVariables);

        int functionCount = scenarios[0].length;

        BooleanFormula[] cpogFunctions = new BooleanFormula[functionCount];

        List<BooleanFormula> tableConditions = new ArrayList<BooleanFormula>();
        //Try to match CPOG functions with generated functions.
        for(int i=0;i<functionCount;i++)
        {
            BooleanNumber varId = generateInt("cpog_f"+i+"_",allVariables.size());
            BooleanFormula plain = new FreeVariable("cpog_f"+i+"_plain");

            BooleanFormula value = iff(plain, numberProvider.select(allVariables.toArray(new BooleanFormula[0]), varId));

            cpogFunctions[i] = value;
        }

        //Generate all possible encodings...
        BooleanFormula [][] encodings = new BooleanFormula [scenarios.length][];
        for(int i=0;i<scenarios.length;i++)
        {
            encodings[i] = new BooleanFormula[variables.length];
            if(i == 0)
            {
                for(int j=0;j<variables.length;j++)
                    encodings[i][j] = ZERO;
                for(int j=0;j<0 && j<variables.length;j++)
                    encodings[i][j] = new FreeVariable("x"+j+"_s"+i);
            }
            else
                for(int j=0;j<variables.length;j++)
                    encodings[i][j] = new FreeVariable("x"+j+"_s"+i);
        }

        //Verify results
        for(int i=0;i<functionCount;i++)
        {
            BooleanFormula value = cpogFunctions[i];
            for(int j=0;j<scenarios.length;j++)
            {
                BooleanFormula substituted = BooleanUtils.prettifyReplace(value, Arrays.asList(variables), Arrays.asList(encodings[j]));

                BooleanFormula required = scenarios[j][i];
                if(required != null) {
                    tableConditions.add(iff(required, substituted));
                }
            }
        }

        tableConditions.add(numberProvider.getConstraints());

        return new CpogOptimisationTask<BooleanFormula>(cpogFunctions, encodings, and(tableConditions));
    }

    /**
     *
     * @param parameters
     * @param functionCount
     * Specifies the number of gates to look for. Ignored if levels != null. Bad design, sure.
     * @return
     */
    private List<BooleanFormula> generateFunctions(List<BooleanFormula> parameters, int functionCount)
    {
        List<BooleanFormula> allVariables = new ArrayList<BooleanFormula>(parameters);
        List<BooleanFormula> lastLevel = new ArrayList<BooleanFormula>(parameters);

        if(levels != null)
        {
            if(levels.length == 1 && levels[0]==-1)
            {
                for(int i=0;i<parameters.size();i++)
                    for(int j=i+1;j<parameters.size();j++)
                    {
                        BooleanFormula param1 = parameters.get(i);
                        BooleanFormula param2 = parameters.get(j);
                        for(int p=0;p<2;p++)
                            for(int q=0;q<2;q++)
                            {
                                BooleanFormula arg1 = p!=0 ? not(param1) : param1;
                                BooleanFormula arg2 = q!=0 ? not(param2) : param2;
                                allVariables.add(and(arg1, arg2));
                            }
                        allVariables.add(iff(param1, param2));
                    }
            }
            else
            {
                int cc = 0;
                for(int level = 0;level<levels.length;level++)
                {
                    List<BooleanFormula> currentLevel = new ArrayList<BooleanFormula>();
                    if(levels[level] == 0)
                        throw new RuntimeException("wtf?");
                    for(int i=0;i<levels[level];i++)
                    {
                        BooleanFormula[] firstArgPool;
                        int firstArgPoolSize = i*2+1;
                        if(firstArgPoolSize > lastLevel.size())
                            firstArgPool = lastLevel.toArray(new BooleanFormula [0]);
                        else
                        {
                            firstArgPool = new BooleanFormula[firstArgPoolSize];
                            for(int k=0;k<firstArgPoolSize;k++)
                                firstArgPool[k] = lastLevel.get(k);
                        }
                        BooleanFormula function = generateBinaryFunction(firstArgPool, allVariables.toArray(new BooleanFormula [0]), cc);
                        cc++;
                        currentLevel.add(function);
                    }
                    allVariables.addAll(currentLevel);
                    lastLevel = currentLevel;
                }
            }
        }
        else
        {
            //Generate all possible functions.
            for(int i=0;i<functionCount;i++)
            {
                BooleanFormula function = generateBinaryFunction(allVariables.toArray(new BooleanFormula [0]), i);
                allVariables.add(function);
            }
        }
        return allVariables;
    }

    public static <BooleanNumber> BooleanFormula evaluateNoNeg(NumberProvider<OneHotIntBooleanFormula> numberProvider, AndFunction<OneHotIntBooleanFormula> function, BooleanFormula[] vars) {
        return and(
                numberProvider.select(vars, function.getVar1Number()),
                numberProvider.select(vars, function.getVar2Number())
                );
    }
}
