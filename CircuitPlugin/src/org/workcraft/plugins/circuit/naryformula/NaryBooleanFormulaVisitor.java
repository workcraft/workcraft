package org.workcraft.plugins.circuit.naryformula;

import java.util.List;

import org.workcraft.formula.BooleanVariable;

public interface NaryBooleanFormulaVisitor<T> {
    T visit(BooleanVariable var);
    T visitNot(NaryBooleanFormula arg);
    T visitAnd(List<NaryBooleanFormula> args);
    T visitOr(List<NaryBooleanFormula> args);
    T visitXor(List<NaryBooleanFormula> args);
}
