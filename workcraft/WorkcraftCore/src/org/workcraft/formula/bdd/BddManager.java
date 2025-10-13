package org.workcraft.formula.bdd;

import jdd.bdd.BDD;
import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.types.Func2;
import org.workcraft.utils.SetUtils;

import java.util.*;

public class BddManager {

//    private final BDD bdd = new DebugBDD(1000, 100);
    private final BDD bdd = new BDD(1000, 100);
    private final Map<BooleanVariable, Integer> varToBddMap = new HashMap<>();
    private final Map<Integer, BooleanFormula> bddToDistinctFormulaMap = new HashMap<>();

    private final class BddGenerator implements BooleanVisitor<Integer> {

        @Override
        public Integer visit(Zero node) {
            return bdd.getZero();
        }

        @Override
        public Integer visit(One node) {
            return bdd.getOne();
        }

        @Override
        public Integer visit(BooleanVariable node) {
            return refDistinctVariable(node);
        }

        @Override
        public Integer visit(Not node) {
            int xBdd = node.getX().accept(this);
            int result = bdd.ref(bdd.not(xBdd));
            bdd.deref(xBdd);
            return result;
        }

        @Override
        public Integer visit(And node) {
            return visitBinaryOperator(node, bdd::and);
        }

        @Override
        public Integer visit(Or node) {
            return visitBinaryOperator(node, bdd::or);
        }

        @Override
        public Integer visit(Iff node) {
            return visitBinaryOperator(node, bdd::biimp);
        }

        @Override
        public Integer visit(Xor node) {
            return visitBinaryOperator(node, bdd::xor);
        }

        @Override
        public Integer visit(Imply node) {
            return visitBinaryOperator(node, bdd::imp);
        }

        public Integer visitBinaryOperator(BinaryBooleanFormula node, Func2<Integer, Integer, Integer> func) {
            int firstBdd = node.getX().accept(this);
            int secondBdd = node.getY().accept(this);
            int result = bdd.ref(func.eval(firstBdd, secondBdd));
            bdd.deref(firstBdd);
            bdd.deref(secondBdd);
            return result;
        }
    }

    private int refDistinctVariable(BooleanVariable var) {
        return varToBddMap.computeIfAbsent(var, key -> bdd.createVar());
    }

    private int refFormula(BooleanFormula formula) {
        return formula.accept(new BddGenerator());
    }

    public BooleanFormula getDistinctFormula(BooleanFormula formula) {
        BooleanFormula distinctFormula = removeRedundantVariables(formula);
        return bddToDistinctFormulaMap.computeIfAbsent(refFormula(distinctFormula), bdd -> distinctFormula);
    }

    public Set<BooleanVariable> getRedundantVariables(BooleanFormula formula) {
        Set<BooleanVariable> allVariables = FormulaUtils.extractVariables(formula);
        Set<BooleanVariable> usefulVariables = FormulaUtils.extractVariables(removeRedundantVariables(formula));
        return SetUtils.difference(allVariables, usefulVariables);
    }

    public BooleanFormula removeRedundantVariables(BooleanFormula formula) {
        for (BooleanVariable var : FormulaUtils.extractVariables(formula)) {
            BooleanFormula var0Formula = FormulaUtils.replaceZero(formula, var);
            if (isEquivalent(formula, var0Formula)) {
                formula = var0Formula;
            }
        }
        return formula;
    }

    public boolean isBinate(BooleanFormula formula, BooleanVariable var) {
        return !isPositiveUnate(formula, var) && !isNegativeUnate(formula, var);
    }

    public boolean isPositiveUnate(BooleanFormula formula, BooleanVariable var) {
        BooleanFormula var0Formula = FormulaUtils.replaceZero(formula, var);
        BooleanFormula var1Formula = FormulaUtils.replaceOne(formula, var);
        return implies(var0Formula, var1Formula);
    }

    public boolean isNegativeUnate(BooleanFormula formula, BooleanVariable var) {
        BooleanFormula var0Formula = FormulaUtils.replaceZero(formula, var);
        BooleanFormula var1Formula = FormulaUtils.replaceOne(formula, var);
        return implies(var1Formula, var0Formula);
    }

    public boolean implies(BooleanFormula leftFormula, BooleanFormula rightFormula) {
        int leftBdd = refFormula(leftFormula);
        int rightBdd = refFormula(rightFormula);
        int notRightBdd = bdd.ref(bdd.not(rightBdd));

        Set<BooleanVariable> vars = new HashSet<>();
        vars.addAll(FormulaUtils.extractOrderedVariables(leftFormula));
        vars.addAll(FormulaUtils.extractOrderedVariables(rightFormula));
        int cubeBdd = buildCube(vars);

        boolean result = bdd.relProd(leftBdd, notRightBdd, cubeBdd) == bdd.getZero();
        bdd.deref(notRightBdd);
        bdd.deref(rightBdd);
        bdd.deref(leftBdd);
        bdd.deref(cubeBdd);
        return result;
    }

    private int buildCube(Collection<BooleanVariable> vars) {
        int cubeBdd = bdd.getOne();
        for (BooleanVariable var : vars) {
            int oldCubeBdd = cubeBdd;
            int varBdd = refDistinctVariable(var);
            cubeBdd = bdd.ref(bdd.and(oldCubeBdd, varBdd));
            bdd.deref(oldCubeBdd);
        }
        return cubeBdd;
    }

    public boolean isEquivalent(BooleanFormula leftFormula, BooleanFormula rightFormula) {
        if (leftFormula == rightFormula) {
            return true;
        }
        if ((leftFormula == null) || (rightFormula == null)) {
            return false;
        }
        int firstBdd = refFormula(leftFormula);
        int secondBdd = refFormula(rightFormula);
        boolean result = (firstBdd == secondBdd);
        bdd.deref(firstBdd);
        bdd.deref(secondBdd);
        return result;
    }

    public boolean isEquivalentToConstant(BooleanFormula formula) {
        if (formula == null) {
            return false;
        }
        if ((formula == Zero.getInstance()) || (formula == One.getInstance())) {
            return true;
        }
        int formulaBdd = refFormula(formula);
        int zeroBdd = refFormula(Zero.getInstance());
        int oneBdd = refFormula(One.getInstance());
        boolean result = (formulaBdd == zeroBdd) || (formulaBdd == oneBdd);
        bdd.deref(formulaBdd);
        bdd.deref(zeroBdd);
        bdd.deref(oneBdd);
        return result;
    }

    public boolean isEquivalentToConstant0(BooleanFormula formula) {
        if (formula == null) {
            return false;
        }
        if (formula == Zero.getInstance()) {
            return true;
        }
        int formulaBdd = refFormula(formula);
        int zeroBdd = refFormula(Zero.getInstance());
        boolean result = (formulaBdd == zeroBdd);
        bdd.deref(formulaBdd);
        bdd.deref(zeroBdd);
        return result;
    }

    public boolean isEquivalentToConstant1(BooleanFormula formula) {
        if (formula == null) {
            return false;
        }
        if (formula == One.getInstance()) {
            return true;
        }
        int formulaBdd = refFormula(formula);
        int oneBdd = refFormula(One.getInstance());
        boolean result = (formulaBdd == oneBdd);
        bdd.deref(formulaBdd);
        bdd.deref(oneBdd);
        return result;
    }

}
