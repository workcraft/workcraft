package org.workcraft.formula.bdd;

import jdd.bdd.BDD;
import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.types.Func2;

import java.util.*;

public class BddManager {

//    private final BDD bdd = new DebugBDD(1000, 100);
    private final BDD bdd = new BDD(1000, 100);
    private final Map<BooleanVariable, Integer> varMap = new HashMap<>();

    public class BddGenerator implements BooleanVisitor<Integer> {

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
            return addVariable(node);
        }

        @Override
        public Integer visit(Not node) {
            int x = node.getX().accept(this);
            int result = bdd.ref(bdd.not(x));
            bdd.deref(x);
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
            int x = node.getX().accept(this);
            int y = node.getY().accept(this);
            int result = bdd.ref(func.eval(x, y));
            bdd.deref(x);
            bdd.deref(y);
            return result;
        }
    }

    private int addVariable(BooleanVariable var) {
        if (!varMap.containsKey(var)) {
            varMap.put(var, bdd.createVar());
        }
        return varMap.get(var);
    }

    private int addFormula(BooleanFormula formula) {
        return formula.accept(new BddGenerator());
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
        int leftBdd = addFormula(leftFormula);
        int rightBdd = addFormula(rightFormula);
        int notRightBdd = bdd.ref(bdd.not(rightBdd));

        List<BooleanVariable> vars = new ArrayList<>();
        vars.addAll(FormulaUtils.extractLiterals(leftFormula));
        vars.addAll(FormulaUtils.extractLiterals(rightFormula));
        int cube = buildCube(vars);

        boolean result = bdd.relProd(leftBdd, notRightBdd, cube) == bdd.getZero();
        bdd.deref(notRightBdd);
        bdd.deref(rightBdd);
        bdd.deref(leftBdd);
        bdd.deref(cube);
        return result;
    }

    private int buildCube(Collection<BooleanVariable> vars) {
        int cube = bdd.getOne();
        for (BooleanVariable var : vars) {
            int oldCube = cube;
            cube = bdd.ref(bdd.and(oldCube, addVariable(var)));
            bdd.deref(oldCube);
        }
        return cube;
    }

    public boolean equal(BooleanFormula firstFormula, BooleanFormula secondFormula) {
        int firstBdd = addFormula(firstFormula);
        int secondBdd = addFormula(secondFormula);
        boolean result = firstBdd == secondBdd;
        bdd.deref(firstBdd);
        bdd.deref(secondBdd);
        return result;
    }

}
