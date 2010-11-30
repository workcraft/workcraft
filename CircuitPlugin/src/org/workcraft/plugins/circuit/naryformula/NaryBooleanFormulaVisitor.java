package org.workcraft.plugins.circuit.naryformula;

import java.util.List;

import org.workcraft.plugins.cpog.optimisation.BooleanVariable;

public interface NaryBooleanFormulaVisitor<T> {
	T visitXor(List<NaryBooleanFormula> args);
	T visitAnd(List<NaryBooleanFormula> args);
	T visitOr(List<NaryBooleanFormula> args);
	T visit(BooleanVariable var);
	T visitNot(NaryBooleanFormula arg);
}
