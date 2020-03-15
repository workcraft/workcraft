package org.workcraft.plugins.circuit.naryformula;

import java.util.Arrays;
import java.util.List;

import org.workcraft.formula.BooleanVariable;

public class NaryDefaultArgExtractor implements NaryBooleanFormulaVisitor<List<NaryBooleanFormula>> {

    private List<NaryBooleanFormula> defaultList(NaryBooleanFormula formula) {
        return Arrays.asList(new NaryBooleanFormula[]{formula});
    }

    @Override
    public List<NaryBooleanFormula> visit(BooleanVariable var) {
        return defaultList(NaryUtils.getVar(var));
    }

    @Override
    public List<NaryBooleanFormula> visitNot(NaryBooleanFormula not) {
        return defaultList(NaryUtils.getNot(not));
    }

    @Override
    public List<NaryBooleanFormula> visitAnd(List<NaryBooleanFormula> args) {
        return defaultList(NaryUtils.getAnd(args));
    }

    @Override
    public List<NaryBooleanFormula> visitOr(List<NaryBooleanFormula> args) {
        return defaultList(NaryUtils.getOr(args));
    }

    @Override
    public List<NaryBooleanFormula> visitXor(List<NaryBooleanFormula> args) {
        return defaultList(NaryUtils.getXor(args));
    }

}
