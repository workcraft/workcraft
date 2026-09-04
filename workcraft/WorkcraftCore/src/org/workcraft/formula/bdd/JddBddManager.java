package org.workcraft.formula.bdd;

import jdd.bdd.BDD;
import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.types.Func2;
import org.workcraft.utils.SetUtils;

import java.util.*;

public class JddBddManager implements BddManager {
    private final Backend backend = new Backend();
    private final Map<BooleanVariable, Integer> varToBddMap = new HashMap<>();
    private final Map<Integer, BooleanVariable> indexToVarMap = new HashMap<>();
    private final Map<Integer, BooleanFormula> bddToDistinctFormulaMap = new HashMap<>();
    private final boolean enableCloseStats;

    // Base class should be BDD in mission mode, or DebugBDD for debugging
    static final class Backend extends BDD {
        private Backend() {
            super(1000, 1000);
        }

        public int calcReferencedNodeCount() {
            // First, garbage-collect dereferenced nodes
            gc();
            // Then, calculate the number of nodes that are still referenced, as follows:
            return debug_table_size() - 2               // Total number of allocatable nodes minus two nodes for 0/1
                    - debug_compute_free_nodes_count()  // minus free nodes that where not previously allocated
                    - 2 * numberOfVariables();          // minus two nodes per variable, positive and negative
        }
    }

    private final class BddGenerator implements BooleanVisitor<Integer> {
        @Override
        public Integer visit(Zero node) {
            return getZeroBdd();
        }

        @Override
        public Integer visit(One node) {
            return getOneBdd();
        }

        @Override
        public Integer visit(BooleanVariable node) {
            return refVariableBdd(node);
        }

        @Override
        public Integer visit(Not node) {
            int bdd = node.getX().accept(this);
            int result = refBdd(backend.not(bdd));
            derefBdd(bdd);
            return result;
        }

        @Override
        public Integer visit(And node) {
            return visitBinaryOperator(node, backend::and);
        }

        @Override
        public Integer visit(Or node) {
            return visitBinaryOperator(node, backend::or);
        }

        @Override
        public Integer visit(Iff node) {
            return visitBinaryOperator(node, backend::biimp);
        }

        @Override
        public Integer visit(Xor node) {
            return visitBinaryOperator(node, backend::xor);
        }

        @Override
        public Integer visit(Imply node) {
            return visitBinaryOperator(node, backend::imp);
        }

        public Integer visitBinaryOperator(BinaryBooleanFormula node, Func2<Integer, Integer, Integer> func) {
            int leftBdd = node.getX().accept(this);
            int rightBdd = node.getY().accept(this);
            int result = refBdd(func.eval(leftBdd, rightBdd));
            derefBdd(leftBdd);
            derefBdd(rightBdd);
            return result;
        }
    }

    public JddBddManager() {
        this(false);
    }

    public JddBddManager(boolean enableCloseStats) {
        this.enableCloseStats = enableCloseStats;
    }

    @Override
    public void close() {
        if (enableCloseStats) {
            printStats("Jdd stats on closing");
        }
        backend.cleanup();
    }

    private int refBdd(int bdd) {
        return backend.ref(bdd);
    }

    private void derefBdd(int bdd) {
        backend.deref(bdd);
    }

    private int getOneBdd() {
        return backend.getOne();
    }

    private int getZeroBdd() {
        return backend.getZero();
    }

    private int refVariableBdd(BooleanVariable var) {
        Integer bdd = varToBddMap.get(var);
        if (bdd == null) {
            bdd = backend.createVar();
            varToBddMap.put(var, bdd);
        }
        int index = backend.getVar(bdd);
        indexToVarMap.put(index, var);
        return refBdd(bdd);
    }

    private int refFormulaBdd(BooleanFormula formula) {
        return formula.accept(new BddGenerator());
    }

    private BooleanFormula buildFormula(int bdd) {
        return buildFormulaRec(bdd, new HashMap<>(), Zero.getInstance());
    }

    private BooleanFormula buildFormulaRec(int bdd, Map<BooleanVariable, Boolean> assignments, BooleanFormula formula) {
        if (bdd == getOneBdd()) {
            BooleanFormula cube = One.getInstance();
            for (BooleanVariable var : assignments.keySet()) {
                BooleanFormula literal = assignments.get(var) ? var : new Not(var);
                cube = One.getInstance().equals(cube) ? literal : new And(cube, literal);
            }
            formula = Zero.getInstance().equals(formula) ? cube : new Or(formula, cube);
        } else if (bdd != getZeroBdd()) {
            int varIndex = backend.getVar(bdd);
            BooleanVariable var = indexToVarMap.get(varIndex);
            if (var != null) {
                assignments.put(var, true);
                int thenBdd = backend.getHigh(bdd);
                formula = buildFormulaRec(thenBdd, assignments, formula);

                assignments.put(var, false);
                int elseBdd = backend.getLow(bdd);
                formula = buildFormulaRec(elseBdd, assignments, formula);

                assignments.remove(var);
            }
        }
        return formula;
    }

    @Override
    public boolean isEquivalent(BooleanFormula leftFormula, BooleanFormula rightFormula) {
        if (leftFormula == rightFormula) {
            return true;
        }
        if ((leftFormula == null) || (rightFormula == null)) {
            return false;
        }
        int leftBdd = refFormulaBdd(leftFormula);
        int rightBdd = refFormulaBdd(rightFormula);
        boolean result = leftBdd == rightBdd;
        derefBdd(leftBdd);
        derefBdd(rightBdd);
        return result;
    }

