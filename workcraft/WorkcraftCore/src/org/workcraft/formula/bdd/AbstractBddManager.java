package org.workcraft.formula.bdd;

import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.types.Pair;
import org.workcraft.utils.SetUtils;

import java.util.*;
import java.util.function.BiFunction;

abstract class AbstractBddManager<T> implements BddManager {

    private final Map<T, BooleanFormula> bddToDistinctFormulaMap = new HashMap<>();

    private final class BddGenerator implements BooleanVisitor<T> {

        private T visitBinaryOperator(BinaryBooleanFormula node, BiFunction<T, T, T> func) {
            T leftBdd = node.getX().accept(this);
            T rightBdd = node.getY().accept(this);
            T resultBdd = refBdd(func.apply(leftBdd, rightBdd));
            derefBdd(leftBdd);
            derefBdd(rightBdd);
            return resultBdd;
        }

        @Override
        public T visit(Zero node) {
            return getZeroBdd();
        }

        @Override
        public T visit(One node) {
            return getOneBdd();
        }

        @Override
        public T visit(BooleanVariable node) {
            return refVariableBdd(node);
        }

        @Override
        public T visit(Not node) {
            T bdd = node.getX().accept(this);
            T resultBdd = refBdd(constructNotBdd(bdd));
            derefBdd(bdd);
            return resultBdd;
        }

        @Override
        public T visit(And node) {
            return visitBinaryOperator(node, AbstractBddManager.this::constructAndBdd);
        }

        @Override
        public T visit(Or node) {
            return visitBinaryOperator(node, AbstractBddManager.this::constructOrBdd);
        }

        @Override
        public T visit(Iff node) {
            return visitBinaryOperator(node, AbstractBddManager.this::constructEquivalenceBdd);
        }

        @Override
        public T visit(Xor node) {
            return visitBinaryOperator(node, AbstractBddManager.this::constructSymmetricDifferenceBdd);
        }

        @Override
        public T visit(Imply node) {
            return visitBinaryOperator(node, AbstractBddManager.this::constructImplicationBdd);
        }
    }


    // BDD construction
    protected abstract T constructNotBdd(T bdd);
    protected abstract T constructAndBdd(T leftBdd, T rightBdd);
    protected abstract T constructOrBdd(T leftBdd, T rightBdd);
    protected abstract T constructEquivalenceBdd(T leftBdd, T rightBdd);
    protected abstract T constructImplicationBdd(T leftBdd, T rightBdd);
    protected abstract T constructSymmetricDifferenceBdd(T leftBdd, T rightBdd);
    protected abstract T constructSetDifferenceBdd(T leftBdd, T rightBdd);

    // BDD comprehension
    protected abstract T getOneBdd();
    protected abstract T getZeroBdd();
    protected abstract T getThenBdd(T bdd);
    protected abstract T getElseBdd(T bdd);
    protected abstract int getVarIndex(T bdd);

    // BDD reference/dereference operations
    protected abstract T refBdd(T bdd);
    protected abstract void derefBdd(T bdd);

    protected T refBdd(T bdd, List<T> refdBdds) {
        T result = refBdd(bdd);
        if ((result != getZeroBdd()) && (result != getOneBdd())) {
            refdBdds.add(result);
        }
        return result;
    }

    protected void derefBdds(Collection<T> refdBdds) {
        refdBdds.forEach(this::derefBdd);
    }

    // BDD variables
    protected abstract T refVariableBdd(BooleanVariable var);
    protected abstract BooleanVariable getVariable(int varIndex);

    protected T refFormulaBdd(BooleanFormula formula) {
        return formula.accept(new BddGenerator());
    }

    // BDD statistics
    protected abstract int getStatReferencedNodeCount();
    protected abstract int getStatVariableCount();

    @Override
    public Map<String, Integer> getStats() {
        Map<String, Integer> result = new HashMap<>();
        result.put(REFERENCED_BDD_COUNT_KEY, getStatReferencedNodeCount());
        result.put(VARIABLE_COUNT_KEY, getStatVariableCount());
        return result;
    }

