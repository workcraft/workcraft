package org.workcraft.plugins.circuit.naryformula;

import org.workcraft.formula.BooleanVariable;

import java.util.Collections;
import java.util.List;

public class NaryDefaultArgExtractor implements NaryBooleanFormulaVisitor<List<NaryBooleanFormula>> {

    @Override
    public List<NaryBooleanFormula> visit(BooleanVariable var) {
        return Collections.singletonList(NaryUtils.getVar(var));
    }

    @Override
    public List<NaryBooleanFormula> visitNot(NaryBooleanFormula not) {
        return Collections.singletonList(NaryUtils.getNot(not));
    }

    @Override
    public List<NaryBooleanFormula> visitAnd(List<NaryBooleanFormula> args) {
        return Collections.singletonList(NaryUtils.getAnd(args));
    }

    @Override
    public List<NaryBooleanFormula> visitOr(List<NaryBooleanFormula> args) {
        return Collections.singletonList(NaryUtils.getOr(args));
    }

    @Override
    public List<NaryBooleanFormula> visitXor(List<NaryBooleanFormula> args) {
        return Collections.singletonList(NaryUtils.getXor(args));
    }

}
