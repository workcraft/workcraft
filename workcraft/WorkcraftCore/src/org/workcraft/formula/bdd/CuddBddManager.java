package org.workcraft.formula.bdd;

import info.scce.addlib.cudd.Cudd;
import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.types.TwoWayMap;
import org.workcraft.utils.SetUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CuddBddManager implements BddManager {

    private final long backend = Cudd.Cudd_Init(0, 0, Cudd.CUDD_UNIQUE_SLOTS, Cudd.CUDD_CACHE_SLOTS, 0);
    private final TwoWayMap<BooleanVariable, Long> varToBddMap = new TwoWayMap<>();
    private final Map<Long, BooleanFormula> bddToDistinctFormulaMap = new HashMap<>();
    private final boolean enableCloseStats;

    private final class BddGenerator implements BooleanVisitor<Long> {
        interface BinaryOperator {
            long apply(long dd, long f, long g);
        }

        @Override
        public Long visit(Zero node) {
            return getZeroBdd();
        }

        @Override
        public Long visit(One node) {
            return getOneBdd();
        }

        @Override
        public Long visit(BooleanVariable node) {
            return refVariableBdd(node);
        }

        @Override
        public Long visit(Not node) {
            long bdd = node.getX().accept(this);
            long resultBdd = refBdd(Cudd.Cudd_Not(bdd));
            derefBdd(bdd);
            return resultBdd;
        }

        @Override
        public Long visit(And node) {
            return visitBinaryOperator(node, Cudd::Cudd_bddAnd);
        }

        @Override
        public Long visit(Or node) {
            return visitBinaryOperator(node, Cudd::Cudd_bddOr);
        }

        @Override
        public Long visit(Iff node) {
            return visitBinaryOperator(node, Cudd::Cudd_bddXnor);
        }

        @Override
        public Long visit(Xor node) {
            return visitBinaryOperator(node, Cudd::Cudd_bddXor);
        }

        @Override
        public Long visit(Imply node) {
            long leftBdd = node.getX().accept(this);
            long notLeftBdd = refBdd(Cudd.Cudd_Not(leftBdd));
            derefBdd(leftBdd);
            long rightBdd = node.getY().accept(this);
            long resultBdd = refBdd(Cudd.Cudd_bddOr(backend, notLeftBdd, rightBdd));
            derefBdd(notLeftBdd);
            derefBdd(rightBdd);
            return resultBdd;
        }

        public Long visitBinaryOperator(BinaryBooleanFormula node, BinaryOperator func) {
            long leftBdd = node.getX().accept(this);
            long rightBdd = node.getY().accept(this);
            long resultBdd = refBdd(func.apply(backend, leftBdd, rightBdd));
            derefBdd(leftBdd);
            derefBdd(rightBdd);
            return resultBdd;
        }
    }

    public CuddBddManager() {
        this(false);
    }

    public CuddBddManager(boolean enableCloseStats) {
        this.enableCloseStats = enableCloseStats;
    }

    @Override
    public void close() {
        if  (enableCloseStats) {
            printStats("Cudd stats on closing");
        }
        Cudd.Cudd_Quit(backend);
    }

    private long refBdd(long bdd) {
        Cudd.Cudd_Ref(bdd);
        return bdd;
    }

    private void derefBdd(long bdd) {
        Cudd.Cudd_RecursiveDeref(backend, bdd);
    }

    private long getZeroBdd() {
        return Cudd.Cudd_ReadLogicZero(backend);
    }

    private long getOneBdd() {
        return Cudd.Cudd_ReadOne(backend);
    }

    private long refVariableBdd(BooleanVariable var) {
        Long bdd = varToBddMap.getValue(var);
        if (bdd == null) {
            bdd = Cudd.Cudd_bddNewVar(backend);
            varToBddMap.put(var, bdd);
        }
        return refBdd(bdd);
    }

    private long refFormulaBdd(BooleanFormula formula) {
        return formula.accept(new BddGenerator());
    }

    private BooleanFormula buildFormula(long bdd) {
        return buildFormulaRec(bdd, new HashMap<>(), Zero.getInstance());
    }

    private BooleanFormula buildFormulaRec(long bdd, Map<BooleanVariable, Boolean> assignments, BooleanFormula formula) {
        boolean isComplement = (Cudd.Cudd_IsComplement(bdd) != 0);
        boolean isConstantBdd = (Cudd.Cudd_IsConstant(bdd) != 0);
        if (isConstantBdd) {
            if (!isComplement) {
                BooleanFormula cube = One.getInstance();
                for (BooleanVariable var : assignments.keySet()) {
                    BooleanFormula literal = assignments.get(var) ? var : new Not(var);
                    cube = One.getInstance().equals(cube) ? literal : new And(cube, literal);
                }
                formula = Zero.getInstance().equals(formula) ? cube : new Or(formula, cube);
            }
        } else {
            int varIndex = Cudd.Cudd_NodeReadIndex(bdd);
            long varBdd = Cudd.Cudd_bddIthVar(backend, varIndex);
            BooleanVariable var = varToBddMap.getKey(varBdd);

            assignments.put(var, true);
            long thenBdd = isComplement ? Cudd.Cudd_Not(Cudd.Cudd_T(bdd)) : Cudd.Cudd_T(bdd);
            formula = buildFormulaRec(thenBdd, assignments, formula);

            assignments.put(var, false);
            long elseBdd = isComplement ? Cudd.Cudd_Not(Cudd.Cudd_E(bdd)) : Cudd.Cudd_E(bdd);
            formula = buildFormulaRec(elseBdd, assignments, formula);

            assignments.remove(var);
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
        long leftBdd = refFormulaBdd(leftFormula);
        long rightBdd = refFormulaBdd(rightFormula);
        derefBdd(leftBdd);
        derefBdd(rightBdd);
        return (leftBdd == rightBdd);
    }

    @Override
    public boolean isEquivalentToConstant(BooleanFormula funcFormula) {
        if (funcFormula == null) {
            return false;
        }
        if ((funcFormula == Zero.getInstance()) || (funcFormula == One.getInstance())) {
            return true;
        }
        long funcBdd = refFormulaBdd(funcFormula);
        derefBdd(funcBdd);
        return (funcBdd == getZeroBdd()) || (funcBdd == getOneBdd());
    }

    @Override
    public boolean isEquivalentToConstant0(BooleanFormula funcFormula) {
        if (funcFormula == null) {
            return false;
        }
        if (funcFormula == Zero.getInstance()) {
            return true;
        }
        long funcBdd = refFormulaBdd(funcFormula);
        derefBdd(funcBdd);
        return (funcBdd == getZeroBdd());
    }

    @Override
    public boolean isEquivalentToConstant1(BooleanFormula funcFormula) {
        if (funcFormula == null) {
            return false;
        }
        if (funcFormula == One.getInstance()) {
            return true;
        }
        long funcBdd = refFormulaBdd(funcFormula);
        derefBdd(funcBdd);
        return (funcBdd == getOneBdd());
    }

    @Override
    public boolean isBinate(BooleanFormula funcFormula, BooleanVariable var) {
        return !isPositiveUnate(funcFormula, var) && !isNegativeUnate(funcFormula, var);
    }

    @Override
    public boolean isPositiveUnate(BooleanFormula funcFormula, BooleanVariable var) {
        long funcBdd = refFormulaBdd(funcFormula);
        long varBdd = refVariableBdd(var);
        int varIndex = Cudd.Cudd_NodeReadIndex(varBdd);
        long resultBdd = refBdd(Cudd.Cudd_Increasing(backend, funcBdd, varIndex));
        derefBdd(funcBdd);
        derefBdd(varBdd);
        boolean result = resultBdd == getOneBdd();
        derefBdd(resultBdd);
        return result;
    }

    @Override
    public boolean isNegativeUnate(BooleanFormula funcFormula, BooleanVariable var) {
        long funcBdd = refFormulaBdd(funcFormula);
        long varBdd = refVariableBdd(var);
        int varIndex = Cudd.Cudd_NodeReadIndex(varBdd);
        long resultBdd = refBdd(Cudd.Cudd_Decreasing(backend, funcBdd, varIndex));
        derefBdd(funcBdd);
        derefBdd(varBdd);
        boolean result = resultBdd == getOneBdd();
        derefBdd(resultBdd);
        return result;
    }

    @Override
    public Set<BooleanVariable> calcRedundantVariables(BooleanFormula formula) {
        Set<BooleanVariable> allVariables = FormulaUtils.extractVariables(formula);
        Set<BooleanVariable> usefulVariables = FormulaUtils.extractVariables(removeRedundantVariables(formula));
        return SetUtils.difference(allVariables, usefulVariables);
    }

    private BooleanFormula removeRedundantVariables(BooleanFormula funcFormula) {
        for (BooleanVariable var : FormulaUtils.extractVariables(funcFormula)) {
            BooleanFormula var0Formula = FormulaUtils.replace(funcFormula, var, Zero.getInstance(),
                    CleverBooleanWorker.getInstance());

            if (isEquivalent(funcFormula, var0Formula)) {
                funcFormula = var0Formula;
            }
        }
        return funcFormula;
    }

    @Override
    public BooleanFormula calcDistinctFormula(BooleanFormula formula) {
        BooleanFormula distinctFormula = removeRedundantVariables(formula);
        long bdd = refFormulaBdd(distinctFormula);
        return bddToDistinctFormulaMap.computeIfAbsent(bdd, ignored -> distinctFormula);
    }

    @Override
    public BooleanFormula calcProjectionFormula(BooleanFormula funcFormula, BooleanFormula caresetFormula) {
        long funcBdd = refFormulaBdd(funcFormula);
        long caresetBdd = refFormulaBdd(caresetFormula);
        long projectionBdd = refBdd(Cudd.Cudd_bddRestrict(backend, funcBdd, caresetBdd));
        derefBdd(funcBdd);
        derefBdd(caresetBdd);
        BooleanFormula result = buildFormula(projectionBdd);
        derefBdd(projectionBdd);
        return result;
    }

    @Override
    public Map<String, Integer> getStats() {
        Map<String, Integer> result = new HashMap<>();
        result.put(REFERENCED_BDD_COUNT_KEY, Cudd.Cudd_CheckZeroRef(backend));
        result.put(VARIABLE_COUNT_KEY, Cudd.Cudd_ReadSize(backend));
        return result;
    }

}
