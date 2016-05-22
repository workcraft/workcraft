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

package org.workcraft.formula.encoding.onehot;

import static org.workcraft.formula.encoding.CnfOperations.literal;
import static org.workcraft.formula.encoding.CnfOperations.not;
import static org.workcraft.formula.encoding.CnfOperations.or;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FreeVariable;
import org.workcraft.formula.Literal;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.cnf.CnfClause;
import org.workcraft.formula.encoding.twohot.TwoHotRange;
import org.workcraft.formula.encoding.twohot.TwoHotRangeProvider;
import org.workcraft.formula.sat.OptimisationTask;
import org.workcraft.formula.sat.SatProblemGenerator;

public class CnfGeneratingOptimiser implements SatProblemGenerator<Cnf> {
    private final List<CnfClause> rho = new ArrayList<>();

    TwoHotRange generateBinaryFunction(int variablesCount, int funcId) {
        TwoHotRangeProvider prov = new TwoHotRangeProvider();
        TwoHotRange result = prov.generate("f" + funcId, variablesCount);
        rho.addAll(prov.getConstraints().getClauses());
        return result;
    }

    private OneHotIntBooleanFormula generateInt(String varPrefix, int variablesCount) {
        return numberProvider.generate(varPrefix, variablesCount);
    }

    CnfGeneratingOneHotNumberProvider numberProvider;

    public CnfGeneratingOptimiser() {
        this.numberProvider = new CnfGeneratingOneHotNumberProvider();
    }

    @Override
    public OptimisationTask<Cnf> getFormula(String[] scenarios, BooleanVariable[] variables, int derivedVariables) {
        int nonDerivedVariables = variables.length;

        //Generate all possible encodings...
        Literal[][] encodings = new Literal[scenarios.length][];
        for (int i = 0; i < scenarios.length; i++) {
            encodings[i] = new Literal[nonDerivedVariables];
            if (i == 0) {
                for (int j = 0; j < nonDerivedVariables; j++) {
                    encodings[i][j] = Literal.ZERO;
                }
            } else {
                for (int j = 0; j < nonDerivedVariables; j++) {
                    encodings[i][j] = literal(new FreeVariable("x" + j + "_s" + i));
                }
            }
        }

        //... and all possible functions.
        TwoHotRange[] derivedFunctions = new TwoHotRange[derivedVariables];
        for (int i = 0; i < derivedVariables; i++) {
            derivedFunctions[i] = generateBinaryFunction(nonDerivedVariables/*+i*/ * 2, i);
        }

        orderFunctions(derivedFunctions);

        //Evaluate all functions for all scenarios.
        Literal[][] functionSpace = new Literal[scenarios.length][];
        int totalVariables = nonDerivedVariables * 2 + derivedVariables * 2;
        for (int i = 0; i < scenarios.length; i++) {
            functionSpace[i] = new Literal[totalVariables];
            for (int j = 0; j < nonDerivedVariables; j++) {
                functionSpace[i][j * 2] = encodings[i][j];
                functionSpace[i][j * 2 + 1] = not(encodings[i][j]);
            }
            for (int j = 0; j < derivedVariables; j++) {
                int jj = j + nonDerivedVariables;
                List<Literal> availableFormulas = new ArrayList<>();

                for (int k = 0; k < /*j+*/nonDerivedVariables; k++) {
                    availableFormulas.add(functionSpace[i][k * 2]);
                    availableFormulas.add(functionSpace[i][k * 2 + 1]);
                }

                Literal value = new Literal("f" + j + "_s" + i);
                selectAnd(value, derivedFunctions[j], availableFormulas.toArray(new Literal[0]));
                functionSpace[i][jj * 2] = value;
                functionSpace[i][jj * 2 + 1] = not(value);
            }
        }

        int functionCount = scenarios[0].length();

        List<CnfClause> tableConditions = new ArrayList<>();

        OneHotIntBooleanFormula[] functions = new OneHotIntBooleanFormula[functionCount];
        //Try to match existing model functions with newly generated functions.
        for (int i = 0; i < functionCount; i++) {
            OneHotIntBooleanFormula varId = generateInt("model_f" + i + "_", totalVariables);
            functions[i] = varId;
            for (int j = 0; j < scenarios.length; j++) {
                boolean inverse;
                char ch = scenarios[j].charAt(i);
                if (ch == '-') {
                    continue;
                } else if (ch == '1') {
                    inverse = false;
                } else if (ch == '0') {
                    inverse = true;
                } else throw new RuntimeException("unknown symbol: " + parseBoolean(ch));

                List<CnfClause> value = select(functionSpace[j], varId, inverse);

                tableConditions.addAll(value);
            }
        }

        List<CnfClause> numberConstraints = numberProvider.getConstraintClauses();
        tableConditions.addAll(numberConstraints);
        tableConditions.addAll(rho);

        // Forming solution output here

        BooleanFormula[] funcs = new BooleanFormula[totalVariables];
        for (int j = 0; j < nonDerivedVariables; j++) {
            funcs[j * 2] = variables[j];
            funcs[j * 2 + 1] = BooleanOperations.not(variables[j]);
        }
        for (int j = 0; j < derivedVariables; j++) {
            int jj = j + nonDerivedVariables;
            List<BooleanFormula> availableFormulas = new ArrayList<>();

            for (int k = 0; k < /*j+*/nonDerivedVariables; k++) {
                availableFormulas.add(funcs[k * 2]);
                availableFormulas.add(funcs[k * 2 + 1]);
            }
            BooleanFormula value = TwoHotRangeProvider.selectAnd(availableFormulas.toArray(new BooleanFormula[0]), derivedFunctions[j]);
            funcs[jj * 2] = value;
            funcs[jj * 2 + 1] = BooleanOperations.not(value);
        }

        BooleanFormula[] functionVars = new BooleanFormula[functionCount];
        for (int i = 0; i < functionCount; i++) {
            functionVars[i] = numberProvider.select(funcs, functions[i]);
        }
        BooleanFormula[][] enc = new BooleanFormula[encodings.length][];
        for (int i = 0; i < enc.length; i++) {
            enc[i] = new BooleanFormula[encodings[i].length];
            for (int j = 0; j < enc[i].length; j++) {
                enc[i][j] = encodings[i][j];
            }
        }

        return new OptimisationTask<Cnf>(functionVars, enc, new Cnf(tableConditions));
    }