    @Override
    public boolean isEquivalentToConstant(BooleanFormula funcFormula) {
        if (funcFormula == null) {
            return false;
        }
        if ((funcFormula == Zero.getInstance()) || (funcFormula == One.getInstance())) {
            return true;
        }
        int funcBdd = refFormulaBdd(funcFormula);
        boolean result = (funcBdd == getZeroBdd()) || (funcBdd == getOneBdd());
        derefBdd(funcBdd);
        return result;
    }

    @Override
    public boolean isEquivalentToConstant0(BooleanFormula funcFormula) {
        if (funcFormula == null) {
            return false;
        }
        if (funcFormula == Zero.getInstance()) {
            return true;
        }
        int funcBdd = refFormulaBdd(funcFormula);
        boolean result = funcBdd == getZeroBdd();
        derefBdd(funcBdd);
        return result;
    }

    @Override
    public boolean isEquivalentToConstant1(BooleanFormula funcFormula) {
        if (funcFormula == null) {
            return false;
        }
        if (funcFormula == One.getInstance()) {
            return true;
        }
        int formulaBdd = refFormulaBdd(funcFormula);
        boolean result = formulaBdd == getOneBdd();
        derefBdd(formulaBdd);
        return result;
    }

    @Override
    public boolean isBinate(BooleanFormula funcFormula, BooleanVariable var) {
        return !isPositiveUnate(funcFormula, var) && !isNegativeUnate(funcFormula, var);
    }

    @Override
    public boolean isPositiveUnate(BooleanFormula funcFormula, BooleanVariable var) {
        BooleanFormula var0Formula = FormulaUtils.replaceZero(funcFormula, var);
        BooleanFormula var1Formula = FormulaUtils.replaceOne(funcFormula, var);
        return implies(var0Formula, var1Formula);
    }

    @Override
    public boolean isNegativeUnate(BooleanFormula funcFormula, BooleanVariable var) {
        BooleanFormula var0Formula = FormulaUtils.replaceZero(funcFormula, var);
        BooleanFormula var1Formula = FormulaUtils.replaceOne(funcFormula, var);
        return implies(var1Formula, var0Formula);
    }

    private boolean implies(BooleanFormula leftFormula, BooleanFormula rightFormula) {
        int leftBdd = refFormulaBdd(leftFormula);
        int rightBdd = refFormulaBdd(rightFormula);
        int notRightBdd = refBdd(backend.not(rightBdd));
        derefBdd(rightBdd);

        Set<BooleanVariable> vars = new HashSet<>();
        vars.addAll(FormulaUtils.extractOrderedVariables(leftFormula));
        vars.addAll(FormulaUtils.extractOrderedVariables(rightFormula));
        int cubeBdd = buildCubeBdd(vars);

        boolean result = backend.relProd(leftBdd, notRightBdd, cubeBdd) == getZeroBdd();
        derefBdd(leftBdd);
        derefBdd(notRightBdd);
        derefBdd(cubeBdd);
        return result;
    }

    private int buildCubeBdd(Collection<BooleanVariable> vars) {
        int cubeBdd = getOneBdd();
        for (BooleanVariable var : vars) {
            int oldCubeBdd = cubeBdd;
            int varBdd = refVariableBdd(var);
            cubeBdd = refBdd(backend.and(oldCubeBdd, varBdd));
            derefBdd(oldCubeBdd);
        }
        return cubeBdd;
    }

    @Override
    public Set<BooleanVariable> calcRedundantVariables(BooleanFormula formula) {
        Set<BooleanVariable> allVariables = FormulaUtils.extractVariables(formula);
        Set<BooleanVariable> usefulVariables = FormulaUtils.extractVariables(removeRedundantVariables(formula));
        return SetUtils.difference(allVariables, usefulVariables);
    }

    private BooleanFormula removeRedundantVariables(BooleanFormula formula) {
        for (BooleanVariable var : FormulaUtils.extractVariables(formula)) {
            BooleanFormula var0Formula = FormulaUtils.replace(formula, var, Zero.getInstance(),
                    CleverBooleanWorker.getInstance());

            if (isEquivalent(formula, var0Formula)) {
                formula = var0Formula;
            }
        }
        return formula;
    }

    @Override
    public BooleanFormula calcDistinctFormula(BooleanFormula formula) {
        BooleanFormula distinctFormula = removeRedundantVariables(formula);
        int bdd = refFormulaBdd(distinctFormula);
        return bddToDistinctFormulaMap.computeIfAbsent(bdd, ignored -> distinctFormula);
    }

    @Override
    public BooleanFormula calcProjectionFormula(BooleanFormula funcFormula, BooleanFormula caresetFormula) {
        int funcBdd = refFormulaBdd(funcFormula);
        int caresetBdd = refFormulaBdd(caresetFormula);
        int projectionBdd = backend.restrict(funcBdd, caresetBdd);
        derefBdd(funcBdd);
        derefBdd(caresetBdd);
        BooleanFormula result = buildFormula(projectionBdd);
        derefBdd(projectionBdd);
        return result;
    }

    @Override
    public Map<String, Integer> getStats() {
        Map<String, Integer> result = new HashMap<>();
        result.put(REFERENCED_BDD_COUNT_KEY, backend.calcReferencedNodeCount());
        result.put(VARIABLE_COUNT_KEY, backend.numberOfVariables());
        return result;
    }

}
