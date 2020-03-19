package org.workcraft.plugins.cpog.sat;

import org.workcraft.formula.*;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.plugins.cpog.formula.MemoryConservingBooleanWorker;
import org.workcraft.plugins.cpog.formula.PrettifyBooleanWorker;
import org.workcraft.plugins.cpog.encoding.NumberProvider;

import java.util.*;

public class Optimiser<T> implements SatProblemGenerator<BooleanFormula> {

    private static final BooleanWorker WORKER = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());

    private NumberProvider<T> numberProvider;
    private int[] levels;

    private BooleanFormula generateBinaryFunction(BooleanFormula[] vars, int funcId) {
        return generateBinaryFunction(vars, vars, funcId);
    }

    private BooleanFormula generateBinaryFunction(BooleanFormula[] arg1, BooleanFormula[] arg2, int funcId) {
        BooleanFormula isIff = Zero.getInstance(); //*/new FV("f" + funcId + "_isIff");
        T var1Number = generateInt("f" + funcId + "_v1_", arg1.length);
        T var2Number = generateInt("f" + funcId + "_v2_", arg2.length);

        BooleanFormula noNegate1 = new FreeVariable("f" + funcId + "_v1_plain");
        BooleanFormula noNegate2 = new FreeVariable("f" + funcId + "_v2_plain");
        BooleanFormula var1 = numberProvider.select(arg1, var1Number);
        BooleanFormula var2 = numberProvider.select(arg2, var2Number);

        BooleanFormula and = WORKER.and(
                WORKER.iff(var1, noNegate1),
                WORKER.iff(var2, noNegate2)
                );

        BooleanFormula iff = WORKER.iff(var1, var2);

        return WORKER.or(
                WORKER.and(isIff, iff),
                WORKER.and(WORKER.not(isIff), and)
        );
    }

    private T generateInt(String varPrefix, int variablesCount) {
        return numberProvider.generate(varPrefix, variablesCount);
    }

    public Optimiser(NumberProvider<T> numberProvider) {
        this.numberProvider = numberProvider;
    }

    /**
     * @param levels
     * Specifies the number of gates to be found on each depth level. Null means no limit.
     */
    public Optimiser(NumberProvider<T> numberProvider, int[] levels) {
        this(numberProvider);
        this.levels = levels;
    }

    @Override
    public OptimisationTask<BooleanFormula> getFormula(String[] scenarios, BooleanVariable[] variables, int derivedVariables) {
        Map<Character, BooleanVariable> forcedVariables = new HashMap<>();

        BooleanFormula[][] parsedMatrix = new BooleanFormula[scenarios.length][];

        for (int i = 0; i < scenarios.length; i++) {
            String s = scenarios[i];
            parsedMatrix[i] = new BooleanFormula[s.length()];
            for (int j = 0; j < s.length(); j++) {
                Character c = s.charAt(j);
                BooleanFormula cell;
                if (c == '1') {
                    cell = One.getInstance();
                } else {
                    if (c == '0') {
                        cell = Zero.getInstance();
                    } else {
                        if (c == '-') {
                            cell = null;
                        } else {
                            boolean upper = false;
                            if (c >= 'A' && c <= 'Z') {
                                upper = true;
                                c = Character.toLowerCase(c);
                            }

                            if (c >= 'a' && c <= 'z') {
                                cell = forcedVariables.get(c);
                                if (cell == null) {
                                    BooleanVariable var = new FreeVariable(c.toString());
                                    forcedVariables.put(c, var);
                                    cell = var;
                                }
                                if (upper) {
                                    cell = WORKER.not(cell);
                                }
                            } else {
                                throw new RuntimeException("unknown character: " + c);
                            }
                        }
                    }
                }
                parsedMatrix[i][j] = cell;
            }
        }

        OptimisationTask<BooleanFormula> preResult = getFormula(parsedMatrix, new ArrayList<BooleanVariable>(forcedVariables.values()), variables, derivedVariables);

        BooleanFormula[][] vars = preResult.getEncodingVars();
        BooleanFormula[] funcs = preResult.getFunctionVars();
        BooleanFormula result = preResult.getTask();

        for (BooleanVariable v : forcedVariables.values()) {
            result = eliminateUnrestrictableVar(result, v);
        }

        return new OptimisationTask<BooleanFormula>(funcs, vars, result);
    }

    private BooleanFormula eliminateUnrestrictableVar(BooleanFormula result, BooleanVariable v) {
        //System.out.println("original: " + FormulaToString.toString(result));

        BooleanFormula one = replace(result, v, One.getInstance());
        BooleanFormula zero = replace(result, v, Zero.getInstance());
        //System.out.println("one: " + FormulaToString.toString(one));
        //System.out.println("zero: " + FormulaToString.toString(zero));
        return WORKER.and(one, zero);
    }

    private BooleanFormula replace(BooleanFormula where, BooleanVariable what, BooleanFormula with) {
        return FormulaUtils.replace(where, what, with, WORKER);
    }

    /**
     * Produces the SAT problem for finding the optimal encoding given the scenarios.
     * @param scenarios
     * @param forcedParams
     * List of forced input signals.
     * @param variables
     * Number of input signals used to encode scenarios
     * @param derivedVariables
     * Number of gates in the decoder. Ignored if levels != null. :(
     * @return
     * Boolean formula to satisfy along with the formulas for all output signals.
     */
    public OptimisationTask<BooleanFormula> getFormula(BooleanFormula[][] scenarios, List<? extends BooleanFormula> forcedParams, BooleanVariable[] variables, int derivedVariables) {
        // Generate function parameters

        List<BooleanFormula> parameters = new ArrayList<>(Arrays.asList(variables));
        parameters.addAll(forcedParams);

        // Generate functions
        List<BooleanFormula> allVariables = generateFunctions(parameters, derivedVariables);

        int functionCount = scenarios[0].length;

        BooleanFormula[] functions = new BooleanFormula[functionCount];

        List<BooleanFormula> tableConditions = new ArrayList<>();
        //Try to match existing model functions with newly generated functions.
        for (int i = 0; i < functionCount; i++) {
            T varId = generateInt("model_f" + i + "_", allVariables.size());
            BooleanFormula plain = new FreeVariable("model_f" + i + "_plain");

            BooleanFormula value = WORKER.iff(plain, numberProvider.select(allVariables.toArray(new BooleanFormula[0]), varId));

            functions[i] = value;
        }

        //Generate all possible encodings...
        BooleanFormula[][] encodings = new BooleanFormula[scenarios.length][];
        for (int i = 0; i < scenarios.length; i++) {
            encodings[i] = new BooleanFormula[variables.length];
            if (i == 0) {
                for (int j = 0; j < variables.length; j++) {
                    encodings[i][j] = Zero.getInstance();
                }
                for (int j = 0; j < 0 && j < variables.length; j++) {
                    encodings[i][j] = new FreeVariable("x" + j + "_s" + i);
                }
            } else {
                for (int j = 0; j < variables.length; j++) {
                    encodings[i][j] = new FreeVariable("x" + j + "_s" + i);
                }
            }
        }

        //Verify results
        for (int i = 0; i < functionCount; i++) {
            BooleanFormula value = functions[i];
            for (int j = 0; j < scenarios.length; j++) {
                BooleanFormula substituted = FormulaUtils.replace(value,
                        Arrays.asList(variables), Arrays.asList(encodings[j]), WORKER);

                BooleanFormula required = scenarios[j][i];
                if (required != null) {
                    tableConditions.add(WORKER.iff(required, substituted));
                }
            }
        }

        tableConditions.add(numberProvider.getConstraints());

        return new OptimisationTask<BooleanFormula>(functions, encodings, FormulaUtils.createAnd(tableConditions, WORKER));
    }

    /**
     *
     * @param parameters
     * @param functionCount
     * Specifies the number of gates to look for. Ignored if levels != null. Bad design, sure.
     * @return
     */
    private List<BooleanFormula> generateFunctions(List<BooleanFormula> parameters, int functionCount) {
        List<BooleanFormula> allVariables = new ArrayList<>(parameters);
        List<BooleanFormula> lastLevel = new ArrayList<>(parameters);

        if (levels != null) {
            if (levels.length == 1 && levels[0] == -1) {
                for (int i = 0; i < parameters.size(); i++) {
                    for (int j = i + 1; j < parameters.size(); j++) {
                        BooleanFormula param1 = parameters.get(i);
                        BooleanFormula param2 = parameters.get(j);
                        for (int p = 0; p < 2; p++) {
                            for (int q = 0; q < 2; q++) {
                                BooleanFormula arg1 = p != 0 ? WORKER.not(param1) : param1;
                                BooleanFormula arg2 = q != 0 ? WORKER.not(param2) : param2;
                                allVariables.add(WORKER.and(arg1, arg2));
                            }
                        }
                        allVariables.add(WORKER.iff(param1, param2));
                    }
                }
            } else {
                int cc = 0;
                for (int level = 0; level < levels.length; level++) {
                    if (levels[level] == 0) {
                        throw new RuntimeException("wtf?");
                    }
                    List<BooleanFormula> currentLevel = new ArrayList<>();
                    for (int i = 0; i < levels[level]; i++) {
                        BooleanFormula[] firstArgPool;
                        int firstArgPoolSize = i * 2 + 1;
                        if (firstArgPoolSize > lastLevel.size()) {
                            firstArgPool = lastLevel.toArray(new BooleanFormula[0]);
                        } else {
                            firstArgPool = new BooleanFormula[firstArgPoolSize];
                            for (int k = 0; k < firstArgPoolSize; k++) {
                                firstArgPool[k] = lastLevel.get(k);
                            }
                        }
                        BooleanFormula function = generateBinaryFunction(firstArgPool, allVariables.toArray(new BooleanFormula[0]), cc);
                        cc++;
                        currentLevel.add(function);
                    }
                    allVariables.addAll(currentLevel);
                    lastLevel = currentLevel;
                }
            }
        } else {
            //Generate all possible functions.
            for (int i = 0; i < functionCount; i++) {
                BooleanFormula function = generateBinaryFunction(allVariables.toArray(new BooleanFormula[0]), i);
                allVariables.add(function);
            }
        }
        return allVariables;
    }

}