    private void orderFunctions(TwoHotRange[] derivedFunctions) {
        if (derivedFunctions.length > 0) {
            rho.add(or(derivedFunctions[0].get(0), derivedFunctions[0].get(1)));
            rho.add(or(derivedFunctions[0].get(2), derivedFunctions[0].get(3)));
            if (derivedFunctions.length > 1) {
                //rho.add(or(derivedFunctions[1].get(0), derivedFunctions[1].get(1), derivedFunctions[1].get(2), derivedFunctions[1].get(3), derivedFunctions[1].get(4), derivedFunctions[1].get(5)));
                //rho.add(or(derivedFunctions[1].get(0), derivedFunctions[1].get(1), derivedFunctions[1].get(2), derivedFunctions[1].get(3), derivedFunctions[1].get(4), derivedFunctions[1].get(5)));
            }
        }

        for (int i = 0; i < derivedFunctions.length; i++) {
            int bits = derivedFunctions[i].size();
            for (int j = i + 1; j < derivedFunctions.length; j++) {
                int bitsj = derivedFunctions[j].size();
                if (bits != bitsj) {
                    throw new RuntimeException("Functions have different widths: " + bits + " and " + bitsj);
                }
                List<Literal> si = derivedFunctions[i].getThermometer();
                List<Literal> xj = derivedFunctions[j];
                for (int k = 0; k < bits; k++) {
                    rho.add(or(si.get(k), not(xj.get(k))));
                }
            }
        }
    }

    private Literal parseBoolean(char ch) {
        if (ch == '0') {
            return Literal.ZERO;
        } else {
            if (ch == '1') {
                return Literal.ONE;
            } else {
                throw new RuntimeException("o_O");
            }
        }
    }

    @SuppressWarnings("unused")
    private void evaluate(Literal result, AndFunction<OneHotIntBooleanFormula> function, Literal[] f) {
        Literal sel1 = new Literal(result.getVariable().getLabel() + "_sel1");
        Literal sel2 = new Literal(result.getVariable().getLabel() + "_sel2");
        OneHotIntBooleanFormula x = function.getVar1Number();
        OneHotIntBooleanFormula y = function.getVar2Number();
        List<CnfClause> sel1C = CnfGeneratingOneHotNumberProvider.select(sel1, f, x);
        List<CnfClause> sel2C = CnfGeneratingOneHotNumberProvider.select(sel2, f, y);

        rho.add(or(result, not(sel1), not(sel2)));
        rho.add(or(not(result), sel1));
        rho.add(or(not(result), sel2));
        rho.addAll(sel1C);
        rho.addAll(sel2C);
    }

    private void selectAnd(Literal result, TwoHotRange function, Literal[] f) {
        List<CnfClause> sel = TwoHotRangeProvider.selectAnd(result, f, function);
        rho.addAll(sel);
    }

    private List<CnfClause> select(Literal[] vars, OneHotIntBooleanFormula number, boolean inverse) {
        return CnfGeneratingOneHotNumberProvider.select(vars, number, inverse);
    }
}
