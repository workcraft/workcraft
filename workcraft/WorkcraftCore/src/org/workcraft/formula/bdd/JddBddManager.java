package org.workcraft.formula.bdd;

import jdd.bdd.BDD;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FormulaUtils;

import java.util.*;

public class JddBddManager extends AbstractBddManager<Integer> {

    private final Backend backend = new Backend();
    private final Map<BooleanVariable, Integer> varToBddMap = new HashMap<>();
    private final Map<Integer, BooleanVariable> indexToVarMap = new HashMap<>();
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

    @Override
    protected Integer constructNotBdd(Integer bdd) {
        return backend.not(bdd);
    }

    @Override
    protected Integer constructAndBdd(Integer leftBdd, Integer rightBdd) {
        return backend.and(leftBdd, rightBdd);
    }

    @Override
    protected Integer constructOrBdd(Integer leftBdd, Integer rightBdd) {
        return backend.or(leftBdd, rightBdd);
    }

    @Override
    protected Integer constructEquivalenceBdd(Integer leftBdd, Integer rightBdd) {
        return backend.biimp(leftBdd, rightBdd);
    }

    @Override
    protected Integer constructImplicationBdd(Integer leftBdd, Integer rightBdd) {
        return backend.imp(leftBdd, rightBdd);
    }

    @Override
    protected Integer constructSymmetricDifferenceBdd(Integer leftBdd, Integer rightBdd) {
        return backend.xor(leftBdd, rightBdd);
    }

    @Override
    protected Integer constructSetDifferenceBdd(Integer leftBdd, Integer rightBdd) {
        Integer notRightBdd = refBdd(constructNotBdd(rightBdd));
        int result = backend.and(leftBdd, notRightBdd);
        derefBdd(notRightBdd);
        return result;
    }

    @Override
    protected Integer getOneBdd() {
        return backend.getOne();
    }

    @Override
    protected Integer getZeroBdd() {
        return backend.getZero();
    }

    @Override
    protected Integer getThenBdd(Integer bdd) {
        return backend.getHigh(bdd);
    }

    @Override
    protected Integer getElseBdd(Integer bdd) {
        return backend.getLow(bdd);
    }

    @Override
    protected int getVarIndex(Integer bdd) {
        return (Objects.equals(bdd, getZeroBdd()) || Objects.equals(bdd, getOneBdd()))
                ? Integer.MAX_VALUE : backend.getVar(bdd);
    }

    @Override
    protected Integer refBdd(Integer bdd) {
        return backend.ref(bdd);
    }

    @Override
    protected void derefBdd(Integer bdd) {
        backend.deref(bdd);
    }

    @Override
    protected Integer refVariableBdd(BooleanVariable var) {
        Integer bdd = varToBddMap.get(var);
        if (bdd == null) {
            bdd = backend.createVar();
            varToBddMap.put(var, bdd);
        }
        int index = backend.getVar(bdd);
        indexToVarMap.put(index, var);
        return refBdd(bdd);
    }

    @Override
    protected BooleanVariable getVariable(int varIndex) {
        return indexToVarMap.get(varIndex);
    }

    @Override
    protected int getStatReferencedNodeCount() {
        return backend.calcReferencedNodeCount();
    }

    @Override
    protected int getStatVariableCount() {
        return backend.numberOfVariables();
    }

    @Override
    public boolean isPositiveUnate(BooleanFormula funcFormula, BooleanVariable var) {
        BooleanFormula var0Formula = FormulaUtils.replaceZero(funcFormula, var);
        BooleanFormula var1Formula = FormulaUtils.replaceOne(funcFormula, var);
        return isImplication(var0Formula, var1Formula);
    }

    @Override
    public boolean isNegativeUnate(BooleanFormula funcFormula, BooleanVariable var) {
        BooleanFormula var0Formula = FormulaUtils.replaceZero(funcFormula, var);
        BooleanFormula var1Formula = FormulaUtils.replaceOne(funcFormula, var);
        return isImplication(var1Formula, var0Formula);
    }

    private boolean isImplication(BooleanFormula leftFormula, BooleanFormula rightFormula) {
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

//    @Override
//    public BooleanFormula calcProjectionFormula(BooleanFormula funcFormula, BooleanFormula caresetFormula) {
//        int funcBdd = refFormulaBdd(funcFormula);
//        int caresetBdd = refFormulaBdd(caresetFormula);
//        int projectionBdd = backend.simplify(caresetBdd, funcBdd);
//        derefBdd(funcBdd);
//        derefBdd(caresetBdd);
//        BooleanFormula result = buildIsopFormula(projectionBdd);
//        derefBdd(projectionBdd);
//        return result;
//    }

}
