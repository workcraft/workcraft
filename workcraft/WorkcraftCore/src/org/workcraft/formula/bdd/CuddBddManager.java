package org.workcraft.formula.bdd;

import info.scce.addlib.cudd.Cudd;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.types.TwoWayMap;

import java.util.Objects;

public class CuddBddManager extends AbstractBddManager<Long> {

    private final long backend = Cudd.Cudd_Init(0, 0, Cudd.CUDD_UNIQUE_SLOTS, Cudd.CUDD_CACHE_SLOTS, 0);
    private final TwoWayMap<BooleanVariable, Long> varToBddMap = new TwoWayMap<>();
    private final boolean enableCloseStats;

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

    @Override
    protected Long constructNotBdd(Long bdd) {
        return Cudd.Cudd_Not(bdd);
    }

    @Override
    protected Long constructAndBdd(Long leftBdd, Long rightBdd) {
        return Cudd.Cudd_bddAnd(backend, leftBdd, rightBdd);
    }

    @Override
    protected Long constructOrBdd(Long leftBdd, Long rightBdd) {
        return Cudd.Cudd_bddOr(backend, leftBdd, rightBdd);
    }

    @Override
    protected Long constructEquivalenceBdd(Long leftBdd, Long rightBdd) {
        return Cudd.Cudd_bddXnor(backend, leftBdd, rightBdd);
    }

    @Override
    protected Long constructImplicationBdd(Long leftBdd, Long rightBdd) {
        return Cudd.Cudd_bddOr(backend, Cudd.Cudd_Not(leftBdd), rightBdd);
    }

    @Override
    protected Long constructSymmetricDifferenceBdd(Long leftBdd, Long rightBdd) {
        return Cudd.Cudd_bddXor(backend, leftBdd, rightBdd);
    }

    @Override
    protected Long constructSetDifferenceBdd(Long leftBdd, Long rightBdd) {
        return Cudd.Cudd_bddAnd(backend, leftBdd, Cudd.Cudd_Not(rightBdd));
    }

    @Override
    protected Long getThenBdd(Long bdd) {
        return (Cudd.Cudd_IsComplement(bdd) == 0) ? Cudd.Cudd_T(bdd) : Cudd.Cudd_Not(Cudd.Cudd_T(bdd));
    }

    @Override
    protected Long getElseBdd(Long bdd) {
        return (Cudd.Cudd_IsComplement(bdd) == 0) ? Cudd.Cudd_E(bdd) : Cudd.Cudd_Not(Cudd.Cudd_E(bdd));
    }

    @Override
    protected int getVarIndex(Long bdd) {
        return Objects.equals(bdd, getZeroBdd()) || Objects.equals(bdd, getOneBdd())
                ? Integer.MAX_VALUE : Cudd.Cudd_NodeReadIndex(bdd);
    }

    @Override
    public Long refBdd(Long bdd) {
        Cudd.Cudd_Ref(bdd);
        return bdd;
    }

    @Override
    public void derefBdd(Long bdd) {
        Cudd.Cudd_RecursiveDeref(backend, bdd);
    }

    @Override
    public Long getZeroBdd() {
        return Cudd.Cudd_ReadLogicZero(backend);
    }

    @Override
    public Long getOneBdd() {
        return Cudd.Cudd_ReadOne(backend);
    }

    @Override
    public Long refVariableBdd(BooleanVariable var) {
        Long bdd = varToBddMap.getValue(var);
        if (bdd == null) {
            bdd = Cudd.Cudd_bddNewVar(backend);
            varToBddMap.put(var, bdd);
        }
        return refBdd(bdd);
    }

    @Override
    protected BooleanVariable getVariable(int varIndex) {
        long varBdd = Cudd.Cudd_bddIthVar(backend, varIndex);
        return varToBddMap.getKey(varBdd);
    }

    @Override
    protected int getStatReferencedNodeCount() {
        return Cudd.Cudd_CheckZeroRef(backend);
    }

    @Override
    protected int getStatVariableCount() {
        return Cudd.Cudd_ReadSize(backend);
    }

    @Override
    public boolean isPositiveUnate(BooleanFormula funcFormula, BooleanVariable var) {
        long funcBdd = refFormulaBdd(funcFormula);
        long varBdd = refVariableBdd(var);
        int varIndex = Cudd.Cudd_NodeReadIndex(varBdd);
        long resultBdd = refBdd(Cudd.Cudd_Increasing(backend, funcBdd, varIndex));
        derefBdd(funcBdd);
        derefBdd(varBdd);
        boolean result = (resultBdd == getOneBdd());
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
        boolean result = (resultBdd == getOneBdd());
        derefBdd(resultBdd);
        return result;
    }

//    @Override
//    public BooleanFormula calcProjectionFormula(BooleanFormula funcFormula, BooleanFormula caresetFormula) {
//        long funcBdd = refFormulaBdd(funcFormula);
//        long caresetBdd = refFormulaBdd(caresetFormula);
//        long projectionBdd = refBdd(Cudd.Cudd_bddRestrict(backend, funcBdd, caresetBdd));
//        derefBdd(funcBdd);
//        derefBdd(caresetBdd);
//        BooleanFormula result = buildIsopFormula(projectionBdd);
//        derefBdd(projectionBdd);
//        return result;
//    }

}