    @Override
    public boolean isEquivalent(BooleanFormula leftFormula, BooleanFormula rightFormula) {
        if (leftFormula == rightFormula) {
            return true;
        }
        if ((leftFormula == null) || (rightFormula == null)) {
            return false;
        }
        T leftBdd = refFormulaBdd(leftFormula);
        T rightBdd = refFormulaBdd(rightFormula);
        boolean result = Objects.equals(leftBdd, rightBdd);
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
        T funcBdd = refFormulaBdd(funcFormula);
        boolean result = Objects.equals(funcBdd, getZeroBdd()) || Objects.equals(funcBdd, getOneBdd());
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
        T funcBdd = refFormulaBdd(funcFormula);
        boolean result = Objects.equals(funcBdd, getZeroBdd());
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
        T funcBdd = refFormulaBdd(funcFormula);
        boolean result = Objects.equals(funcBdd, getOneBdd());
        derefBdd(funcBdd);
        return result;
    }

    @Override
    public boolean isBinate(BooleanFormula funcFormula, BooleanVariable var) {
        return !isPositiveUnate(funcFormula, var) && !isNegativeUnate(funcFormula, var);
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
    public Set<BooleanVariable> calcRedundantVariables(BooleanFormula formula) {
        Set<BooleanVariable> allVariables = FormulaUtils.extractVariables(formula);
        Set<BooleanVariable> usefulVariables = FormulaUtils.extractVariables(removeRedundantVariables(formula));
        return SetUtils.difference(allVariables, usefulVariables);
    }

    @Override
    public BooleanFormula calcDistinctFormula(BooleanFormula formula) {
        BooleanFormula distinctFormula = removeRedundantVariables(formula);
        T bdd = refFormulaBdd(distinctFormula);
        return bddToDistinctFormulaMap.computeIfAbsent(bdd, ignored -> distinctFormula);
    }

    @Override
    public BooleanFormula calcProjectionFormula(BooleanFormula funcFormula, BooleanFormula caresetFormula) {
        T funcBdd = refFormulaBdd(funcFormula);
        T caresetBdd = refFormulaBdd(caresetFormula);
        T dontcaresetBdd = refBdd(constructNotBdd(caresetBdd));
        T lowerBdd = refBdd(constructAndBdd(funcBdd, caresetBdd));
        T upperBdd = refBdd(constructOrBdd(funcBdd, dontcaresetBdd));
        derefBdd(funcBdd);
        derefBdd(caresetBdd);
        derefBdd(dontcaresetBdd);
        BooleanFormula result = buildIsopFormula(lowerBdd, upperBdd);
        derefBdd(lowerBdd);
        derefBdd(upperBdd);
        return result;
    }

    private BooleanFormula buildIsopFormula(T lowerBdd, T upperBdd) {
        List<T> refdBdds = new ArrayList<>();
        Cover cover = buildIsopCover(lowerBdd, upperBdd, new IsopCache(), refdBdds);
        derefBdds(refdBdds);
        return cover.getFormula();
    }

    // Container for a partial cover produced while running the Minato-Morreale algorithm:
    // bdd is the BDD of the generated cover, formula is its irredundant Sum-of-Products form.
    private final class Cover extends Pair<T, BooleanFormula> {
        Cover(T bdd, BooleanFormula formula) {
            super(bdd, formula);
        }

        public T getBdd() {
            return getFirst();
        }

        public BooleanFormula getFormula() {
            return getSecond();
        }
    }

    private final class IsopCache extends HashMap<Pair<T, T>, Cover> {
    }

    // Minato-Morreale algorithm builds an Irredundant Sum-of-Products (ISoP) cover of a
    // function bounded between lowerBdd (must be covered) and upperBdd (may be covered).
    // Share terms between the two cofactors of the top variable whenever possible.
    private Cover buildIsopCover(T lowerBdd, T upperBdd, IsopCache isopCache, List<T> refdBdds) {
        if ((Objects.equals(upperBdd, getZeroBdd())) || (Objects.equals(lowerBdd, getZeroBdd()))) {
            return new Cover(getZeroBdd(), Zero.getInstance());
        }
        if (Objects.equals(lowerBdd, getOneBdd())) {
            return new Cover(getOneBdd(), One.getInstance());
        }

        Pair<T, T> cacheKey = Pair.of(lowerBdd, upperBdd);
        Cover cover = isopCache.get(cacheKey);
        if (cover == null) {
            int varIndex = Math.min(getVarIndex(lowerBdd), getVarIndex(upperBdd));
            BooleanVariable var = getVariable(varIndex);

            T varHiLowerBdd = (getVarIndex(lowerBdd) == varIndex) ? getThenBdd(lowerBdd) : lowerBdd;
            T varHiUpperBdd = (getVarIndex(upperBdd) == varIndex) ? getThenBdd(upperBdd) : upperBdd;
            T varLoLowerBdd = (getVarIndex(lowerBdd) == varIndex) ? getElseBdd(lowerBdd) : lowerBdd;
            T varLoUpperBdd = (getVarIndex(upperBdd) == varIndex) ? getElseBdd(upperBdd) : upperBdd;

            T varLoLowerCaresetBdd = refBdd(constructSetDifferenceBdd(varLoLowerBdd, varHiUpperBdd), refdBdds);
            Cover onlyLoCover = buildIsopCover(varLoLowerCaresetBdd, varLoUpperBdd, isopCache, refdBdds);

            T varHiLowerCaresetBdd = refBdd(constructSetDifferenceBdd(varHiLowerBdd, varLoUpperBdd), refdBdds);
            Cover onlyHiCover = buildIsopCover(varHiLowerCaresetBdd, varHiUpperBdd, isopCache, refdBdds);

            T varLoUncoveredLowerBdd = refBdd(constructSetDifferenceBdd(varLoLowerBdd, onlyLoCover.getBdd()), refdBdds);
            T varHiUncoveredLowerBdd = refBdd(constructSetDifferenceBdd(varHiLowerBdd, onlyHiCover.getBdd()), refdBdds);
            T commonLowerBdd = refBdd(constructOrBdd(varLoUncoveredLowerBdd, varHiUncoveredLowerBdd), refdBdds);
            T commonUpperBdd = refBdd(constructAndBdd(varLoUpperBdd, varHiUpperBdd), refdBdds);
            Cover commonCover = buildIsopCover(commonLowerBdd, commonUpperBdd, isopCache, refdBdds);

            cover = createIsopCover(var, onlyHiCover, onlyLoCover, commonCover, refdBdds);
            isopCache.put(cacheKey, cover);
        }
        return cover;
    }

    private Cover createIsopCover(BooleanVariable var, Cover onlyHiCover, Cover onlyLoCover, Cover commonCover,
            List<T> refdBdds) {

        BooleanFormula coverFormula = FormulaUtils.createSop(CleverBooleanWorker.getInstance(),
                List.of(commonCover.getFormula()),
                List.of(new Not(var), onlyLoCover.getFormula()),
                List.of(var, onlyHiCover.getFormula()));

        T varBdd = refVariableBdd(var);
        refdBdds.add(varBdd);
        T notVarBdd = refBdd(constructNotBdd(varBdd), refdBdds);
        T loTermBdd = refBdd(constructAndBdd(notVarBdd, onlyLoCover.getBdd()), refdBdds);
        T hiTermBdd = refBdd(constructAndBdd(varBdd, onlyHiCover.getBdd()), refdBdds);
        T termsBdd = refBdd(constructOrBdd(loTermBdd, hiTermBdd), refdBdds);
        T coverBdd = refBdd(constructOrBdd(termsBdd, commonCover.getBdd()), refdBdds);

        return new Cover(coverBdd, coverFormula);
    }

}
